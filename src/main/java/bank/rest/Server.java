package bank.rest;

import java.net.URI;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Server {

	// TODO: When starting I get a lot of Errors...
	public static void main(String[] args) throws Exception {
		URI baseUri = new URI("http://localhost:1234/rest-bank/");
		
		ResourceConfig rc = new ResourceConfig().packages("bank.rest");
//	    ResourceConfig rc = new ResourceConfig(BankResource.class);
		
		// Create and start the JDK HttpServerwith the Jersey application
		System.out.println("Starting HttpServer...");
		JdkHttpServerFactory.createHttpServer(baseUri, rc);
	}
}
