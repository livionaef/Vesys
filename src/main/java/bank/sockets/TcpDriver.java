package bank.sockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import bank.Bank;
import bank.BankDriver;

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
		bank = new CommandBank(request -> {
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
}
