package bank.rest.command;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import bank.Bank;
import bank.BankDriver;
import bank.command.Command;
import bank.command.CommandBank;
import bank.command.CommandBank.CommandHandler;

public class RestDriver implements BankDriver {

	private Bank bank = null;

	@Override
	public void connect(String[] args) throws IOException {
		String path = args[0];
		System.out.println("connecting to " + path);

		Client client = ClientBuilder.newClient();
		client.register(CommandProvider.class);
		WebTarget target = client.target(path);

		bank = new CommandBank(new RestHandler(target));
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	private class RestHandler implements CommandHandler {

		private final WebTarget target;

		public RestHandler(WebTarget target) {
			this.target = target;
		}

		@Override
		public Command handle(Command request) throws IOException {
			try {
				// TODO: delete unused!
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				ObjectOutputStream out = new ObjectOutputStream(baos);
//				out.writeObject(request);
//				out.close();
				Entity<Command> entity = Entity.entity(request, MediaType.APPLICATION_OCTET_STREAM);
				// TODO: what is target?
				Response response = target.request().post(entity);
				return response.readEntity(Command.class);
//				InputStream inputStream = response.readEntity(InputStream.class);
//				ObjectInputStream in = new ObjectInputStream(inputStream);
//				return (Command) in.readObject();
			} catch (Exception e) {
				throw new IOException();
			}
		}
	}
}
