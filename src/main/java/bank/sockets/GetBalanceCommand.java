/**
 * 
 */
package bank.sockets;

import bank.Bank;

/**
 * @author Livio NÃ¤f
 */
public class GetBalanceCommand extends Command {

	private static final long serialVersionUID = 850036206520836353L;
	private final String number;
	private double result;

	public GetBalanceCommand(String number) {
		this.number = number;
	}

	/**
	 * Aufpassen, dass Error nicht auf Serverseite ausgegeben wird.
	 * Es muss UNBEDINGT eine Antwort an den Klienten geschickt werden!
	 */
	@Override
	public Command execute(Bank b) throws Exception {
		try {
			this.result = b.getAccount(number).getBalance();
		} catch(Exception e) {
			// falls b.getAccount == null wäre würde dies hier abgefangen werden
			// und die Exception wird im this Objekt gespeichert und mitgegeben.
			this.setException(e);
		}
		return this;
	}

	public Double getResult() {
		return result;
	}

}
