package net.robocode2.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import net.robocode2.schema.*;
import net.robocode2.util.MathUtil;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class BotClient1 extends WebSocketClient {

	private final Gson gson;

	{
		RuntimeTypeAdapterFactory<Event> typeFactory = RuntimeTypeAdapterFactory.of(Event.class)
				.registerSubtype(net.robocode2.schema.BotDeathEvent.class, "BotDeathEvent")
				.registerSubtype(net.robocode2.schema.BotHitBotEvent.class, "BotHitBotEvent")
				.registerSubtype(net.robocode2.schema.BotHitWallEvent.class, "BotHitWallEvent")
				.registerSubtype(net.robocode2.schema.BulletFiredEvent.class, "BulletFiredEvent")
				.registerSubtype(net.robocode2.schema.BulletHitBotEvent.class, "BulletHitBotEvent")
				.registerSubtype(
						net.robocode2.schema.BulletHitBulletEvent.class, "BulletHitBulletEvent")
				.registerSubtype(net.robocode2.schema.BulletHitWallEvent.class, "BulletHitWallEvent")
				.registerSubtype(net.robocode2.schema.ScannedBotEvent.class, "ScannedBotEvent")
				.registerSubtype(net.robocode2.schema.SkippedTurnEvent.class, "SkippedTurnEvent")
				.registerSubtype(net.robocode2.schema.TickEventForBot.class, "TickEvent")
				.registerSubtype(net.robocode2.schema.WonRoundEvent.class, "WonRoundEvent");

		gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
	}

	private static final String TYPE = "type";

	private int turn;
	private double targetSpeed = 10;

	private Double targetX;
	private Double targetY;

	private BotClient1(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(org.java_websocket.handshake.ServerHandshake serverHandshake) {
		System.out.println("onOpen()");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("onClose(), code: " + code + ", reason: " + reason);
	}

	@Override
	public void onMessage(String message) {
		System.out.println("onMessage(): " + message);

		JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);

		JsonElement jsonType = jsonMessage.get(TYPE);
		if (jsonType != null) {
			String type = jsonType.getAsString();

			if (ServerHandshake.Type.SERVER_HANDSHAKE.toString().equalsIgnoreCase(type)) {

				// Send bot handshake
				BotHandshake handshake = new BotHandshake();
				handshake.setType(BotHandshake.Type.BOT_HANDSHAKE);
				handshake.setName("Bot name");
				handshake.setVersion("0.1");
				handshake.setAuthor("Author name");
				handshake.setCountryCode("DK");
				handshake.setGameTypes(Arrays.asList("melee", "1v1"));
				handshake.setProgrammingLang("Java");

				String msg = gson.toJson(handshake);
				send(msg);

			} else if (GameStartedEventForBot.Type.GAME_STARTED_EVENT_FOR_BOT.toString().equalsIgnoreCase(type)) {
				// Send ready signal
				BotReady ready = new BotReady();
				ready.setType(BotReady.Type.BOT_READY);

				String msg = gson.toJson(ready);
				send(msg);

			} else if (TickEventForBot.Type.TICK_EVENT_FOR_BOT.toString().equalsIgnoreCase(type)) {
				TickEventForBot tick = gson.fromJson(message, TickEventForBot.class);
				double botX = tick.getBotState().getX();
				double botY = tick.getBotState().getY();

				// Prepare intent
				BotIntent intent = new BotIntent();
				intent.setType(BotIntent.Type.BOT_INTENT);

				for (Event event : tick.getEvents()) {
					if (event instanceof ScannedBotEvent) {
						ScannedBotEvent scanEvent = (ScannedBotEvent) event;
						targetX = scanEvent.getX();
						targetY = scanEvent.getY();
					}
				}

				if (++turn % 25 == 0) {
					targetSpeed *= -1;
					intent.setTargetSpeed(targetSpeed);
				}

				intent.setFirePower(0.1 + Math.random() * 2.9);
				intent.setRadarTurnRate(45.0);

				if (targetX != null && targetY != null) {
					double dx = targetX - botX;
					double dy = targetY - botY;

					double angle = Math.toDegrees(Math.atan2(dy, dx));

					double gunTurnRate = MathUtil.normalRelativeDegrees(angle - tick.getBotState().getGunDirection());

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
		WebSocketClient client = new BotClient1(new URI("ws://localhost:55000"));
		client.connect();
	}

	@Override
	public void send(String message) {
		System.out.println("Sending: " + message);

		super.send(message);
	}
}