package bank.graphql.command;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import bank.Account;
import bank.Bank;
import bank.BankDriver;
import bank.InactiveException;
import bank.OverdrawException;
import bank.command.CommandBank;
import kotlin.text.Charsets;

public class GraphQLDriver implements bank.BankDriver, Account {
	
	private Bank bank;
	private HttpClient client;
	private HttpRequest.Builder requestBuilder;
	private String number;
	
	@Override
	public void connect(String[] args) throws IOException {
		client = HttpClient.newHttpClient();
		requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(args[0]))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json");
		bank = new CommandBank(null);
	}
	
	@Override
	public void disconnect() throws IOException {
		bank = null;
	}
	
	@Override
	public Bank getBank() {
		return bank;
	}

	@Override
	public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
		JsonObject json = sendRequest("mutation Deposit($number: ID!, $amount: Float!) {" +
									  " deposit(number: $number, amount: $amount)" +
									  "}",
									  Map.of("number", this.getNumber(), "amount", amount);
		String response = json.getJsonObject("data").getString("deposit");
		if ("INACTIVE".equals(response)) {
			throw new InactiveException();
		} else if ("ILLEGAL".equals(response)) {
			throw new IllegalArgumentException();
		}
	}
	
	private JsonObject sendRequest(String request, Map<String, Object> variables) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String requestBody = objectMapper.writeValueAsString(new Query(request, variables));
		
		HttpRequest httpRequest = requestBuilder
				.POST(HttpRequest.BodyPublishers.ofString(requestBody, Charset.forName(Charsets.UTF_8.name())))
				.build();
		
		try {
			HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			return new JsonObject(response.body());
		} catch (InterruptedException e) { throw new IOException(e); }
	}

	static class Query {
		
		private final String query;
		private final Map<String, Object> variables;
		
		public Query(String query, Map<String, Object> variables) {
			this.query = query;
			this.variables = variables;
		}
		
		public Query(String query) {
			this(query, null);
		}
		
		public String getQuery() {
			return query;
		}
		
		public Map<String, Object> getVariables() {
			return variables;
		}
	}

	@Override
	public String getNumber() throws IOException {
		return number;
	}

	@Override
	public String getOwner() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActive() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void withdraw(double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getBalance() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
