package bank.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import bank.Bank;
import bank.BankDriver;
import bank.command.Command;
import bank.command.CommandBank;
import bank.command.CommandBank.CommandHandler;

/** Client side **/
public class UdpDriver implements BankDriver {

	private Bank bank;
	private DatagramSocket udpSocket;

	@Override
	public void connect(String[] args) throws IOException {
		udpSocket = new DatagramSocket();
		udpSocket.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
		bank = new CommandBank(new UdpHandler());
		System.out.println("connected to " + args[0] + ":" + args[1]);
	}

	@Override
	public void disconnect() throws IOException {
		udpSocket.close();
		bank = null; // bank auf null setzen
		System.out.println("disconnected");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	private int transactionId = 0;

	private class UdpHandler implements CommandHandler {

		@Override
		public Command handle(Command request) throws IOException {
			// PREPARATION
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			int id = transactionId++;
			out.writeInt(id);
			out.writeObject(request);
			out.flush(); // never forget
			out.close();
			
			// DATAGRAM PACKETS
			byte[] buf = baos.toByteArray();
			DatagramPacket requestDP = new DatagramPacket(buf, buf.length);
			buf = new byte[2048];
			DatagramPacket resultDP = new DatagramPacket(buf, buf.length);
			
			// HANDLING
			while (true) {
				// Continue to send requests until corresponding response arrived
				udpSocket.send(requestDP);
				try {
					udpSocket.receive(resultDP);
					ByteArrayInputStream bais = new ByteArrayInputStream(resultDP.getData());
					ObjectInputStream in = new ObjectInputStream(bais);
					int resultId = in.readInt();
					if (resultId == id) { // if response belongs to request
						try {
							return (Command) in.readObject(); // return it
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}
				} catch (SocketTimeoutException e) {
					// ignored
					// timeout has been set on socket
				}
			}
		}
	}
}
