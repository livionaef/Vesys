package bank.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import bank.Bank;
import bank.sockets.Command;

/**
 * Serializes the Commands, executes them and sends the changed Command back.
 * Server side.
 * 
 * @author Livio Näf
 */
public class TcpHandler implements Runnable {

	private final Socket socket;
	private final Bank localBank;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	/**
	 * Out must be opened before in -> else deadlock danger
	 */
	public TcpHandler(Socket socket, Bank bank) throws IOException {
		this.socket = socket;
		this.localBank = bank;
		out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		out.flush();
		in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	}

	/**
	 * Reads request by request and handles them
	 */
	@Override
	public void run() {
		while (true) {
			try {
				Command request = (Command) in.readObject(); // Reads Request (Command) from InputStream
				out.writeObject(request.execute(localBank)); // Writes Response to OutputStream
				out.flush(); // never forget
			} catch (EOFException e) {
				try {
					socket.close();
				} catch (IOException ioE) {
					throw new RuntimeException(ioE);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}