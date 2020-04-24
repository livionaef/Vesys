package bank.sockets;

import java.io.IOException;

/**
 * Interface for all the specific Handlers.
 * 
 * @author Livio Näf
 */
public interface CommandHandler {

	/**
	 * Method to to handle the Requests
	 * @throws Exception 
	 * @throws IOException 
	 */
	public Command handle(Command request) throws IOException, Exception;

}
