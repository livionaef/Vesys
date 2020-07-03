package bank.graphql.command;

import org.springframework.beans.factory.annotation.Autowired;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.stereotype.Component;
import java.io.IOException;
import bank.Bank;

@Component
public class Mutation implements GraphQLMutationResolver {

	@Autowired
	private Bank bank;
	
	public boolean closeAccount(String number) throws IOException {
		return bank.closeAccount(number);
	}
	
	public void deposit(String number, double amount) {
		try {
			bank.getAccount(number).deposit(amount);
		} catch (Exception e) { throw new ApplicationException(e); }
	}
}
