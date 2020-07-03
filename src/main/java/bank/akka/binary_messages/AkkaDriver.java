package bank.akka.binary_messages;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import bank.Bank;
import bank.BankDriver2;
import bank.command.Command;
import bank.command.CommandBank;

public class AkkaDriver implements BankDriver2 {

	private Bank bank;
	private final ActorSystem system;
	private final ActorRef client;
	private ActorSelection serverActor;

	{
		Config config = ConfigFactory.load().getConfig("BankClient");
		system = ActorSystem.create("bank-client", config);
		client = system.actorOf(Props.create(ClientActor.class, this), "ClientActor");
	}

	/*
	 * Runtime argument is path to the remote actor:
	 * akka://bank-server@127.0.0.1:2552/user/BankServer
	 */
	@Override
	public void connect(String[] args) throws IOException {
		serverActor = system.actorSelection(args[0]);
		serverActor.tell(new ConnectMessage(), client);
//		Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

		bank = new CommandBank(cmd -> {
			try {
				serverActor.tell(cmd, client);
				return queue.take(); // instead of ask pattern
//				Future<Object> result = Patterns.ask(serverActor, cmd, timeout);
//				return (Command) Await.result(result, timeout.duration());
			} catch (Exception e) {
				throw new IOException();
			}
		});
	}

	@Override
	public void disconnect() throws IOException {
		serverActor.tell(new DisconnectMessage(), client);
		serverActor = null;
		bank = null;
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	private final SynchronousQueue<Command> queue = new SynchronousQueue<>();

	class ClientActor extends AbstractActor {
		@Override
		public Receive createReceive() {
			return receiveBuilder().match(NotifyMessage.class, msg -> notifyUpdateHandlers(msg.getNumber()))
					.matchAny(event -> unhandled(event)).build();
		}
	}

	// Listeners list must be thread safe asnotification and registration are
	// performed by different threads.
	private List<UpdateHandler> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void registerUpdateHandler(UpdateHandler handler) throws IOException {
		listeners.add(handler);
	}

	private void notifyUpdateHandlers(String number) {
		listeners.forEach(listener -> {
			try {
				listener.accountChanged(number);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
