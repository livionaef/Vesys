/**
 * 
 */
package bank.sockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

import bank.Bank;

/**
 * @author Livio Näf
 *
 */
public class UdpHandler implements CommandHandler, Runnable {

	// alles final deklarieren
	private final DatagramSocket udpSocket;
	private final Bank localBank;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	/**
	 * Out must be opened before in -> else deadlock danger
	 * 
	 * @param udpSocket
	 * @param bank
	 * @throws IOException
	 */
	public UdpHandler(DatagramSocket udpSocket, Bank bank) throws IOException {
		this.udpSocket = udpSocket;
		this.localBank = bank;
		out = new ObjectOutputStream(new BufferedOutputStream(udpSocket.getOutputStream()));
		out.flush();
		in = new ObjectInputStream(new BufferedInputStream(udpSocket.getInputStream()));
	}

	/**
	 * Executes the Command and writes the Command to the OutputStream
	 */
	@Override
	public Command handle(Command request) throws IOException, Exception {
		out.writeObject(request.execute(localBank));
		out.flush();
		return request;
	}

	/**
	 * Reads request by request and handles them
	 */
	@Override
	public void run() {
		while (true) {
			try {
				Command request = (Command) in.readObject(); // Reads Command from InputStream
				handle(request);
			} catch (EOFException e) {
				try {
					udpSocket.close();
				} catch (IOException ioE) {
					throw new RuntimeException(ioE);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
