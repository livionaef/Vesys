package bank.websockets;

import javax.websocket.server.ServerEndpointConfig;

public class WsConfig extends ServerEndpointConfig.Configurator {

	// Singleton instance
	private final WsServer server = new WsServer();

	// TODO: what does this mean? Any influence on the programm? Best Practices?
	// I just put it because the warnings disturbed me a bit
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) {
		return endpointClass == WsServer.class ? (T) server : null;
	}

}
