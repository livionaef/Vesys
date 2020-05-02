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
	private String path;
	private HttpClient httpClient = HttpClient.newHttpClient();

	@Override
	public void connect(String[] args) throws IOException {
		bank = new ServletBank(new ServletHandler());
		// TODO: connect to hostname & port
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: what Object is client?
		HttpResponse<Stream<String>> response = null;
		try {
			response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response.body();
	}

	private class ServletBank extends CommandBank {

		private String number;

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
			
			private Account(String number) {
				super(number);
			}

			public void withdraw(double amount) throws IOException, OverdrawException, InactiveException {
				// TODO: what is number?
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
				throw new RuntimeException(e1);
			}
			ObjectInputStream in = new ObjectInputStream(httpResponse.body());
			try {
				return (Command) in.readObject();
			} catch (ClassNotFoundException | IOException e2) {
				// TODO correct exception?
				throw new RuntimeException(e2);
			}
		}
	}

	// TODO: Should it be implemented like this?
	public String encode(String url) {
		try {
			return java.net.URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO: correct exception?
			throw new RuntimeException(e);
		}
	}
}
