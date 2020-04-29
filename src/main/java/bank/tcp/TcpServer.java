package bank.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bank.Bank;
import bank.local.Driver;

/**
 * A simple dynamic Threadpool which uses the local implementation of the bank
 * driver.
 * 
 * @author Livio Näf
 */
public class TcpServer {

	private final ServerSocket serverSocket;
	private final Bank localBank;

	public static void main(String[] args) throws IOException {
		TcpServer vesysServer = new TcpServer(1234);
		vesysServer.start();
	}

	public TcpServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		localBank = new Driver.LocalBank();
	}

	/**
	 * Starts the Server with a given CommandHandler.
	 */
	public void start() throws IOException {
		ExecutorService service = Executors.newCachedThreadPool();
		while (true) {
			Socket socket = serverSocket.accept();
			service.execute(new TcpHandler(socket, localBank));
		}
	}
}
