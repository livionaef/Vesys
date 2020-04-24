/**
 * 
 */
package bank.sockets;

import java.io.IOException;

import bank.Bank;

/**
 * @author Livio Näf
 *
 */
public class CloseAccountCommand extends Command {

	private static final long serialVersionUID = 3991618769969181602L;
	private final String number;
	private boolean result;

	public CloseAccountCommand(String number) {
		this.number = number;
	}

	@Override
	public Command execute(Bank b) throws IOException {
		this.result = b.closeAccount(number);
		return this;
	}

	public Boolean getResult() {
		return result;
	}

}
