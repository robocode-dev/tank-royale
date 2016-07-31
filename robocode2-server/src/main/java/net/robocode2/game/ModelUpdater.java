package net.robocode2.game;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.robocode2.model.Arc;
import net.robocode2.model.Arena;
import net.robocode2.model.Bot;
import net.robocode2.model.Bot.BotBuilder;
import net.robocode2.model.GameState;
import net.robocode2.model.GameState.GameStateBuilder;
import net.robocode2.model.Position;
import net.robocode2.model.Round;
import net.robocode2.model.Round.RoundBuilder;
import net.robocode2.model.Score.ScoreBuilder;
import net.robocode2.model.Setup;
import net.robocode2.model.Size;
import net.robocode2.model.Turn;
import net.robocode2.model.Turn.TurnBuilder;

public class ModelUpdater {

	private static final double INITIAL_BOT_ENERGY = 100.0;
	private static final double RADAR_RADIUS = 1200.0;

	private final Setup setup;

	private GameStateBuilder gameStateBuilder;
	private RoundBuilder roundBuilder;
	private TurnBuilder turnBuilder;

	public ModelUpdater(Setup setup) {
		this.setup = setup;

		initialize();
	}

	private void initialize() {
		// Prepare game state builders
		gameStateBuilder = new GameStateBuilder();
		roundBuilder = new RoundBuilder();
		turnBuilder = new TurnBuilder();

		// Prepare game state builder
		Arena arena = new Arena(new Size(setup.getArenaWidth(), setup.getArenaHeight()));
		gameStateBuilder.setArena(arena);
	}

	public GameState newRound() {
		roundBuilder.incrementRoundNumber();

		Set<Bot> bots = initialBotStates();
		turnBuilder.setBots(bots);

		return buildGameState();
	}

	public GameState nextTurn() {
		turnBuilder.incrementTurnNumber();

		return buildGameState();
	}

	public GameState buildGameState() {
		Turn turn = turnBuilder.build();
		roundBuilder.appendTurn(turn);

		Round round = roundBuilder.build();
		gameStateBuilder.appendRound(round);

		GameState gameState = gameStateBuilder.build();
		return gameState;
	}

	private Set<Bot> initialBotStates() {
		Set<Bot> bots = new HashSet<Bot>();

		Set<Integer> occupiedCells = new HashSet<Integer>();

		for (int id : setup.getParticipantIds()) {

			BotBuilder builder = new BotBuilder();
			builder.setId(id);
			builder.setEnergy(INITIAL_BOT_ENERGY);
			builder.setSpeed(0);
			builder.setPosition(randomBotPosition(occupiedCells));
			builder.setDirection(randomDirection());
			builder.setTurretDirection(randomDirection());
			builder.setRadarDirection(randomDirection());
			builder.setScanArc(new Arc(0, RADAR_RADIUS));
			builder.setScore(new ScoreBuilder().build());

			Bot bot = builder.build();
			bots.add(bot);
		}

		return bots;
	}

	private Position randomBotPosition(Set<Integer> occupiedCells) {

		// The max width and height on the axes of the tank, e.g. when turning 45 degrees
		final int botHypot = (int) Math.sqrt(Bot.WIDTH * Bot.WIDTH + Bot.HEIGHT * Bot.HEIGHT) + 1;

		final int gridWidth = setup.getArenaWidth() / 100;
		final int gridHeight = setup.getArenaHeight() / 100;

		final int cellCount = gridWidth * gridHeight;

		final int numBots = setup.getParticipantIds().size();
		if (cellCount < numBots) {
			throw new IllegalArgumentException("Area size (" + setup.getArenaWidth() + ',' + setup.getArenaHeight()
					+ ") is to small to contain " + numBots + " bots");
		}

		final int cellWidth = setup.getArenaWidth() / gridWidth;
		final int cellHeight = setup.getArenaHeight() / gridHeight;

		double x, y;

		while (true) {
			int cell = (int) (Math.random() * cellCount);
			if (!occupiedCells.contains(cell)) {
				occupiedCells.add(cell);

				y = cell / gridWidth;
				x = cell - y * gridWidth;

				x *= cellWidth;
				y *= cellHeight;

				x += Math.random() * (cellWidth - botHypot);
				y += Math.random() * (cellHeight - botHypot);

				break;
			}
		}
		return new Position(x, y);
	}

	private static double randomDirection() {
		return Math.random() * 360;
	}

	public static void main(String[] args) {

		Setup setup = new Setup("gameType", false, 200, 100, 0, 0, 0, new HashSet<Integer>(Arrays.asList(1, 2)));

		ModelUpdater updater = new ModelUpdater(setup);
		updater.initialBotStates();
	}
}
