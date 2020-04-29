/**
 * 
 */
package bank.command;

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
		public Command handle(Command request) throws IOException; // XXX das public wäre hier nicht nötig, denn in einem Interface sind per default ALLE Methoden public
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

	@Override
	public Account getAccount(String number) throws IOException {
		if (writeRequest(new GetAccountCommand(number)).getResult()) {
			return new AccountProxy(number);
		} else {
			return null; // siehe API Beschreibung
		}
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
	
	private class AccountProxy implements Account {
		
		private final String number; // als final deklarieren.

		private AccountProxy(String number) {
			this.number = number;
		}

		@Override
		public double getBalance() throws IOException {
			return writeRequest(new GetBalanceCommand(number)).getResult();
		}

		@Override
		public String getOwner() throws IOException {
			return writeRequest(new GetOwnerCommand(number)).getResult();
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() throws IOException {
			return writeRequest(new IsActiveCommand(number)).getResult();
		}

		@Override
		public void deposit(double amount) throws InactiveException, IOException {
			DepositCommand response = writeRequest(new DepositCommand(number, amount));
			try {
				response.throwException();
			} catch (OverdrawException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void withdraw(double amount) throws IOException, InactiveException, OverdrawException {
			WithdrawCommand response = writeRequest(new WithdrawCommand(number, amount));
			response.throwException();
		}
	}
}
