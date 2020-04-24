/**
 * 
 */
package bank.sockets;

import java.io.IOException;

import bank.Account;
import bank.Bank;

/**
 * @author Livio Näf
 */
public class WithdrawCommand extends Command {

	private static final long serialVersionUID = -3958321604810897616L;
	private final String number;
	private final double amount;

	public WithdrawCommand(String number, double amount) {
		this.number = number;
		this.amount = amount;
	}

	@Override
	public Command execute(Bank b) {
		try {
			Account account = b.getAccount(number);
			if (account != null) {
				try { account.withdraw(amount); }
				catch (Exception e) { setException(e); }
				return this;

			} else {
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
