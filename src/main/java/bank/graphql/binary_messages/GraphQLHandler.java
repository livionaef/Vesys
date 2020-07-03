package bank.graphql.binary_messages;

import bank.command.CommandBank.CommandHandler;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.io.ObjectInputStream;
import javax.json.JsonObject;
import bank.command.Command;
import java.io.IOException;
import java.util.Base64;

public class GraphQLHandler implements CommandHandler {
	
	private URI uri;
	
	@Override
	public Command handle(Command command) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(command);
		out.close();
		String request = Base64.getEncoder().encodeToString(baos.toByteArray());
		
		JsonObject json = sendRequest("{\"query\":\"mutation { execute" + "(command : \\\"" + request + "\\\" ) }\",\"variables\":null}");
		String response = json.getJsonObject("data").getString("execute");
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(response)));
		try {
			return (Command) in.readObject();
		} catch (Exception e) { throw new IOException(e); }
	}
	
	private JsonObject sendRequest(String request) throws IOException {
		HttpClient client = HttpClient.newHttpClient();
		BodyPublisher body = HttpRequest.BodyPublishers.ofString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(uri)
				.header("Content-Type", "application/json")
				.POST(body)
				.build();
		try {
			HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			return new JsonObject(response.body());
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
}
