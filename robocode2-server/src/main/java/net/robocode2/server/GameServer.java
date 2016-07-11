package net.robocode2.server;

import java.util.ArrayList;
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

import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.Game;
import net.robocode2.json_schema.NewBattleForBot;
import net.robocode2.json_schema.NewBattleForObserver;
import net.robocode2.json_schema.ObserverHandshake;

public class GameServer {

	ServerSetup setup;
	ConnectionListener connectionObserver;
	ConnectionHandler connectionHandler;

	GameState gameState;
	Game game;
	Set<Bot> participants;
	Set<Bot> readyParticipants;
	
	final Timer readyTimer = new Timer();

	public GameServer() {
		this.setup = new ServerSetup();
		this.connectionObserver = new ConnectionObserver();
		this.connectionHandler = new ConnectionHandler(setup, connectionObserver);
		this.gameState = GameState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
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

		game = gameAndParticipants.game;
		participants = gameAndParticipants.participants;

		if (participants.size() > 0) {
			prepareGame();
		}
	}

	private void startGameIfParticipantsReady() {
		if (readyParticipants.size() == participants.size()) {
			readyParticipants.clear();
			startGame();
		}
	}
	

	private void prepareGame() {
		System.out.println("#### PREPARE GAME #####");
		
		gameState = GameState.WAIT_FOR_READY_PARTICIPANTS;

		Gson gson = new Gson();

		// Send NewBattle to all participant bots to get them started

		NewBattleForBot ngb = new NewBattleForBot();
		ngb.setMessageType(NewBattleForBot.MessageType.NEW_BATTLE_FOR_BOT);
		ngb.setGameType(game.getGameType());
		ngb.setArenaWidth(game.getArenaWidth());
		ngb.setArenaHeight(game.getArenaHeight());
		ngb.setObstacles(game.getObstacles());
		ngb.setNumberOfRounds(game.getNumberOfRounds());
		ngb.setMinNumberOfParticipants(game.getMinNumberOfParticipants());
		ngb.setMaxNumberOfParticipants(game.getMaxNumberOfParticipants());
		ngb.setTurnTimeout(game.getTurnTimeout());
		ngb.setReadyTimeout(game.getReadyTimeout());

		int participantId = 1;
		for (Bot participant : participants) {
			participant.setId(participantId);
			ngb.setMyId(participantId);
			
			String msg = gson.toJson(ngb);
			send(participant.getConnection(), msg);
			
			participantId++;
		}
		

		readyParticipants = new HashSet<Bot>();

		// Start 'ready' timer

		int readyTimeout = game.getReadyTimeout();
		readyTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				onReadyTimeout();
			}
			
		}, readyTimeout);
		
		// Send NewBattle to all participant observers to get them started

		NewBattleForObserver ngo = new NewBattleForObserver();
		ngo.setMessageType(NewBattleForObserver.MessageType.NEW_BATTLE_FOR_OBSERVER);
		ngo.setGameType(game.getGameType());
		ngo.setArenaWidth(game.getArenaWidth());
		ngo.setArenaHeight(game.getArenaHeight());
		ngo.setObstacles(game.getObstacles());
		ngo.setNumberOfRounds(game.getNumberOfRounds());
		ngo.setMinNumberOfParticipants(game.getMinNumberOfParticipants());
		ngo.setMaxNumberOfParticipants(game.getMaxNumberOfParticipants());
		ngb.setTurnTimeout(game.getTurnTimeout());
		ngb.setReadyTimeout(game.getReadyTimeout());
		
		String msg = gson.toJson(ngo);

		for (Entry<WebSocket, ObserverHandshake> entry : connectionHandler.getObserverConnections().entrySet()) {
			WebSocket observer = entry.getKey();
			send(observer, msg);
		}
	}

	// Should be moved to a "strategy" class
	private GameAndParticipants selectGameAndParticipants(Map<WebSocket, BotHandshake> candidateBots) {

		Map<String, Set<Bot>> candidateBotsPerGameType = new HashMap<>();

		Set<String> gameTypes = getGameTypes();

		// Generate a map over potential participants per game type
		Iterator<Entry<WebSocket, BotHandshake>> iterator = candidateBots.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<WebSocket, BotHandshake> entry = iterator.next();
			BotHandshake botHandshake = entry.getValue();
			List<String> availGameTypes = new ArrayList<>(botHandshake.getGameTypes());
			availGameTypes.retainAll(gameTypes);

			for (String gameType : availGameTypes) {
				Set<Bot> candidates = candidateBotsPerGameType.get(gameType);
				if (candidates == null) {
					candidates = new HashSet<>();
					candidateBotsPerGameType.put(gameType, candidates);
				}
				candidates.add(new Bot(entry.getKey(), entry.getValue()));
			}
		}

		// Run through the list of games and see if anyone has enough
		// participants to start the game
		Set<Game> games = setup.getGames();
		for (Entry<String, Set<Bot>> entry : candidateBotsPerGameType.entrySet()) {
			Game game = games.stream().filter(g -> g.getGameType().equalsIgnoreCase(entry.getKey())).findAny()
					.orElse(null);

			Set<Bot> participants = entry.getValue();
			int count = participants.size();
			if (count >= game.getMinNumberOfParticipants() && count <= game.getMaxNumberOfParticipants()) {
				// enough participants
				GameAndParticipants gameAndParticipants = new GameAndParticipants();
				gameAndParticipants.game = game;
				gameAndParticipants.participants = participants;
				return gameAndParticipants;
			}
		}

		return null; // not enough participants
	}

	private void startGame() {
		System.out.println("#### START GAME #####");
		
		gameState = GameState.RUNNING;
	}
	
	private Set<String> getGameTypes() {
		Set<String> gameTypes = new HashSet<>();
		for (Game game : setup.getGames()) {
			gameTypes.add(game.getGameType());
		}
		return gameTypes;
	}

	private void onReadyTimeout() {
		System.out.println("#### READY TIMEOUT #####");

		if (readyParticipants.size() >= game.getMinNumberOfParticipants()) {
			// Start the game with the participants that are ready
			participants = readyParticipants;
			startGame();

		} else {
			// Not enough participants -> prepare another game
			gameState = GameState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
			prepareGameIfEnoughCandidates();
		}
	}

	private static void send(WebSocket conn, String message) {
		System.out.println("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);
		
		conn.send(message);
	}
	
	private class ConnectionObserver implements ConnectionListener {

		@Override
		public void onException(Exception exception) {
			exception.printStackTrace();
		}
		
		@Override
		public void onBotJoined(Bot bot) {
			if (gameState == GameState.WAIT_FOR_PARTICIPANTS_TO_JOIN) {
				prepareGameIfEnoughCandidates();
			}
		}

		@Override
		public void onBotLeft(Bot bot) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onObserverJoined(Observer observer) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onObserverLeft(Observer observer) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onBotReady(Bot bot) {			
			if (gameState == GameState.WAIT_FOR_READY_PARTICIPANTS) {
				readyParticipants.add(bot);
				startGameIfParticipantsReady();
			}
		}
	}

	private class GameAndParticipants {
		Game game;
		Set<Bot> participants;
	}
}
