package bank.akka.every_account_as_actor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.stream.scaladsl.BroadcastHub.Closed;

public class BankActor extends AbstractActor {

	private final Map<String, ActorRef> accounts = new HashMap<>();
	private final Set<String> closed = new HashSet<>();
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(CreateAccountRequest.class, msg -> {
					ActorRef a = getContext().actorOf(Props.create(AccountActor.class, msg.owner));
					String p = a.path().toStringWithoutAddress();
					accounts.put(p, a);
					getSender().tell(p, getSelf());
				})
				.match(GetAccountRequest.class, msg -> getSender().tell(Optional.ofNullable(accounts.get(msg.number)), getSelf()))
				.match(GetAccountsRequest.class, msg -> getSender().tell(accounts.keySet().stream().filter(a -> !closed.contains(a)).collect(Collectors.toSet()), getSelf()))
				.match(Closed.class, msg -> closed.add(getSender().path().toStringWithoutAddress()))
				.matchAny(msg -> unhandled(msg))
				.build();
	}

}
