package net.robocode2.model.controller;

import static net.robocode2.model.IRuleConstants.BOT_BOUNDING_CIRCLE_DIAMETER;
import static net.robocode2.model.IRuleConstants.BOT_BOUNDING_CIRCLE_RADIUS;
import static net.robocode2.model.IRuleConstants.INITIAL_BOT_ENERGY;
import static net.robocode2.model.IRuleConstants.INITIAL_GUN_HEAT;
import static net.robocode2.model.IRuleConstants.MAX_BULLET_POWER;
import static net.robocode2.model.IRuleConstants.MAX_BULLET_SPEED;
import static net.robocode2.model.IRuleConstants.MIN_BULLET_POWER;
import static net.robocode2.model.IRuleConstants.RAM_DAMAGE;
import static net.robocode2.util.MathUtil.normalAbsoluteDegrees;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.robocode2.game.ScoreKeeper;
import net.robocode2.model.Arena;
import net.robocode2.model.Bot;
import net.robocode2.model.Bot.BotBuilder;
import net.robocode2.model.BotIntent;
import net.robocode2.model.Bullet;
import net.robocode2.model.GameSetup;
import net.robocode2.model.GameState;
import net.robocode2.model.IRuleConstants;
import net.robocode2.model.Point;
import net.robocode2.model.Round;
import net.robocode2.model.RuleMath;
import net.robocode2.model.Score;
import net.robocode2.model.Size;
import net.robocode2.model.Turn;
import net.robocode2.model.Turn.TurnBuilder;
import net.robocode2.model.events.BotDeathEvent;
import net.robocode2.model.events.BotHitBotEvent;
import net.robocode2.model.events.BotHitWallEvent;
import net.robocode2.model.events.BulletFiredEvent;
import net.robocode2.model.events.BulletHitBotEvent;
import net.robocode2.model.events.BulletHitBulletEvent;
import net.robocode2.model.events.BulletMissedEvent;
import net.robocode2.model.events.ScannedBotEvent;
import net.robocode2.util.MathUtil;

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
	private final ScoreKeeper scoreKeeper;

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
	 * @return model updater
	 */
	private ModelUpdater(GameSetup setup, Set<Integer> participantIds) {
		this.setup = setup;
		this.participantIds = new HashSet<>(participantIds);

		this.scoreKeeper = new ScoreKeeper(participantIds);

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
			nextRound();
		}
		nextTurn();

		return buildUpdatedGameState();
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
	 * Proceed to next round
	 */
	private void nextRound() {
		roundNumber++;
		
		round = round.toBuilder().roundNumber(roundNumber).build();

		roundEnded = false;

		nextBulletId = 0;

		bullets.clear();

		initializeBotStates();

		scoreKeeper.clear();
	}

	/**
	 * Proceed to next turn
	 */
	private void nextTurn() {

		previousTurn = round.getLastTurn();

		// Reset events
		turnBuilder.resetEvents();

		turnNumber++;
		turnBuilder.turnNumber(turnNumber);

		// Remove dead bots (cannot participate in new round)
		removeDefeatedBots();

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
	 * Build updated game state
	 * 
	 * @return new game state
	 */
	private GameState buildUpdatedGameState() {
		round = round.toBuilder().turn(turnBuilder.build()).build();
		
		return gameState.toBuilder().round(round).build();
	}

	/**
	 * Initializes bot states
	 */
	private void initializeBotStates() {
		Set<Integer> occupiedCells = new HashSet<Integer>();

		for (int id : participantIds) {
			BotBuilder botBuilder = Bot.builder()
				.id(id)
				.energy(INITIAL_BOT_ENERGY)
				.speed(0)
				.position(randomBotPosition(occupiedCells))
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
		return new Point(x, y);
	}

	/**
	 * Execute bot intents
	 */
	private void executeBotIntents() {

		for (Integer botId : botBuilderMap.keySet()) {
			BotBuilder botBuilder = botBuilderMap.get(botId);

			// Bot cannot move, if it is disabled
			if (botBuilder.isDead() || botBuilder.isDisabled()) {
				continue;
			}

			BotIntent botIntent = botIntentsMap.get(botId);
			if (botIntent == null) {
				continue;
			}

			BotIntent immuBotIntent = botIntent.zerofied();
			// Turn body, gun, radar, and move bot to new position

			double speed = RuleMath.calcNewBotSpeed(botBuilder.getSpeed(), immuBotIntent.getTargetSpeed());

			double limitedTurnRate = RuleMath.limitTurnRate(immuBotIntent.getTurnRate(), speed);
			double limitedGunTurnRate = RuleMath.limitGunTurnRate(immuBotIntent.getGunTurnRate());
			double limitedRadarTurnRate = RuleMath.limitRadarTurnRate(immuBotIntent.getRadarTurnRate());

			double direction = normalAbsoluteDegrees(botBuilder.getDirection() + limitedTurnRate);
			double gunDirection = normalAbsoluteDegrees(botBuilder.getGunDirection() + limitedGunTurnRate);
			double radarDirection = normalAbsoluteDegrees(botBuilder.getRadarDirection() + limitedRadarTurnRate);

			botBuilder.direction(direction);
			botBuilder.gunDirection(gunDirection);
			botBuilder.radarDirection(radarDirection);
			botBuilder.radarSpreadAngle(limitedRadarTurnRate);
			botBuilder.speed(speed);

			botBuilder.moveToNewPosition();
		}
	}

	/**
	 * Check bullet hits
	 */
	private void checkBulletHits() {
		Line[] boundingLines = new Line[bullets.size()];

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
			Point endPos1 = boundingLines[i].end;
			for (int j = i - 1; j >= 0; j--) {
				Point endPos2 = boundingLines[j].end;

				// Check if the bullets bounding circles intersects (is fast) before checking if the bullets bounding
				// lines intersect (is slower)
				if (isBulletsMaxBoundingCirclesColliding(endPos1, endPos2) && MathUtil.isLineIntersectingLine(
						boundingLines[i].start, boundingLines[i].end, boundingLines[j].start, boundingLines[j].end)) {

					Bullet bullet1 = bulletArray[i];
					Bullet bullet2 = bulletArray[j];

					BulletHitBulletEvent bulletHitBulletEvent1 = new BulletHitBulletEvent(bullet1, bullet2);
					BulletHitBulletEvent bulletHitBulletEvent2 = new BulletHitBulletEvent(bullet2, bullet1);

					turnBuilder.addPrivateBotEvent(bullet1.getBotId(), bulletHitBulletEvent1);
					turnBuilder.addPrivateBotEvent(bullet2.getBotId(), bulletHitBulletEvent2);

					// Observers only need a single event
					turnBuilder.addObserverEvent(bulletHitBulletEvent1);

					// Remove bullets from the arena
					bullets.remove(bulletArray[i]);
					bullets.remove(bulletArray[j]);
				}
			}

			// Check bullet-bot collision (hit)

			Point startPos1 = boundingLines[i].start;

			for (BotBuilder botBuilder : botBuilderMap.values()) {
				Point botPos = botBuilder.getPosition();

				Bullet bullet = bulletArray[i];

				int botId = bullet.getBotId();
				int victimId = botBuilder.getId();

				if (botId == victimId) {
					// A bot cannot shot itself. The bullet must leave the cannon before it counts
					continue;
				}

				if (MathUtil.isLineIntersectingCircle(startPos1.x, startPos1.y, endPos1.x, endPos1.y, botPos.x, botPos.y, BOT_BOUNDING_CIRCLE_RADIUS)) {

					double damage = RuleMath.calcBulletDamage(bullet.getPower());
					boolean killed = botBuilder.addDamage(damage);

					double energyBonus = IRuleConstants.BULLET_HIT_ENERGY_GAIN_FACTOR * bullet.getPower();
					botBuilderMap.get(botId).changeEnergy(energyBonus);

					scoreKeeper.registerBulletHit(botId, victimId, damage, killed);

					BulletHitBotEvent bulletHitBotEvent = new BulletHitBotEvent(bullet, victimId, damage, botBuilder.getEnergy());

					turnBuilder.addPrivateBotEvent(botId, bulletHitBotEvent);
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
		if (Math.abs(dy) > BULLET_MAX_BOUNDING_CIRCLE_DIAMETER) {
			return false;
		}
		return ((dx * dx) + (dy * dy) <= BULLET_MAX_BOUNDING_CIRCLE_DIAMETER_SQUARED);
	}

	/**
	 * Check collisions between bots
	 */
	private void checkBotCollisions() {

		BotBuilder[] botBuilderArray = new BotBuilder[botBuilderMap.size()];
		botBuilderArray = botBuilderMap.values().toArray(botBuilderArray);

		for (int i = botBuilderArray.length - 1; i >= 0; i--) {
			Point pos1 = botBuilderArray[i].getPosition();

			for (int j = i - 1; j >= 0; j--) {
				Point pos2 = botBuilderArray[j].getPosition();

				if (isBotsBoundingCirclesColliding(pos1, pos2)) {
					final double overlapDist = BOT_BOUNDING_CIRCLE_DIAMETER - MathUtil.distance(pos1, pos2);

					final BotBuilder botBuilder1 = botBuilderArray[i];
					final BotBuilder botBuilder2 = botBuilderArray[j];

					final int botId1 = botBuilder1.getId();
					final int botId2 = botBuilder2.getId();

					final boolean bot1Killed = botBuilder1.addDamage(RAM_DAMAGE);
					final boolean bot2Killed = botBuilder2.addDamage(RAM_DAMAGE);

					final boolean bot1RammedBot2 = isRamming(botBuilder1, botBuilder2);
					final boolean bot2rammedBot1 = isRamming(botBuilder2, botBuilder1);

					double bot1BounceDist = 0;
					double bot2BounceDist = 0;

					if (bot1RammedBot2) {
						botBuilder1.speed(0);
						bot1BounceDist = overlapDist;
						scoreKeeper.registerRamHit(botId2, botId1, RAM_DAMAGE, bot1Killed);
					}
					if (bot2rammedBot1) {
						botBuilder2.speed(0);
						bot2BounceDist = overlapDist;
						scoreKeeper.registerRamHit(botId1, botId2, RAM_DAMAGE, bot2Killed);
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

					BotHitBotEvent BotHitBotEvent1 = new BotHitBotEvent(botId1, botId2, botBuilder2.getEnergy(), botBuilder2.getPosition(), bot1RammedBot2);
					BotHitBotEvent BotHitBotEvent2 = new BotHitBotEvent(botId2, botId1, botBuilder1.getEnergy(), botBuilder1.getPosition(), bot2rammedBot1);

					turnBuilder.addPrivateBotEvent(botId1, BotHitBotEvent1);
					turnBuilder.addPrivateBotEvent(botId2, BotHitBotEvent2);

					turnBuilder.addObserverEvent(BotHitBotEvent1);
					turnBuilder.addObserverEvent(BotHitBotEvent2);
				}
			}
		}
	}

	/** Square of the bounding circle diameter of a bot */
	private static final double BOT_BOUNDING_CIRCLE_DIAMETER_SQUARED = BOT_BOUNDING_CIRCLE_DIAMETER
			* BOT_BOUNDING_CIRCLE_DIAMETER;

	/**
	 * Checks if the bounding circles of two bots are colliding.
	 * 
	 * @param bot1Position
	 *            is the position of the 1st bot
	 * @param bot2Position
	 *            is the position of the 2nd bot
	 * @return true if the bounding circles are colliding; false otherwise
	 */
	private static boolean isBotsBoundingCirclesColliding(Point bot1Position, Point bot2Position) {
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

		double dx = victim.getPosition().x - bot.getPosition().x;
		double dy = victim.getPosition().y - bot.getPosition().y;

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

			Point position = botBuilder.getPosition();
			double x = position.x;
			double y = position.y;

			if (previousTurn == null) {
				continue;
			}
			Bot prevBotState = previousTurn.getBot(botBuilder.getId());
			if (prevBotState == null) {
				continue;
			}
			Point oldPosition = prevBotState.getPosition();
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
				botBuilder.position(new Point(x, y));

				// Skip this check, if the bot hit the wall in the previous turn
				if (previousTurn.getBotEvents(botBuilder.getId()).stream().anyMatch(e -> e instanceof BotHitWallEvent)) {
					continue;
				}

				BotHitWallEvent botHitWallEvent = new BotHitWallEvent(botBuilder.getId());
				turnBuilder.addPrivateBotEvent(botBuilder.getId(), botHitWallEvent);
				turnBuilder.addObserverEvent(botHitWallEvent);

				double damage = RuleMath.calcWallDamage(botBuilder.getSpeed());
				botBuilder.addDamage(damage);
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

				BulletMissedEvent bulletMissedEvent = new BulletMissedEvent(bullet);
				turnBuilder.addPrivateBotEvent(bullet.getBotId(), bulletMissedEvent);
				turnBuilder.addObserverEvent(bulletMissedEvent);
			}
		}
	}

	/**
	 * Checks if any bots have been defeated
	 */
	private void checkForDefeatedBots() {
		for (BotBuilder botBuilder : botBuilderMap.values()) {
			if (botBuilder.isDead()) {
				int victimId = botBuilder.getId();

				BotDeathEvent botDeathEvent = new BotDeathEvent(victimId);
				turnBuilder.addPublicBotEvent(botDeathEvent);
				turnBuilder.addObserverEvent(botDeathEvent);
			}
		}
	}

	/**
	 * Removes defeated bots
	 */
	private void removeDefeatedBots() {
		Iterator<BotBuilder> iterator = botBuilderMap.values().iterator(); // due to removal
		while (iterator.hasNext()) {
			BotBuilder botBuilder = iterator.next();
			if (botBuilder.isDead()) {
				iterator.remove(); // remove bot from arena
			}
		}
	}

	/**
	 * Cool down and fire guns
	 */
	private void cooldownAndFireGuns() {
		for (BotBuilder botBuilder : botBuilderMap.values()) {

			// Bot cannot fire if it is disabled
			if (botBuilder.isDead() || botBuilder.isDisabled()) {
				continue;
			}

			// Fire gun, if the gun heat is zero
			double gunHeat = botBuilder.getGunHeat();
			if (gunHeat == 0) {
				// Gun can fire => Check if intent is to fire gun
				BotIntent botIntent = botIntentsMap.get(botBuilder.getId());
				if (botIntent == null) {
					continue;
				}
				double firepower = botIntent.zerofied().getBulletPower();
				if (firepower >= MIN_BULLET_POWER) {
					// Gun is fired
					firepower = Math.min(firepower, MAX_BULLET_POWER);
					handleFiredBullet(botBuilder, firepower);
				}
			} else {
				// Gun is too hot => Cool down gun
				gunHeat = Math.max(gunHeat - setup.getGunCoolingRate(), 0);
				botBuilder.gunHeat(gunHeat);
			}
		}
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
			.firePosition(botBuilder.getPosition())
			.direction(botBuilder.getGunDirection())
			.build();
		
		bullets.add(bullet);

		BulletFiredEvent bulletFiredEvent = new BulletFiredEvent(bullet);
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
			Point scanCenter = scanningBot.getPosition();

			double arcStartAngle, arcEndAngle;
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

				if (MathUtil.isCircleIntersectingCircleSector(scannedBot.getPosition(), BOT_BOUNDING_CIRCLE_RADIUS,
						scanCenter, IRuleConstants.RADAR_RADIUS, arcStartAngle, arcEndAngle)) {

					ScannedBotEvent scannedBotEvent = new ScannedBotEvent(scanningBot.getId(), scannedBot.getId(),
							scannedBot.getEnergy(), scannedBot.getPosition(), scannedBot.getDirection(),
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
		if (botBuilderMap.size() <= 1) {
			// Round ended
			roundEnded = true;

			if (roundNumber == setup.getNumberOfRounds()) {
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
