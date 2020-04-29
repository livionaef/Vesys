/**
 * 
 */
package bank.command;

import java.io.IOException;
import java.util.Set;

import bank.Bank;

/**
 * @author Livio Näf
 *
 */
public class GetAccountNumbersCommand extends Command {

	private static final long serialVersionUID = -235392429602264355L;
	private Set<String> result;

	@Override
	public Command execute(Bank b) throws IOException {
		this.result = b.getAccountNumbers();
		return this;
	}

	public Set<String> getResult() {
		return result;
	}

}
