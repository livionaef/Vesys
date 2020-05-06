package bank.rest.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import bank.Bank;
import bank.command.Command;
import bank.local.Driver;

// TODO: When starting I get a lot of Errors...
public class Server {

	private Bank bank;

	public Server() {
		bank = new Driver.LocalBank();
	}

	public static void main(String[] args) throws Exception {
		URI baseUri = new URI("http://localhost:1234/");

		// @Singleton annotations will be respected 
	    ResourceConfig rc = new ResourceConfig(BankResource.class);
//		ResourceConfig rc = new ResourceConfig().packages("bank.rest.command");

		// Create and start the JDK HttpServer with the Jersey application
		JdkHttpServerFactory.createHttpServer(baseUri, rc);
	}

	@POST
	public Response execute(InputStream inputStream) throws Exception {
		ObjectInputStream in = new ObjectInputStream(inputStream);
		Command request = (Command) in.readObject();
		Command response = request.execute(bank);

		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream outputStream) throws IOException {
				ObjectOutputStream out = new ObjectOutputStream(outputStream);
				out.writeObject(response);
				out.flush();
			}
		};
		return Response.ok(stream).build();
	}
}
