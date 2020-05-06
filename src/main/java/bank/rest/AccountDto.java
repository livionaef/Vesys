package bank.rest;

import java.io.IOException;
import bank.Account;
import bank.InactiveException;
import bank.OverdrawException;

// TODO: Correct implementation of DTO Methods?
public class AccountDto {

	private Account account;

	public AccountDto(Account a) {
		this.account = a;
	}

	public double getBalance() throws IOException {
		return account.getBalance();
	}

	public void setBalance(double amount) throws IllegalArgumentException, IOException, OverdrawException, InactiveException {
		// Remove already existing Money
		account.withdraw(getBalance());
		// Set Balance
		account.deposit(amount);
	}
}
