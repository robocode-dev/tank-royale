import static jsweet.dom.Globals.console;
import static jsweet.dom.Globals.document;
import static jsweet.dom.Globals.window;
import static jsweet.util.Globals.union;

import java.util.Set;

import json_schema.GameSetup;
import json_schema.Participant;
import json_schema.messages.Message;
import json_schema.messages.NewBattleForObserver;
import json_schema.messages.ObserverHandshake;
import json_schema.messages.TickForObserver;
import json_schema.states.BotState;
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
		console.info("onOpen: " + e.toString());

		ObserverHandshake handshake = new ObserverHandshake();
		handshake.setName("Observer name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");

		ws.send(JSON.stringify(handshake));

		return null;
	}

	private Void onClose(CloseEvent e) {
		console.info("onClose: " + e.toString());
		return null;
	}

	private Void onMessage(MessageEvent e) {
		console.info("onMessage: " + e.toString());

		java.lang.Object data = e.$get("data");
		if (data instanceof String) {
			java.lang.Object obj = JSON.parse((String) data);

			Message msg = Message.map(obj);
			String messageType = msg.getMessageType();

			if (NewBattleForObserver.MESSAGE_TYPE.equals(messageType)) {
				handleNewBattleForObserver(NewBattleForObserver.map(obj));

			} else if (TickForObserver.MESSAGE_TYPE.equals(messageType)) {
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
		console.info("handle(NewBattleForObserver)");

		gameSetup = nbfo.getGameSetup();
		participants = nbfo.getParticipants();

		canvas.width = gameSetup.getArenaWidth();
		canvas.height = gameSetup.getArenaHeight();

		draw();
	}

	private void handleTickForObserver(TickForObserver tfo) {
		console.info("handle(TickForObserver)");

		botStates = tfo.getBotStates();
		bulletStates = tfo.getBulletStates();

		draw();
	}

	private void draw() {
		// Clear canvas
		ctx.fillStyle = union("#000000");
		ctx.fillRect(0, 0, canvas.width, canvas.height);

		for (BotState bot : botStates) {
			Position pos = bot.getPosition();
			drawBotBody(pos.getX(), pos.getY(), bot.getDirection());
			fillCircle(pos.getX(), pos.getY(), 18, "red");
		}
	}

	private void drawBotBody(double x, double y, double angle) {
		ctx.save();

		ctx.beginPath();
		ctx.translate(x, y);
		ctx.rotate(angle * Math.PI / 180);

		ctx.fillStyle = union("blue");
		ctx.fillRect(-18, -18 + 6, 36, 36 - 2 * 6);

		ctx.fillStyle = union("gray");
		ctx.fillRect(-18, -18, 36, 6);
		ctx.fillRect(-18, 18 - 6, 36, 6);

		ctx.restore();
	}

	private void fillCircle(double x, double y, double r, String color) {
		ctx.beginPath();
		ctx.arc(x, y, r, 0, 2 * Math.PI, false);
		ctx.fillStyle = union(color);
		ctx.fill();
	}
}
