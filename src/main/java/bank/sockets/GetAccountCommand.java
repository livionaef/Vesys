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
public class GetAccountCommand extends Command {

	private static final long serialVersionUID = -2698524479618546373L;
	private final String number;
	private boolean result;

	public GetAccountCommand(String number) {
		this.number = number;
	}

	/**
	 * Problem: result = Account ist nicht möglich weil Account nicht serliaisierbar
	 * ist (macht sinn: das Konto sollte den Server nicht verlassen!)
	 * 
	 * result = boolean (account != null) -> ob account existiert
	 */
	@Override
	public Command execute(Bank b) throws IOException {
		this.result = b.getAccount(number) != null;
		return this;
	}

	public Boolean getResult() {
		return result;
	}

}
