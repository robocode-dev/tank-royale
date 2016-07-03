package net.robocode2.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	public GameServer() {
		this.setup = new ServerSetup();
		this.connectionObserver = new ConnectionObserver();
		this.connectionHandler = new ConnectionHandler(setup, connectionObserver);
		this.gameState = GameState.PENDING;
	}

	public void start() {
		connectionHandler.start();
	}

	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.start();
	}

	public void sendStartSignalIfEnoughParticipants() {
		GameAndParticipants gameAndParticipants = selectGameAndParticipants(connectionHandler.getBotConnections());
		if (gameAndParticipants == null) {
			return;
		}
		if (gameAndParticipants.participants.size() > 0) {
			gameState = GameState.READY;
			onReadyState(gameAndParticipants);
		}
	}

	private void onReadyState(GameAndParticipants gameAndParticipants) {
		System.out.println("#### READY #####");

		Gson gson = new Gson();
		Game game = gameAndParticipants.game;

		// Send NewBattle to all participant bots to get them started

		Map<WebSocket, BotHandshake> participants = gameAndParticipants.participants;

		NewBattleForBot ngb = new NewBattleForBot();
		ngb.setMessageType(NewBattleForBot.MessageType.NEW_BATTLE_FOR_BOT);
		ngb.setGameType(game.getGameType());
		ngb.setArenaWidth(game.getArenaWidth());
		ngb.setArenaHeight(game.getArenaHeight());
		ngb.setObstacles(game.getObstacles());
		ngb.setNumberOfRounds(game.getNumberOfRounds());
		ngb.setMinNumberOfParticipants(game.getMinNumberOfParticipants());
		ngb.setMaxNumberOfParticipants(game.getMaxNumberOfParticipants());
		ngb.setTimeLimit(game.getTimeLimit());

		String msg = gson.toJson(ngb);

		for (Entry<WebSocket, BotHandshake> entry : participants.entrySet()) {
			WebSocket bot = entry.getKey();
			bot.send(msg);
		}

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
		ngo.setTimeLimit(game.getTimeLimit());
		
		msg = gson.toJson(ngo);

		for (Entry<WebSocket, ObserverHandshake> entry : connectionHandler.getObserverConnections().entrySet()) {
			WebSocket observer = entry.getKey();
			observer.send(msg);
		}
	}

	// Should be moved to a "strategy" class
	private GameAndParticipants selectGameAndParticipants(Map<WebSocket, BotHandshake> candidateBots) {

		Map<String, Map<WebSocket, BotHandshake>> candidateBotsPerGameType = new HashMap<>();

		Set<String> gameTypes = getGameTypes();

		// Generate a map over potential participants per game type
		Iterator<Entry<WebSocket, BotHandshake>> iterator = candidateBots.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<WebSocket, BotHandshake> entry = iterator.next();
			BotHandshake botHandshake = entry.getValue();
			List<String> availGameTypes = new ArrayList<>(botHandshake.getGameTypes());
			availGameTypes.retainAll(gameTypes);

			for (String gameType : availGameTypes) {
				Map<WebSocket, BotHandshake> map = candidateBotsPerGameType.get(gameType);
				if (map == null) {
					map = new HashMap<>();
					candidateBotsPerGameType.put(gameType, map);
				}
				map.put(entry.getKey(), entry.getValue());
			}
		}

		// Run through the list of games and see if anyone has enough
		// participants to start the game
		Set<Game> games = setup.getGames();
		for (Entry<String, Map<WebSocket, BotHandshake>> entry : candidateBotsPerGameType.entrySet()) {
			Game game = games.stream().filter(g -> g.getGameType().equalsIgnoreCase(entry.getKey())).findAny()
					.orElse(null);

			Map<WebSocket, BotHandshake> map = entry.getValue();
			int count = map.size();
			if (count >= game.getMinNumberOfParticipants() && count <= game.getMaxNumberOfParticipants()) {
				// enough participants
				GameAndParticipants gameAndParticipants = new GameAndParticipants();
				gameAndParticipants.game = game;
				gameAndParticipants.participants = map;
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

	private class ConnectionObserver implements ConnectionListener {

		@Override
		public void onBotJoined(BotHandshake botHandshake) {
			switch (gameState) {
			case PENDING:
				sendStartSignalIfEnoughParticipants();
				break;
			default:
				throw new IllegalStateException("Unhandled game state");
			}
		}

		@Override
		public void onObserverJoined(ObserverHandshake observerHandshake) {
		}

		@Override
		public void onBotLeft(BotHandshake botHandshake) {
		}

		@Override
		public void onObserverLeft(ObserverHandshake observerHandshake) {
		}
	}

	private class GameAndParticipants {
		Game game;
		Map<WebSocket, BotHandshake> participants;
	}
}
