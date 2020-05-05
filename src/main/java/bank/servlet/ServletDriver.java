package bank.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import bank.Bank;
import bank.BankDriver;
import bank.command.Command;
import bank.command.CommandBank;
import bank.command.CommandBank.CommandHandler;

/** Client side **/
public class ServletDriver implements BankDriver {

	private Bank bank;
	private HttpClient httpClient = HttpClient.newHttpClient();
	private String path;

	/**
	 * Feedback: HTTP ist ein zustandsloses Protokoll, daher wird (hier im connect)
	 * keine verbindung aufgebaut, die Ausgabe "connected" ist daher verwirrend.
	 * Wemm Sie mit Comamnds arbeiten, dann können Sie direkt eine Instanz der
	 * Klasse CommandBank erzeugen, die Klasse ServletBank braucht es dann nicht.
	 */
	@Override
	public void connect(String[] args) throws IOException {
		// args[2] = contextPath von TomcatServer
		path = "http://" + args[0] + ":" + args[1] + args[2];
		bank = new CommandBank(new ServletHandler());
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	/**
	 * Dies ist der Handler der beim Erzeugen einer Instanz der Command-Bank
	 * übergeben wird. Dieser schickt die serialisierten Commands per POST an den
	 * Server.
	 */
	private class ServletHandler implements CommandHandler {

		@Override
		public Command handle(Command request) throws IOException {
			// PREPARATION
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(request);
			out.flush();
			out.close();

			// REQUEST
			HttpRequest httpRequest = null;
			try {
				httpRequest = HttpRequest.newBuilder()
						// Wenn keine URI gesetzt wird bricht es ab und sagt uri == null
						.uri(new URI(path))
						.POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
						.build();
			} catch (URISyntaxException e) { e.printStackTrace(); }
			
			// RESPONSE
			HttpResponse<InputStream> httpResponse = null;
			try { httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream()); }
			catch (IOException | InterruptedException e) { throw new IOException(e.getMessage()); }

			// RETURN COMMAND
			ObjectInputStream in = new ObjectInputStream(httpResponse.body());
			try { return (Command) in.readObject(); } 
			catch (IOException e) { throw new IOException(e.getMessage()); }
			catch (ClassNotFoundException e) { throw new RuntimeException(e.getMessage()); }
			// InternalError steht für Fehler der JVM:
			// * Thrown to indicate some unexpected internal error has occurred in
			// * the Java Virtual Machine.
		}
	}
}
