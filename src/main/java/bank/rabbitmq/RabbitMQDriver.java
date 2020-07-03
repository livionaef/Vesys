package bank.rabbitmq;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import bank.Bank;
import bank.BankDriver2;
import bank.command.Command;
import bank.command.CommandBank;
import bank.command.CommandBank.CommandHandler;

public class RabbitMQDriver implements BankDriver2 {
	
	private static final String QUEUE_NAME = "bank";
	private static final String EXCHANGE_NAME = "bank-notifications";
	private Connection connection;
	private Channel channel;
	private String replyQueueName;
	private Bank bank;
	private final SynchronousQueue<Command> queue = new SynchronousQueue<>();

	@Override
	public void connect(String[] args) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("livio");
		factory.setPassword("1234");
		factory.setVirtualHost("virtual host");
		factory.setHost(args[0]);
		factory.setPort(Integer.parseInt(args[1]));
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch (TimeoutException e) { throw new IOException(e); }
		
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		replyQueueName = channel.queueDeclare().getQueue();
		channel.basicConsume(replyQueueName, true,
				(consumerTag, message) -> {
			try {
				queue.put(deserialize(message.getBody()));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}, tag -> {});
		
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, EXCHANGE_NAME, "");

		bank = new CachingBank(CommandBank(new RabbitMQHandler()));
		
		DeliverCallback deliverCallback = (consumerTag, message) -> {
			String id = new String(message.getBody(), "UTF-8");
			bank.invalidate(id);
			notifyListeners(id);
		};
		channel.basicConsume(queueName, true, deliverCallback, tag -> {});
	}

	@Override
	public void disconnect() throws IOException {
		connection.close();
		bank = null;
	}

	@Override
	public Bank getBank() {
		return bank;
	}
	
	private final List<UpdateHandler> listeners = new CopyOnWriteArrayList<>();
	
	private void notifyListeners(String id) {
		listeners.forEach(l -> {
			try {
				l.accountChanged(id);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void registerUpdateHandler(UpdateHandler handler) throws IOException {
		listeners.add(handler);
	}
	
	private class RabbitMQHandler implements CommandHandler {
		
		@Override
		public Command handle(Command request) throws IOException {
			AMQP.BasicProperties props = new AMQP.BasicProperties
					.Builder()
					.replyTo(replyQueueName)
					.build();
			channel.basicPublish("", QUEUE_NAME, props, serialize(request));
			try {
				return (Command) queue.take();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
