package net.robocode2.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;

import net.robocode2.schema.comm.ObserverHandshake;

public class ObserverClient1 extends WebSocketClient {

	final Gson gson = new Gson();

	static final String TYPE = "type";

	public ObserverClient1(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public ObserverClient1(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println("onOpen()");

		ObserverHandshake handshake = new ObserverHandshake();
		handshake.setType(ObserverHandshake.Type.OBSERVER_HANDSHAKE);
		handshake.setName("Observer name");
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
		WebSocketClient client = new ObserverClient1(new URI("ws://localhost:50000"));
		client.connect();
	}

	@Override
	public void send(String message) {
		System.out.println("Sending: " + message);

		super.send(message);
	}
}