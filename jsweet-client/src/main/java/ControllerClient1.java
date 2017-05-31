import static jsweet.dom.Globals.alert;
import static jsweet.dom.Globals.console;
import static jsweet.dom.Globals.document;
import static jsweet.dom.Globals.window;

import json_schema.BotAddress;
import json_schema.GameSetup2;
import json_schema.controller.commands.ListBots;
import json_schema.controller.commands.ListGameTypes;
import json_schema.controller.commands.StartGame;
import json_schema.messages.BotInfo;
import json_schema.messages.BotList;
import json_schema.messages.ControllerHandshake;
import json_schema.messages.GameTypeList;
import json_schema.messages.Message2;
import jsweet.dom.CloseEvent;
import jsweet.dom.Event;
import jsweet.dom.EventListener;
import jsweet.dom.HTMLButtonElement;
import jsweet.dom.HTMLCollection;
import jsweet.dom.HTMLElement;
import jsweet.dom.HTMLInputElement;
import jsweet.dom.HTMLOptionElement;
import jsweet.dom.HTMLSelectElement;
import jsweet.dom.MessageEvent;
import jsweet.dom.WebSocket;
import jsweet.lang.Array;
import jsweet.lang.JSON;

public class ControllerClient1 {

	private final static String NONE_TEXT = "[none]";

	HTMLButtonElement connectButton = (HTMLButtonElement) document.getElementById("connect");

	HTMLSelectElement gameTypeSelect = (HTMLSelectElement) document.getElementById("game-type-list");

	HTMLInputElement arenaWidthInput = (HTMLInputElement) document.getElementById("arena-width");
	HTMLInputElement arenaHeightInput = (HTMLInputElement) document.getElementById("arena-height");
	HTMLInputElement minNumberOfParticipantsInput = (HTMLInputElement) document
			.getElementById("min-number-of-participants");
	HTMLInputElement maxNumberOfParticipantsInput = (HTMLInputElement) document
			.getElementById("max-number-of-participants");
	HTMLInputElement numberOfRoundsInput = (HTMLInputElement) document.getElementById("number-of-rounds");
	HTMLInputElement gunCoolingRateInput = (HTMLInputElement) document.getElementById("gun-cooling-rate");
	HTMLInputElement inactivityTurnsInput = (HTMLInputElement) document.getElementById("inactivity-turns");
	HTMLInputElement turnsTimeoutInput = (HTMLInputElement) document.getElementById("turn-timeout");
	HTMLInputElement readyTimeoutInput = (HTMLInputElement) document.getElementById("ready-timeout");
	HTMLInputElement delayedObserverTurnsInput = (HTMLInputElement) document.getElementById("delayed-observer-turns");

	public static void main(String[] args) {
		window.onload = e -> {
			return new ControllerClient1();
		};
	}

	private WebSocket ws;

	private GameTypeList gameTypeList;
	private GameSetup2 selectedGameType;

	public ControllerClient1() {

		onClick(connectButton, e -> connect());

		onChange(gameTypeSelect, e -> handleSelectGameType());

		// onClick((HTMLButtonElement) document.getElementById("list-bots"), evt -> {
		// listBots();
		// updateGameSetup();
		// });
		//
		// onClick((HTMLButtonElement) document.getElementById("start-game"), evt -> {
		// startGame();
		// });

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

		sendHandshake();

		listGameTypes();

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

	private void onClick(HTMLElement element, EventListener listener) {
		element.addEventListener("click", listener);
	}

	private void onChange(HTMLElement element, EventListener listener) {
		element.addEventListener("change", listener);
	}

	private void sendHandshake() {
		ControllerHandshake handshake = new ControllerHandshake();
		handshake.setName("Controller name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");

		ws.send(JSON.stringify(handshake));
	}

	private void listGameTypes() {
		ws.send(JSON.stringify(new ListGameTypes()));
	}

	private void handleSelectGameType() {
		System.out.println("handleSelectGameType");

		selectedGameType = null;
		if (gameTypeSelect.selectedOptions.length > 0) {
			int selectedIndex = (int) gameTypeSelect.selectedIndex - 1; // Due to [none] item
			if (selectedIndex >= 0) {
				selectedGameType = gameTypeList.getGameTypes().get(selectedIndex);
			}
		}

		updateGameSetup();
	}

	private void listBots() {
		ListBots listBots = new ListBots();

		Array<String> gameTypes = null;

		// HTMLSelectElement select = (HTMLSelectElement) document.getElementById("game-type-list");
		// selectedGameType = null;
		// if (select.selectedOptions.length > 0) {
		// selectedGameType = gameTypeList.getGameTypes().get((int) select.selectedIndex);
		// }
		listBots.setGameTypes(gameTypes);

		ws.send(JSON.stringify(listBots));
	}

	private void handleBotList(BotList botList) {
		HTMLSelectElement select = (HTMLSelectElement) document.getElementById("bot-list");
		select.options.length = 0;

		for (BotInfo botInfo : botList.getBots()) {
			HTMLOptionElement option = (HTMLOptionElement) document.createElement("option");

			option.value = botInfo.getHostName() + ':' + botInfo.getPort();

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
		arenaWidthInput.disabled = selectedGameType.isArenaWidthFixed();
		arenaWidthInput.value = "" + selectedGameType.getArenaWidth();

		arenaHeightInput.disabled = selectedGameType.isArenaHeightFixed();
		arenaHeightInput.value = "" + selectedGameType.getArenaHeight();

		minNumberOfParticipantsInput.disabled = selectedGameType.isMinNumberOfParticipantsFixed();
		minNumberOfParticipantsInput.value = "" + selectedGameType.getMinNumberOfParticipants();

		maxNumberOfParticipantsInput.disabled = selectedGameType.isMaxNumberOfParticipantsFixed();
		Integer maxNumberOfParticipants = selectedGameType.getMaxNumberOfParticipants();
		maxNumberOfParticipantsInput.value = (maxNumberOfParticipants == null) ? null : "" + maxNumberOfParticipants;

		numberOfRoundsInput.disabled = selectedGameType.isNumberOfRoundsFixed();
		numberOfRoundsInput.value = "" + selectedGameType.getNumberOfRounds();

		gunCoolingRateInput.disabled = selectedGameType.isGunCoolingRateFixed();
		gunCoolingRateInput.value = "" + selectedGameType.getGunCoolingRate();

		inactivityTurnsInput.disabled = selectedGameType.isInactivityTurnsFixed();
		inactivityTurnsInput.value = "" + selectedGameType.getInactivityTurns();

		turnsTimeoutInput.disabled = selectedGameType.isTurnTimeoutFixed();
		turnsTimeoutInput.value = "" + selectedGameType.getTurnTimeout();

		readyTimeoutInput.value = "" + selectedGameType.getReadyTimeout();

		delayedObserverTurnsInput.disabled = selectedGameType.isDelayedObserverTurnsFixed();
		delayedObserverTurnsInput.value = "" + selectedGameType.getDelayedObserverTurns();
	}

	private void startGame() {
		console.info("startGame");

		HTMLSelectElement select = (HTMLSelectElement) document.getElementById("bot-list");

		HTMLCollection selectedOptions = select.selectedOptions;

		Array<BotAddress> botAddresses = new Array<>();

		for (int i = 0; i < selectedOptions.length; i++) {
			BotAddress botAddr = new BotAddress();

			HTMLOptionElement option = (HTMLOptionElement) selectedOptions.$get(i).valueOf();
			String value = option.value;

			String split[] = value.split(":");
			if (split.length >= 2) {
				botAddr.setHostName(split[0]);
				botAddr.setPort(split[1]);

				botAddresses.push(botAddr);
			}
		}

		StartGame startGame = new StartGame();
		startGame.setBotAddresses(botAddresses);

		GameSetup2 gameSetup = new GameSetup2();
		startGame.setGameSetup(gameSetup);

		gameSetup.setGameType(selectedGameType.getGameType());
		gameSetup.setArenaWidth(Integer.valueOf(arenaWidthInput.value));
		gameSetup.setArenaHeight(Integer.valueOf(arenaHeightInput.value));
		gameSetup.setMinNumberOfParticipants(Integer.valueOf(minNumberOfParticipantsInput.value));
		gameSetup.setMaxNumberOfParticipants(Integer.valueOf(maxNumberOfParticipantsInput.value));
		gameSetup.setNumberOfRounds(Integer.valueOf(numberOfRoundsInput.value));
		// gameSetup.setGunCoolingRate(Double.valueOf(gunCoolingRateInput.value.replace(',', '.'))); // FIXME: is "0.1"
		gameSetup.setGunCoolingRate(0.1);
		gameSetup.setTurnTimeout(Integer.valueOf(turnsTimeoutInput.value));
		gameSetup.setReadyTimeout(Integer.valueOf(readyTimeoutInput.value));
		gameSetup.setDelayedObserverTurns(Integer.valueOf(delayedObserverTurnsInput.value));

		ws.send(JSON.stringify(startGame));
	}
}