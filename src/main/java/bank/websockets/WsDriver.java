package bank.websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import bank.Bank;
import bank.BankDriver2;
import bank.command.Command;
import bank.command.CommandBank;

@ClientEndpoint(decoders = RequestDecoder.class, encoders = RequestEncoder.class)
public class WsDriver implements BankDriver2 {

	private Bank bank;
	private final SynchronousQueue<Command> queue = new SynchronousQueue<>();
	private Session session;
	private final List<UpdateHandler> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Binary Message (due to Decoder.BinaryStream<Command>)
	 */
	@OnMessage
	public void OnMessage(Command command) throws InterruptedException {
		queue.put(command);
	}

	/**
	 * Text message.
	 */
	@OnMessage
	public void onMessage(String id) {
		for (UpdateHandler handler : listeners) {
			try {
				handler.accountChanged(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void connect(String[] args) throws IOException {
		try {
			// TODO: what is the correct URI?
			// like underneath -> Connection failed error.
			// with added + ":" + args[1] -> Handshake error (Response code was not 101: 404).
			URI uri = new URI("ws://" + args[0] + "/ws-bank");
			System.out.println("connecting to " + uri);
			ClientManager client = ClientManager.createClient();
			session = client.connectToServer(this, uri);
		} catch (URISyntaxException | DeploymentException e) {
			throw new IOException(e);
		}
		bank = new CommandBank(command -> {
			try {
				session.getBasicRemote().sendObject(command);
				return queue.take();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
		session.close();
		System.out.println("disconnected...");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	@Override
	public void registerUpdateHandler(UpdateHandler handler) throws IOException {
		listeners.add(handler);
	}
}
