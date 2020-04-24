/**
 * 
 */
package bank.sockets;

import bank.Bank;

/**
 * @author Livio Näf
 *
 */
public class TransferCommand extends Command {
	private static final long serialVersionUID = 7044565522344960231L;

	private final String from;
	private final String to;
	private final double amount;
	//private void result;

	public TransferCommand(String from, String to, double amount) {
		this.from = from;
		this.to = to;
		this.amount = amount;
	}

	@Override
	public Command execute(Bank b) {
		try {
			b.transfer(b.getAccount(from), b.getAccount(to), amount);
		} catch(Exception e) {
			setException(e);
		}
		return this;
	}

	@Override
	Object getResult() {
		return result;
	}

}
