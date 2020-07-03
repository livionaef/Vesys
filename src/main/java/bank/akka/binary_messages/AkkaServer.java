package bank.akka.binary_messages;

import java.util.ArrayList;
import java.util.List;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import bank.Bank;
import bank.command.Command;
import bank.local.Driver;

public class AkkaServer {

	public static void main(String[] args) {
		Config config = ConfigFactory.load().getConfig("BankServer");
		ActorSystem system = ActorSystem.create("bank-server", config);
		system.actorOf(Props.create(BankActor.class), "BankServer");
	}
	
	static class BankActor extends AbstractActor {
		
		private final List<ActorRef> sessions = new ArrayList<>();
		// BankImplis a decorator which notifies changes on accounts
		private final Bank bank = new BankImpl(new Driver.LocalBank(), id -> notifyListeners(id));

		@Override
		public Receive createReceive() {
			return receiveBuilder()
					.match(Command.class, msg -> getSender().tell(msg.execute(bank), getSelf()))
					.match(ConnectMessage.class, msg -> sessions.add(getSender()))
					.match(DisconnectMessage.class, msg -> sessions.remove(getSender()))
					.matchAny(msg -> unhandled(msg))
					.build();
		}
		
		private void notifyListeners(String id) {
			// removes the clients which are no longer reachable
			sessions.removeIf(actor -> actor.isTerminated());
			sessions.forEach(actor -> actor.tell(new NotifyMessage(id), getSelf()));
		}
	}
}
