import static jsweet.dom.Globals.alert;
import static jsweet.dom.Globals.document;
import static jsweet.dom.Globals.window;

import json_schema.GameSetup2;
import json_schema.controller.commands.ListBots;
import json_schema.controller.commands.ListGameTypes;
import json_schema.messages.BotInfo;
import json_schema.messages.BotList;
import json_schema.messages.ControllerHandshake;
import json_schema.messages.GameTypeList;
import json_schema.messages.Message2;
import jsweet.dom.CloseEvent;
import jsweet.dom.Event;
import jsweet.dom.EventListener;
import jsweet.dom.HTMLButtonElement;
import jsweet.dom.HTMLInputElement;
import jsweet.dom.HTMLOptionElement;
import jsweet.dom.HTMLSelectElement;
import jsweet.dom.MessageEvent;
import jsweet.dom.WebSocket;
import jsweet.lang.Array;
import jsweet.lang.JSON;

public class ControllerClient1 {

	private final static String NONE_TEXT = "[none]";

	public static void main(String[] args) {
		window.onload = e -> {
			return new ControllerClient1();
		};
	}

	private WebSocket ws;

	private GameTypeList gameTypeList;
	private GameSetup2 selectedGameType;

	public ControllerClient1() {

		onClick((HTMLButtonElement) document.getElementById("connect"), evt -> {
			connect();
		});

		onClick((HTMLButtonElement) document.getElementById("list-game-types"), evt -> {
			listGameTypes();
		});

		onClick((HTMLButtonElement) document.getElementById("list-bots"), evt -> {
			listBots();
			updateGameSetup();
		});
	}

	private void connect() {
		disconnect();

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

	private void disconnect() {
		if (ws != null) {
			ws.close(0);
		}
	}

	private Void onOpen(Event e) {
		alert("Connection successful");

		ControllerHandshake handshake = new ControllerHandshake();
		handshake.setName("Controller name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");

		ws.send(JSON.stringify(handshake));

		return null;
	}

	private Void onClose(CloseEvent e) {
		alert("Connection closed");
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
			} else if (GameTypeList.TYPE.equals(type)) {
				gameTypeList = GameTypeList.map(obj);
				handleGameTypeList(gameTypeList);
			}
		}
		return null;
	}

	private Void onError(Event e) {
		alert("Connection error");
		return null;
	}

	private void onClick(HTMLButtonElement button, EventListener onClick) {
		button.addEventListener("click", onClick);
	}

	private void listGameTypes() {
		ws.send(JSON.stringify(new ListGameTypes()));
	}

	private void listBots() {
		ListBots listBots = new ListBots();

		HTMLSelectElement select = (HTMLSelectElement) document.getElementById("game-type-list");

		Array<String> gameTypes;
		gameTypes = null;
		selectedGameType = null;

		if (select.selectedOptions.length > 0) {
			selectedGameType = gameTypeList.getGameTypes().get((int) select.selectedIndex);
		}
		listBots.setGameTypes(gameTypes);

		ws.send(JSON.stringify(listBots));
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

	private void handleGameTypeList(GameTypeList gameTypeList) {
		HTMLSelectElement select = (HTMLSelectElement) document.getElementById("game-type-list");
		select.options.length = 0;

		HTMLOptionElement option = (HTMLOptionElement) document.createElement("option");
		option.text = NONE_TEXT;
		select.appendChild(option);

		for (GameSetup2 gameSetup : gameTypeList.getGameTypes()) {
			option = (HTMLOptionElement) document.createElement("option");
			option.text = gameSetup.getGameType();
			select.appendChild(option);
		}
	}

	private void updateGameSetup() {
		HTMLInputElement input = (HTMLInputElement) document.getElementById("arena-width");
		input.disabled = selectedGameType.isArenaWidthFixed();
		input.value = "" + selectedGameType.getArenaWidth();

		input = (HTMLInputElement) document.getElementById("arena-height");
		input.disabled = selectedGameType.isArenaHeightFixed();
		input.value = "" + selectedGameType.getArenaHeight();

		input = (HTMLInputElement) document.getElementById("min-number-of-participants");
		input.disabled = selectedGameType.isMinNumberOfParticipantsFixed();
		input.value = "" + selectedGameType.getMinNumberOfParticipants();

		input = (HTMLInputElement) document.getElementById("max-number-of-participants");
		input.disabled = selectedGameType.isMaxNumberOfParticipantsFixed();
		input.value = "" + selectedGameType.getMaxNumberOfParticipants();

		input = (HTMLInputElement) document.getElementById("number-of-rounds");
		input.disabled = selectedGameType.isNumberOfRoundsFixed();
		input.value = "" + selectedGameType.getNumberOfRounds();

		input = (HTMLInputElement) document.getElementById("gun-cooling-rate");
		input.disabled = selectedGameType.isGunCoolingRateFixed();
		input.value = "" + selectedGameType.getGunCoolingRate();

		input = (HTMLInputElement) document.getElementById("inactivity-turns");
		input.disabled = selectedGameType.isInactivityTurnsFixed();
		input.value = "" + selectedGameType.getInactivityTurns();

		input = (HTMLInputElement) document.getElementById("turn-timeout");
		input.disabled = selectedGameType.isTurnTimeoutFixed();
		input.value = "" + selectedGameType.getTurnTimeout();

		input = (HTMLInputElement) document.getElementById("ready-timeout");
		input.value = "" + selectedGameType.getReadyTimeout();

		input = (HTMLInputElement) document.getElementById("delayed-observer-turns");
		input.disabled = selectedGameType.isDelayedObserverTurnsFixed();
		input.value = "" + selectedGameType.getDelayedObserverTurns();
	}
}