/**
 * 
 */
package bank.sockets;

import java.io.IOException;

import bank.Account;
import bank.Bank;

/**
 * @author Livio Näf
 *
 */
public class DepositCommand extends Command {

	private static final long serialVersionUID = 693892503696738027L;
	private final String number;
	private final double amount;

	public DepositCommand(String number, double amount) {
		this.number = number;
		this.amount = amount;
	}

	@Override
	public Command execute(Bank b) throws Exception {
		try {
			Account account = b.getAccount(number);
			if (account != null) {
				try {
					account.deposit(amount);
				} catch (Exception e) {
					setException(e);
				}
				return this;

			} else { // falls account == null
				setException(new IllegalArgumentException());
				return this;
			}

		} catch (IOException e) { // should not happen
			throw new RuntimeException(e);
		}
	}

	@Override
	Object getResult() {
		return result;
	}

}
