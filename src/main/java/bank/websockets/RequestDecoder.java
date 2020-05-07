package bank.websockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import bank.command.Command;

public class RequestDecoder implements Decoder.BinaryStream<Command> {

	@Override
	public void init(EndpointConfig config) {}

	@Override
	public void destroy() {}

	@Override
	public Command decode(InputStream is) throws DecodeException, IOException {
		try {
			return (Command) new ObjectInputStream(is).readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
