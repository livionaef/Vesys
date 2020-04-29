package bank.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import bank.Bank;
import bank.local.Driver;
import bank.sockets.Command;

public class UdpServer {

	public static void main(String[] args) throws Exception {
		UdpServer udpServer = new UdpServer();
		udpServer.start();
	}

	private Map<SocketAddress, Response> answers = new HashMap<>();
	private final Bank localBank;

	public UdpServer() throws Exception {
		localBank = new Driver.LocalBank();
	}

	static class Response {
		private int transactionId = -1;
		private Command command;
	}

	public void start() throws IOException {
		try (DatagramSocket serverSocket = new DatagramSocket(1234)) {
			byte[] buf = new byte[2048];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);

			while (true) {
				try {
					packet.setData(buf);
					serverSocket.receive(packet); // always same packet
					serverSocket.send(handle(packet));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	DatagramPacket handle(DatagramPacket packet) throws Exception {
		// READ PACKET FROM IN
		SocketAddress address = packet.getSocketAddress();
		ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
		ObjectInputStream in = new ObjectInputStream(bais);
		// Read transaction id which identifies the request for each client
		int transactionId = in.readInt();

		// STORE IN MAP
		answers.putIfAbsent(address, new Response());
		Response response = answers.get(address);

		// RETRIEVE & EXECUTE & STORE COMMAND IN RESPONSE
		Command command;
		// Check whether this request has already been executed on the server
		if (response.transactionId == transactionId) {
			command = response.command;
		} else {
			try {
				command = (Command) in.readObject();
			} catch (ClassNotFoundException e) {
				throw new InternalError(e.getMessage());
			}
			response.transactionId = transactionId;
			// Execute this command and store the result in the response object
			response.command = command.execute(localBank);
		}

		// WRITE TO OUT & SET DATA FOR PACKET & RETURN
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeInt(transactionId);
		// Send result for this transaction-id back to the client (could be an old one)
		out.writeObject(command);
		out.flush();
		byte[] buf = baos.toByteArray();
		packet.setData(buf);
		return packet;
	}
}
