/**
 * 
 */
package bank.sockets;

import java.io.IOException;
import java.util.Set;
import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

/**
 * @author Livio Näf
 *
 */
public class CommandBank implements Bank {

	public interface CommandHandler {
		public Command handle(Command request) throws IOException;
	}

	private final CommandHandler handler;

	public CommandBank(CommandHandler h) {
		handler = h; // Client bank can be parameterized with a handler instance
	}

	@SuppressWarnings("unchecked")
	<C extends Command> C writeRequest(C command) throws IOException {
		C response = (C) handler.handle(command);
		if (response.getException() != null) {
			Exception e = response.getException();
			if (e instanceof IOException)
				throw (IOException) e;
		}
		return response;
	}

	/**
	 * ???
	 */
	@Override
	public Account getAccount(String number) throws IOException {
		return null;
	}

	@Override
	public String createAccount(String owner) throws IOException {
		return writeRequest(new CreateAccountCommand(owner)).getResult();
	}

	@Override
	public boolean closeAccount(String number) throws IOException {
		return writeRequest(new CloseAccountCommand(number)).getResult();
	}

	@Override
	public Set<String> getAccountNumbers() throws IOException {
		return writeRequest(new GetAccountNumbersCommand()).getResult();
	}

	@Override
	public void transfer(Account a, Account b, double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
		TransferCommand response = writeRequest(new TransferCommand(a.getNumber(), b.getNumber(), amount));
		response.throwException();
	}
}
