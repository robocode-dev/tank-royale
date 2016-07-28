package net.robocode2.game;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.robocode2.model.Arc;
import net.robocode2.model.Bot;
import net.robocode2.model.Bot.BotBuilder;
import net.robocode2.model.GameState;
import net.robocode2.model.Position;
import net.robocode2.model.Score.ScoreBuilder;
import net.robocode2.model.Setup;

public class ModelUpdater {

	private final Setup setup;
	private GameState gameState;

	public ModelUpdater(Setup setup) {
		this.setup = setup;
	}

	public GameState newRound() {
		Set<Bot> bots = initialBotStates();

		return null; // TODO
	}

	public GameState nextTurn() {
		return null; // TODO
	}

	private Set<Bot> initialBotStates() {
		Set<Bot> bots = new HashSet<Bot>();

		Set<Integer> occupiedCells = new HashSet<Integer>();

		for (int id : setup.getParticipantIds()) {

			BotBuilder builder = new BotBuilder();
			builder.setId(id);
			builder.setEnergy(100);
			builder.setSpeed(0);
			builder.setPosition(randomBotPosition(occupiedCells));
			builder.setDirection(randomDirection());
			builder.setTurretDirection(randomDirection());
			builder.setRadarDirection(randomDirection());
			builder.setScanArc(new Arc(0, 1200));
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
