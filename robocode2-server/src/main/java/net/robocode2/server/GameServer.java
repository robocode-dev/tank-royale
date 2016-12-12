package net.robocode2.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import net.robocode2.model.BotIntent;
import net.robocode2.model.BotIntent.Builder;
import net.robocode2.model.GameSetup;
import net.robocode2.model.GameState;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;
import net.robocode2.server.mappers.GameSetupToGameSetupMapper;
import net.robocode2.server.mappers.TurnToTickForBotMapper;

public final class GameServer {

	private ServerSetup serverSetup;
	private ConnListener connectionObserver;
	private ConnHandler connectionHandler;

	private ServerState gameState;
	private GameSetup gameSetup;

	private Set<BotConn> participants;
	private Set<BotConn> readyParticipants;
	private Map<BotConn, Builder> botIntents = new HashMap<>();

	private final Timer readyTimer = new Timer();

	private final Gson gson = new Gson();

	private ModelUpdater modelUpdater;

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
		GameAndParticipants gap = selectGameAndParticipants(connectionHandler.getBotConnections());
		if (gap == null) {
			return;
		}

		gameSetup = gap.gameSetup;
		participants = gap.participants;

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

		int participantId = 1;
		for (BotConn participant : participants) {
			participant.setId(participantId);
			newBattleForBot.setMyId(participantId);

			String msg = gson.toJson(newBattleForBot);
			send(participant.getConnection(), msg);

			participantId++;
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
			GameSetup game = games.stream().filter(g -> g.getGameType().equalsIgnoreCase(entry.getKey())).findAny()
					.orElse(null);

			Set<BotConn> participants = entry.getValue();
			int count = participants.size();
			if (count >= game.getMinNumberOfParticipants()
					&& (game.getMaxNumberOfParticipants() != null && count <= game.getMaxNumberOfParticipants())) {

				// enough participants
				GameAndParticipants gameAndParticipants = new GameAndParticipants();
				gameAndParticipants.gameSetup = game;
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

		// Update game state
		Set<Integer> participantIds = new HashSet<>();
		for (BotConn bot : participants) {
			participantIds.add(bot.getId());
		}

		modelUpdater = new ModelUpdater(gameSetup);

		// TODO: Invoke after X time by some timer?
		updateGameState();
	}

	private void updateGameState() {

		Map<Integer /* BotId */, BotIntent> mappedIntents = new HashMap<>();

		for (Entry<BotConn, BotIntent.Builder> entry : botIntents.entrySet()) {
			int botId = entry.getKey().getId();
			BotIntent intent = entry.getValue().build();
			mappedIntents.put(botId, intent);
		}

		GameState gameState = modelUpdater.update(Collections.unmodifiableMap(mappedIntents));

		Round lastRound = gameState.getLastRound();
		Turn lastTurn = lastRound.getLastTurn();

		// Send game state as 'tick' to participants
		for (BotConn participant : participants) {
			TickForBot tickForBot = TurnToTickForBotMapper.map(lastRound, lastTurn, participant.getId());

			String msg = gson.toJson(tickForBot);
			send(participant.getConnection(), msg);
		}

		// TODO: Game state must be delayed for observers
	}

	private Set<String> getGameTypes() {
		Set<String> gameTypes = new HashSet<>();
		for (GameSetup gameSetup : serverSetup.getGames()) {
			gameTypes.add(gameSetup.getGameType());
		}
		return gameTypes;
	}

	private void onReadyTimeout() {
		System.out.println("#### READY TIMEOUT #####");

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

	private static void send(WebSocket conn, String message) {
		System.out.println("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

		conn.send(message);
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

	private void updateBotIntent(BotConn bot, net.robocode2.json_schema.messages.BotIntent intent) {

		BotIntent.Builder builder = botIntents.get(bot);
		if (builder == null) {
			builder = new BotIntent.Builder();
			botIntents.put(bot, builder);
		}
		if (intent.getTurnRate() != null) {
			builder.setBodyTurnRate(intent.getTurnRate());
		}
		if (intent.getTurretTurnRate() != null) {
			builder.setTurretTurnRate(intent.getTurretTurnRate());
		}
		if (intent.getRadarTurnRate() != null) {
			builder.setRadarTurnRate(intent.getRadarTurnRate());
		}
		if (intent.getTargetSpeed() != null) {
			builder.setTargetSpeed(intent.getTargetSpeed());
		}
		if (intent.getBulletPower() != null) {
			builder.setBulletPower(intent.getBulletPower());
		}
	}

	private class GameAndParticipants {
		GameSetup gameSetup;
		Set<BotConn> participants;
	}
}
