package bank.akka.binary_messages;

import akka.actor.AbstractActor;
import bank.Bank;
import bank.command.Command;
import bank.local.Driver;

// Bank does not have to be Thread safe!
public class BankActor extends AbstractActor {

	private final Bank bank = new Driver.LocalBank();

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Command.class, msg -> getSender().tell(msg.execute(bank), getSelf()))
				.matchAny(msg -> unhandled(msg))
				.build();
	}
}
