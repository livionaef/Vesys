/**
 * 
 */
package bank.sockets;

import bank.Bank;

/**
 * @author Livio Näf
 *
 */
public class GetOwnerCommand extends Command {

	private static final long serialVersionUID = -6673087252498062522L;
	private final String number;
	private String result;

	public GetOwnerCommand(String number) {
		this.number = number;
	}

	@Override
	public Command execute(Bank b) throws Exception {
		this.result = b.getAccount(number).getOwner();
		return this;
	}

	public String getResult() {
		return result;
	}

}
