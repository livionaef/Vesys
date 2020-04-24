/**
 * 
 */
package bank.sockets;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import bank.Bank;
import bank.BankDriver;

/**
 * @author Livio Näf
 */
public class UdpDriver implements BankDriver {
	
	private DatagramSocket udpSocket;
	private Bank bank;

	@Override
	public void connect(String[] args) throws IOException {
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		udpSocket = new DatagramSocket();
		udpSocket.connect(new InetSocketAddress(host, port));
		udpSocket.setSoTimeout(2000); // 2 seconds

//		bank = new CommandBank(new UdpHandler());
		System.out.println("connected to " + host + ":" + port);
	}

	@Override
	public void disconnect() throws IOException {

	}

	@Override
	public Bank getBank() {
		return null;
	}

}
