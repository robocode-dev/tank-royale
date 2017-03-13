package net.robocode2.game;

import static net.robocode2.game.MathUtil.normalAbsoluteAngleDegrees;
import static net.robocode2.model.Physics.BOT_BOUNDING_CIRCLE_DIAMETER;
import static net.robocode2.model.Physics.BOT_BOUNDING_CIRCLE_RADIUS;
import static net.robocode2.model.Physics.INITIAL_BOT_ENERGY;
import static net.robocode2.model.Physics.INITIAL_GUN_HEAT;
import static net.robocode2.model.Physics.MAX_BULLET_POWER;
import static net.robocode2.model.Physics.MAX_BULLET_SPEED;
import static net.robocode2.model.Physics.MIN_BULLET_POWER;
import static net.robocode2.model.Physics.RADAR_RADIUS;
import static net.robocode2.model.Physics.calcBotSpeed;
import static net.robocode2.model.Physics.calcBulletSpeed;
import static net.robocode2.model.Physics.calcGunHeat;
import static net.robocode2.model.Physics.calcScanAngle;
import static net.robocode2.model.Physics.calcTurnRate;

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
import net.robocode2.model.ImmutableBot;
import net.robocode2.model.Physics;
import net.robocode2.model.Position;
import net.robocode2.model.Round;
import net.robocode2.model.Score;
import net.robocode2.model.Size;
import net.robocode2.model.Turn;
import net.robocode2.model.events.BotDeathEvent;
import net.robocode2.model.events.BotHitBotEvent;
import net.robocode2.model.events.BotHitWallEvent;
import net.robocode2.model.events.BulletFiredEvent;
import net.robocode2.model.events.BulletHitBotEvent;
import net.robocode2.model.events.BulletHitBulletEvent;
import net.robocode2.model.events.BulletMissedEvent;
import net.robocode2.model.events.ScannedBotEvent;

public class ModelUpdater {

	private final static double RAM_DAMAGE = 0.6;

	private final static int BULLET_HIT_ENERGY_GAIN_FACTOR = 3;

	private final GameSetup setup;
	private final Set<Integer> participantIds;

	private final ScoreKeeper scoreKeeper;

	private GameState.Builder gameStateBuilder;
	private Round.Builder roundBuilder;
	private Turn.Builder turnBuilder;

	private int roundNumber;
	private int turnNumber;
	private boolean roundEnded;

	private int nextBulletId;

	private Turn previousTurn;

	private Map<Integer /* BotId */, BotIntent.Builder> botIntentsMap = new HashMap<>();
	private Map<Integer /* BotId */, Bot.Builder> botBuildersMap = new HashMap<>();
	private Set<Bullet.Builder> bulletBuildersSet = new HashSet<>();

	public ModelUpdater(GameSetup setup, Set<Integer> participantIds) {
		this.setup = setup;
		this.participantIds = new HashSet<>(participantIds);

		this.scoreKeeper = new ScoreKeeper(participantIds);

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

		updateBotIntents(botIntents);

		if (roundNumber == 0 && turnNumber == 0) {
			nextRound();

		} else {
			if (roundEnded) {
				nextRound();
			}
			nextTurn();
		}

		return buildUpdatedGameState();
	}

	private void updateBotIntents(Map<Integer /* BotId */, BotIntent> botIntents) {
		for (Map.Entry<Integer, BotIntent> entry : botIntents.entrySet()) {
			BotIntent.Builder builder = botIntentsMap.get(entry.getKey());
			if (builder == null) {
				builder = new BotIntent.Builder();
				botIntentsMap.put(entry.getKey(), builder);
			}
			builder.update(entry.getValue());
		}
	}

	private void nextRound() {
		roundNumber++;
		roundBuilder.setRoundNumber(roundNumber);

		roundEnded = false;

		nextBulletId = 0;

		initializeBotStates();

		scoreKeeper.reset();
	}

	private void nextTurn() {

		previousTurn = turnBuilder.build();

		// Reset events
		turnBuilder.resetEvents();

		turnNumber++;
		turnBuilder.setTurnNumber(turnNumber);

		// Remove dead bots (cannot participate in new round)
		removeDeadBots();

		// Execute bot intents
		executeBotIntents();

		// Check bot wall collisions
		checkBotWallCollisions();

		// Check bot to bot collisions
		checkBotCollisions();

		// Update bullet positions to new position
		updateBulletPositions();

		// Check bullet wall collisions
		checkBulletWallCollisions();

		// Check bullet hits
		checkBulletHits();

		// Cleanup killed robots (events)
		checkForKilledBots();

		// Fire guns
		cooldownAndFireGuns();

		// Generate scan events
		checkScanArcs();

		// Check if the round is over
		checkIfRoundOrGameOver();

		// Store bot snapshots
		Set<Bot> bots = new HashSet<>();
		for (Bot.Builder botBuilder : botBuildersMap.values()) {
			bots.add(botBuilder.build());
		}
		turnBuilder.setBots(bots);

		// Store bullet snapshots
		Set<Bullet> bullets = new HashSet<>();
		for (Bullet.Builder bulletBuilder : bulletBuildersSet) {
			bullets.add(bulletBuilder.build());
		}
		turnBuilder.setBullets(bullets);
	}

	private GameState buildUpdatedGameState() {
		Turn turn = turnBuilder.build();
		roundBuilder.appendTurn(turn);

		Round round = roundBuilder.build();
		gameStateBuilder.appendRound(round);

		return gameStateBuilder.build();
	}

	private void initializeBotStates() {
		Set<Integer> occupiedCells = new HashSet<Integer>();

		for (int id : participantIds) {
			Bot.Builder botBuilder = new Bot.Builder();
			botBuilder.setId(id);
			botBuilder.setEnergy(INITIAL_BOT_ENERGY);
			botBuilder.setSpeed(0);
			botBuilder.setPosition(randomBotPosition(occupiedCells));
			botBuilder.setDirection(randomDirection());
			botBuilder.setGunDirection(randomDirection());
			botBuilder.setRadarDirection(randomDirection());
			botBuilder.setScanArc(new Arc(0, RADAR_RADIUS));
			botBuilder.setGunHeat(INITIAL_GUN_HEAT);
			botBuilder.setScore(new Score.Builder().build());

			botBuildersMap.put(id, botBuilder);
		}

		// Store bot snapshots into current turn
		Set<Bot> bots = new HashSet<>();
		for (Bot.Builder botBuilder : botBuildersMap.values()) {
			bots.add(botBuilder.build());
		}
		turnBuilder.setBots(bots);
	}

	private Position randomBotPosition(Set<Integer> occupiedCells) {

		final int gridWidth = setup.getArenaWidth() / 50;
		final int gridHeight = setup.getArenaHeight() / 50;

		final int cellCount = gridWidth * gridHeight;

		final int numBots = participantIds.size();
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

				x += BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (cellWidth - BOT_BOUNDING_CIRCLE_DIAMETER);
				y += BOT_BOUNDING_CIRCLE_RADIUS + Math.random() * (cellHeight - BOT_BOUNDING_CIRCLE_DIAMETER);

				break;
			}
		}
		return new Position(x, y);
	}

	private void executeBotIntents() {

		for (Integer botId : botBuildersMap.keySet()) {
			Bot.Builder botBuilder = botBuildersMap.get(botId);

			// Bot cannot move, if it is disabled
			if (botBuilder.isDead() || botBuilder.isDisabled()) {
				continue;
			}

			BotIntent.Builder intentBuilder = botIntentsMap.get(botId);
			if (intentBuilder == null) {
				continue;
			}

			BotIntent intent = intentBuilder.build();
			if (intent == null) {
				continue;
			}

			// Turn body, gun, radar, and move bot to new position

			double speed = calcBotSpeed(botBuilder.getSpeed(), intent.getTargetSpeed());
			double turnRate = calcTurnRate(intent.getBodyTurnRate(), speed);
			double direction = normalAbsoluteAngleDegrees(botBuilder.getDirection() + turnRate);
			double gunDirection = normalAbsoluteAngleDegrees(botBuilder.getGunDirection() + intent.getGunTurnRate());
			double radarDirection = normalAbsoluteAngleDegrees(
					botBuilder.getRadarDirection() + intent.getRadarTurnRate());
			Arc scanArc = new Arc(calcScanAngle(intent.getRadarTurnRate()), RADAR_RADIUS);

			botBuilder.setDirection(direction);
			botBuilder.setGunDirection(gunDirection);
			botBuilder.setRadarDirection(radarDirection);
			botBuilder.setScanArc(scanArc);
			botBuilder.setSpeed(speed);
			botBuilder.moveToNewPosition();
		}
	}

	private void checkBulletHits() {
		Line[] boundingLines = new Line[bulletBuildersSet.size()];

		Bullet.Builder[] bulletBuilders = new Bullet.Builder[bulletBuildersSet.size()];
		bulletBuilders = bulletBuildersSet.toArray(bulletBuilders);

		for (int i = boundingLines.length - 1; i >= 0; i--) {
			Bullet.Builder bulletBuilder = bulletBuilders[i];

			Line line = new Line();
			line.start = bulletBuilder.calcPosition();
			line.end = bulletBuilder.calcNextPosition();

			boundingLines[i] = line;
		}

		for (int i = boundingLines.length - 1; i >= 0; i--) {

			// Check bullet-bullet collision
			Position endPos1 = boundingLines[i].end;
			for (int j = i - 1; j >= 0; j--) {
				Position endPos2 = boundingLines[j].end;

				// Check if the bullets bounding circles intersects (is fast) before checking if the bullets bounding
				// lines intersect (is slower)
				if (isBulletsBoundingCirclesColliding(endPos1, endPos2) && MathUtil.doLinesIntersect(
						boundingLines[i].start, boundingLines[i].end, boundingLines[j].start, boundingLines[j].end)) {

					Bullet.Builder bulletBuilder1 = bulletBuilders[i];
					Bullet.Builder bulletBuilder2 = bulletBuilders[j];

					Bullet bullet1 = bulletBuilder1.build();
					Bullet bullet2 = bulletBuilder2.build();

					BulletHitBulletEvent bulletHitBulletEvent1 = new BulletHitBulletEvent(bullet1, bullet2);
					turnBuilder.addPrivateBotEvent(bullet1.getBotId(), bulletHitBulletEvent1);

					BulletHitBulletEvent bulletHitBulletEvent2 = new BulletHitBulletEvent(bullet2, bullet1);
					turnBuilder.addPrivateBotEvent(bullet2.getBotId(), bulletHitBulletEvent2);

					// Observers only need a single event
					turnBuilder.addObserverEvent(bulletHitBulletEvent1);

					// Remove bullets from the arena
					bulletBuildersSet.remove(bulletBuilder1);
					bulletBuildersSet.remove(bulletBuilder2);
				}
			}

			// Check bullet-bot collision (hit)

			Position startPos1 = boundingLines[i].start;

			for (Bot.Builder botBuilder : botBuildersMap.values()) {
				Position botPos = botBuilder.getPosition();

				Bullet.Builder bulletBuilder = bulletBuilders[i];

				int botId = bulletBuilder.getBotId();
				int victimId = botBuilder.getId();

				if (botId == victimId) {
					// A bot cannot shot itself. The bullet must leave the cannon before it counts
					continue;
				}

				if (MathUtil.isLineIntersectingCircle(startPos1.x, startPos1.y, endPos1.x, endPos1.y, botPos.x,
						botPos.y, BOT_BOUNDING_CIRCLE_RADIUS)) {

					double damage = Physics.calcBulletDamage(bulletBuilder.getPower());
					boolean killed = botBuilder.addDamage(damage);

					double energyBonus = BULLET_HIT_ENERGY_GAIN_FACTOR * bulletBuilder.getPower();
					botBuildersMap.get(botId).increaseEnergy(energyBonus);

					scoreKeeper.addBulletHit(botId, victimId, damage, killed);

					BulletHitBotEvent bulletHitBotEvent = new BulletHitBotEvent(bulletBuilder.build(), victimId, damage,
							botBuilder.getEnergy());

					turnBuilder.addPrivateBotEvent(botId, bulletHitBotEvent);
					turnBuilder.addObserverEvent(bulletHitBotEvent);

					// Remove bullet from the arena
					bulletBuildersSet.remove(bulletBuilder);
				}
			}
		}
	}

	private static final double BULLET_BOUNDING_CIRCLE_DIAMETER = 2 * MAX_BULLET_SPEED;
	private static final double BULLET_BOUNDING_CIRCLE_DIAMETER_SQUARED = BULLET_BOUNDING_CIRCLE_DIAMETER
			* BULLET_BOUNDING_CIRCLE_DIAMETER;

	private static boolean isBulletsBoundingCirclesColliding(Position bullet1Position, Position bullet2Position) {
		double dx = bullet2Position.x - bullet1Position.x;
		if (Math.abs(dx) > BULLET_BOUNDING_CIRCLE_DIAMETER) {
			return false;
		}
		double dy = bullet2Position.y - bullet1Position.y;
		if (Math.abs(dy) > BULLET_BOUNDING_CIRCLE_DIAMETER) {
			return false;
		}
		return ((dx * dx) + (dy * dy) <= BULLET_BOUNDING_CIRCLE_DIAMETER_SQUARED);
	}

	private void checkBotCollisions() {

		Bot.Builder[] botBuilders = new Bot.Builder[botBuildersMap.size()];
		botBuilders = botBuildersMap.values().toArray(botBuilders);

		for (int i = botBuilders.length - 1; i >= 0; i--) {
			Position pos1 = botBuilders[i].getPosition();

			for (int j = i - 1; j >= 0; j--) {
				Position pos2 = botBuilders[j].getPosition();

				if (isBotsBoundingCirclesColliding(pos1, pos2)) {
					final double overlapDist = BOT_BOUNDING_CIRCLE_DIAMETER - MathUtil.distance(pos1, pos2);

					final Bot.Builder botBuilder1 = botBuilders[i];
					final Bot.Builder botBuilder2 = botBuilders[j];

					final int botId1 = botBuilder1.getId();
					final int botId2 = botBuilder2.getId();

					final boolean bot1Killed = botBuilder1.addDamage(RAM_DAMAGE);
					final boolean bot2Killed = botBuilder2.addDamage(RAM_DAMAGE);

					final boolean bot1RammedBot2 = isRamming(botBuilder1, botBuilder2);
					final boolean bot2rammedBot1 = isRamming(botBuilder2, botBuilder1);

					double bot1BounceDist = 0;
					double bot2BounceDist = 0;

					if (bot1RammedBot2) {
						botBuilder1.setSpeed(0);
						bot1BounceDist = overlapDist;
						scoreKeeper.addRamHit(botId2, botId1, RAM_DAMAGE, bot1Killed);
					}
					if (bot2rammedBot1) {
						botBuilder2.setSpeed(0);
						bot2BounceDist = overlapDist;
						scoreKeeper.addRamHit(botId1, botId2, RAM_DAMAGE, bot2Killed);
					}
					if (bot1RammedBot2 && bot2rammedBot1) {
						double totalSpeed = botBuilder1.getSpeed() + botBuilder1.getSpeed();

						if (totalSpeed == 0.0) {
							bot1BounceDist /= 2;
							bot2BounceDist /= 2;

						} else {
							double t = overlapDist / totalSpeed;

							// The faster speed, the less bounce distance. Hence the speeds for the bots are swapped
							bot1BounceDist = botBuilder2.getSpeed() * t;
							bot2BounceDist = botBuilder1.getSpeed() * t;
						}
					}
					if (bot1BounceDist != 0) {
						botBuilder1.bounceBack(bot1BounceDist);
					}
					if (bot2BounceDist != 0) {
						botBuilder2.bounceBack(bot2BounceDist);
					}

					pos1 = botBuilder1.getPosition();
					pos2 = botBuilder2.getPosition();

					BotHitBotEvent BotHitBotEvent1 = new BotHitBotEvent(botId1, botId2, botBuilder2.getEnergy(),
							botBuilder2.getPosition(), bot1RammedBot2);
					BotHitBotEvent BotHitBotEvent2 = new BotHitBotEvent(botId2, botId1, botBuilder1.getEnergy(),
							botBuilder1.getPosition(), bot2rammedBot1);

					turnBuilder.addPrivateBotEvent(botId1, BotHitBotEvent1);
					turnBuilder.addPrivateBotEvent(botId2, BotHitBotEvent2);

					turnBuilder.addObserverEvent(BotHitBotEvent1);
					turnBuilder.addObserverEvent(BotHitBotEvent2);
				}
			}
		}
	}

	private static final double BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED = BOT_BOUNDING_CIRCLE_DIAMETER
			* BOT_BOUNDING_CIRCLE_DIAMETER;

	private static boolean isBotsBoundingCirclesColliding(Position bot1Position, Position bot2Position) {
		double dx = bot2Position.x - bot1Position.x;
		if (Math.abs(dx) > BOT_BOUNDING_CIRCLE_DIAMETER) { // 2 x radius
			return false;
		}
		double dy = bot2Position.y - bot1Position.y;
		if (Math.abs(dy) > BOT_BOUNDING_CIRCLE_DIAMETER) { // 2 x radius
			return false;
		}
		return ((dx * dx) + (dy * dy) <= BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED);
	}

	private static final double RADAR_RADIUS_SQUARED = RADAR_RADIUS * RADAR_RADIUS;

	private static boolean isBotInScanArc(Bot.Builder scanningBot, Position scannedBotPosition) {
		Position scanningPosition = scanningBot.getPosition();

		double dx = scannedBotPosition.x - scanningPosition.x;
		if (Math.abs(dx) > RADAR_RADIUS) {
			return false;
		}
		double dy = scannedBotPosition.y - scannedBotPosition.y;
		if (Math.abs(dy) > RADAR_RADIUS) {
			return false;
		}
		if ((dx * dx) + (dy * dy) > RADAR_RADIUS_SQUARED) {
			return false;
		}

		Arc scanArc = scanningBot.getScanArc();

		double botAngle = Math.atan2(dy, dx);
		double spanAngle = scanArc.getAngle();
		double startAngle = scanningBot.getRadarDirection();
		double endAngle = startAngle + spanAngle;
		return (((spanAngle >= 0) && (botAngle >= startAngle && botAngle <= endAngle))
				|| (botAngle >= endAngle && botAngle >= startAngle));
	}

	private static boolean isRamming(ImmutableBot bot, ImmutableBot victim) {

		double dx = victim.getPosition().x - bot.getPosition().x;
		double dy = victim.getPosition().y - bot.getPosition().y;

		double angle = Math.atan2(dy, dx);

		double bearing = MathUtil.normalRelativeAngleDegrees(Math.toDegrees(angle) - bot.getDirection());

		return ((bot.getSpeed() > 0 && (bearing > -90 && bearing < 90))
				|| (bot.getSpeed() < 0 && (bearing < -90 || bearing > 90)));
	}

	private void updateBulletPositions() {
		for (Bullet.Builder bulletBuilder : bulletBuildersSet) {
			bulletBuilder.incrementTick(); // The tick is used to calculate new position by calling getPosition()
		}
	}

	private void checkBotWallCollisions() {

		for (Bot.Builder botBuilder : botBuildersMap.values()) {

			Position position = botBuilder.getPosition();
			double x = position.x;
			double y = position.y;

			Position oldPosition = previousTurn.getBot(botBuilder.getId()).get().getPosition();
			double dx = x - oldPosition.x;
			double dy = y - oldPosition.y;

			boolean hitWall = false;

			if (x - BOT_BOUNDING_CIRCLE_RADIUS <= 0) {
				hitWall = true;

				x = BOT_BOUNDING_CIRCLE_RADIUS;

				if (dx != 0) {
					double dx_cut = x - oldPosition.x;
					y = oldPosition.y + (dx_cut * dy / dx);
				}
			} else if (x + BOT_BOUNDING_CIRCLE_RADIUS >= setup.getArenaWidth()) {
				hitWall = true;

				x = setup.getArenaWidth() - BOT_BOUNDING_CIRCLE_RADIUS;

				if (dx != 0) {
					double dx_cut = x - oldPosition.x;
					y = oldPosition.y + (dx_cut * dy / dx);
				}
			} else if (y - BOT_BOUNDING_CIRCLE_RADIUS <= 0) {
				hitWall = true;

				y = BOT_BOUNDING_CIRCLE_RADIUS;

				if (dy != 0) {
					double dy_cut = y - oldPosition.y;
					x = oldPosition.x + (dy_cut * dx / dy);
				}
			} else if (y + BOT_BOUNDING_CIRCLE_RADIUS >= setup.getArenaHeight()) {
				hitWall = true;

				y = setup.getArenaHeight() - BOT_BOUNDING_CIRCLE_RADIUS;

				if (dy != 0) {
					double dy_cut = y - oldPosition.y;
					x = oldPosition.x + (dy_cut * dx / dy);
				}
			}

			if (hitWall) {
				botBuilder.setPosition(new Position(x, y));

				// Skip this check, if the bot hit the wall in the previous turn
				if (previousTurn.getBotEvents(botBuilder.getId()).stream()
						.anyMatch(e -> e instanceof BotHitWallEvent)) {
					continue;
				}

				BotHitWallEvent botHitWallEvent = new BotHitWallEvent(botBuilder.getId());
				turnBuilder.addPrivateBotEvent(botBuilder.getId(), botHitWallEvent);
				turnBuilder.addObserverEvent(botHitWallEvent);

				double damage = Physics.calcWallDamage(botBuilder.getSpeed());
				botBuilder.addDamage(damage);
			}
		}
	}

	private void checkBulletWallCollisions() {
		Iterator<Bullet.Builder> iterator = bulletBuildersSet.iterator(); // due to removal
		while (iterator.hasNext()) {
			Bullet.Builder bulletBuilder = iterator.next();
			Position position = bulletBuilder.calcPosition();

			if ((position.x <= 0) || (position.x >= setup.getArenaWidth()) || (position.y <= 0)
					|| (position.y >= setup.getArenaHeight())) {

				iterator.remove(); // remove bullet from arena

				BulletMissedEvent bulletMissedEvent = new BulletMissedEvent(bulletBuilder.build());
				turnBuilder.addPrivateBotEvent(bulletBuilder.getBotId(), bulletMissedEvent);
				turnBuilder.addObserverEvent(bulletMissedEvent);
			}
		}
	}

	private void checkForKilledBots() {
		for (Bot.Builder botBuilder : botBuildersMap.values()) {
			if (botBuilder.isDead()) {
				int victimId = botBuilder.getId();

				BotDeathEvent botDeathEvent = new BotDeathEvent(victimId);
				turnBuilder.addPublicBotEvent(botDeathEvent);
				turnBuilder.addObserverEvent(botDeathEvent);
			}
		}
	}

	private void removeDeadBots() {
		Iterator<Bot.Builder> iterator = botBuildersMap.values().iterator(); // due to removal
		while (iterator.hasNext()) {
			Bot.Builder botBuilder = iterator.next();
			if (botBuilder.isDead()) {
				iterator.remove(); // remove bot from arena
			}
		}
	}

	private void cooldownAndFireGuns() {
		for (Bot.Builder botBuilder : botBuildersMap.values()) {

			// Bot cannot fire if it is disabled
			if (botBuilder.isDead() || botBuilder.isDisabled()) {
				continue;
			}

			// Fire gun, if the gun heat is zero
			double gunHeat = botBuilder.getGunHeat();
			if (gunHeat == 0) {
				// Gun can fire => Check if intent is to fire gun
				BotIntent.Builder intentBuilder = botIntentsMap.get(botBuilder.getId());
				if (intentBuilder == null) {
					continue;
				}
				BotIntent intent = intentBuilder.build();

				double firepower = intent.getBulletPower();
				if (firepower >= MIN_BULLET_POWER) {
					// Gun is fired
					firepower = Math.min(firepower, MAX_BULLET_POWER);
					handleFiredBullet(botBuilder, firepower);
				}
			} else {
				// Gun is too hot => Cool down gun
				gunHeat = Math.max(gunHeat - setup.getGunCoolingRate(), 0);
				botBuilder.setGunHeat(gunHeat);
			}
		}
	}

	private void handleFiredBullet(Bot.Builder botBuilder, double firepower) {
		int botId = botBuilder.getId();

		double gunHeat = calcGunHeat(firepower);
		botBuilder.setGunHeat(gunHeat);

		Bullet.Builder bulletBuilder = new Bullet.Builder();
		bulletBuilder.setBotId(botId);
		bulletBuilder.setBulletId(++nextBulletId);
		bulletBuilder.setPower(firepower);
		bulletBuilder.setFirePosition(botBuilder.getPosition());
		bulletBuilder.setDirection(botBuilder.getGunDirection());
		bulletBuilder.setSpeed(calcBulletSpeed(firepower));

		bulletBuildersSet.add(bulletBuilder);

		Bullet bullet = bulletBuilder.build();
		BulletFiredEvent bulletFiredEvent = new BulletFiredEvent(bullet);
		turnBuilder.addPrivateBotEvent(botId, bulletFiredEvent);
		turnBuilder.addObserverEvent(bulletFiredEvent);
	}

	private void checkScanArcs() {

		Bot.Builder[] botBuilders = new Bot.Builder[botBuildersMap.size()];
		botBuilders = botBuildersMap.values().toArray(botBuilders);

		for (int i = botBuilders.length - 1; i >= 0; i--) {
			Bot.Builder scanningBot = botBuilders[i];

			for (int j = i - 1; j >= 0; j--) {
				Bot.Builder scannedBot = botBuilders[j];

				Position scannedPos = null;

				Position currentPos = botBuilders[j].getPosition();
				if (isBotInScanArc(scanningBot, currentPos)) {
					scannedPos = currentPos;
				} else {
					Position prevPos = previousTurn.getBot(scannedBot.getId()).get().getPosition();

					if (isBotInScanArc(scanningBot, prevPos)) {
						scannedPos = prevPos;
					}
				}

				if (scannedPos != null) {
					ScannedBotEvent scannedBotEvent = new ScannedBotEvent(scanningBot.getId(), scannedBot.getId(),
							scannedBot.getEnergy(), scannedBot.getPosition(), scannedBot.getDirection(),
							scannedBot.getSpeed());

					turnBuilder.addPrivateBotEvent(scanningBot.getId(), scannedBotEvent);
					turnBuilder.addObserverEvent(scannedBotEvent);
				}
			}
		}
	}

	private void checkIfRoundOrGameOver() {
		if (botBuildersMap.size() <= 1) {
			// Round ended
			roundEnded = true;

			if (roundNumber == setup.getNumberOfRounds()) {
				// Game over
				gameStateBuilder.setGameEnded();
			}
		}
	}

	private static double randomDirection() {
		return Math.random() * 360;
	}

	private class Line {
		Position start;
		Position end;
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
