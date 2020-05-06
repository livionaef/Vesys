package bank.rest.command;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import bank.Bank;
import bank.command.Command;
import javax.inject.Singleton;

@Singleton
@Path("/rest-command")
public class BankResource {
	
	private final Bank bank;

	public BankResource() {
		bank = new bank.local.Driver.LocalBank();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Command execute(Command request) throws Exception {
		return request.execute(bank);
	}
}
