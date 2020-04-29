/**
 * 
 */
package bank.command;

import bank.Bank;

/**
 * @author Livio Näf
 *
 */
public class IsActiveCommand extends Command {

	private static final long serialVersionUID = 6290408479278773565L;
	private final String number;
	private boolean result;

	public IsActiveCommand(String number) {
		this.number = number;
	}

	@Override
	public Command execute(Bank b) throws Exception {
		this.result = b.getAccount(number).isActive();
		return this;
	}

	public Boolean getResult() {
		return result;
	}

}
