package bank.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import bank.Bank;
import bank.command.Command;
import bank.command.CommandBank;
import bank.local.Driver.LocalBank;

public class RabbitMQServer {
	
	private static final String QUEUE_NAME = "bank";
	private static final String EXCHANGE_NAME = "bank-notifications";
	private static Bank bank;
	
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("livio");
		factory.setPassword("1234");
		factory.setVirtualHost("virtual host");
		factory.setHost("host");
		factory.setPort(1234);
		
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		
		final Queue<String> pendingNotifications = new ConcurrentLinkedQueue<>();
		bank = new LocalBank();
		bank = new BankImpl(bank, nr -> pendingNotifications.offer(nr));
		
		DeliverCallback deliverCallback = (consumerTag, message) -> {
			Command request = deserialize(message.getBody());
			Command response = request.execute(bank);
			channel.basicPublish("", message.getProperties().getReplyTo(), null, serialize(response));
			while (!pendingNotifications.isEmpty()) {
				channel.basicPublish(EXCHANGE_NAME, "", null, pendingNotifications.poll().getBytes("UTF-8"));
			}
		};
		channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
	}
	
	static byte[] serialize(Command c) throws IOException {
		return serialize(c);
	}
	
	static Command deserialize(byte[] buf) throws IOException {
		return deserialize(buf);
	}
}
