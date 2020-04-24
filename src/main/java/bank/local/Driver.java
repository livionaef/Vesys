/*
 * Copyright (c) 2020 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {
	private LocalBank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new LocalBank();
		System.out.println("connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public bank.Bank getBank() {
		return bank;
	}

	public static class LocalBank implements Bank {

		// Map with the Account number and the corresponding Account Object
		private final Map<String, LocalAccount> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() {
			// Only active acounts will be returned
			return accounts.keySet().stream().filter(id -> accounts.get(id).active).collect(Collectors.toSet());
		}

		@Override
		public String createAccount(String owner) {
			LocalAccount create = new LocalAccount(owner);
			accounts.put(create.getNumber(), create);
			return create.getNumber();
		}

		@Override
		public boolean closeAccount(String number) {
			LocalAccount a = accounts.get(number);
			if (a != null) {
				if (!a.isActive() || a.getBalance() != 0) return false;
				a.passivate();
				return true;
			}
			return false;
		}

		@Override
		public bank.Account getAccount(String number) {
			return accounts.get(number);
		}

		@Override
		public void transfer(Account from, Account to, double amount) throws IOException, InactiveException, OverdrawException {
			if (!to.isActive()) {
				throw new InactiveException("account " + to.getNumber() + " is not active");
			}
			from.withdraw(amount);
			to.deposit(amount);
		}
	}

	private static class LocalAccount implements bank.Account {
		// die Felder number & owner final deklarieren.
		private final String number;
		private final String owner;
		
		private double balance;
		private boolean active = true;
		
		private static final String PREFIX = "101-47-";
		private static int id = 0;

		private LocalAccount(String owner) {
			this.owner = owner;
			// maximum of 999 accounts - for more implementation needs to be changed
			this.number = PREFIX + String.format("%03d", id++);
		}

		@Override
		public double getBalance() {
			return balance;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() {
			return active;
		}
		
		private void passivate() {
			active = false;
		}

		@Override
		public void deposit(double amount) throws InactiveException {
			if (!isActive()) throw new InactiveException("account not active");
			if (amount < 0) throw new IllegalArgumentException("only positive amount allowed");
			balance += amount;
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException {
			if (!isActive()) throw new InactiveException("account not active");
			if (amount < 0) throw new IllegalArgumentException("only positive amount allowed");
			if (amount > balance) throw new OverdrawException("Account cannot be overdrawn");
			balance -= amount;
		}
	}
}