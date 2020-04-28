package bank.sockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import bank.Account;
import bank.Bank;
import bank.BankDriver;
import bank.InactiveException;
import bank.OverdrawException;

/** Client side **/
public class TcpDriver implements BankDriver {

	private Bank bank;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	/**
	 * Establish connection to the server and the BankProxy.
	 */
	@Override
	public void connect(String[] args) throws IOException {
		System.out.println("connect called with arguments " + Arrays.deepToString(args));
		
		// CONNECTION
		socket = new Socket(args[0], Integer.parseInt(args[1]));
		out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		out.flush(); // nie vergessen
		in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		
		// SETUP BANK
		// Das BankProxy-Objekt ist Platzhalter für Server (auf Klientenseite).
		bank = new BankProxy(request -> {
			out.writeObject(request);
			out.flush();
			try {
				return (Command) in.readObject();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});

		System.out.println("connected to " + args[0] + ":" + args[1]);
	}

	@Override
	public void disconnect() throws IOException {
		out.close();
		in.close();
		socket.close();
		bank = null; // bank auf null setzen
		System.out.println("disconnected");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	/**
	 * Klasse BankProxy und AccountProxy sind Elementklassen (kein static) So kann
	 * auf den OutputStream und den InputStream der Driver-Klasse zugegriffen
	 * werden.
	 */
	public class BankProxy extends CommandBank {

		public BankProxy(CommandHandler h) {
			super(h);
		}

		@Override
		public Account getAccount(String number) throws IOException {
			if (writeRequest(new GetAccountCommand(number)).getResult()) {
				return new AccountProxy(number);
			} else {
				return null; // siehe API Beschreibung
			}
		}

		/**
		 * Elementklassen -> Nicht static
		 */
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
}
