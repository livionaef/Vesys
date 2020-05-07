package bank.websockets;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import bank.command.Command;

public class RequestEncoder implements Encoder.BinaryStream<Command> {

	@Override
	public void init(EndpointConfig config) {}
	
	@Override
	public void destroy() {}
	
	@Override
	public void encode(Command command,  OutputStream os) throws EncodeException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(os);
		out.writeObject(command);
	}
}
