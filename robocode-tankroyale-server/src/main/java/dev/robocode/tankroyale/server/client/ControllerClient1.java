package dev.robocode.tankroyale.server.client;

import com.google.gson.Gson;
import dev.robocode.tankroyale.schema.ControllerHandshake;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ControllerClient1 extends WebSocketClient {

	private final Gson gson = new Gson();

	private ControllerClient1(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println("onOpen()");

		ControllerHandshake handshake = new ControllerHandshake();
		handshake.setType(ControllerHandshake.Type.CONTROLLER_HANDSHAKE);
		handshake.setName("Controller name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");

		String msg = gson.toJson(handshake);
		send(msg);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("onClose(), code: " + code + ", reason: " + reason);
	}

	@Override
	public void onMessage(String message) {
		System.out.println("onMessage(): " + message);
	}

	@Override
	public void onError(Exception ex) {
		System.err.println("onError():" + ex);
	}

	public static void main(String[] args) throws URISyntaxException {
		WebSocketClient client = new ControllerClient1(new URI("ws://localhost:55000"));
		client.connect();
	}

	@Override
	public void send(String message) {
		System.out.println("Sending: " + message);

		super.send(message);
	}
}