package bank.websockets;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;
import bank.Account;
import bank.Bank;
import bank.BankDriver2.UpdateHandler;
import bank.InactiveException;
import bank.OverdrawException;
import bank.command.Command;
import bank.local.Driver;

@ServerEndpoint(value = "/ws", configurator = WsConfig.class, decoders = RequestDecoder.class, encoders = RequestEncoder.class)
public class WsServer {

	// For each session a new instance is created -> bank & sessions are static
	// and initialised inside the main method.
	// But when having a WsConfig class static is no longer needed.
	private final Bank bank = new WsBankImpl(new Driver.LocalBank(), id -> notifyListeners(id));
	private final List<Session> sessions = new CopyOnWriteArrayList<>();

	public static void main(String[] args) throws Exception {
		Server server = new Server("localhost", 1234, "/ws-bank", null, WsServer.class);
		server.start();
		System.out.println("Server started, press any key to stop the server");
		System.in.read(); // keeps Server alive
	}

	/**
	 * Declared static as it accesses the sessions. When having a WsConfig class
	 * static is no longer needed.
	 */
	private void notifyListeners(String id) {
		for (Session session : sessions) {
			try {
				// account number is returned as text message
				session.getBasicRemote().sendText(id);
			} catch (Exception e) {
				sessions.remove(session);
			}
		}
	}

	/**
	 * Command response is returned as a binary message.
	 */
	@OnMessage
	public Command onMessage(Command command) throws Exception {
		return command.execute(bank);
	}

	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		sessions.remove(session);
	}

	static class WsBankImpl implements Bank {

		private final Bank bank;
		private final UpdateHandler handler;

		public WsBankImpl(Bank bank, UpdateHandler handler) {
			this.bank = bank;
			this.handler = handler;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			String id = bank.createAccount(owner);
			if (id != null)
				handler.accountChanged(id);
			return id;
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			boolean result = bank.closeAccount(number);
			if (result)
				handler.accountChanged(number);
			return result;
		}

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			return bank.getAccountNumbers();
		}

		@Override
		public Account getAccount(String number) throws IOException {
			Account account = bank.getAccount(number);
			if (account == null)
				return null;
			return new WsAccountImpl(account, handler);
		}

		@Override
		public void transfer(Account from, Account to, double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			handler.accountChanged(from.getNumber());
			handler.accountChanged(to.getNumber());
		}
	}

	static class WsAccountImpl implements Account {

		private final Account account;
		private final UpdateHandler handler;

		public WsAccountImpl(Account account, UpdateHandler handler) {
			this.account = account;
			this.handler = handler;
		}

		@Override
		public String getNumber() throws IOException {
			return account.getNumber();
		}

		@Override
		public String getOwner() throws IOException {
			return account.getOwner();
		}

		@Override
		public boolean isActive() throws IOException {
			return account.isActive();
		}

		@Override
		public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
			account.deposit(amount);
			handler.accountChanged(account.getNumber());
		}

		@Override
		public void withdraw(double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			account.withdraw(amount);
			handler.accountChanged(account.getNumber());
		}

		@Override
		public double getBalance() throws IOException {
			return account.getBalance();
		}
	}
}
