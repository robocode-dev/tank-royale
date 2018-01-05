package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;

import net.robocode2.json_schema.Participant;
import net.robocode2.json_schema.comm.BotAddress;
import net.robocode2.json_schema.comm.BotHandshake;
import net.robocode2.json_schema.comm.BotInfo;
import net.robocode2.json_schema.comm.BotListUpdate;
import net.robocode2.json_schema.comm.ControllerHandshake;
import net.robocode2.json_schema.comm.Message;
import net.robocode2.json_schema.comm.ObserverHandshake;
import net.robocode2.json_schema.events.GameStartedEventForBot;
import net.robocode2.json_schema.events.GameStartedEventForObserver;
import net.robocode2.json_schema.events.TickEventForBot;
import net.robocode2.json_schema.events.TickEventForObserver;
import net.robocode2.model.BotIntent;
import net.robocode2.model.GameSetup;
import net.robocode2.model.IRound;
import net.robocode2.model.ITurn;
import net.robocode2.model.ImmutableGameState;
import net.robocode2.model.controller.ModelUpdater;
import net.robocode2.model.mappers.BotHandshakeToBotInfoMapper;
import net.robocode2.model.mappers.BotIntentToBotIntentMapper;
import net.robocode2.model.mappers.GameSetupToGameSetupMapper;
import net.robocode2.model.mappers.TurnToGameTickForBotMapper;
import net.robocode2.model.mappers.TurnToGameTickForObserverMapper;

public final class GameServer {

	private ServerSetup serverSetup;
	private ConnListener connListener;
	private ConnHandler connHandler;

	private RunningState runningState;
	private GameSetup gameSetup;
	private ImmutableGameState gameState;

	private Set<WebSocket> participants;
	private Set<WebSocket> readyParticipants;

	private Map<WebSocket, Integer> participantIds = new HashMap<>();

	private Map<WebSocket, BotIntent> botIntents = new HashMap<>();

	private Timer readyTimer;
	private Timer turnTimer;

	private ModelUpdater modelUpdater;

	private int delayedObserverTurnNumber;

	private final Gson gson = new Gson();

	public GameServer() {
		this.serverSetup = new ServerSetup();
		this.connListener = new GameServerConnListener();
		this.connHandler = new ConnHandler(serverSetup, connListener);
		this.runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
	}

	public void start() {
		connHandler.start();
	}

	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.start();
	}

	private void startGameIfParticipantsReady() {
		if (readyParticipants.size() == participants.size()) {

			if (readyTimer != null) {
				readyTimer.cancel();
			}
			readyParticipants.clear();
			botIntents.clear();

			startGame();
		}
	}

	private void prepareGame() {
		System.out.println("#### PREPARE GAME #####");

		runningState = RunningState.WAIT_FOR_READY_PARTICIPANTS;

		participantIds.clear();

		// Send NewBattle to all participant bots to get them started

		GameStartedEventForBot gameStartedForBot = new GameStartedEventForBot();
		gameStartedForBot.setType(GameStartedEventForBot.Type.GAME_STARTED_EVENT_FOR_BOT);
		gameStartedForBot.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup.toImmutableGameSetup()));

		int id = 1;
		for (WebSocket participant : participants) {
			participantIds.put(participant, id);
			gameStartedForBot.setMyId(id);

			String msg = gson.toJson(gameStartedForBot);
			send(participant, msg);

			id++;
		}

		readyParticipants = new HashSet<WebSocket>();

		// Start 'ready' timer

		readyTimer = new Timer("Bot-ready-timer");
		readyTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				onReadyTimeout();
			}
		}, gameSetup.getReadyTimeout());
	}

	private void startGame() {
		System.out.println("#### START GAME #####");

		runningState = RunningState.GAME_RUNNING;

		// Send NewBattle to all participant observers to get them started
		if (connHandler.getObserverConnections().size() > 0) {
			GameStartedEventForObserver gameStartedForObserver = new GameStartedEventForObserver();
			gameStartedForObserver.setType(GameStartedEventForObserver.Type.GAME_STARTED_EVENT_FOR_OBSERVER);
			gameStartedForObserver.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup.toImmutableGameSetup()));

			List<Participant> list = new ArrayList<>();
			for (WebSocket bot : participants) {
				BotHandshake h = connHandler.getBotConnections().get(bot);
				Participant p = new Participant();
				p.setId(participantIds.get(bot));
				p.setAuthor(h.getAuthor());
				p.setCountryCode(h.getCountryCode());
				p.setGameTypes(h.getGameTypes());
				p.setName(h.getName());
				p.setProgrammingLanguage(h.getProgrammingLanguage());
				p.setVersion(h.getVersion());
				list.add(p);
			}
			gameStartedForObserver.setParticipants(list);

			String msg = gson.toJson(gameStartedForObserver);

			for (Entry<WebSocket, ObserverHandshake> entry : connHandler.getObserverConnections().entrySet()) {
				WebSocket observer = entry.getKey();
				send(observer, msg);
			}
		}

		// Prepare model update

		modelUpdater = ModelUpdater.create(gameSetup, new HashSet<Integer>(participantIds.values()));

		// Create timer to updating game state

		turnTimer = new Timer("turn-timer");
		turnTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				onUpdateGameState();
			}
		}, gameSetup.getTurnTimeout(), gameSetup.getTurnTimeout());
	}

	private void stopGame() {
		System.out.println("#### STOP GAME #####");

		runningState = RunningState.GAME_STOPPED;

		// TODO: Present score for bots and observers. Af that, set game state to the initial state
	}

	private void pauseGame() {
		System.out.println("#### PAUSE GAME #####");

		runningState = RunningState.GAME_PAUSED;
	}

	private void resumeGame() {
		System.out.println("#### RESUME GAME #####");

		if (runningState == RunningState.GAME_PAUSED) {
			runningState = RunningState.GAME_RUNNING;
		}
	}

	private ImmutableGameState updateGameState() {
		Map<Integer /* BotId */, BotIntent> mappedBotIntents = new HashMap<>();

		for (Entry<WebSocket, BotIntent> entry : botIntents.entrySet()) {
			int botId = participantIds.get(entry.getKey());
			mappedBotIntents.put(botId, entry.getValue());
		}

		return modelUpdater.update(Collections.unmodifiableMap(mappedBotIntents));
	}

	private void onReadyTimeout() {
		System.out.println("#### READY TIMEOUT EVENT #####");

		if (readyParticipants.size() >= gameSetup.getMinNumberOfParticipants()) {
			// Start the game with the participants that are ready
			participants = readyParticipants;
			startGame();

		} else {
			// Not enough participants -> prepare another game
			runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
		}
	}

	private void onUpdateGameState() {
		System.out.println("#### UPDATE GAME STATE EVENT #####");

		if (runningState == RunningState.GAME_PAUSED) {
			return;
		}

		// Update game state
		if (runningState != RunningState.GAME_STOPPED) {
			gameState = updateGameState();

			// Clear bot intents
			botIntents.clear();

			// Send tick to bots

			IRound round = gameState.getLastRound();
			ITurn turn = round.getLastTurn();

			// Send game state as 'game tick' to participants
			for (WebSocket participant : participants) {
				TickEventForBot gameTickForBot = TurnToGameTickForBotMapper.map(round, turn, participantIds.get(participant));
				if (gameTickForBot != null) { // Bot alive?
					String msg = gson.toJson(gameTickForBot);
					send(participant, msg);
				}
			}
		}

		// Send delayed tick to observers

		IRound observerRound = gameState.getLastRound();
		ITurn observerTurn = observerRound.getLastTurn();

		if (gameState.isGameEnded() || runningState == RunningState.GAME_STOPPED) {
			delayedObserverTurnNumber++;
			if (delayedObserverTurnNumber == observerTurn.getTurnNumber()) {

				System.out.println("#### GAME ENDED #####");

				// Stop timer for updating game state
				turnTimer.cancel();

				// Game has stopped
				runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
			}
		} else {
			delayedObserverTurnNumber = observerTurn.getTurnNumber() - gameSetup.getDelayedObserverTurns();
			if (delayedObserverTurnNumber < 0) {
				int delayedRoundNumber = observerRound.getRoundNumber() - 1;
				if (delayedRoundNumber >= 0) {
					observerRound = gameState.getRounds().get(delayedRoundNumber);
					delayedObserverTurnNumber += observerRound.getTurns().size();
				}
			}
		}
		if (delayedObserverTurnNumber >= 0) {
			observerTurn = observerRound.getTurns().get(delayedObserverTurnNumber);

			// Send game state as 'tick' to observers
			for (Map.Entry<WebSocket, ObserverHandshake> entry : connHandler.getObserverConnections().entrySet()) {
				TickEventForObserver gameTickForObserver = TurnToGameTickForObserverMapper.map(observerRound,
						observerTurn);

				String msg = gson.toJson(gameTickForObserver);
				send(entry.getKey(), msg);
			}
		}
	}

	private static void send(WebSocket conn, String message) {
		System.out.println("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

		conn.send(message);
	}

	private void updateBotIntent(WebSocket bot, net.robocode2.json_schema.comm.BotIntent intent) {
		if (!participants.contains(bot)) {
			return;
		}
		BotIntent botIntent = botIntents.get(bot);
		if (botIntent == null) {
			botIntent = BotIntent.builder().build();
			botIntents.put(bot, botIntent);
		}
		botIntent.update(BotIntentToBotIntentMapper.map(intent));
	}

	private String createBotListUpdateMessage() {
		BotListUpdate botListUpdate = new BotListUpdate();
		botListUpdate.setType(Message.Type.BOT_LIST_UPDATE);
		List<BotInfo> bots = new ArrayList<BotInfo>();
		botListUpdate.setBots(bots);

		Map<WebSocket, BotHandshake> botConnections = connHandler.getBotConnections();
		if (botConnections != null) {
			for (Entry<WebSocket, BotHandshake> entry : botConnections.entrySet()) {
				InetSocketAddress address = entry.getKey().getRemoteSocketAddress();
				BotInfo botInfo = BotHandshakeToBotInfoMapper.map(entry.getValue(), address.getHostString(), address.getPort());
				bots.add(botInfo);
			}
		}

		String msg = gson.toJson(botListUpdate);

		return msg;
	}
	
	private void sendBotListUpdateToObservers() {
		String msg = createBotListUpdateMessage();
		for (Entry<WebSocket, ObserverHandshake> entry : connHandler.getObserverConnections().entrySet()) {
			WebSocket observer = entry.getKey();
			send(observer, msg);
		}
		for (Entry<WebSocket, ControllerHandshake> entry : connHandler.getControllerConnections().entrySet()) {
			WebSocket controller = entry.getKey();
			send(controller, msg);
		}
	}

	private class GameServerConnListener implements ConnListener {

		@Override
		public void onException(Exception exception) {
			exception.printStackTrace();
		}

		@Override
		public void onBotJoined(WebSocket socket, BotHandshake bot) {
			sendBotListUpdateToObservers();
		}

		@Override
		public void onBotLeft(WebSocket socket) {
			sendBotListUpdateToObservers();
		}

		@Override
		public void onObserverJoined(WebSocket socket, ObserverHandshake bot) {
			String msg = createBotListUpdateMessage();
			send(socket, msg);
		}

		@Override
		public void onObserverLeft(WebSocket socket) {
			// Do nothing
		}

		@Override
		public void onControllerJoined(WebSocket socket, ControllerHandshake bot) {
			String msg = createBotListUpdateMessage();
			send(socket, msg);
		}

		@Override
		public void onControllerLeft(WebSocket socket) {
			// Do nothing
		}

		@Override
		public void onBotReady(WebSocket socket) {
			if (runningState == RunningState.WAIT_FOR_READY_PARTICIPANTS) {
				readyParticipants.add(socket);
				startGameIfParticipantsReady();
			}
		}

		@Override
		public void onBotIntent(WebSocket socket, net.robocode2.json_schema.comm.BotIntent intent) {
			updateBotIntent(socket, intent);
		}


		@Override
		public void onStartGame(WebSocket socket, net.robocode2.json_schema.GameSetup gameSetup,
				Collection<BotAddress> botAddresses) {

			GameServer.this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup);
			participants = connHandler.getBotConnections(botAddresses);

			if (participants.size() > 0) {
				prepareGame();
			}
		}

		@Override
		public void onStopGame(WebSocket socket) {
			stopGame();
		}

		@Override
		public void onPauseGame(WebSocket socket) {
			pauseGame();
		}

		@Override
		public void onResumeGame(WebSocket socket) {
			resumeGame();
		}
	}
}
