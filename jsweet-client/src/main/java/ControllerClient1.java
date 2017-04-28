import static def.jquery.Globals.$;
import static jsweet.dom.Globals.console;
import static jsweet.dom.Globals.document;
import static jsweet.dom.Globals.window;
import static jsweet.util.Globals.union;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import json_schema.GameSetup;
import json_schema.events.BotDeathEvent;
import json_schema.events.BulletHitBotEvent;
import json_schema.events.ScannedBotEvent;
import json_schema.messages.Message;
import json_schema.messages.NewBattleForObserver;
import json_schema.messages.ObserverHandshake;
import json_schema.messages.TickForObserver;
import json_schema.states.BotStateWithId;
import json_schema.states.BulletState;
import json_schema.types.Point;
import json_schema.types.ScanField;
import jsweet.dom.CanvasRenderingContext2D;
import jsweet.dom.CloseEvent;
import jsweet.dom.Event;
import jsweet.dom.HTMLCanvasElement;
import jsweet.dom.MessageEvent;
import jsweet.dom.WebSocket;
import jsweet.lang.JSON;
import jsweet.util.StringTypes;

public class ObserverClient1 {

	public static void main(String[] args) {
		window.onload = e -> {
			return new ObserverClient1();
		};
	}

	private WebSocket ws;
	private HTMLCanvasElement canvas;
	private CanvasRenderingContext2D ctx;

	private GameSetup gameSetup;
	// private Set<Participant> participants;
	private Set<BotStateWithId> botStates;
	private Set<BulletState> bulletStates;
	private Set<json_schema.events.Event> events;

	private Set<json_schema.events.ScannedBotEvent> scanEvents;

	private Map<Integer /* botId */, Point> lastBotPositions = new HashMap<>();
	private Set<Explosion> explosions = new HashSet<>();

	public ObserverClient1() {
		ws = new jsweet.dom.WebSocket("ws://localhost:50000");

		ws.onopen = e -> {
			return onOpen(e);
		};
		ws.onclose = e -> {
			return onClose(e);
		};
		ws.onmessage = e -> {
			return onMessage(e);
		};
		ws.onerror = e -> {
			return onError(e);
		};

		canvas = (HTMLCanvasElement) document.getElementById("canvas");
		ctx = canvas.getContext(StringTypes._2d);

		// drawSingleBot(); // FIXME
	}

	private Void onOpen(Event e) {
		ObserverHandshake handshake = new ObserverHandshake();
		handshake.setName("Observer name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");

		ws.send(JSON.stringify(handshake));

		return null;
	}

	private Void onClose(CloseEvent e) {
		return null;
	}

	private Void onMessage(MessageEvent e) {
		java.lang.Object data = e.$get("data");
		if (data instanceof String) {
			java.lang.Object obj = JSON.parse((String) data);

			Message msg = Message.map(obj);
			String type = msg.getType();

			if (NewBattleForObserver.TYPE.equals(type)) {
				handleNewBattleForObserver(NewBattleForObserver.map(obj));

			} else if (TickForObserver.TYPE.equals(type)) {
				handleTickForObserver(TickForObserver.map(obj));
			}
		}
		return null;
	}

	private Void onError(Event e) {
		console.error("onError: " + e.toString());
		return null;
	}

	private void handleNewBattleForObserver(NewBattleForObserver nbfo) {
		gameSetup = nbfo.getGameSetup();
		// participants = nbfo.getParticipants();

		canvas.width = gameSetup.getArenaWidth();
		canvas.height = gameSetup.getArenaHeight();

		draw();
	}

	private void handleTickForObserver(TickForObserver tfo) {
		botStates = tfo.getBotStates();
		bulletStates = tfo.getBulletStates();
		events = tfo.getEvents();

		scanEvents = new HashSet<>();

		for (BotStateWithId bot : botStates) {
			lastBotPositions.put(bot.getId(), bot.getPosition());
		}

		for (json_schema.events.Event event : events) {
			if (BotDeathEvent.TYPE.equals(event.getType())) {
				BotDeathEvent botDeathEvent = (BotDeathEvent) $.extend(false, new BotDeathEvent(), event);

				Point pos = lastBotPositions.get(botDeathEvent.getVictimId());
				explosions.add(new Explosion(pos, 40));

			} else if (BulletHitBotEvent.TYPE.equals(event.getType())) {
				BulletHitBotEvent bulletHitBotEvent = (BulletHitBotEvent) $.extend(false, new BulletHitBotEvent(),
						event);

				explosions.add(new Explosion(bulletHitBotEvent.getBullet().getPosition(), 15));

			} else if (ScannedBotEvent.TYPE.equals(event.getType())) {
				ScannedBotEvent scannedBotEvent = (ScannedBotEvent) $.extend(false, new ScannedBotEvent(), event);

				scanEvents.add(scannedBotEvent);
			}
		}

		draw();

		for (Explosion explosion : explosions) {
			explosion.size -= 5;
			if (explosion.size <= 0) {
				explosions.remove(explosion);
			}
		}
	}

	private void drawSingleBot() {
		// Clear canvas
		ctx.fillStyle = union("black");
		ctx.fillRect(0, 0, canvas.width, canvas.height);

		double x = 50;
		double y = 50;

		Point pos = new Point();
		pos.$set("x", x);
		pos.$set("y", y);

		ScanField scanField = new ScanField();
		scanField.$set("angle", 0);
		scanField.$set("radius", 1200);

		BotStateWithId bot = new BotStateWithId();
		bot.$set("id", "Bot");
		bot.$set("energy", 100);
		bot.$set("position", pos);
		bot.$set("direction", 0);
		bot.$set("gun-direction", 30);
		bot.$set("radar-direction", 20);
		bot.$set("speed", 0);
		bot.$set("scan-arc", scanField);

		drawBot(x, y, bot);
	}

	private void draw() {
		// Clear canvas
		ctx.fillStyle = union("black");
		ctx.fillRect(0, 0, canvas.width, canvas.height);

		if (bulletStates != null) {
			for (BulletState bullet : bulletStates) {
				Point pos = bullet.getPosition();

				drawBullet(pos.getX(), pos.getY(), bullet.getPower());
			}
		}

		if (botStates != null) {
			for (BotStateWithId bot : botStates) {
				Point pos = bot.getPosition();

				double x = pos.getX();
				double y = pos.getY();

				drawBot(x, y, bot);
			}
		}

		if (scanEvents != null) {
			for (ScannedBotEvent scanEvent : scanEvents) {

				Point pos = scanEvent.getPosition();

				fillCircle(pos.getX(), pos.getY(), 18, "rgba(255, 255, 0, 1.0");
			}
		}

		if (explosions != null) {
			for (Explosion explosion : explosions) {
				Point pos = explosion.pos;
				fillCircle(pos.getX(), pos.getY(), explosion.size, "red");
			}
		}
	}

	private void drawBullet(double x, double y, double power) {
		double size = Math.max(Math.sqrt(5 * power), 1);
		fillCircle(x, y, size, "white");
	}

	private void drawBot(double x, double y, BotStateWithId bot) {
		drawBotBody(x, y, bot.getDirection());
		drawGun(x, y, bot.getGunDirection());
		drawRadar(x, y, bot.getRadarDirection());
		drawScanField(x, y, bot.getRadarDirection(), bot.getScanField());
		drawLabels(x, y, bot.getId(), bot.getEnergyLevel());
	}

	private void drawBotBody(double x, double y, double direction) {
		ctx.save();

		ctx.translate(x, y);
		ctx.rotate(toRad(direction));

		ctx.fillStyle = union("blue");
		ctx.beginPath();
		ctx.fillRect(-18, -18 + 1 + 6, 36, 36 - 2 * 7);

		ctx.fillStyle = union("gray");
		ctx.beginPath();
		ctx.fillRect(-18, -18, 36, 6);
		ctx.fillRect(-18, 18 - 6, 36, 6);

		ctx.restore();
	}

	private void drawGun(double x, double y, double direction) {
		ctx.save();

		ctx.translate(x, y);

		ctx.fillStyle = union("lightgray");
		ctx.beginPath();
		ctx.arc(0, 0, 10, 0, toRad(360));
		ctx.fill();

		ctx.beginPath();
		ctx.rotate(toRad(direction));
		ctx.rect(10, -2, 14, 4);
		ctx.fill();

		ctx.restore();
	}

	private void drawRadar(double x, double y, double direction) {
		ctx.save();

		ctx.translate(x, y);
		ctx.rotate(toRad(direction));

		ctx.fillStyle = union("red");
		ctx.beginPath();
		ctx.arc(10, 0, 15, 7 * Math.PI / 10, Math.PI * 2 - 7 * Math.PI / 10, false);
		ctx.arc(12, 0, 13, Math.PI * 2 - 7 * Math.PI / 10, 7 * Math.PI / 10, true);
		ctx.fill();

		ctx.beginPath();
		ctx.arc(0, 0, 4, 0, 2 * Math.PI);
		ctx.fill();

		ctx.restore();
	}

	private void drawScanField(double x, double y, double direction, ScanField scanField) {
		console.info("drawScanField: direction=" + direction + ",scanField.angle=" + scanField.getAngle());

		double angle = toRad(scanField.getAngle());

		String color = "rgba(0, 255, 255, 0.5)";

		ctx.save();
		ctx.translate(x, y);
		ctx.rotate(toRad(direction));

		if (Math.abs(angle) < 0.0001) {
			ctx.strokeStyle = union(color);
			ctx.lineTo(scanField.getRadius(), 0);
			ctx.stroke();

		} else {
			ctx.fillStyle = union(color);
			ctx.beginPath();
			ctx.moveTo(0, 0);
			ctx.arc(0, 0, scanField.getRadius(), 0, angle, (angle < 0));
			ctx.lineTo(0, 0);
			ctx.fill();
		}

		ctx.restore();
	}

	private void drawLabels(double x, double y, int botId, double energy) {
		ctx.save();

		ctx.fillStyle = union("white");
		ctx.font = "10px Arial";

		int energyTimes10 = (int) (10 * energy);
		int energyDec = energyTimes10 % 10;
		int energyInt = energyTimes10 / 10;

		String idStr = "" + botId;
		String energyStr = energyInt + "." + energyDec;
		double idWidth = ctx.measureText(idStr).width;
		double energyWidth = ctx.measureText(energyStr).width;

		ctx.fillText(idStr, x - idWidth / 2, y + 30 + 10);
		ctx.fillText(energyStr, x - energyWidth / 2, y - 30);

		ctx.restore();
	}

	private void fillCircle(double x, double y, double r, String color) {
		ctx.fillStyle = union(color);
		ctx.beginPath();
		ctx.arc(x, y, r, 0, 2 * Math.PI);
		ctx.fill();
	}

	private double toRad(double deg) {
		return deg * Math.PI / 180;
	}

	class Explosion {
		Point pos;
		int size;

		Explosion(Point pos, int size) {
			this.pos = pos;
			this.size = size;
		}
	}
}
