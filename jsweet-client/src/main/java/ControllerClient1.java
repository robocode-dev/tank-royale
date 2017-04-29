import static jsweet.dom.Globals.console;
import static jsweet.dom.Globals.document;
import static jsweet.dom.Globals.window;

import json_schema.controller.commands.ListBots;
import json_schema.messages.BotInfo;
import json_schema.messages.BotList;
import json_schema.messages.ControllerHandshake;
import json_schema.messages.Message2;
import jsweet.dom.CloseEvent;
import jsweet.dom.Event;
import jsweet.dom.EventListener;
import jsweet.dom.HTMLButtonElement;
import jsweet.dom.HTMLOptionElement;
import jsweet.dom.HTMLSelectElement;
import jsweet.dom.MessageEvent;
import jsweet.dom.WebSocket;
import jsweet.lang.JSON;

public class ControllerClient1 {

	public static void main(String[] args) {
		window.onload = e -> {
			return new ControllerClient1();
		};
	}

	private WebSocket ws;

	public ControllerClient1() {
		initializeWebSocket();

		HTMLButtonElement listBotsButton = (HTMLButtonElement) document.getElementById("list-bots-button");
		onClick(listBotsButton, evt -> {
			listBots();
		});
	}

	private void initializeWebSocket() {
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
		ControllerHandshake handshake = new ControllerHandshake();
		handshake.setName("Controller name");
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

			Message2 msg = Message2.map(obj);
			String type = msg.getType();

			if (BotList.TYPE.equals(type)) {
				handleBotList(BotList.map(obj));
			}
		}
		return null;
	}

	private Void onError(Event e) {
		console.error("onError: " + e.toString());
		return null;
	}

	private void onClick(HTMLButtonElement button, EventListener onClick) {
		button.addEventListener("click", onClick);
	}

	private void listBots() {
		ws.send(JSON.stringify(new ListBots()));
	}

	private void handleBotList(BotList botList) {
		HTMLSelectElement select = (HTMLSelectElement) document.getElementById("bot-list");

		select.options.length = 0;

		for (BotInfo botInfo : botList.getBots()) {
			HTMLOptionElement option = (HTMLOptionElement) document.createElement("option");

			option.text = botInfo.getHostName() + ":" + botInfo.getPort() + " | " + botInfo.getName() + " "
					+ botInfo.getVersion() + " | " + botInfo.getAuthor() + " | " + botInfo.getProgrammingLanguage();
			select.appendChild(option);
		}
	}
}