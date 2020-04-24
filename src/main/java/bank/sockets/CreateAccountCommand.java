/**
 * 
 */
package bank.sockets;

import java.io.IOException;
import bank.Bank;

/**
 * @author Livio Näf The concrete implementation of the abstract class Command
 *         for creating an account.
 *
 */
public class CreateAccountCommand extends Command {

	private static final long serialVersionUID = -5909902081287894412L;
	private final String owner;
	private String result;

	public CreateAccountCommand(String owner) {
		this.owner = owner;
	}

	@Override
	public Command execute(Bank b) throws IOException {
		this.result = b.createAccount(owner);
		return this;
	}

	public String getResult() {
		return result;
	}
}