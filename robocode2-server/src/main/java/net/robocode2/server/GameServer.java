package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lombok.val;
import net.robocode2.json_schema.events.*;
import net.robocode2.model.*;
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
import net.robocode2.mappers.BotHandshakeToBotInfoMapper;
import net.robocode2.mappers.BotIntentToBotIntentMapper;
import net.robocode2.mappers.GameSetupToGameSetupMapper;
import net.robocode2.mappers.TurnToGameTickForBotMapper;
import net.robocode2.mappers.TurnToGameTickForObserverMapper;

import static java.lang.Math.round;

public final class GameServer {

	private ConnHandler connHandler;

	private RunningState runningState;
	private GameSetup gameSetup;
	private GameState gameState;

	private Set<String /* clientKey */> participants;
	private Set<String /* clientKey */> readyParticipants;

	private Map<String /* clientKey */, Integer> participantIds = new HashMap<>();

	private Map<String /* clientKey */, BotIntent> botIntents = new ConcurrentHashMap<>();

	private Timer readyTimer;
	private Timer turnTimer;

	private ModelUpdater modelUpdater;

	private int delayedObserverTurnNumber;
	private List<BotResultsForObserver> resultsForObservers;

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

			sendMessageToObservers(gson.toJson(gameStartedForObserver));
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

	private void startGame(net.robocode2.json_schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {
		System.out.println("#### START GAME #####");

		this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup);
		participants = connHandler.getBotKeys(botAddresses);
		if (participants.size() > 0) {
			prepareGame();
		}
	}

	private void abortGame() {
		System.out.println("#### ABORT GAME #####");

		runningState = RunningState.GAME_STOPPED;

		GameAbortedEventForObserver abortedEvent = new GameAbortedEventForObserver();
		abortedEvent.setType(GameAbortedEventForObserver.Type.GAME_ABORTED_EVENT_FOR_OBSERVER);
		sendMessageToObservers(gson.toJson(abortedEvent));

		// No score is generated for aborted games
	}

	private List<BotResultsForBot> getResultsForBots() {
		List<BotResultsForBot> botResultsList = new ArrayList<>();

		this.modelUpdater.getResults().forEach(score -> {
			BotResultsForBot botResults = new BotResultsForBot();
			botResultsList.add(botResults);

			botResults.setId(score.getId());
			botResults.setSurvival((int) round(score.getSurvival()));
			botResults.setLastSurvivorBonus((int) round(score.getLastSurvivorBonus()));
			botResults.setBulletDamage((int) round(score.getBulletDamage()));
			botResults.setBulletKillBonus((int) round(score.getBulletKillBonus()));
			botResults.setRamDamage((int) round(score.getRamDamage()));
			botResults.setRamKillBonus((int) round(score.getRamKillBonus()));
			botResults.setTotalScore((int) round(score.getTotalScore()));
			botResults.setFirstPlaces(score.getFirstPlaces());
			botResults.setSecondPlaces(score.getSecondPlaces());
			botResults.setThirdPlaces(score.getThirdPlaces());
		});

		int rank = 1;
		for (BotResultsForBot botResult : botResultsList) {
			botResult.setRank(rank++);
		}

		return botResultsList;
	}

	private List<BotResultsForObserver> getResultsForObservers() {
		List<BotResultsForObserver> botResultsList = new ArrayList<>();

		this.modelUpdater.getResults().forEach(score -> {
			BotResultsForObserver botResults = new BotResultsForObserver();
			botResultsList.add(botResults);

			botResults.setId(score.getId());
			botResults.setSurvival((int) round(score.getSurvival()));
			botResults.setLastSurvivorBonus((int) round(score.getLastSurvivorBonus()));
			botResults.setBulletDamage((int) round(score.getBulletDamage()));
			botResults.setBulletKillBonus((int) round(score.getBulletKillBonus()));
			botResults.setRamDamage((int) round(score.getRamDamage()));
			botResults.setRamKillBonus((int) round(score.getRamKillBonus()));
			botResults.setTotalScore((int) round(score.getTotalScore()));
			botResults.setFirstPlaces(score.getFirstPlaces());
			botResults.setSecondPlaces(score.getSecondPlaces());
			botResults.setThirdPlaces(score.getThirdPlaces());

			String clientKey = null;
			for (Entry<String, Integer> entry2 : participantIds.entrySet()) {
				if (entry2.getValue().equals(score.getId())) {
					clientKey = entry2.getKey();
					break;
				}
			}

			BotHandshake botHandshake = connHandler.getBotHandshakes().get(clientKey);
			botResults.setName(botHandshake.getName());
			botResults.setVersion(botHandshake.getVersion());
		});

		int rank = 1;
		for (BotResultsForObserver botResult : botResultsList) {
			botResult.setRank(rank++);
		}

		return botResultsList;
	}

	private void pauseGame() {
		System.out.println("#### PAUSE GAME #####");

		GamePausedEventForObserver pausedEvent = new GamePausedEventForObserver();
		pausedEvent.setType(GamePausedEventForObserver.Type.GAME_PAUSED_EVENT_FOR_OBSERVER);
        sendMessageToObservers(gson.toJson(pausedEvent));

        runningState = RunningState.GAME_PAUSED;
	}

	private void resumeGame() {
		System.out.println("#### RESUME GAME #####");

        GameResumedEventForObserver resumedEvent = new GameResumedEventForObserver();
        resumedEvent.setType(GameResumedEventForObserver.Type.GAME_RESUMED_EVENT_FOR_OBSERVER);
        sendMessageToObservers(gson.toJson(resumedEvent));

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

		if (runningState != RunningState.GAME_STOPPED) {
			// Update game state
			gameState = updateGameState();

			if (gameState.isGameEnded()) {
				runningState = RunningState.GAME_STOPPED;

				System.out.println("#### GAME ENDED FOR BOTS #####");

				GameEndedEventForBot endEventForBot = new GameEndedEventForBot();
				endEventForBot.setType(GameEndedEventForObserver.Type.GAME_ENDED_EVENT_FOR_BOT);
				endEventForBot.setResults(getResultsForBots());
				sendMessageToBots(gson.toJson(endEventForBot));

				// Store result for observers for later (before score is reset)
				resultsForObservers = getResultsForObservers();

			} else {
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
		}

		// Send delayed tick to observers

		Round observerRound = gameState.getLastRound();
		if (observerRound != null) {
			Turn observerTurn = observerRound.getLastTurn();

			if (gameState.isGameEnded() && runningState == RunningState.GAME_STOPPED) {
				delayedObserverTurnNumber++;
				if (observerTurn != null && delayedObserverTurnNumber == observerTurn.getTurnNumber()) {

					System.out.println("#### GAME ENDED FOR OBSERVERS #####");

					// Stop timer for updating game state
					turnTimer.cancel();

					// End game for bots
					GameEndedEventForObserver endEventForObserver = new GameEndedEventForObserver();
					endEventForObserver.setType(GameEndedEventForObserver.Type.GAME_ENDED_EVENT_FOR_OBSERVER);
					endEventForObserver.setResults(resultsForObservers); // Use the stored score!
					sendMessageToObservers(gson.toJson(endEventForObserver));

//					runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN; // TODO: Correct?
				}
			} else if (observerTurn != null) {
				delayedObserverTurnNumber = observerTurn.getTurnNumber() - gameSetup.getDelayedObserverTurns();
				if (delayedObserverTurnNumber < 0) {
					int delayedRoundNumber = observerRound.getRoundNumber() - 1;
					if (delayedRoundNumber >= 0 && gameState.getRounds().size() < delayedRoundNumber) {
						observerRound = gameState.getRounds().get(delayedRoundNumber);
						delayedObserverTurnNumber += observerRound.getTurns().size();
					}
				}
			}
			if (delayedObserverTurnNumber >= 0 && delayedObserverTurnNumber < observerRound.getTurns().size()) {
				observerTurn = observerRound.getTurns().get(delayedObserverTurnNumber);

				// Send game state as 'tick' to observers
				TickEventForObserver gameTickForObserver =
						TurnToGameTickForObserverMapper.map(observerRound, observerTurn);
				sendMessageToObservers(gson.toJson(gameTickForObserver));
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

	private void sendMessageToBots(String msg) {
		connHandler.getBotConnections().values().forEach(conn -> send(conn, msg));
	}

	private void sendMessageToObservers(String msg) {
		connHandler.getObserverAndControllerConnections().values().forEach(conn -> send(conn, msg));
	}

	private void sendBotListUpdateToObservers() {
		sendMessageToObservers(createBotListUpdateMessage());
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
			startGame(gameSetup, botAddresses);
		}

		@Override
		public void onAbortGame() {
			abortGame();
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
