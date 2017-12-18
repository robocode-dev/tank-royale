import static def.dom.Globals.alert;
import static def.dom.Globals.console;
import static def.dom.Globals.document;
import static def.dom.Globals.setInterval;
import static def.dom.Globals.window;
import static jsweet.util.Lang.function;

import java.util.ArrayList;
import java.util.List;

import def.dom.CloseEvent;
import def.dom.Event;
import def.dom.EventListener;
import def.dom.HTMLButtonElement;
import def.dom.HTMLCollection;
import def.dom.HTMLElement;
import def.dom.HTMLInputElement;
import def.dom.HTMLOptionElement;
import def.dom.HTMLSelectElement;
import def.dom.MessageEvent;
import def.dom.WebSocket;
import def.js.Array;
import def.js.JSON;
import json_schema.BotAddress;
import json_schema.GameSetup2;
import json_schema.controller.commands.ListBots;
import json_schema.controller.commands.PauseGame;
import json_schema.controller.commands.ResumeGame;
import json_schema.controller.commands.StartGame;
import json_schema.controller.commands.StopGame;
import json_schema.messages.BotInfo;
import json_schema.messages.BotList;
import json_schema.messages.ControllerHandshake;
import json_schema.messages.GameTypeList;
import json_schema.messages.Message2;
import json_schema.messages.ServerHandshake;

public class ControllerClient1 {

	HTMLButtonElement connectButton = (HTMLButtonElement) document.getElementById("connect");
	HTMLButtonElement startGameButton = (HTMLButtonElement) document.getElementById("start-game");
	HTMLButtonElement stopGameButton = (HTMLButtonElement) document.getElementById("stop-game");
	HTMLButtonElement pauseGameButton = (HTMLButtonElement) document.getElementById("pause-game");
	HTMLButtonElement resumeGameButton = (HTMLButtonElement) document.getElementById("resume-game");

	HTMLSelectElement gameTypeSelect = (HTMLSelectElement) document.getElementById("game-type-list");
	HTMLSelectElement botSelect = (HTMLSelectElement) document.getElementById("bot-list");

	HTMLInputElement arenaWidthInput = (HTMLInputElement) document.getElementById("arena-width");
	HTMLInputElement arenaHeightInput = (HTMLInputElement) document.getElementById("arena-height");
	HTMLInputElement minNumberOfParticipantsInput = (HTMLInputElement) document.getElementById("min-number-of-participants");
	HTMLInputElement maxNumberOfParticipantsInput = (HTMLInputElement) document.getElementById("max-number-of-participants");
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

	private List<GameSetup2> gameTypeList;
	private GameSetup2 selectedGameType;

	public ControllerClient1() {

		onClick(connectButton, e -> connect());

		onChange(gameTypeSelect, e -> handleSelectGameType());

		onClick(startGameButton, evt -> startGame());
		onClick(stopGameButton, evt -> stopGame());
		onClick(pauseGameButton, evt -> pauseGame());
		onClick(resumeGameButton, evt -> resumeGame());
	}

	private void connect() {
		disconnect();

		ws = new def.dom.WebSocket("ws://localhost:50000");

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

		return null;
	}

	private Void onClose(CloseEvent e) {
		alert("Connection closed");
		return null;
	}

	private Void onMessage(MessageEvent e) {
		Object data = e.$get("data");
		if (data instanceof String) {
			Object obj = JSON.parse((String) data);

			Message2 msg = Message2.map(obj); // Fails here
			String type = msg.getType();

			if (ServerHandshake.TYPE.equals(type)) {
				handleServerHandshake(ServerHandshake.map(obj));

			} else if (BotList.TYPE.equals(type)) {
				handleBotList(BotList.map(obj));
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

	private void handleSelectGameType() {
		System.out.println("handleSelectGameType");

		setSelectedGameType();
		updateGameSetupInputFields();

		setInterval(function(this::onRefreshBotList), 1000);
	}

	private void setSelectedGameType() {
		selectedGameType = null;
		if (gameTypeSelect.selectedOptions.length > 0) {
			int selectedIndex = (int) gameTypeSelect.selectedIndex;
			if (selectedIndex >= 0) {
				selectedGameType = gameTypeList.get(selectedIndex);
			}
		}
	}

	private void onRefreshBotList() {
		sendListBots();
	}

	private void sendListBots() {
		if (selectedGameType == null) {
			return;
		}

		ListBots listBots = new ListBots();

		Array<String> gameTypes = new Array<>();
		gameTypes.push(selectedGameType.getGameType());

		listBots.setGameTypes(gameTypes);

		ws.send(JSON.stringify(listBots));
	}

	private void handleServerHandshake(ServerHandshake handshake) {
		gameTypeList = handshake.getGames();
		
		gameTypeSelect.options.length = 0;

		for (GameSetup2 gameSetup : gameTypeList) {
			HTMLOptionElement option = (HTMLOptionElement) document.createElement("option");
			option.text = gameSetup.getGameType();
			gameTypeSelect.appendChild(option);
		}

		if (gameTypeSelect.options.length > 0) {
			HTMLOptionElement option = (HTMLOptionElement) gameTypeSelect.options.$get(0);
			option.selected = true;
		}

		handleSelectGameType();
	}
	
	private void handleBotList(BotList botList) {
		List<String> selectedValues = new ArrayList<>();

		HTMLCollection selectedOptions = botSelect.selectedOptions;

		for (int i = 0; i < selectedOptions.length; i++) {
			HTMLOptionElement option = (HTMLOptionElement) selectedOptions.$get(i);
			if (option.selected) {
				selectedValues.add(option.value);
			}
		}

		botSelect.options.length = 0;

		for (BotInfo botInfo : botList.getBots()) {
			HTMLOptionElement option = (HTMLOptionElement) document.createElement("option");

			option.value = botInfo.getHostName() + ':' + botInfo.getPort();

			option.text = botInfo.getHostName() + ":" + botInfo.getPort() + " | " + botInfo.getName() + " "
					+ botInfo.getVersion() + " | " + botInfo.getAuthor() + " | " + botInfo.getProgrammingLanguage();

			if (selectedValues.contains(option.value)) {
				option.selected = true;
			}

			botSelect.appendChild(option);
		}
	}

	private void updateGameSetupInputFields() {
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

		HTMLCollection selectedOptions = botSelect.selectedOptions;

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
		Integer maxNumberOfParticipant = null;
		if (maxNumberOfParticipantsInput.value != null && maxNumberOfParticipantsInput.value.length() > 0) {
			maxNumberOfParticipant = Integer.valueOf(maxNumberOfParticipantsInput.value);
		}
		gameSetup.setMaxNumberOfParticipants(maxNumberOfParticipant);
		gameSetup.setNumberOfRounds(Integer.valueOf(numberOfRoundsInput.value));
		// gameSetup.setGunCoolingRate(Double.valueOf(gunCoolingRateInput.value.replace(',', '.'))); // FIXME: is "0.1"
		gameSetup.setGunCoolingRate(0.1);
		gameSetup.setTurnTimeout(Integer.valueOf(turnsTimeoutInput.value));
		gameSetup.setReadyTimeout(Integer.valueOf(readyTimeoutInput.value));
		gameSetup.setDelayedObserverTurns(Integer.valueOf(delayedObserverTurnsInput.value));

		ws.send(JSON.stringify(startGame));
	}

	private void stopGame() {
		console.info("stopGame");
		ws.send(JSON.stringify(new StopGame()));
	}

	private void pauseGame() {
		console.info("pauseGame");
		ws.send(JSON.stringify(new PauseGame()));
	}

	private void resumeGame() {
		console.info("resumeGame");
		ws.send(JSON.stringify(new ResumeGame()));
	}
}