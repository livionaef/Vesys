package bank.sockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;
import bank.Account;
import bank.Bank;
import bank.BankDriver;
import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements BankDriver {

	private Bank bank;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	/**
	 * Client side: establish connection to the server and the BankProxy.
	 */
	@Override
	public void connect(String[] args) throws IOException {
		System.out.println("connect called with arguments " + Arrays.deepToString(args));

		int port = Integer.parseInt(args[1]);
		socket = new Socket(args[0], port);
		out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		out.flush(); // nie vergessen
		in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

		// Das BankProxy-Objekt ist Platzhalter auf Klientenseite f�r Server.
		// Dieses Objekt muss die Anfragen an den Server weiterleiten.
		bank = new BankProxy(); 
//XXX				request -> {
//			out.writeObject(request); out.flush();
//			try {
//				return (Command) in.readObject();
//			} catch (ClassNotFoundException e) {
//				throw new RuntimeException(e);
//			}
//		});

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
	 * Methode writeRequest ist in der Klasse Driver und nicht in AccountProxy.
	 */
	@SuppressWarnings("unchecked")
	protected <C extends Command> C writeRequest(C command) throws IOException {
		// Write command and flush
		out.writeObject(command);
		out.flush(); // nie vergessen

		// Try to get the response
		C response = null;
		try {
			response = (C) in.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		// ich werfe hier eine IOException die auf dem Server geworfen worden ist und
		// daher im Body des commands übertragen worden ist auch noch. Andere
		// IOExceptions können beim Aufruf von readObject geworfen werden.
		if (response.getException() != null) {
			Exception e = response.getException();
			if (e instanceof IOException)
				throw (IOException) e;
		}
		return response;
	}

	/**
	 * Klasse BankProxy und AccountProxy sind Elementklassen (ohne static) So kann
	 * auf den OutputStream und den InputStream der Driver-Klasse zugegriffen
	 * werden.
	 * 
	 * Die Methoden hier werden analog implementiert wie diese in der AccountProxy
	 * Klasse.
	 */
	public class BankProxy implements Bank {

		// Keine Map auf Klientenseite!
//XXX		private final CommandHandler handler;

//		public BankProxy(CommandHandler h) {
//			handler = h;
//		}
//		
//		@SuppressWarnings("unchecked")
//		<R extends Command> R writeRequest(R request) throws IOException {
//			R response = (R) handler.handle(request);
//			if (response.getException() != null) {
//				Exception e = response.getException();
//				if (e instanceof IOException) throw (IOException) e;
//			}
//			return response;
//		}

		/**
		 * Problem: - send Request to Server over the ObjectOutputStream - read Response
		 * from Server (with the Set<Kontonummern>)
		 * 
		 * Ihr Code...
		 * <p>
		 * GetAccountNumbersCommand command = new GetAccountNumbersCommand();
		 * <p>
		 * command.throwException();
		 * <p>
		 * return (Set<String>) command.getResult();
		 * 
		 * ...erzeugt NUR ein Request-Objekt und liest dann daraus das Resultat aus, und
		 * das ist noch null.
		 * 
		 */
		@Override
		public Set<String> getAccountNumbers() throws IOException {
			return writeRequest(new GetAccountNumbersCommand()).getResult();
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
		public Account getAccount(String number) throws IOException {
			if (writeRequest(new GetAccountCommand(number)).getResult()) {
				return new AccountProxy(number);
			} else {
				// siehe API Beschreibung
				return null;
			}
		}

		@Override
		public void transfer(Account from, Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			TransferCommand response = writeRequest(new TransferCommand(from.getNumber(), to.getNumber(), amount));
			response.throwException();
		}

		/**
		 * Elementklassen -> Nicht static!
		 */
		private class AccountProxy implements Account {
			// Als final deklarieren.
			private final String number;

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
