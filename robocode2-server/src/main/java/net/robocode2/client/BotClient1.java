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

import net.robocode2.json_schema.messages.BotHandshake;
import net.robocode2.json_schema.messages.BotIntent;
import net.robocode2.json_schema.messages.BotReady;
import net.robocode2.json_schema.messages.NewBattleForBot;
import net.robocode2.json_schema.messages.TickForBot;

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

			} else if (TickForBot.MessageType.TICK_FOR_BOT.toString().equalsIgnoreCase(messageType)) {
				// Send intent
				BotIntent intent = new BotIntent();
				intent.setMessageType(BotIntent.MessageType.BOT_INTENT);

				int rnd = (int) (Math.random() * 5);
				if (rnd == 0) {
					intent.setTurnRate(Math.random() * 2 * 10 - 10);
				} else if (rnd == 1) {
					intent.setGunTurnRate(Math.random() * 2 * 20 - 20);
				} else if (rnd == 2) {
					intent.setRadarTurnRate(Math.random() * 2 * 45 - 45);
				} else if (rnd == 3) {
					intent.setTargetSpeed(Math.random() * 2 * 8 - 8);
				} else if (rnd == 4) {
					intent.setBulletPower(Math.random() * 2.9 + 0.1);
				}

				String msg = gson.toJson(intent);
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