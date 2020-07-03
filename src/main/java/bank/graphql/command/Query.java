package bank.graphql.command;

import org.springframework.beans.factory.annotation.Autowired;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.Collection;
import java.io.IOException;
import java.util.Optional;
import bank.Account;
import bank.Bank;

@Component
public class Query implements GraphQLQueryResolver {
	
	@Autowired
	private Bank bank;

	public Collection<Account> accounts() throws IOException {
		return bank.getAccountNumbers().parallelStream().map(nr -> {
			try {
				return bank.getAccount(nr);
			} catch (Exception e) { throw new RuntimeException(e); }
		}).collect(Collectors.toList());
	}
	
	public Optional<Account> account(String id) throws IOException {
		return Optional.ofNullable(bank.getAccount(id));
	}
}
