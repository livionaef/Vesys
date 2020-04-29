package bank.command;

import java.io.IOException;
import java.io.Serializable;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

/**
 * Every action is implemented as a concrete Command. The Result and possible
 * Exceptions will be saved.
 * 
 * <li>Used to transfer a request from client to server
 * <li>Used to transfer the result & exception from server to client
 * 
 * <li>Designfrage: 'protected Object result' bereits in der Basisklasse
 * deklarieren oder gerade in den Unterklassen?
 * Antwort Hr. Gruntz:
	 * Ich habe das bei mir glaub ich auch so gemacht. Das mit den weniger Casts
	 * stimmt jedoch nicht zwingend, denn wenn Sie in der Basisklasse bereits eine
	 * Methode abstract Object getResult() definieren, dann müssen Sie das in allen
	 * Unterklassen definieren, aber sie dürfen den Resultattyp covariant
	 * verstärken, d.h. sie Können das überschreiben mit Set<String> getResult()
	 * nicht aber mit primitiven Resultattypen, d.h. Double getResult() geht, nicht
	 * aber double getResult()
 * 
 * @author Livio Näf
 */
public abstract class Command implements Serializable {

	private static final long serialVersionUID = -5094565827986799595L;
	protected Object result;
	private Exception exception;
	
	abstract Object getResult();

	protected void setException(Exception e) {
		this.exception = e;
	}

	public Exception getException() {
		return exception;
	}

	/**
	 * Handles Command on server side, reference to the local bank is passed as a
	 * parameter
	 */
	public abstract Command execute(Bank b) throws Exception;

	public void throwException() throws IOException, InactiveException, OverdrawException {
		if (exception != null) {
			try {
				throw exception;
			} catch (InactiveException | OverdrawException | IOException | RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}