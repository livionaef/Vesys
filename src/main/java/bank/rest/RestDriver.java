package bank.rest;

import java.io.IOException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import bank.Account;
import bank.Bank;
import bank.BankDriver;
import bank.InactiveException;
import bank.OverdrawException;
import bank.command.Command;
import bank.command.CommandBank;
import bank.command.CommandBank.CommandHandler;

public class RestDriver implements BankDriver {

	private Bank bank = null;
	private WebTarget target;
	// TODO: Confused with the path... 
	private final static String DEFAULT_PATH = "http://localhost:1234/rest-bank/accounts";
	private String path = DEFAULT_PATH;

	@Override
	public void connect(String[] args) throws IOException {
		String path = this.path;// args[0];
		System.out.println("connecting to " + path);

		Client client = ClientBuilder.newClient();
//		client.register(CommandProvider.class);
		target = client.target(path);

		bank = new RestBank(new RestHandler(target));
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	private class RestBank extends CommandBank {

		public RestBank(CommandHandler h) {
			super(h);
		}

		@Override
		public String createAccount(String owner) throws IOException {
			Form form = new Form();
			form.param("owner", owner);
			Response response = target.request().post(Entity.form(form));

			if (response.getStatusInfo() != Status.CREATED)
				return null;
			String location = response.getHeaderString("Location");
			return location.substring(location.lastIndexOf("/") + 1);
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			try {
				Response response = target.path(number).request().delete();
				return response.getStatus() == 200;
			} catch (ProcessingException | WebApplicationException e) {
				throw new IOException(e);
			}
		}

		@SuppressWarnings("unused")
		private class RestAccount implements Account {
			
			private final String number;
			
			private RestAccount(String number) {
				this.number = number;
			}

			// TODO: correct when this method is in this class?
			// If yes how do I need to implement the other methods of RestAccount?
			// If not where do I need to put the withdraw() method?
			public void withdraw(double amount)
					throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
				if (amount < 0)
					throw new IllegalArgumentException();
				try {
					int code;
					do {
						Response response = target.path(number).request("application/json").get();
						if (response.getStatus() == Status.NOT_FOUND.getStatusCode())
							throw new IllegalArgumentException();
						String eTag = response.getEntityTag().getValue();
						AccountDto account = response.readEntity(AccountDto.class);
						account.setBalance(account.getBalance() - amount);

						response = target.path(number).request(MediaType.APPLICATION_JSON)
								.header("If-Match", "\"" + eTag + "\"")
								.put(Entity.entity(account, MediaType.APPLICATION_JSON));
						code = response.getStatus();
						if (code == Status.GONE.getStatusCode())
							throw new InactiveException(); // 410
						if (code == Status.BAD_REQUEST.getStatusCode())
							throw new IllegalArgumentException(); // 400
						if (code == Status.FORBIDDEN.getStatusCode())
							throw new OverdrawException(); // 403
					} while (code == Status.PRECONDITION_FAILED.getStatusCode());
					if (code != 200)
						throw new IllegalStateException("Response code: " + code);
				} catch (ProcessingException | WebApplicationException e) {
					throw new IOException(e);
				}
			}

			@Override
			public String getNumber() throws IOException {
				// TODO Auto-generated method stub
				return null;
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
			public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
				// TODO Auto-generated method stub

			}

			@Override
			public double getBalance() throws IOException {
				// TODO Auto-generated method stub
				return 0;
			}

		}
	}

	private class RestHandler implements CommandHandler {

		private final WebTarget target;

		public RestHandler(WebTarget target) {
			this.target = target;
		}

		@Override
		public Command handle(Command request) throws IOException {
			try {
				Entity<Command> entity = Entity.entity(request, MediaType.APPLICATION_OCTET_STREAM);
				Response response = target.request().post(entity);
				return response.readEntity(Command.class);
			} catch (Exception e) {
				throw new IOException();
			}
		}
	}
}
