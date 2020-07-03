package bank.graphql.binary_messages;

import org.springframework.beans.factory.annotation.Autowired;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import bank.command.Command;
import java.util.Base64;
import bank.Bank;

public class GraphQLServer {

	@Component
	public class Mutation implements GraphQLMutationResolver {

		@Autowired
		private Bank bank;

		public String execute(String command) throws Exception {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(command)));
			Command cmd = (Command) in.readObject();
			cmd = cmd.execute(bank);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(cmd);
			out.close();
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
	}
}
