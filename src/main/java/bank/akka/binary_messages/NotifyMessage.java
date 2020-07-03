package bank.akka.binary_messages;

import java.io.Serializable;

public class NotifyMessage implements Serializable {
	
	private static final long serialVersionUID = 4037984489621098780L;
	private final String number;
	
	public NotifyMessage(String number) {
		this.number = number;
	}
	
	public String getNumber() {
		return number;
	}
}
