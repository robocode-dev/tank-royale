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
	GameAndParticipants gameAndParticipants;
	Participant[] participantOrder; // participant id = index (starting at index 1)

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

	private void prepareGameIfEnoughParticipants() {
		gameAndParticipants = selectGameAndParticipants(connectionHandler.getBotConnections());
		if (gameAndParticipants == null) {
			return;
		}
		if (gameAndParticipants.participants.size() > 0) {
			prepareGame();
		}
	}

	private void startGameIfParticipantsReady() {
		
	}
	

	private void prepareGame() {
		System.out.println("#### PREPARE GAME #####");
		
		gameState = GameState.WAIT_FOR_READY_PARTICIPANTS;

		Game game = gameAndParticipants.game;
		Gson gson = new Gson();
		
		Set<Participant> participants = gameAndParticipants.participants;

		participantOrder = new Participant[1 + participants.size()];

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

		int botId = 1;
		for (Participant participant : participants) {
			// The array manifests the bot ids from the index
			participantOrder[botId] = participant;
			ngb.setMyId(botId);
			
			String msg = gson.toJson(ngb);
			participant.webSocket.send(msg);
			
			botId++;
		}

		// Start 'ready' timer
		
		int readyTimeout = gameAndParticipants.game.getReadyTimeout();
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
			observer.send(msg);
		}
	}

	// Should be moved to a "strategy" class
	private GameAndParticipants selectGameAndParticipants(Map<WebSocket, BotHandshake> candidateBots) {

		Map<String, Set<Participant>> candidateBotsPerGameType = new HashMap<>();

		Set<String> gameTypes = getGameTypes();

		// Generate a map over potential participants per game type
		Iterator<Entry<WebSocket, BotHandshake>> iterator = candidateBots.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<WebSocket, BotHandshake> entry = iterator.next();
			BotHandshake botHandshake = entry.getValue();
			List<String> availGameTypes = new ArrayList<>(botHandshake.getGameTypes());
			availGameTypes.retainAll(gameTypes);

			for (String gameType : availGameTypes) {
				Set<Participant> candidates = candidateBotsPerGameType.get(gameType);
				if (candidates == null) {
					candidates = new HashSet<>();
					candidateBotsPerGameType.put(gameType, candidates);
				}
				candidates.add(new Participant(entry.getKey(), entry.getValue()));
			}
		}

		// Run through the list of games and see if anyone has enough
		// participants to start the game
		Set<Game> games = setup.getGames();
		for (Entry<String, Set<Participant>> entry : candidateBotsPerGameType.entrySet()) {
			Game game = games.stream().filter(g -> g.getGameType().equalsIgnoreCase(entry.getKey())).findAny()
					.orElse(null);

			Set<Participant> participants = entry.getValue();
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

	private Set<String> getGameTypes() {
		Set<String> gameTypes = new HashSet<>();
		for (Game game : setup.getGames()) {
			gameTypes.add(game.getGameType());
		}
		return gameTypes;
	}

	private void onReadyTimeout() {
		
	}

	private class ConnectionObserver implements ConnectionListener {

		@Override
		public void onBotJoined(BotHandshake botHandshake) {
			if (gameState == GameState.WAIT_FOR_PARTICIPANTS_TO_JOIN) {
				prepareGameIfEnoughParticipants();
			}
		}

		@Override
		public void onObserverJoined(ObserverHandshake observerHandshake) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onBotLeft(BotHandshake botHandshake) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onObserverLeft(ObserverHandshake observerHandshake) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onBotReady(BotHandshake botHandshake) {
			if (gameState == GameState.WAIT_FOR_READY_PARTICIPANTS) {
				startGameIfParticipantsReady();
			}
		}
	}

	private class GameAndParticipants {
		Game game;
		Set<Participant> participants;
	}

	private class Participant {
		
		Participant(WebSocket webSocket, BotHandshake botHandskake) {
			this.webSocket = webSocket;
			this.botHandskake = botHandskake;			
		}
		
		WebSocket webSocket;
		BotHandshake botHandskake;
	}
}
