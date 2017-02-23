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
import json_schema.Participant;
import json_schema.events.BotDeathEvent;
import json_schema.events.BulletHitBotEvent;
import json_schema.messages.Message;
import json_schema.messages.NewBattleForObserver;
import json_schema.messages.ObserverHandshake;
import json_schema.messages.TickForObserver;
import json_schema.states.BotStateWithId;
import json_schema.states.BulletState;
import json_schema.types.Position;
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
	private Set<Participant> participants;
	private Set<BotStateWithId> botStates;
	private Set<BulletState> bulletStates;
	private Set<json_schema.events.Event> events;

	private Map<Integer /* botId */, Position> lastBotPositions = new HashMap<>();
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
		participants = nbfo.getParticipants();

		canvas.width = gameSetup.getArenaWidth();
		canvas.height = gameSetup.getArenaHeight();

		draw();
	}

	private void handleTickForObserver(TickForObserver tfo) {
		botStates = tfo.getBotStates();
		bulletStates = tfo.getBulletStates();
		events = tfo.getEvents();

		for (BotStateWithId bot : botStates) {
			lastBotPositions.put(bot.getId(), bot.getPosition());
		}

		for (json_schema.events.Event event : events) {
			if (BotDeathEvent.TYPE.equals(event.getType())) {
				BotDeathEvent botDeathEvent = (BotDeathEvent) $.extend(false, new BotDeathEvent(), event);

				Position pos = lastBotPositions.get(botDeathEvent.getVictimId());
				explosions.add(new Explosion(pos, 40));

			} else if (BulletHitBotEvent.TYPE.equals(event.getType())) {
				BulletHitBotEvent bulletHitBotEvent = (BulletHitBotEvent) $.extend(false, new BulletHitBotEvent(),
						event);

				explosions.add(new Explosion(bulletHitBotEvent.getBullet().getPosition(), 15));
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

	private void draw() {
		// Clear canvas
		ctx.fillStyle = union("black");
		ctx.fillRect(0, 0, canvas.width, canvas.height);

		for (BulletState bullet : bulletStates) {
			Position pos = bullet.getPosition();

			drawBullet(pos.getX(), pos.getY(), bullet.getPower());
		}

		for (BotStateWithId bot : botStates) {
			Position pos = bot.getPosition();

			double x = pos.getX();
			double y = pos.getY();

			drawBotBody(x, y, bot.getDirection());
			drawGun(x, y, bot.getGunDirection());
		}

		for (Explosion explosion : explosions) {
			Position pos = explosion.pos;
			fillCircle(pos.getX(), pos.getY(), explosion.size, "red");
		}
	}

	private void drawBullet(double x, double y, double power) {
		double size = Math.max(Math.sqrt(5 * power), 1);
		fillCircle(x, y, size, "white");
	}

	private void drawBotBody(double x, double y, double angle) {
		ctx.save();

		ctx.beginPath();
		ctx.translate(x, y);
		ctx.rotate(angle * Math.PI / 180);

		ctx.fillStyle = union("blue");
		ctx.fillRect(-18, -18 + 1 + 6, 36, 36 - 2 * 7);

		ctx.fillStyle = union("gray");
		ctx.fillRect(-18, -18, 36, 6);
		ctx.fillRect(-18, 18 - 6, 36, 6);

		ctx.restore();
	}

	private void drawGun(double x, double y, double angle) {
		ctx.save();

		fillCircle(x, y, 10, "cyan");

		ctx.beginPath();
		ctx.translate(x, y);
		ctx.rotate(angle * Math.PI / 180);

		ctx.fillStyle = union("cyan");
		ctx.fillRect(10, -2, 14, 4);

		ctx.restore();
	}

	private void fillCircle(double x, double y, double r, String color) {
		ctx.beginPath();
		ctx.fillStyle = union(color);
		ctx.arc(x, y, r, 0, 2 * Math.PI, false);
		ctx.fill();
	}

	class Explosion {
		Position pos;
		int size;

		Explosion(Position pos, int size) {
			this.pos = pos;
			this.size = size;
		}
	}
}
