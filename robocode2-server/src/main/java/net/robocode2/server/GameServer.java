package net.robocode2.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;

import net.robocode2.game.ModelUpdater;
import net.robocode2.json_schema.Participant;
import net.robocode2.json_schema.messages.BotHandshake;
import net.robocode2.json_schema.messages.NewBattleForBot;
import net.robocode2.json_schema.messages.NewBattleForObserver;
import net.robocode2.json_schema.messages.ObserverHandshake;
import net.robocode2.json_schema.messages.TickForBot;
import net.robocode2.json_schema.messages.TickForObserver;
import net.robocode2.model.BotIntent;
import net.robocode2.model.GameSetup;
import net.robocode2.model.GameState;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;
import net.robocode2.server.mappers.BotIntentToBotIntentMapper;
import net.robocode2.server.mappers.GameSetupToGameSetupMapper;
import net.robocode2.server.mappers.TurnToTickForBotMapper;
import net.robocode2.server.mappers.TurnToTickForObserverMapper;

public final class GameServer {

	private ServerSetup serverSetup;
	private ConnListener connectionObserver;
	private ConnHandler connectionHandler;

	private ServerState gameState;
	private GameSetup gameSetup;

	private Set<BotConn> participants;
	private Set<BotConn> readyParticipants;

	private Map<BotConn, BotIntent.Builder> botIntents = new HashMap<>();

	private final Timer readyTimer = new Timer("Bot-ready-timer");
	private final Timer updateGameStateTimer = new Timer("Update-game-state-timer");

	private final Gson gson = new Gson();

	private ModelUpdater modelUpdater;

	private int delayedObserverTurnNumber;

	public GameServer() {
		this.serverSetup = new ServerSetup();
		this.connectionObserver = new ConnectionObserver();
		this.connectionHandler = new ConnHandler(serverSetup, connectionObserver);
		this.gameState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
	}

	public void start() {
		connectionHandler.start();
	}

	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.start();
	}

	private void prepareGameIfEnoughCandidates() {
		GameAndParticipants gameAndParticipants = selectGameAndParticipants(connectionHandler.getBotConnections());
		if (gameAndParticipants == null) {
			return;
		}

		gameSetup = gameAndParticipants.gameSetup;
		participants = gameAndParticipants.participants;

		if (participants.size() > 0) {
			prepareGame();
		}
	}

	private void startGameIfParticipantsReady() {
		if (readyParticipants.size() == participants.size()) {

			readyTimer.cancel();
			readyParticipants.clear();
			botIntents.clear();

			startGame();
		}
	}

	private void prepareGame() {
		System.out.println("#### PREPARE GAME #####");

		gameState = ServerState.WAIT_FOR_READY_PARTICIPANTS;

		// Send NewBattle to all participant bots to get them started

		NewBattleForBot newBattleForBot = new NewBattleForBot();
		newBattleForBot.setMessageType(NewBattleForBot.MessageType.NEW_BATTLE_FOR_BOT);
		newBattleForBot.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));

		int id = 1;
		List<Integer> participantIds = new ArrayList<>();
		for (BotConn participant : participants) {
			participantIds.add(id);
			participant.setId(id);
			newBattleForBot.setMyId(id);

			String msg = gson.toJson(newBattleForBot);
			send(participant.getConnection(), msg);

			id++;
		}

		readyParticipants = new HashSet<BotConn>();

		// Start 'ready' timer

		readyTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				onReadyTimeout();
			}
		}, gameSetup.getReadyTimeout());
	}

	// Should be moved to a "strategy" class
	private GameAndParticipants selectGameAndParticipants(Map<WebSocket, BotHandshake> candidateBots) {

		Map<String, Set<BotConn>> candidateBotsPerGameType = new HashMap<>();

		Set<String> gameTypes = getGameTypes();

		// Generate a map over potential participants per game type
		Iterator<Entry<WebSocket, BotHandshake>> iterator = candidateBots.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<WebSocket, BotHandshake> entry = iterator.next();
			BotHandshake botHandshake = entry.getValue();
			List<String> availGameTypes = new ArrayList<>(botHandshake.getGameTypes());
			availGameTypes.retainAll(gameTypes);

			for (String gameType : availGameTypes) {
				Set<BotConn> candidates = candidateBotsPerGameType.get(gameType);
				if (candidates == null) {
					candidates = new HashSet<>();
					candidateBotsPerGameType.put(gameType, candidates);
				}
				candidates.add(new BotConn(entry.getKey(), entry.getValue()));
			}
		}

		// Run through the list of games and see if anyone has enough
		// participants to start the game
		Set<GameSetup> games = serverSetup.getGames();
		for (Entry<String, Set<BotConn>> entry : candidateBotsPerGameType.entrySet()) {
			GameSetup gameSetup = games.stream().filter(g -> g.getGameType().equalsIgnoreCase(entry.getKey())).findAny()
					.orElse(null);

			Set<BotConn> participants = entry.getValue();
			int count = participants.size();
			if (count >= gameSetup.getMinNumberOfParticipants() && (gameSetup.getMaxNumberOfParticipants() != null
					&& count <= gameSetup.getMaxNumberOfParticipants())) {

				// enough participants
				GameAndParticipants gameAndParticipants = new GameAndParticipants();
				gameAndParticipants.gameSetup = gameSetup;
				gameAndParticipants.participants = participants;
				return gameAndParticipants;
			}
		}

		return null; // not enough participants
	}

	private void startGame() {
		System.out.println("#### START GAME #####");

		gameState = ServerState.GAME_RUNNING;

		// Send NewBattle to all participant observers to get them started
		if (connectionHandler.getObserverConnections().size() > 0) {
			NewBattleForObserver newBattleForObserver = new NewBattleForObserver();
			newBattleForObserver.setMessageType(NewBattleForObserver.MessageType.NEW_BATTLE_FOR_OBSERVER);
			newBattleForObserver.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));

			List<Participant> list = new ArrayList<>();
			for (BotConn bot : participants) {
				Participant p = new Participant();
				BotHandshake h = bot.getHandshake();
				p.setAuthor(h.getAuthor());
				p.setCountryCode(h.getCountryCode());
				p.setGameTypes(h.getGameTypes());
				p.setId(bot.getId());
				p.setName(h.getName());
				p.setProgrammingLanguage(h.getProgrammingLanguage());
				p.setVersion(h.getVersion());
				list.add(p);
			}
			newBattleForObserver.setParticipants(list);

			String msg = gson.toJson(newBattleForObserver);

			for (Entry<WebSocket, ObserverHandshake> entry : connectionHandler.getObserverConnections().entrySet()) {
				WebSocket observer = entry.getKey();
				send(observer, msg);
			}
		}

		// Prepare model update

		Set<Integer> participantIds = new HashSet<>();
		for (BotConn bot : participants) {
			participantIds.add(bot.getId());
		}
		modelUpdater = new ModelUpdater(gameSetup, participantIds);

		// Create timer to updating game state

		updateGameStateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				onUpdateGameState();
			}
		}, gameSetup.getTurnTimeout(), gameSetup.getTurnTimeout());
	}

	private GameState updateGameState() {
		Map<Integer /* BotId */, BotIntent> mappedBotIntents = new HashMap<>();

		for (Entry<BotConn, BotIntent.Builder> entry : botIntents.entrySet()) {
			int botId = entry.getKey().getId();
			BotIntent intent = entry.getValue().build();
			mappedBotIntents.put(botId, intent);
		}

		return modelUpdater.update(Collections.unmodifiableMap(mappedBotIntents));
	}

	private Set<String> getGameTypes() {
		Set<String> gameTypes = new HashSet<>();
		for (GameSetup gameSetup : serverSetup.getGames()) {
			gameTypes.add(gameSetup.getGameType());
		}
		return gameTypes;
	}

	private void onReadyTimeout() {
		System.out.println("#### READY TIMEOUT EVENT #####");

		if (readyParticipants.size() >= gameSetup.getMinNumberOfParticipants()) {
			// Start the game with the participants that are ready
			participants = readyParticipants;
			startGame();

		} else {
			// Not enough participants -> prepare another game
			gameState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
			prepareGameIfEnoughCandidates();
		}
	}

	private void onUpdateGameState() {
		System.out.println("#### UPDATE GAME STATE EVENT #####");

		// Update game state
		GameState gameState = updateGameState();

		// Clear bot intents
		botIntents.clear();

		// Send tick to bots

		Round currentRound = gameState.getLastRound();
		Turn currentTurn = currentRound.getLastTurn();

		// Send game state as 'tick' to participants
		for (BotConn participant : participants) {
			TickForBot tickForBot = TurnToTickForBotMapper.map(currentRound, currentTurn, participant.getId());

			String msg = gson.toJson(tickForBot);
			send(participant.getConnection(), msg);
		}

		// Send delayed tick to observers

		Round observerRound = gameState.getLastRound();
		Turn observerTurn = observerRound.getLastTurn();

		if (gameState.isGameEnded()) {
			delayedObserverTurnNumber++;
			if (delayedObserverTurnNumber == observerTurn.getTurnNumber()) {
				// Stop timer for updating game state
				updateGameStateTimer.cancel();
			}
		} else {
			delayedObserverTurnNumber = observerTurn.getTurnNumber() - gameSetup.getNumberOfDelayedTurnsForObservers();
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
			for (Map.Entry<WebSocket, ObserverHandshake> entry : connectionHandler.getObserverConnections()
					.entrySet()) {
				TickForObserver tickForObserver = TurnToTickForObserverMapper.map(observerRound, observerTurn);

				String msg = gson.toJson(tickForObserver);
				send(entry.getKey(), msg);
			}
		}

		if (currentTurn.getTurnNumber() > 1000) { // FIXME
			updateGameStateTimer.cancel();
		}
	}

	private static void send(WebSocket conn, String message) {
		System.out.println("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

		conn.send(message);
	}

	private void updateBotIntent(BotConn bot, net.robocode2.json_schema.messages.BotIntent intent) {
		Integer botId = getBotId(bot);
		if (botId == null) {
			return;
		}

		bot.setId(botId);

		BotIntent.Builder builder = botIntents.get(bot);
		if (builder == null) {
			builder = new BotIntent.Builder();
			botIntents.put(bot, builder);
		}
		builder.update(BotIntentToBotIntentMapper.map(intent));
	}

	private Integer getBotId(BotConn botConn) {
		Optional<BotConn> bot = participants.stream().filter(b -> b.getConnection() == botConn.getConnection())
				.findFirst();
		if (bot.isPresent()) {
			return bot.get().getId();
		}
		return null;
	}

	private class ConnectionObserver implements ConnListener {

		@Override
		public void onException(Exception exception) {
			exception.printStackTrace();
		}

		@Override
		public void onBotJoined(BotConn bot) {
			if (gameState == ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN) {
				prepareGameIfEnoughCandidates();
			}
		}

		@Override
		public void onBotLeft(BotConn bot) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onObserverJoined(ObserverConn observer) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onObserverLeft(ObserverConn observer) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onBotReady(BotConn bot) {
			if (gameState == ServerState.WAIT_FOR_READY_PARTICIPANTS) {
				readyParticipants.add(bot);
				startGameIfParticipantsReady();
			}
		}

		@Override
		public void onBotIntent(BotConn bot, net.robocode2.json_schema.messages.BotIntent intent) {
			updateBotIntent(bot, intent);
		}
	}

	private class GameAndParticipants {
		GameSetup gameSetup;
		Set<BotConn> participants;
	}
}
