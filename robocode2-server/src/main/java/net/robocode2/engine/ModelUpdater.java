package net.robocode2.engine;

import net.robocode2.events.*;
import net.robocode2.model.*;
import net.robocode2.model.Bot.BotBuilder;
import net.robocode2.model.Turn.TurnBuilder;
import net.robocode2.util.MathUtil;

import java.util.*;

import static net.robocode2.model.RuleConstants.*;
import static net.robocode2.util.MathUtil.normalAbsoluteDegrees;

/**
 * Model updater, which keep track of the model state for each turn of a game.
 *
 * @author Flemming N. Larsen
 */
public class ModelUpdater {

	/** Game setup */
	private final GameSetup setup;
	/** Participant ids */
	private final Set<Integer> participantIds;

	/** Score keeper */
	private final ScoreTracker scoreTracker;

	/** Map over bot intents identified by bot ids */
	private final Map<Integer /* BotId */, BotIntent> botIntentsMap = new HashMap<>();
	/** Map over bots identified by bot ids */
	private final Map<Integer /* BotId */, BotBuilder> botBuilderMap = new HashMap<>();
	/** Bullets */
	private final Set<Bullet> bullets = new HashSet<>();

	/** Game state */
	private GameState gameState;
	/** Round record */
	private Round round;
	/** Turn record */
	private TurnBuilder turnBuilder;

	/** Current round number */
	private int roundNumber;
	/** Current turn number */
	private int turnNumber;
	/** Flag specifying if the round has ended */
	private boolean roundEnded;

	/** Id for the next bullet that comes into existence */
	private int nextBulletId;

	/** Previous turn */
	private Turn previousTurn;

	/** Inactivity counter */
	private int inactivityCounter;

	/**
	 * Creates a new model updater
	 *
	 * @param setup
	 *            is the game setup
	 * @param participantIds
	 *            is the ids of the participating bots
	 * @return model updater
	 */
	public static ModelUpdater create(GameSetup setup, Set<Integer> participantIds) {
		return new ModelUpdater(setup, participantIds);
	}

	/**
	 * Creates a new model updater
	 *
	 * @param setup
	 *            is the game setup
	 * @param participantIds
	 *            is the ids of the participating bots
	 */
	private ModelUpdater(GameSetup setup, Set<Integer> participantIds) {
		this.setup = setup;
		this.participantIds = new HashSet<>(participantIds);

		this.scoreTracker = new ScoreTracker(participantIds);

		round = Round.builder().build();
		turnBuilder = Turn.builder();

		// Prepare game state builder
		Arena arena = new Arena(new Size(setup.getArenaWidth(), setup.getArenaHeight()));

		gameState = GameState.builder().arena(arena).build();

		roundNumber = 0;
		turnNumber = 0;
	}

	/**
	 * Updates game state
	 *
	 * @param botIntents
	 *            is the bot intents, which gives instructions to the game from the individual bot
	 * @return new game state
	 */
	public GameState update(Map<Integer /* BotId */, BotIntent> botIntents) {

		updateBotIntents(botIntents);

		if ((roundNumber == 0 && turnNumber == 0) || roundEnded) {
			if (roundEnded) {
				calculatePlacements();
			}
			nextRound();
		}
		nextTurn();

		return updateGameState();
	}

	/**
	 * Calculates and sets placements for all bots, i.e. 1st, 2nd, and 3rd places.
	 */
	public void calculatePlacements() {
		scoreTracker.calculatePlacements();
	}

	/**
	 * Returns the current results ordered with highest total scores first.
	 *
	 * @return a list of scores.
	 */
	public List<Score> getResults() {
		return scoreTracker.getResults();
	}


	/**
	 * Returns the current turn number.
	 */
	public int getTurnNumber() {
		return turnNumber;
	}

	/**
	 * Returns the number of rounds played so far.
	 */
	public int getNumberOfRounds() {
		return gameState == null ? 0 : gameState.getRounds().size();
	}

	/**
	 * Updates the current bot intents with the new bot intents
	 *
	 * @param botIntents
	 *            is the new bot intents
	 */
	private void updateBotIntents(Map<Integer /* BotId */, BotIntent> botIntents) {
		for (Map.Entry<Integer, BotIntent> entry : botIntents.entrySet()) {
			Integer botId = entry.getKey();
			BotIntent botIntent = botIntentsMap.get(botId);
			if (botIntent == null) {
				botIntent = BotIntent.builder().build();
			}
			botIntent = botIntent.update(entry.getValue());
			botIntentsMap.put(botId, botIntent);
		}
	}

	/**
	 * Proceed with next round
	 */
	private void nextRound() {
		roundNumber++;

		round = round.toBuilder().roundNumber(roundNumber).build();

		roundEnded = false;

		nextBulletId = 0;

		bullets.clear();

		initializeBotStates();

		scoreTracker.prepareRound();

		inactivityCounter = 0;
	}

	/**
	 * Proceed with next turn
	 */
	private void nextTurn() {

		previousTurn = round.getLastTurn();

		// Reset events
		turnBuilder.resetEvents();

		turnNumber++;
		turnBuilder.turnNumber(turnNumber);

		// Remove dead bots (cannot participate in new round)
		botBuilderMap.values().removeIf(BotBuilder::isDead);

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

		// Check for inactivity
		checkInactivity();

		// Check for disabled bots
		checkForDisabledBots();

		// Cleanup defeated bots (events)
		checkForDefeatedBots();

		// Fire guns
		cooldownAndFireGuns();

		// Generate scan events
		checkScanFields();

		// Check if the round is over
		checkIfRoundOrGameOver();

		// Store bot snapshots
		Set<Bot> botSet = new HashSet<>();
		for (BotBuilder botBuilder : botBuilderMap.values()) {
			botSet.add(botBuilder.build());
		}
		turnBuilder.bots(botSet);

		// Store bullet snapshots
		turnBuilder.bullets(bullets);
	}

	/**
	 * Update game state
	 *
	 * @return new game state
	 */
	private GameState updateGameState() {
		round = round.toBuilder().turn(turnBuilder.build()).build();

		List<Round> rounds = gameState.getRounds();
		if (rounds == null) {
			rounds = new ArrayList<>();
		} else {
			rounds = new ArrayList<>(rounds); // copy due to immutable list
		}

		int roundIndex = round.getRoundNumber() - 1;

		if (rounds.size() == roundIndex) {
			rounds.add(round);
		} else {
			rounds.set(roundIndex, round);
		}
		gameState = gameState.toBuilder().rounds(rounds).build();
		return gameState;
	}

	/**
	 * Initializes bot states
	 */
	private void initializeBotStates() {
		Set<Integer> occupiedCells = new HashSet<>();

		for (int id : participantIds) {
			Point randomPos = randomBotPosition(occupiedCells);
			BotBuilder botBuilder = Bot.builder()
				.id(id)
				.energy(INITIAL_BOT_ENERGY)
				.speed(0)
				.x(randomPos.x)
				.y(randomPos.y)
				.direction(MathUtil.randomDirection())
				.gunDirection(MathUtil.randomDirection())
				.radarDirection(MathUtil.randomDirection())
				.gunHeat(INITIAL_GUN_HEAT)
				.score(Score.builder().build());

			botBuilderMap.put(id, botBuilder);
		}

		// Store bot snapshots into current turn
		Set<Bot> botSet = new HashSet<>();
		for (BotBuilder botBuilder : botBuilderMap.values()) {
			botSet.add(botBuilder.build());
		}
		turnBuilder.bots(botSet);
	}

	/**
	 * Calculates a random bot position
	 *
	 * @param occupiedCells
	 *            is the occupied cells, where other bots are positioned
	 * @return a random bot position
	 */
	private Point randomBotPosition(Set<Integer> occupiedCells) {

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

		double x;
		double y;

		Random random = new Random();
		while (true) {
			int cell = random.nextInt(cellCount);

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
		return new Point(x, y);
	}

	/**
	 * Execute bot intents
	 */
	private void executeBotIntents() {

		for (Map.Entry<Integer /* botId */, BotBuilder> entry : botBuilderMap.entrySet()) {
			int botId = entry.getKey();

			BotBuilder botBuilder = botBuilderMap.get(botId);

			if (!botBuilder.isDisabled()) {

				BotIntent botIntent = botIntentsMap.get(botId);
				if (botIntent != null) {

					BotIntent immuBotIntent = botIntent.zerofied();

					double speed = RuleMath.calcNewBotSpeed(botBuilder.getSpeed(), immuBotIntent.getTargetSpeed());

					double limitedTurnRate = RuleMath.limitTurnRate(immuBotIntent.getTurnRate(), speed);
					double limitedGunTurnRate = RuleMath.limitGunTurnRate(immuBotIntent.getGunTurnRate());
					double limitedRadarTurnRate = RuleMath.limitRadarTurnRate(immuBotIntent.getRadarTurnRate());

					double totalTurnRate = limitedTurnRate;
					double direction = normalAbsoluteDegrees(botBuilder.getDirection() + totalTurnRate);

					// Gun direction depends on the turn rate of both the body and the gun
					totalTurnRate += limitedGunTurnRate;
					double gunDirection = normalAbsoluteDegrees(botBuilder.getGunDirection() + totalTurnRate);

					// Radar direction depends on the turn rate of the body, the gun, and the radar
					totalTurnRate += limitedRadarTurnRate;
					double radarDirection = normalAbsoluteDegrees(botBuilder.getRadarDirection() + totalTurnRate);

					botBuilder.direction(direction);
					botBuilder.gunDirection(gunDirection);
					botBuilder.radarDirection(radarDirection);
					botBuilder.radarSpreadAngle(limitedRadarTurnRate);
					botBuilder.speed(speed);

					botBuilder.moveToNewPosition();
				}
			}
		}
	}

	/**
	 * Check bullet hits
	 */
	private void checkBulletHits() {
		final Line[] boundingLines = new Line[bullets.size()];

		Bullet[] bulletArray = new Bullet[bullets.size()];
		bulletArray = bullets.toArray(bulletArray);

		for (int i = boundingLines.length - 1; i >= 0; i--) {
			Bullet bullet = bulletArray[i];
			if (bullet == null) {
				continue;
			}

			Line line = new Line();
			line.start = bullet.calcPosition();
			line.end = bullet.calcNextPosition();

			boundingLines[i] = line;
		}

		for (int i = boundingLines.length - 1; i >= 0; i--) {

			// Check bullet-bullet collision
			Line line1 = boundingLines[i];
			if (line1 == null) {
				continue;
			}

			Point endPos1 = line1.end;
			for (int j = i - 1; j >= 0; j--) {
				Line line2 = boundingLines[j];
				if (line2 == null) {
					continue;
				}

				Point endPos2 = line2.end;

				// Check if the bullets bounding circles intersects (is fast) before checking if the bullets bounding
				// lines intersect (is slower)
				if (isBulletsMaxBoundingCirclesColliding(endPos1, endPos2) && MathUtil.isLineIntersectingLine(
						boundingLines[i].start, boundingLines[i].end, boundingLines[j].start, boundingLines[j].end)) {

					Bullet bullet1 = bulletArray[i];
					Bullet bullet2 = bulletArray[j];

					BulletHitBulletEvent bulletHitBulletEvent1 = new BulletHitBulletEvent(turnNumber, bullet1, bullet2);
					BulletHitBulletEvent bulletHitBulletEvent2 = new BulletHitBulletEvent(turnNumber, bullet2, bullet1);

					turnBuilder.addPrivateBotEvent(bullet1.getBotId(), bulletHitBulletEvent1);
					turnBuilder.addPrivateBotEvent(bullet2.getBotId(), bulletHitBulletEvent2);

					// Observers only need a single event
					turnBuilder.addObserverEvent(bulletHitBulletEvent1);

					// Remove bullets from the arena
					bullets.remove(bulletArray[i]);
					bullets.remove(bulletArray[j]);
				}
			}

			// Check bullet-hit-bot collision (hit)

			Point startPos1 = boundingLines[i].start;

			for (BotBuilder botBuilder : botBuilderMap.values()) {
				double botX = botBuilder.getX();
				double botY = botBuilder.getY();

				Bullet bullet = bulletArray[i];

				int botId = bullet.getBotId();
				int victimId = botBuilder.getId();

				if (botId == victimId) {
					continue; // A bot cannot shot itself
				}

				if (MathUtil.isLineIntersectingCircle(startPos1.x, startPos1.y, endPos1.x, endPos1.y, botX, botY, BOT_BOUNDING_CIRCLE_RADIUS)) {

					inactivityCounter = 0; // reset collective inactivity counter due to bot taking bullet damage

					double damage = RuleMath.calcBulletDamage(bullet.getPower());
					boolean killed = botBuilder.addDamage(damage);

					double energyBonus = RuleConstants.BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.getPower();
					BotBuilder enemyBotBuilder = botBuilderMap.get(botId);
					if (enemyBotBuilder != null) {
						enemyBotBuilder.changeEnergy(energyBonus);
					}
					scoreTracker.registerBulletHit(botId, victimId, damage, killed);

					BulletHitBotEvent bulletHitBotEvent = new BulletHitBotEvent(turnNumber, bullet, victimId, damage, botBuilder.getEnergy());
					turnBuilder.addPrivateBotEvent(botId, bulletHitBotEvent); // Bot itself gets event
					turnBuilder.addPrivateBotEvent(victimId, bulletHitBotEvent); // Victim bot gets event too
					turnBuilder.addObserverEvent(bulletHitBotEvent);

					// Remove bullet from the arena
					bullets.remove(bullet);
				}
			}
		}
	}

	/** Maximum bounding circle diameter of a bullet moving with max speed */
	private static final double BULLET_MAX_BOUNDING_CIRCLE_DIAMETER = 2 * MAX_BULLET_SPEED;
	/** Square of maximum bounding circle diameter of a bullet moving with max speed */
	private static final double BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED = BULLET_MAX_BOUNDING_CIRCLE_DIAMETER
			* BULLET_MAX_BOUNDING_CIRCLE_DIAMETER;

	/**
	 * Checks if the maximum bounding circles of two bullets are colliding. This is a pre-check if two bullets might be
	 * colliding.
	 *
	 * @param bullet1Position
	 *            is the position of the 1st bullet
	 * @param bullet2Position
	 *            is the position of the 2nd bullet
	 * @return true if the bounding circles are colliding; false otherwise
	 */
	private static boolean isBulletsMaxBoundingCirclesColliding(Point bullet1Position, Point bullet2Position) {
		double dx = bullet2Position.x - bullet1Position.x;
		if (Math.abs(dx) > BULLET_MAX_BOUNDING_CIRCLE_DIAMETER) {
			return false;
		}
		double dy = bullet2Position.y - bullet1Position.y;
		return !(Math.abs(dy) > BULLET_MAX_BOUNDING_CIRCLE_DIAMETER) && ((dx * dx) + (dy * dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED);
	}

	/**
	 * Check collisions between bots
	 */
	private void checkBotCollisions() {

		BotBuilder[] botBuilderArray = new BotBuilder[botBuilderMap.size()];
		botBuilderArray = botBuilderMap.values().toArray(botBuilderArray);

		for (int i = botBuilderArray.length - 1; i >= 0; i--) {
			double bot1x = botBuilderArray[i].getX();
			double bot1y = botBuilderArray[i].getY();

			for (int j = i - 1; j >= 0; j--) {
				double bot2x = botBuilderArray[j].getX();
				double bot2y = botBuilderArray[j].getY();

				if (isBotsBoundingCirclesColliding(bot1x, bot1y, bot2x, bot2y)) {
					final double overlapDist = BOT_BOUNDING_CIRCLE_DIAMETER - MathUtil.distance(bot1x, bot1y, bot2x, bot2y);

					final BotBuilder botBuilder1 = botBuilderArray[i];
					final BotBuilder botBuilder2 = botBuilderArray[j];

					final int botId1 = botBuilder1.getId();
					final int botId2 = botBuilder2.getId();

					final boolean bot1Killed = botBuilder1.addDamage(RAM_DAMAGE);
					final boolean bot2Killed = botBuilder2.addDamage(RAM_DAMAGE);

					final boolean bot1RammedBot2 = isRamming(botBuilder1, botBuilder2);
					final boolean bot2rammedBot1 = isRamming(botBuilder2, botBuilder1);

					if (bot1RammedBot2) {
						scoreTracker.registerRamHit(botId2, botId1, RAM_DAMAGE, bot1Killed);
					}
					if (bot2rammedBot1) {
						scoreTracker.registerRamHit(botId1, botId2, RAM_DAMAGE, bot2Killed);
					}
					double totalSpeed = botBuilder1.getSpeed() + botBuilder2.getSpeed();

					double bot1BounceDist;
					double bot2BounceDist;

					if (totalSpeed == 0.0) {
						bot1BounceDist = overlapDist / 2;
						bot2BounceDist = overlapDist / 2;

					} else {
						double t = overlapDist / totalSpeed;

						// The faster speed, the less bounce distance. Hence the speeds for the bots are swapped0
						bot1BounceDist = botBuilder2.getSpeed() * t;
						bot2BounceDist = botBuilder1.getSpeed() * t;
					}

					double oldBot1X = botBuilder1.getX();
					double oldBot1Y = botBuilder1.getY();
					double oldBot2X = botBuilder2.getX();
					double oldBot2Y = botBuilder2.getY();

					botBuilder1.bounceBack(bot1BounceDist);
					botBuilder2.bounceBack(bot2BounceDist);

					double newBot1X = botBuilder1.getX();
					double newBot1Y = botBuilder1.getY();
					double newBot2X = botBuilder2.getX();
					double newBot2Y = botBuilder2.getY();

					// Check if one of the bot bounced into a wall

					if (newBot1X < BOT_BOUNDING_CIRCLE_RADIUS || newBot1Y < BOT_BOUNDING_CIRCLE_RADIUS ||
							newBot1X > (setup.getArenaWidth() - BOT_BOUNDING_CIRCLE_RADIUS) ||
							newBot1Y > (setup.getArenaHeight() - BOT_BOUNDING_CIRCLE_RADIUS)) {

						botBuilder1.x(oldBot1X);
						botBuilder1.y(oldBot1Y);

						botBuilder2.bounceBack(bot1BounceDist /* remaining distance */);
					}

					if (newBot2X < BOT_BOUNDING_CIRCLE_RADIUS || newBot2Y < BOT_BOUNDING_CIRCLE_RADIUS ||
							newBot2X > (setup.getArenaWidth() - BOT_BOUNDING_CIRCLE_RADIUS) ||
							newBot2Y > (setup.getArenaHeight() - BOT_BOUNDING_CIRCLE_RADIUS)) {

						botBuilder2.x(oldBot2X);
						botBuilder2.y(oldBot2Y);

						botBuilder1.bounceBack(bot2BounceDist /* remaining distance */);
					}

					if (bot1RammedBot2) {
            			botBuilder1.speed(0);
					}
					if (bot2rammedBot1) {
            			botBuilder2.speed(0);
					}

					BotHitBotEvent botHitBotEvent1 = new BotHitBotEvent(turnNumber, botId1, botId2, botBuilder2.getEnergy(), botBuilder2.getX(), botBuilder2.getY(), bot1RammedBot2);
					BotHitBotEvent botHitBotEvent2 = new BotHitBotEvent(turnNumber, botId2, botId1, botBuilder1.getEnergy(), botBuilder1.getX(), botBuilder1.getY(), bot2rammedBot1);

					turnBuilder.addPrivateBotEvent(botId1, botHitBotEvent1);
					turnBuilder.addPrivateBotEvent(botId2, botHitBotEvent2);

					turnBuilder.addObserverEvent(botHitBotEvent1);
					turnBuilder.addObserverEvent(botHitBotEvent2);

					break;
				}
			}
		}
	}

	/** Square of the bounding circle diameter of a bot */
	private static final double BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED = (double) BOT_BOUNDING_CIRCLE_DIAMETER
			* BOT_BOUNDING_CIRCLE_DIAMETER;

	/**
	 * Checks if the bounding circles of two bots are colliding.
	 *
	 * @param bot1x
	 *            is the x coordinate of the 1st bot
	 * @param bot1y
	 *            is the y coordinate of the 1st bot
	 * @param bot2x
	 *            is the x coordinate of the 2nd bot
	 * @param bot2y
	 *            is the y coordinate of the 2nd bot
	 * @return true if the bounding circles are colliding; false otherwise
	 */
	private static boolean isBotsBoundingCirclesColliding(double bot1x, double bot1y, double bot2x, double bot2y) {
		double dx = bot2x - bot1x;
		if (Math.abs(dx) > BOT_BOUNDING_CIRCLE_DIAMETER) { // 2 x radius
			return false;
		}
		double dy = bot2y - bot1y;
		// 2 x radius
		return !(Math.abs(dy) > BOT_BOUNDING_CIRCLE_DIAMETER) && ((dx * dx) + (dy * dy) <= BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED);
	}

	/**
	 * Checks if a bot is ramming another bot
	 *
	 * @param bot
	 *            is the bot the attempts ramming
	 * @param victim
	 *            is the victim bot
	 * @return true if the bot is ramming; false otherwise
	 */
	private static boolean isRamming(BotBuilder bot, BotBuilder victim) {

		double dx = victim.getX() - bot.getX();
		double dy = victim.getY() - bot.getY();

		double angle = Math.atan2(dy, dx);

		double bearing = MathUtil.normalRelativeDegrees(Math.toDegrees(angle) - bot.getDirection());

		return ((bot.getSpeed() > 0 && (bearing > -90 && bearing < 90))
				|| (bot.getSpeed() < 0 && (bearing < -90 || bearing > 90)));
	}

	/**
	 * Updates bullet positions
	 */
	private void updateBulletPositions() {

		Set<Bullet> newBulletSet = new HashSet<>();

		for (Bullet bullet : bullets) {
			// The tick is used to calculate new position by calling getPosition()
			Bullet updatedBullet = bullet.toBuilder().tick(bullet.getTick() + 1).build();

			newBulletSet.add(updatedBullet);
		}

		bullets.clear();
		bullets.addAll(newBulletSet);
	}

	/**
	 * Chekcs collisions between bots and the walls
	 */
	private void checkBotWallCollisions() {

		for (BotBuilder botBuilder : botBuilderMap.values()) {

			double x = botBuilder.getX();
			double y = botBuilder.getY();

			if (previousTurn != null) {
				Bot prevBotState = previousTurn.getBot(botBuilder.getId());
				if (prevBotState == null) {
					continue;
				}
				double oldX = prevBotState.getX();
				double oldY = prevBotState.getY();
				double dx = x - oldX;
				double dy = y - oldY;

				boolean hitWall = false;

				if (x - BOT_BOUNDING_CIRCLE_RADIUS < 0) {
					hitWall = true;

					x = BOT_BOUNDING_CIRCLE_RADIUS;

					if (dx != 0) {
						double dxCut = x - oldX;
						y = oldY + (dxCut * dy / dx);
					}
				} else if (x + BOT_BOUNDING_CIRCLE_RADIUS > setup.getArenaWidth()) {
					hitWall = true;

					x = (double) setup.getArenaWidth() - BOT_BOUNDING_CIRCLE_RADIUS;

					if (dx != 0) {
						double dxCut = x - oldX;
						y = oldY + (dxCut * dy / dx);
					}
				} else if (y - BOT_BOUNDING_CIRCLE_RADIUS < 0) {
					hitWall = true;

					y = BOT_BOUNDING_CIRCLE_RADIUS;

					if (dy != 0) {
						double dyCut = y - oldY;
						x = oldX + (dyCut * dx / dy);
					}
				} else if (y + BOT_BOUNDING_CIRCLE_RADIUS > setup.getArenaHeight()) {
					hitWall = true;

					y = (double) setup.getArenaHeight() - BOT_BOUNDING_CIRCLE_RADIUS;

					if (dy != 0) {
						double dyCut = y - oldY;
						x = oldX + (dyCut * dx / dy);
					}
				}

				if (hitWall) {
					botBuilder.x(x);
					botBuilder.y(y);

					// Skip this check, if the bot hit the wall in the previous turn
					if (previousTurn.getBotEvents(botBuilder.getId()).stream().noneMatch(e -> e instanceof BotHitWallEvent)) {

						BotHitWallEvent botHitWallEvent = new BotHitWallEvent(turnNumber, botBuilder.getId());
						turnBuilder.addPrivateBotEvent(botBuilder.getId(), botHitWallEvent);
						turnBuilder.addObserverEvent(botHitWallEvent);

						double damage = RuleMath.calcWallDamage(botBuilder.getSpeed());
						botBuilder.addDamage(damage);
					}

					// Bot is stopped to zero speed regardless of its previous direction
					botBuilder.speed(0);
				}
			}
		}
	}

	/**
	 * Checks collisions between the bullets and the walls
	 */
	private void checkBulletWallCollisions() {
		Iterator<Bullet> iterator = bullets.iterator(); // due to removal
		while (iterator.hasNext()) {
			Bullet bullet = iterator.next();
			Point position = bullet.calcPosition();

			if ((position.x <= 0) || (position.x >= setup.getArenaWidth()) || (position.y <= 0)
					|| (position.y >= setup.getArenaHeight())) {

				iterator.remove(); // remove bullet from arena

				BulletHitWallEvent bulletHitWallEvent = new BulletHitWallEvent(turnNumber, bullet);
				turnBuilder.addPrivateBotEvent(bullet.getBotId(), bulletHitWallEvent);
				turnBuilder.addObserverEvent(bulletHitWallEvent);
			}
		}
	}

	/**
	 * Check if the bots are inactive collectively. That is when no bot have been hit by bullets for some time
	 */
	private void checkInactivity() {
		if (inactivityCounter++ > setup.getInactivityTurns()) {
			botBuilderMap.values().forEach(bot -> bot.addDamage(INACTIVITY_DAMAGE));
		}
	}

	/**
	 * Check if the bots have been disabled (when energy is zero or close to zero)
	 */
	private void checkForDisabledBots() {
		botBuilderMap.values().forEach(bot -> {
			if (bot.getEnergy() < 0.01 && bot.getEnergy() > 0) {
				bot.energy(0);
			}
			// If bot is disabled => Set then reset all bot intent values to zeros
			if (bot.getEnergy() == 0) {
				botIntentsMap.put(bot.getId(), BotIntent.builder().build().zerofied());
			}
		});
	}

	/**
	 * Checks if any bots have been defeated
	 */
	private void checkForDefeatedBots() {
		for (BotBuilder botBuilder : botBuilderMap.values()) {
			if (botBuilder.isDead()) {
				int victimId = botBuilder.getId();

				BotDeathEvent botDeathEvent = new BotDeathEvent(turnNumber, victimId);
				turnBuilder.addPublicBotEvent(botDeathEvent);
				turnBuilder.addObserverEvent(botDeathEvent);

				scoreTracker.registerBotDeath(victimId);
			}
		}
	}

	/**
	 * Cool down and fire guns
	 */
	private void cooldownAndFireGuns() {
		for (BotBuilder botBuilder : botBuilderMap.values()) {

			// Bot cannot fire if it is disabled
			if (!botBuilder.isDisabled()) {

				// Fire gun, if the gun heat is zero
				double gunHeat = botBuilder.getGunHeat();
				if (gunHeat == 0) {
					// Gun can fire => Check if intent is to fire gun
					BotIntent botIntent = botIntentsMap.get(botBuilder.getId());
					if (botIntent != null) {
						double firepower = botIntent.zerofied().getBulletPower();
						if (firepower >= MIN_FIREPOWER) {
							fireBullet(botBuilder, firepower);
						}
					}
				} else {
					// Gun is too hot => Cool down gun
					gunHeat = Math.max(gunHeat - setup.getGunCoolingRate(), 0);
					botBuilder.gunHeat(gunHeat);
				}
			}
		}
	}

	private void fireBullet(BotBuilder botBuilder, double firepower) {
		// Gun is fired
		firepower = Math.min(firepower, MAX_FIREPOWER);
		handleFiredBullet(botBuilder, firepower);
	}

	/**
	 * Handle fired bullet
	 *
	 * @param botBuilder
	 *            is the bot firing the bullet
	 * @param firepower
	 *            is the firepower of the bullet
	 */
	private void handleFiredBullet(BotBuilder botBuilder, double firepower) {
		int botId = botBuilder.getId();

		double gunHeat = RuleMath.calcGunHeat(firepower);
		botBuilder.gunHeat(gunHeat);

		Bullet bullet = Bullet.builder()
			.botId(botId)
			.bulletId(++nextBulletId)
			.power(firepower)
			.startX(botBuilder.getX())
			.startY(botBuilder.getY())
			.direction(botBuilder.getGunDirection())
			.build();

		bullets.add(bullet);

		BulletFiredEvent bulletFiredEvent = new BulletFiredEvent(turnNumber, bullet);
		turnBuilder.addPrivateBotEvent(botId, bulletFiredEvent);
		turnBuilder.addObserverEvent(bulletFiredEvent);
	}

	/**
	 * Checks the scan field for scanned bots
	 */
	private void checkScanFields() {

		BotBuilder[] botArray = new BotBuilder[botBuilderMap.size()];
		botArray = botBuilderMap.values().toArray(botArray);

		for (int i = botArray.length - 1; i >= 0; i--) {
			BotBuilder scanningBot = botArray[i];

			double spreadAngle = scanningBot.getRadarSpreadAngle();
			double scanCenterX = scanningBot.getX();
			double scanCenterY = scanningBot.getY();

			double arcStartAngle;
			double arcEndAngle;

			if (spreadAngle < 0) {
				arcStartAngle = scanningBot.getRadarDirection();
				arcEndAngle = arcStartAngle - spreadAngle;
			} else {
				arcEndAngle = scanningBot.getRadarDirection();
				arcStartAngle = arcEndAngle + spreadAngle;
			}

			for (int j = botArray.length - 1; j >= 0; j--) {
				if (i == j) {
					continue;
				}

				BotBuilder scannedBot = botArray[j];

				if (MathUtil.isCircleIntersectingCircleSector(scannedBot.getX(), scannedBot.getY(),
						BOT_BOUNDING_CIRCLE_RADIUS, scanCenterX, scanCenterY,
						RuleConstants.RADAR_RADIUS, arcStartAngle, arcEndAngle)) {

					ScannedBotEvent scannedBotEvent = new ScannedBotEvent(turnNumber, scanningBot.getId(), scannedBot.getId(),
							scannedBot.getEnergy(), scannedBot.getX(), scannedBot.getY(), scannedBot.getDirection(),
							scannedBot.getSpeed());

					turnBuilder.addPrivateBotEvent(scanningBot.getId(), scannedBotEvent);
					turnBuilder.addObserverEvent(scannedBotEvent);
				}
			}
		}
	}

	/**
	 * Checks if the round is ended or game is over
	 */
	private void checkIfRoundOrGameOver() {
		if (botBuilderMap.size() < 2) {
			// Round ended
			roundEnded = true;

			if (roundNumber >= setup.getNumberOfRounds()) {
				// Game over
				gameState = gameState.toBuilder().gameEnded(true).build();
			}
		}
	}

	/** Simple line class */
	private class Line {
		Point start;
		Point end;
	}
}
