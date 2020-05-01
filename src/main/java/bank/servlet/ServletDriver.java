package bank.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServlet;
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
	private HttpServlet client; // ??? where does client come from?

	@Override
	public void connect(String[] args) throws IOException {
		bank = new ServletBank(new ServletHandler());
		System.out.println("connected to " + args[0] + ":" + args[1]);
	}

	@Override
	public void disconnect() throws IOException {
		bank = null; // bank auf null setzen
		System.out.println("disconnected");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	private Stream<String> sendRequest(String query) throws IOException {
		HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://" + path + "?" + query)).GET().build();
		HttpResponse<Stream<String>> response = client.send(request, HttpResponse.BodyHandlers.ofLines());
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
		public void withdraw(double amount) throws IOException, OverdrawException, InactiveException {
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

	private class ServletHandler implements CommandHandler {

		@Override
		public Command handle(Command request) throws IOException {
			// PREPARATION
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(request);
			out.flush(); // never forget
			out.close();
			
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
					.build();
			// ??? what is client?
			HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
			ObjectInputStream in = new ObjectInputStream(response.body());
			return (Command) in.readObject();
		}
	}

	public String encode(String url) {
		String encodedUrl = null;
		try {
			encodedUrl = java.net.URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodedUrl;
	}
}
