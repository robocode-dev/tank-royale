package net.robocode2.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import net.robocode2.json_schema.events.Event;
import net.robocode2.json_schema.events.ScannedBotEvent;
import net.robocode2.json_schema.messages.BotHandshake;
import net.robocode2.json_schema.messages.BotIntent;
import net.robocode2.json_schema.messages.BotReady;
import net.robocode2.json_schema.messages.GameStartedForBot;
import net.robocode2.json_schema.messages.GameTickForBot;
import net.robocode2.json_schema.types.Point;
import net.robocode2.util.MathUtil;

public class BotClient1 extends WebSocketClient {

	final Gson gson;
	{
		RuntimeTypeAdapterFactory<Event> typeFactory = RuntimeTypeAdapterFactory.of(Event.class)
				// .registerSubtype(BotDeathEvent.class, "bot-death-event")
				// .registerSubtype(BotHitBotEvent.class, "bot-hit-bot-event")
				// .registerSubtype(BotHitWallEvent.class, "bot-hit-wall-event")
				// .registerSubtype(BulletFiredEvent.class, "bullet-fired-event")
				// .registerSubtype(BulletHitBotEvent.class, "bullet-hit-bot-event")
				// .registerSubtype(BulletHitBulletEvent.class, "bullet-hit-bullet-event")
				// .registerSubtype(BulletMissedEvent.class, "bullet-missed-event")
				.registerSubtype(ScannedBotEvent.class, "scanned-bot-event")
		// .registerSubtype(SkippedTurnEvent.class, "skipped-turn-event")
		;

		gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
	}

	static final String TYPE = "type";

	int turn;
	double targetSpeed = 10;

	Point targetPos;

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
		handshake.setType(BotHandshake.Type.BOT_HANDSHAKE);
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

		JsonElement jsonElement = jsonObject.get(TYPE);
		if (jsonElement != null) {
			String type = jsonElement.getAsString();

			if (GameStartedForBot.Type.GAME_STARTED_FOR_BOT.toString().equalsIgnoreCase(type)) {
				// Send ready signal
				BotReady ready = new BotReady();
				ready.setType(BotReady.Type.BOT_READY);

				String msg = gson.toJson(ready);
				send(msg);

			} else if (GameTickForBot.Type.GAME_TICK_FOR_BOT.toString().equalsIgnoreCase(type)) {
				GameTickForBot tick = gson.fromJson(message, GameTickForBot.class);

				Point botPos = tick.getBotState().getPosition();

				// Prepare intent
				BotIntent intent = new BotIntent();
				intent.setType(BotIntent.Type.BOT_INTENT);

				for (Event event : tick.getEvents()) {
					if (event instanceof ScannedBotEvent) {
						ScannedBotEvent scanEvent = (ScannedBotEvent) event;
						targetPos = scanEvent.getPosition();
					}
				}

				if (++turn % 25 == 0) {
					targetSpeed *= -1;
					intent.setTargetSpeed(targetSpeed);
				}

				intent.setBulletPower(Math.random() * 2.9 + 0.1);
				intent.setRadarTurnRate(45.0);

				if (targetPos != null) {
					double dx = targetPos.getX() - botPos.getX();
					double dy = targetPos.getY() - botPos.getY();

					double angle = Math.toDegrees(Math.atan2(dy, dx));

					double gunTurnRate = MathUtil
							.normalRelativeAngleDegrees(angle - tick.getBotState().getGunDirection());

					intent.setGunTurnRate(gunTurnRate);
				}

				// Send intent
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