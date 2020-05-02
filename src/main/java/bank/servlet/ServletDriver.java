package bank.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import bank.Bank;
import bank.BankDriver;
import bank.InactiveException;
import bank.OverdrawException;
import bank.command.Command;
import bank.command.CommandBank;
import bank.command.CommandBank.CommandHandler;

/** Client side **/
public class ServletDriver implements BankDriver {

	private Bank bank;
	private String path; // TODO: what is the path
	// XXX das ist der Pfad auf den Server, also die URL, wird in sendRequest verwendet um auf den Dienst zuzugreifen:
	//				.uri(new URI("http://" + path + "?" + query))
	private HttpClient httpClient = HttpClient.newHttpClient();

	@Override
	public void connect(String[] args) throws IOException {
		bank = new ServletBank(new ServletHandler());
		// TODO: connect to hostname & port
		// XXX HTTP ist ein zustandsloses Protokoll, daher wird (hier im connect) keine verbindung aufgebaut,
		//     die Ausgabe "connected" ist daher verwirrend.
		//     
		//	   Wemm Sie mit Comamnds arbeiten, dann können Sie direkt eine Instanz der Klasse CommandBank 
		//     erzeugen, die KLasse ServletBank braucht es dann nicht.
		System.out.println("connected to " + args[0] + ":" + args[1]);
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
		System.out.println("disconnected");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	private Stream<String> sendRequest(String query) throws IOException {
		HttpRequest httpRequest = null;
		try {
			httpRequest = HttpRequest.newBuilder()
					.uri(new URI("http://" + path + "?" + query))
					.GET()
					.build();
		} catch (URISyntaxException e) {
			throw new InternalError(e.getMessage());
		}
		// TODO: what Object is client?
		// XXX:  Diese Frage habe ich nicht verstanden. Das "sendRequest" schickt ein GET Request an den Server und gibt die Antwort zurück.
		//       Die Instanz httpclient ist oben erzeugt worden.
		HttpResponse<Stream<String>> response = null;
		try {
			response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());
		} catch (IOException | InterruptedException e) {
			throw new InternalError(e.getMessage());
		}
		return response.body();
	}

	private class ServletBank extends CommandBank {
		// XXX Erweiterung von Command-Bank macht hier keinen Sinn, d.h. wenn die Methode sendRequest verwendet wird welche die Kommandos per GET Request an den Server überträgt.
		
		
		private String number; // XXX diese Deklaration macht hier keinen Sinn.

		public ServletBank(CommandHandler h) {
			super(h);
		}

		public Set<String> getAccountNumbers() throws IOException {
			Stream<String> lines = sendRequest("action=getAccountNumbers");
			return lines.skip(1).collect(Collectors.toSet());
		}

		public String createAccount(String owner) throws IOException {
			Stream<String> lines = sendRequest("action=createAccount&owner=encode(owner)");
			return lines.findFirst().map(id -> "".equals(id) ? null : id).orElseGet(() -> null);
		}
		
		@SuppressWarnings("unused")
		private class Account extends CommandBank.AccountProxy {
			// XXX jetzt haben wir irgendwie ein Durcheinander, d.h. werden die im Paket bank.command definierten Command-Objekte
			//     an den Server geschickt (dann würde ich POST requests verwenden) oder handelt es sich hier um eine andere
			//     Implementierung welche die Kommandos (wie hier unten im deposit implementiert) per GET-Requests an den
			//     Server schickt.
			
			private Account(String number) {
				super(number);
			}

			public void withdraw(double amount) throws IOException, OverdrawException, InactiveException {
				// TODO: what is number?
				// XXX   Das ist auch eine Folge des oben erwähnten Durcheinanders. number ist die Konto-Nummer,
				//       und die wird in der Basisklasse deklariert, aber eigentlich müssen Sie nicht von dieser
				//       ableiten; also: number ist das Feld dieses Kontos, und das müssten Sie in der KLasse Account
				//       deklarieren.
				
				Stream<String> lines = sendRequest(
						"action=accountWithdraw" + "&number=" + encode(number) + "&amount=" + encode("" + amount));
				String response = lines.findFirst().get();
				if (!response.equals("")) {
					var reason = response.substring(response.indexOf(":"));
					if (response.contains("OverdrawException")) {
						throw new OverdrawException(reason);
					} else if (response.contains("InactiveException")) {
						throw new InactiveException(reason);
					} else if (response.contains("IllegalArgumentException")) {
						throw new IllegalArgumentException(reason);
					}
				}
			}
		}
	}
	
	private class ServletHandler implements CommandHandler {
		// XXX diese ist der Handler der beim Erzeugen einer Instanz der Command-Bank übergeben wird.
		//     Dieser schickt die serialisierten Commands per POST an den Server.

		@Override
		public Command handle(Command request) throws IOException {
			// PREPARATION
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(request);
			out.flush();
			out.close();

			// SEND REQUEST & RECEIVE RESPONSE
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
					.build();
			HttpResponse<InputStream> httpResponse = null;
			try {
				httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
			} catch (IOException | InterruptedException e1) {
				// TODO correct exception?
				// XXX ich würde hier bei der IOException sicher eine IOException werfen, und wohl auch bei der InterruptedException.
//				throw new RuntimeException(e1);
				throw new InternalError(e1.getMessage());
			}
			ObjectInputStream in = new ObjectInputStream(httpResponse.body());
			try {
				return (Command) in.readObject();
			} catch (ClassNotFoundException | IOException e2) {
				// TODO correct exception?
				// XXX ja, das ist beides möglich, ich würde allerdings bei der IOException eine IOException werfen.
				//     InternalError steht für Fehler der JVM:
				//					 * Thrown to indicate some unexpected internal error has occurred in
				//					 * the Java Virtual Machine.
				//     das pass nicht wirklich, daher würde ich in diesem Fall eine RuntimeException werfen.
//				throw new RuntimeException(e2);
				throw new InternalError(e2.getMessage());
			}
		}
	}

	// TODO: Should it be implemented like this?
	public String encode(String url) {
		try {
			return java.net.URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO: correct exception?
			// XXX ist hier eigentlich egal, denn diese Exception wird nur geworfen wenn es das Encoding nicht gibt, aber UTF-8 gibt es.
			//     Wenn, dann wäre es ein Programmierfehler und für diesen würde ich eine RuntimeException verwenden, vielleicht auch eine
			//     konkrete wie IllegalArgumentException.
//			throw new RuntimeException(e);
			throw new InternalError(e.getMessage());
		}
	}
}
