package bank.akka.every_account_as_actor;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import bank.Account;
import bank.InactiveException;
import bank.OverdrawException;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class AccountImpl implements Account {
	
	private final ActorRef accountActor;
	
	public AccountImpl(ActorRef actorRef) {
		accountActor = actorRef;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T sendRequest(ActorRef server, Object request) throws IOException {
		try {
			Timeout timeout = new Timeout(5,TimeUnit.SECONDS);
			Future<Object> result = Patterns.ask(server, request, timeout);
			return (T) Await.result(result, timeout.duration());
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public String getNumber() throws IOException {
		return accountActor.path().toStringWithoutAddress();
	}

	@Override
	public String getOwner() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActive() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
		Optional<Exception> e = sendRequest(accountActor, new DepositRequest(amount));
		if (e.isPresent()) {
			try {
				throw e.get();
			} catch (IOException | IllegalArgumentException | InactiveException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new IOException(ex);
			}
		}
	}

	@Override
	public void withdraw(double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getBalance() throws IOException {
		return sendRequest(accountActor, new GetBalanceRequest());
	}

}
