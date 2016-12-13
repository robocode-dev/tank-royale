package net.robocode2.game;

import static net.robocode2.model.Physics.INITIAL_BOT_ENERGY;
import static net.robocode2.model.Physics.INITIAL_GUN_HEAT;
import static net.robocode2.model.Physics.MAX_BULLET_POWER;
import static net.robocode2.model.Physics.MIN_BULLET_POWER;
import static net.robocode2.model.Physics.RADAR_RADIUS;
import static net.robocode2.model.Physics.calcBotSpeed;
import static net.robocode2.model.Physics.calcBulletSpeed;
import static net.robocode2.model.Physics.calcGunHeat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.robocode2.model.Arc;
import net.robocode2.model.Arena;
import net.robocode2.model.Bot;
import net.robocode2.model.BotIntent;
import net.robocode2.model.Bullet;
import net.robocode2.model.GameSetup;
import net.robocode2.model.GameState;
import net.robocode2.model.Position;
import net.robocode2.model.Round;
import net.robocode2.model.Score;
import net.robocode2.model.Size;
import net.robocode2.model.Turn;
import net.robocode2.model.events.BulletFiredEvent;

public class ModelUpdater {

	private final GameSetup setup;

	private GameState.Builder gameStateBuilder;
	private Round.Builder roundBuilder;
	private Turn.Builder turnBuilder;

	private int roundNumber;
	private int turnNumber;
	private boolean roundEnded;

	private int nextBulletId;

	private Turn previousTurn;

	private Map<Integer /* BotId */, BotIntent> botIntentMap = new HashMap<>();
	private Map<Integer /* BotId */, Bot.Builder> botStateMap = new HashMap<>();

	public ModelUpdater(GameSetup setup) {
		this.setup = setup;

		initialize();
	}

	private void initialize() {
		// Prepare game state builders
		gameStateBuilder = new GameState.Builder();
		roundBuilder = new Round.Builder();
		turnBuilder = new Turn.Builder();

		// Prepare game state builder
		Arena arena = new Arena(new Size(setup.getArenaWidth(), setup.getArenaHeight()));
		gameStateBuilder.setArena(arena);

		roundNumber = 0;
		turnNumber = 0;
	}

	public GameState update(Map<Integer /* BotId */, BotIntent> botIntents) {
		botIntentMap = botIntents;

		if (roundEnded || roundNumber == 0) {
			nextRound();
		}

		nextTurn();

		return buildGameState();
	}

	private void nextRound() {
		roundNumber++;
		roundBuilder.setRoundNumber(roundNumber);

		roundEnded = false;

		Set<Bot> bots = initialBotStates();
		turnBuilder.setBots(bots);
	}

	private void nextTurn() {

		previousTurn = turnBuilder.build();

		turnNumber++;
		turnBuilder.setTurnNumber(turnNumber);

		// Prepare map over new bot states
		botStateMap.clear();
		for (Bot bot : previousTurn.getBots()) {
			botStateMap.put(bot.getId(), new Bot.Builder(bot));
		}

		// Execute bot intents
		executeBotIntents();

		// Update bullet future positions
		// Check bot wall collisions
		// Check bullet wall collisions
		// Check bullet to bullet collisions
		// Check bot to bot collisions
		// Check bullet to bot collisions

		// Fire guns
		fireGuns();
	}

	private GameState buildGameState() {
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

			Bot.Builder builder = new Bot.Builder();
			builder.setId(id);
			builder.setEnergy(INITIAL_BOT_ENERGY);
			builder.setSpeed(0);
			builder.setPosition(randomBotPosition(occupiedCells));
			builder.setDirection(randomDirection());
			builder.setGunDirection(randomDirection());
			builder.setRadarDirection(randomDirection());
			builder.setScanArc(new Arc(0, RADAR_RADIUS));
			builder.setGunHeat(INITIAL_GUN_HEAT);
			builder.setScore(new Score.Builder().build());

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

	private void executeBotIntents() {

		for (Integer botId : botStateMap.keySet()) {
			BotIntent intent = botIntentMap.get(botId);
			Bot.Builder state = botStateMap.get(botId);

			// Turn body, gun, radar, and move bot to new position
			double direction = state.getDirection() + intent.getBodyTurnRate();
			double gunDirection = state.getGunDirection() + intent.getGunTurnRate();
			double radarDirection = state.getRadarDirection() + intent.getRadarTurnRate();
			double speed = calcBotSpeed(state.getSpeed(), intent.getTargetSpeed());

			state.setDirection(direction);
			state.setGunDirection(gunDirection);
			state.setRadarDirection(radarDirection);
			state.setSpeed(speed);
			state.setPosition(state.getPosition().calcNewPosition(direction, speed));
		}
	}

	private void fireGuns() {
		for (Integer botId : botStateMap.keySet()) {
			BotIntent intent = botIntentMap.get(botId);
			Bot.Builder state = botStateMap.get(botId);

			// Fire gun, if the gun heat is zero
			double gunHeat = state.getGunHeat();
			gunHeat = Math.max(gunHeat - setup.getGunCoolingRate(), 0);

			if (gunHeat == 0) {
				// Gun can fire. Check if gun must be fired by intent
				double firepower = intent.getBulletPower();
				if (firepower >= MIN_BULLET_POWER) {
					// Gun is fired
					firepower = Math.min(firepower, MAX_BULLET_POWER);
					gunHeat = calcGunHeat(firepower);

					handleFiredBullet(state, firepower);
				}
			}
			state.setGunHeat(gunHeat);
		}
	}

	private void handleFiredBullet(Bot.Builder state, double firepower) {
		Position position = state.getPosition();

		int botId = state.getId();
		int bulletId = ++nextBulletId;
		double direction = state.getGunDirection();
		double speed = calcBulletSpeed(firepower);

		Bullet bullet = new Bullet(botId, bulletId, firepower, position, direction, speed, 0);

		turnBuilder.addBullet(bullet);

		BulletFiredEvent bulletFiredEvent = new BulletFiredEvent(bullet);
		turnBuilder.addBotEvent(botId, bulletFiredEvent);
		turnBuilder.addObserverEvent(bulletFiredEvent);
	}

	private static double randomDirection() {
		return Math.random() * 360;
	}

	public static void main(String[] args) {

		// Setup setup = new Setup("gameType", 200, 100, 0, 0, 0, new HashSet<Integer>(Arrays.asList(1, 2)));
		//
		// ModelUpdater updater = new ModelUpdater(setup);
		// updater.initialBotStates();

		// System.out.println("#0: " + computeNewSpeed(0, 0));
		//
		// System.out.println("#1: " + computeNewSpeed(1, 10));
		// System.out.println("#2: " + computeNewSpeed(8, 10));
		//
		// System.out.println("#3: " + computeNewSpeed(1, 1.5));
		// System.out.println("#4: " + computeNewSpeed(0, 0.3));
		//
		// System.out.println("#5: " + computeNewSpeed(8, 0));
		// System.out.println("#6: " + computeNewSpeed(7.5, -3));
		//
		// System.out.println("#7: " + computeNewSpeed(8, -8));
		//
		// System.out.println("#-1: " + computeNewSpeed(-1, -10));
		// System.out.println("#-2: " + computeNewSpeed(-8, -10));
		//
		// System.out.println("#-3: " + computeNewSpeed(-1, -1.5));
		// System.out.println("#-4: " + computeNewSpeed(0, -0.3));
		//
		// System.out.println("#-5: " + computeNewSpeed(-8, 0));
		// System.out.println("#-6: " + computeNewSpeed(-7.5, 3));
		//
		// System.out.println("#-7: " + computeNewSpeed(-8, 8));
	}
}
