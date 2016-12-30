package net.robocode2.game;

import static net.robocode2.model.Physics.BOT_BOUNDING_CIRCLE_DIAMETER;
import static net.robocode2.model.Physics.BOT_BOUNDING_CIRCLE_RADIUS;
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
import java.util.Iterator;
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
import net.robocode2.model.events.BotHitWallEvent;
import net.robocode2.model.events.BulletFiredEvent;
import net.robocode2.model.events.BulletHitBulletEvent;
import net.robocode2.model.events.BulletMissedEvent;

public class ModelUpdater {

	private final GameSetup setup;

	private GameState.Builder gameStateBuilder;
	private Round.Builder roundBuilder;
	private Turn.Builder turnBuilder;

	private int roundNumber;
	private int turnNumber;
	private boolean roundEnded;

	private int nextBulletId;

	private Map<Integer /* BotId */, BotIntent> botIntentMap = new HashMap<>();
	private Map<Integer /* BotId */, Bot.Builder> botStateMap = new HashMap<>();
	private Set<Bullet.Builder> bulletStateSet = new HashSet<>();

	private static final int BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED = BOT_BOUNDING_CIRCLE_DIAMETER
			* BOT_BOUNDING_CIRCLE_DIAMETER;

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

		nextBulletId = 0;

		Set<Bot> bots = initialBotStates();
		turnBuilder.setBots(bots);
	}

	private void nextTurn() {

		Turn previousTurn = turnBuilder.build();

		turnNumber++;
		turnBuilder.setTurnNumber(turnNumber);

		// Prepare map over new bot states
		botStateMap.clear();
		for (Bot bot : previousTurn.getBots()) {
			botStateMap.put(bot.getId(), new Bot.Builder(bot));
		}

		// Prepare new bullet states
		bulletStateSet.clear();
		for (Bullet bullet : previousTurn.getBullets()) {
			bulletStateSet.add(new Bullet.Builder(bullet));
		}

		// Execute bot intents
		executeBotIntents();

		// Check bullet to bullet collisions
		checkBulletToBulletCollisions();

		// Update bullet positions
		updateBulletPositions();

		// Check bot wall collisions
		checkBotWallCollisions();

		// Check bullet wall collisions
		checkBulletWallCollisions();

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

			bots.add(builder.build());
		}

		return bots;
	}

	private Position randomBotPosition(Set<Integer> occupiedCells) {

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

				x += Math.random() * (cellWidth - BOT_BOUNDING_CIRCLE_DIAMETER);
				y += Math.random() * (cellHeight - BOT_BOUNDING_CIRCLE_DIAMETER);

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

	private void checkBulletToBulletCollisions() {
		Line[] boundingLines = new Line[bulletStateSet.size()];

		Bullet.Builder[] bulletBuilders = new Bullet.Builder[bulletStateSet.size()];
		bulletBuilders = bulletStateSet.toArray(bulletBuilders);

		for (int i = boundingLines.length - 1; i >= 0; i--) {
			Bullet.Builder bulletBuilder1 = bulletBuilders[i];

			Line line = new Line();
			line.start = bulletBuilder1.calcPosition();
			line.end = bulletBuilder1.calcNextPosition();

			boundingLines[i++] = line;

			Position endPos1 = boundingLines[i].end;

			for (int j = i - 1; j >= 0; j--) {
				Position endPos2 = boundingLines[j].end;

				// First calculate distance between bullets
				double dx = endPos2.getX() - endPos1.getX();
				if (Math.abs(dx) > BOT_BOUNDING_CIRCLE_DIAMETER) {
					continue;
				}
				double dy = endPos2.getY() - endPos1.getY();
				if (Math.abs(dy) > BOT_BOUNDING_CIRCLE_DIAMETER) {
					continue;
				}
				if ((dx * dx) + (dy * dy) > BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED) {
					continue;
				}
				if (linesIntersects(boundingLines[i], boundingLines[j])) {
					Bullet.Builder bulletBuilder2 = bulletBuilders[j];

					Bullet bullet1 = bulletBuilder1.build();
					Bullet bullet2 = bulletBuilder2.build();

					BulletHitBulletEvent bulletHitBulletEvent1 = new BulletHitBulletEvent(bullet1, bullet2);
					turnBuilder.addBotEvent(bullet1.getBotId(), bulletHitBulletEvent1);

					BulletHitBulletEvent bulletHitBulletEvent2 = new BulletHitBulletEvent(bullet2, bullet1);
					turnBuilder.addBotEvent(bullet2.getBotId(), bulletHitBulletEvent2);

					// Observers only need a single event
					turnBuilder.addObserverEvent(bulletHitBulletEvent1);

					// Remove bullets from the arena
					bulletStateSet.remove(bulletBuilder1);
					bulletStateSet.remove(bulletBuilder2);
				}
			}
		}
	}

	private void updateBulletPositions() {
		for (Bullet.Builder state : bulletStateSet) {
			state.incrementTick(); // The tick is used to calculate new position by calling getPosition()
		}
	}

	private void checkBotWallCollisions() {
		for (Bot.Builder bot : botStateMap.values()) {
			Position position = bot.getPosition();
			double x = position.getX();
			double y = position.getY();

			boolean hitWall = false;

			if (x - BOT_BOUNDING_CIRCLE_RADIUS <= 0) {
				x = 0;
				hitWall = true;
			} else if (x + BOT_BOUNDING_CIRCLE_RADIUS >= setup.getArenaWidth()) {
				x = setup.getArenaWidth();
				hitWall = true;
			} else if (y - BOT_BOUNDING_CIRCLE_RADIUS <= 0) {
				y = 0;
				hitWall = true;
			} else if (y + BOT_BOUNDING_CIRCLE_RADIUS >= setup.getArenaHeight()) {
				y = setup.getArenaHeight();
				hitWall = true;
			}

			if (hitWall) {
				bot.setPosition(new Position(x, y));

				BotHitWallEvent botHitWallEvent = new BotHitWallEvent(bot.getId());
				turnBuilder.addBotEvent(bot.getId(), botHitWallEvent);
				turnBuilder.addObserverEvent(botHitWallEvent);
			}
		}
	}

	private void checkBulletWallCollisions() {
		Iterator<Bullet.Builder> iterator = bulletStateSet.iterator(); // due to removal
		while (iterator.hasNext()) {
			Bullet.Builder bullet = iterator.next();
			Position position = bullet.calcPosition();

			if ((position.getX() <= 0) || (position.getX() >= setup.getArenaWidth()) || (position.getY() <= 0)
					|| (position.getY() >= setup.getArenaHeight())) {

				iterator.remove(); // remove bullet from arena,

				BulletMissedEvent bulletMissedEvent = new BulletMissedEvent(bullet.build());
				turnBuilder.addBotEvent(bullet.getBotId(), bulletMissedEvent);
				turnBuilder.addObserverEvent(bulletMissedEvent);
			}
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
		int botId = state.getId();

		Bullet.Builder builder = new Bullet.Builder();
		builder.setBotId(botId);
		builder.setBulletId(++nextBulletId);
		builder.setPower(firepower);
		builder.setFirePosition(state.getPosition());
		builder.setDirection(state.getGunDirection());
		builder.setSpeed(calcBulletSpeed(firepower));

		Bullet bullet = builder.build();

		turnBuilder.addBullet(bullet);

		BulletFiredEvent bulletFiredEvent = new BulletFiredEvent(bullet);
		turnBuilder.addBotEvent(botId, bulletFiredEvent);
		turnBuilder.addObserverEvent(bulletFiredEvent);
	}

	private static double randomDirection() {
		return Math.random() * 360;
	}

	private class Line {
		Position start;
		Position end;
	}

	private static boolean linesIntersects(Line line1, Line line2) {
		double x1 = line1.start.getX();
		double y1 = line1.start.getY();
		double x2 = line1.end.getX();
		double y2 = line1.end.getY();
		double x3 = line2.start.getX();
		double y3 = line2.start.getY();
		double x4 = line2.end.getX();
		double y4 = line2.end.getY();

		double dx13 = (x1 - x3), dx21 = (x2 - x1), dx43 = (x4 - x3);
		double dy13 = (y1 - y3), dy21 = (y2 - y1), dy43 = (y4 - y3);

		double dn = dy43 * dx21 - dx43 * dy21;

		double ua = (dx43 * dy13 - dy43 * dx13) / dn;
		double ub = (dx21 * dy13 - dy21 * dx13) / dn;

		return (ua >= 0 && ua <= 1) && (ub >= 0 && ub <= 1);
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
