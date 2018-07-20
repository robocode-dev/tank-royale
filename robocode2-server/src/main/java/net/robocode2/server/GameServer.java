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

import lombok.val;
import org.java_websocket.WebSocket;

import com.google.gson.Gson;

import net.robocode2.engine.ModelUpdater;
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
import net.robocode2.mappers.BotHandshakeToBotInfoMapper;
import net.robocode2.mappers.BotIntentToBotIntentMapper;
import net.robocode2.mappers.GameSetupToGameSetupMapper;
import net.robocode2.mappers.TurnToGameTickForBotMapper;
import net.robocode2.mappers.TurnToGameTickForObserverMapper;
import net.robocode2.model.BotIntent;
import net.robocode2.model.GameSetup;
import net.robocode2.model.GameState;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;

public final class GameServer {

	private ConnHandler connHandler;

	private RunningState runningState;
	private GameSetup gameSetup;
	private GameState gameState;

	private Set<String /* clientKey */> participants;
	private Set<String /* clientKey */> readyParticipants;

	private Map<String /* clientKey */, Integer> participantIds = new HashMap<>();

	private Map<String /* clientKey */, BotIntent> botIntents = new HashMap<>();

	private Timer readyTimer;
	private Timer turnTimer;

	private ModelUpdater modelUpdater;

	private int delayedObserverTurnNumber;

	private final Gson gson = new Gson();

	private GameServer() {
		val serverSetup = new ServerSetup();
		val connListener = new GameServerConnListener();
		this.connHandler = new ConnHandler(serverSetup, connListener);
		this.runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
	}

	private void start() {
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
		gameStartedForBot.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));

		int id = 1;
		for (String botKey : participants) {
			participantIds.put(botKey, id);
			gameStartedForBot.setMyId(id);

			String msg = gson.toJson(gameStartedForBot);
			send(botKey, msg);

			id++;
		}

		readyParticipants = new HashSet<>();

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

		List<Participant> participantList = new ArrayList<>();
		if (connHandler.getObserverAndControllerConnections().size() > 0) {
			for (String botKey : participants) {
				BotHandshake h = connHandler.getBotHandshakes().get(botKey);
				Participant p = new Participant();
				p.setId(participantIds.get(botKey));
				p.setAuthor(h.getAuthor());
				p.setCountryCode(h.getCountryCode());
				p.setGameTypes(h.getGameTypes());
				p.setName(h.getName());
				p.setProgrammingLanguage(h.getProgrammingLanguage());
				p.setVersion(h.getVersion());
				participantList.add(p);
			}
		}

		// Send GameStarted to all participant observers to get them started
		if (connHandler.getObserverAndControllerConnections().size() > 0) {
			GameStartedEventForObserver gameStartedForObserver = new GameStartedEventForObserver();
			gameStartedForObserver.setType(GameStartedEventForObserver.Type.GAME_STARTED_EVENT_FOR_OBSERVER);
			gameStartedForObserver.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));

			gameStartedForObserver.setParticipants(participantList);

			String msg = gson.toJson(gameStartedForObserver);

			for (String clientKey: connHandler.getObserverAndControllerConnections().keySet()) {
				send(clientKey, msg);
			}
		}

		// Prepare model update

		modelUpdater = ModelUpdater.create(gameSetup, new HashSet<>(participantIds.values()));

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

	private GameState updateGameState() {
		Map<Integer /* BotId */, BotIntent> mappedBotIntents = new HashMap<>();

		for (Entry<String, BotIntent> entry : botIntents.entrySet()) {
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

			Round round = gameState.getLastRound();
			if (round != null) {
				Turn turn = round.getLastTurn();
				if (turn != null) {
                    // Send game state as 'game tick' to participants
                    for (String botKey : participants) {
                        TickEventForBot gameTickForBot = TurnToGameTickForBotMapper.map(round, turn, participantIds.get(botKey));
                        if (gameTickForBot != null) { // Bot alive?
                            String msg = gson.toJson(gameTickForBot);
                            send(botKey, msg);
                        }
                    }
                }
			}
		}

		// Send delayed tick to observers

		Round observerRound = gameState.getLastRound();
		if (observerRound != null) {
			Turn observerTurn = observerRound.getLastTurn();

			if (gameState.isGameEnded() || runningState == RunningState.GAME_STOPPED) {
				delayedObserverTurnNumber++;
				if (observerTurn != null && delayedObserverTurnNumber == observerTurn.getTurnNumber()) {

					System.out.println("#### GAME ENDED #####");

					// Stop timer for updating game state
					turnTimer.cancel();

					// Game has stopped
					runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
				}
			} else if (observerTurn != null) {
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
				for (String clientKey : connHandler.getObserverAndControllerConnections().keySet()) {
					TickEventForObserver gameTickForObserver = TurnToGameTickForObserverMapper.map(observerRound,
							observerTurn);

					String msg = gson.toJson(gameTickForObserver);
					send(clientKey, msg);
				}
			}
		}
	}

	private void send(String clientKey, String message) {
		WebSocket conn = connHandler.getConnection(clientKey);
		send(conn, message);
	}

	private static void send(WebSocket conn, String message) {
		System.out.println("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

		conn.send(message);
	}

	private void updateBotIntent(String botKey, net.robocode2.json_schema.comm.BotIntent intent) {
		if (!participants.contains(botKey)) {
			return;
		}
		BotIntent botIntent = botIntents.get(botKey);
		if (botIntent == null) {
			botIntent = BotIntent.builder().build();
		}
		botIntent = botIntent.update(BotIntentToBotIntentMapper.map(intent));
		botIntents.put(botKey, botIntent);
	}

	private String createBotListUpdateMessage() {
		BotListUpdate botListUpdate = new BotListUpdate();
		botListUpdate.setType(Message.Type.BOT_LIST_UPDATE);
		List<BotInfo> bots = new ArrayList<>();
		botListUpdate.setBots(bots);

		Map<String, WebSocket> botConnections = connHandler.getBotConnections();
        for (Entry<String, WebSocket> entry : botConnections.entrySet()) {
        	String clientKey = entry.getKey();
            InetSocketAddress address = entry.getValue().getRemoteSocketAddress();
            BotInfo botInfo = BotHandshakeToBotInfoMapper.map(
            		connHandler.getBotHandshakes().get(clientKey), address.getHostString(), address.getPort());
            bots.add(botInfo);
        }
		return gson.toJson(botListUpdate);
	}
	
	private void sendBotListUpdateToObservers() {
		String msg = createBotListUpdateMessage();
		for (WebSocket conn : connHandler.getObserverAndControllerConnections().values()) {
			send(conn, msg);
		}
	}

	private class GameServerConnListener implements ConnListener {

		@Override
		public void onException(Exception exception) {
			exception.printStackTrace();
		}

		@Override
		public void onBotJoined(String clientKey, BotHandshake bot) {
			sendBotListUpdateToObservers();
		}

		@Override
		public void onBotLeft(String clientKey) {
			sendBotListUpdateToObservers();
		}

		@Override
		public void onObserverJoined(String clientKey, ObserverHandshake bot) {
			String msg = createBotListUpdateMessage();
			send(clientKey, msg);
		}

		@Override
		public void onObserverLeft(String clientKey) {
			// Do nothing
		}

		@Override
		public void onControllerJoined(String clientKey, ControllerHandshake bot) {
			String msg = createBotListUpdateMessage();
			send(clientKey, msg);
		}

		@Override
		public void onControllerLeft(String clientKey) {
			// Do nothing
		}

		@Override
		public void onBotReady(String clientKey) {
			if (runningState == RunningState.WAIT_FOR_READY_PARTICIPANTS) {
				readyParticipants.add(clientKey);
				startGameIfParticipantsReady();
			}
		}

		@Override
		public void onBotIntent(String clientKey, net.robocode2.json_schema.comm.BotIntent intent) {
			updateBotIntent(clientKey, intent);
		}


		@Override
		public void onStartGame(net.robocode2.json_schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {

			GameServer.this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup);
			participants = connHandler.getBotKeys(botAddresses);
			if (participants.size() > 0) {
				prepareGame();
			}
		}

		@Override
		public void onStopGame() {
			stopGame();
		}

		@Override
		public void onPauseGame() {
			pauseGame();
		}

		@Override
		public void onResumeGame() {
			resumeGame();
		}
	}
}
