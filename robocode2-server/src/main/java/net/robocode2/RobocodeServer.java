package net.robocode2;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.robocode2.json_schema.Game;
import net.robocode2.json_schema.ServerHandshake;

public class RobocodeServer {

	public static void main(String[] args) {
		Game game = new Game();
		game.setArenaWidth(1000);
		game.setArenaHeight(800);
		game.setGameType("melee");
		game.setMinNumberOfParticipants(2);
		game.setMaxNumberOfParticipants(8);
		game.setNumberOfRounds(10);
		game.setObstacles(false);
		game.setTimeLimit(100);

		ServerHandshake handshake = new ServerHandshake();
		Set<Game> games = new HashSet<>();
		games.add(game);
		handshake.setGames(games);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(handshake);
		
		System.out.println(json);
	}
}
