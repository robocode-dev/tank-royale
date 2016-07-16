package net.robocode2.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.BotReady;
import net.robocode2.json_schema.NewBattleForBot;

public class BotClient1 extends WebSocketClient {

	final Gson gson = new Gson();

	static final String MESSAGE_TYPE_FIELD = "message-type";

	public BotClient1(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public BotClient1(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println("onOpen()");

		BotHandshake handshake = new BotHandshake();
		handshake.setMessageType(BotHandshake.MessageType.BOT_HANDSHAKE);
		handshake.setName("Bot name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");
		handshake.setCountryCode("DK");
		handshake.setGameTypes(Arrays.asList("melee", "1v1"));
		handshake.setProgrammingLanguage("Java");

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

		JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

		JsonElement jsonElement = jsonObject.get(MESSAGE_TYPE_FIELD);
		if (jsonElement != null) {
			String messageType = jsonElement.getAsString();

			if (NewBattleForBot.MessageType.NEW_BATTLE_FOR_BOT.toString().equalsIgnoreCase(messageType)) {
				// Send ready signal
				BotReady ready = new BotReady();
				ready.setMessageType(BotReady.MessageType.BOT_READY);

				String msg = gson.toJson(ready);
				send(msg);
			}
		}
	}

	@Override
	public void onError(Exception ex) {
		System.err.println("onError():" + ex);
	}

	public static void main(String[] args) throws URISyntaxException {
		WebSocketClient client = new BotClient1(new URI("ws://localhost:50000"), new Draft_10());
		client.connect();
	}

	@Override
	public void send(String message) {
		System.out.println("Sending: " + message);

		super.send(message);
	}
}