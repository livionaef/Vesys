package bank.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

public class CachingBank implements Bank {
	
	private final Bank bank;
	private final Map<String, CachingAccount> cache = new HashMap<>();
	
	public CachingBank(Bank bank) throws IOException {
		this.bank = bank;
		bank.getAccountNumbers().forEach(id -> invalidate(id));
	}

	private Object invalidate(String id) {
		if (cache.containsKey(id)) {
			cache.get(id).invalidate();
		} else {
			cache.put(id, new CachingAccount(id));
		}
		return null;
	}

	@Override
	public String createAccount(String owner) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean closeAccount(String number) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getAccountNumbers() throws IOException {
		return cache.values().stream().filter(a -> {
			try {
				return a.isActive();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).map(a -> {
			try {
				return a.getNumber();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toSet());
	}

	@Override
	public Account getAccount(String number) throws IOException {
		return cache.get(number);
	}

	@Override
	public void transfer(Account a, Account b, double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
		// TODO Auto-generated method stub
	}

	class CachingAccount implements Account {
		
		private final String number;
		private String owner;
		private boolean active;
		private double balance;
		private Account account;
		private boolean valid;
		
		public CachingAccount(String number) {
			this.number = number;
		}
		
		private void invalidate() { this.valid = false; }
		private void validate() throws IOException {
			if (!valid) {
				active = getAccount().isActive();
				balance = getAccount().getBalance();
				valid = true;
			}
		}

		private Account getAccount() throws IOException {
			if (account == null) {
				account = bank.getAccount(getNumber());
			}
			return account;
		}

		@Override
		public String getNumber() throws IOException {
			return number;
		}

		@Override
		public String getOwner() throws IOException {
			if (owner == null) {
				this.owner = getAccount().getOwner();
			}
			return owner;
		}

		@Override
		public boolean isActive() throws IOException {
			validate();
			return active;
		}

		@Override
		public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
			getAccount().deposit(amount);
		}

		@Override
		public void withdraw(double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			getAccount().withdraw(amount);
		}

		@Override
		public double getBalance() throws IOException {
			validate();
			return balance;
		}
	}
}
