import static def.jquery.Globals.$;
import static jsweet.dom.Globals.console;
import static jsweet.dom.Globals.window;

import json_schema.GameSetup;
import json_schema.Message;
import json_schema.NewBattleForObserver;
import json_schema.ObserverHandshake;
import json_schema.Participant;
import jsweet.dom.CloseEvent;
import jsweet.dom.Event;
import jsweet.dom.MessageEvent;
import jsweet.dom.WebSocket;
import jsweet.lang.Array;
import jsweet.lang.JSON;

public class ObserverClient1 {

	public static void main(String[] args) {
		window.onload = e -> {
			return new ObserverClient1();
		};
	}

	private WebSocket ws;

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

	@SuppressWarnings("unchecked")
	private Void onMessage(MessageEvent e) {
		console.info("onMessage: " + e.toString());

		java.lang.Object data = e.$get("data");
		if (data instanceof String) {
			java.lang.Object obj = JSON.parse((String) data);

			Message msg = (Message) $.extend(new NewBattleForObserver(), obj);

			if (NewBattleForObserver.MESSAGE_TYPE.equals(msg.getMessageType())) {
				NewBattleForObserver nbfo = (NewBattleForObserver) $.extend(new NewBattleForObserver(), obj);

				GameSetup gameSetup = (GameSetup) $.extend(new GameSetup(), nbfo.getGameSetup());

				Array<Participant> participants = (Array<Participant>) $.extend(new Array<Participant>(),
						nbfo.getParticipants());

				console.info("game type: " + gameSetup.getGameType());
				console.info("num participants: " + participants.length);

				Participant Participant1 = (Participant) $.extend(new Participant(), participants.$get(0));
				Participant Participant2 = (Participant) $.extend(new Participant(), participants.$get(1));

				console.info("name #1: " + Participant1.getName());
				console.info("name #2: " + Participant2.getName());
			}
		}

		return null;
	}

	private Void onError(Event e) {
		console.error("onError: " + e.toString());
		return null;
	}
}
