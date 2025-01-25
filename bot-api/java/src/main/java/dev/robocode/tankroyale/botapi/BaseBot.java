package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.BotEvent;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.internal.BaseBotInternals;
import dev.robocode.tankroyale.schema.game.BotIntent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static dev.robocode.tankroyale.botapi.util.MathUtil.clamp;

/**
 * Abstract bot class that takes care of communication between the bot and the server, and sends
 * notifications through the event handlers. Most bots can inherit from this class to get access to
 * basic methods.
 */
public abstract class BaseBot implements IBaseBot {

    final BaseBotInternals baseBotInternals;

    /**
     * Constructor for initializing a new instance of the BaseBot class.
     * This constructor should be used when both {@link BotInfo} and server URL is provided through
     * environment variables, i.e., when starting up the bot using a booter. These environment
     * variables must be set to provide the server URL and bot information, and are automatically
     * set by the booter tool for Robocode.
     * <p><br>
     * Example of how to set the predefined environment variables used for connecting to the server:
     * <ul>
     * <li>{@code SERVER_URL=ws://localhost:7654}</li>
     * <li>{@code SERVER_SECRET=xzoEeVbnBe5TGjCny0R1yQ}</li>
     * </ul>
     * <p>
     * Example of how to set the environment variables that covers the {@link BotInfo}:
     * <ul>
     * <li>{@code BOT_NAME=MyBot}</li>
     * <li>{@code BOT_VERSION=1.0}</li>
     * <li>{@code BOT_AUTHORS=John Doe}</li>
     * <li>{@code BOT_DESCRIPTION=Short description}</li>
     * <li>{@code BOT_HOMEPAGE=https://somewhere.net/MyBot}</li>
     * <li>{@code BOT_COUNTRY_CODES=us}</li>
     * <li>{@code BOT_GAME_TYPES=classic, melee, 1v1}</li>
     * <li>{@code BOT_PLATFORM=JVM}</li>
     * <li>{@code BOT_PROG_LANG=Java 11}</li>
     * <li>{@code BOT_INITIAL_POS=50,70, 270}</li>
     * </ul>
     * <p>
     * These environment variables <em>must</em> be set prior to using this constructor:
     * <ul>
     * <li>{@code BOT_NAME}</li>
     * <li>{@code BOT_VERSION}</li>
     * <li>{@code BOT_AUTHORS}</li>
     * </ul>
     * <p>
     * These value can take multiple values separated by a comma:
     * <ul>
     * <li>{@code BOT_AUTHORS, e.g. "John Doe, Jane Doe"}</li>
     * <li>{@code BOT_COUNTRY_CODES, e.g. "se, no, dk"}</li>
     * <li>{@code BOT_GAME_TYPES, e.g. "classic, melee, 1v1"}</li>
     * </ul>
     * <p>
     * The {@code BOT_INITIAL_POS} variable is optional and should <em>only</em> be used for debugging.
     * <p>
     * The {@code SERVER_SECRET} must be set if the server requires a server secret for the bots trying
     * to connect. Otherwise, the bot will be disconnected as soon as it attempts to connect to
     * the server.
     * <p>
     * If the {@code SERVER_URL} is not set, then this default URL is used: ws://localhost:7654
     */
    protected BaseBot() {
        baseBotInternals = new BaseBotInternals(this, null, null, null);
    }

    /**
     * Constructor for initializing a new instance of the BaseBot class.
     * This constructor assumes the server URL and secret is provided by the environment
     * variables SERVER_URL and SERVER_SECRET.
     *
     * @param botInfo is the bot info containing information about your bot.
     */
    protected BaseBot(final BotInfo botInfo) {
        baseBotInternals = new BaseBotInternals(this, botInfo, null, null);
    }

    /**
     * Constructor for initializing a new instance of the BaseBot class.
     *
     * @param botInfo   is the bot info containing information about your bot.
     * @param serverUrl is the server URL
     */
    protected BaseBot(final BotInfo botInfo, URI serverUrl) {
        baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, null);
    }

    /**
     * Constructor for initializing a new instance of the BaseBot class.
     *
     * @param botInfo      is the bot info containing information about your bot.
     * @param serverUrl    is the server URL
     * @param serverSecret is the server secret for bots
     */
    protected BaseBot(final BotInfo botInfo, URI serverUrl, String serverSecret) {
        baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, serverSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void start() {
        baseBotInternals.start();
    }

    /**
     * {@inheritDoc}
     */
    // We allow override of go() here to let the Robocode Bridge hook into this method
    @Override
    public void go() {
        baseBotInternals.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getVariant() {
        return baseBotInternals.getVariant();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getVersion() {
        return baseBotInternals.getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMyId() {
        return baseBotInternals.getMyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getGameType() {
        return baseBotInternals.getGameSetup().getGameType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getArenaWidth() {
        return baseBotInternals.getGameSetup().getArenaWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getArenaHeight() {
        return baseBotInternals.getGameSetup().getArenaHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNumberOfRounds() {
        return baseBotInternals.getGameSetup().getNumberOfRounds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunCoolingRate() {
        return baseBotInternals.getGameSetup().getGunCoolingRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxInactivityTurns() {
        return baseBotInternals.getGameSetup().getMaxInactivityTurns();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTurnTimeout() {
        return baseBotInternals.getGameSetup().getTurnTimeout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTimeLeft() {
        return baseBotInternals.getTimeLeft();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getRoundNumber() {
        return baseBotInternals.getCurrentTickOrThrow().getRoundNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTurnNumber() {
        return baseBotInternals.getCurrentTickOrThrow().getTurnNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getEnemyCount() {
        return baseBotInternals.getCurrentTickOrThrow().getBotState().getEnemyCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getEnergy() {
        return baseBotInternals.getCurrentTickOrThrow().getBotState().getEnergy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDisabled() {
        return getEnergy() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getX() {
        var tick = baseBotInternals.getCurrentTickOrNull();
        if (tick != null) {
            return tick.getBotState().getX();
        }
        return baseBotInternals.getInitialPosition().getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getY() {
        var tick = baseBotInternals.getCurrentTickOrNull();
        if (tick != null) {
            return tick.getBotState().getY();
        }
        return baseBotInternals.getInitialPosition().getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getDirection() {
        var tick = baseBotInternals.getCurrentTickOrNull();
        if (tick != null) {
            return tick.getBotState().getDirection();
        }
        return baseBotInternals.getInitialPosition().getDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunDirection() {
        var tick = baseBotInternals.getCurrentTickOrNull();
        if (tick != null) {
            return tick.getBotState().getGunDirection();
        }
        return baseBotInternals.getInitialPosition().getDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getRadarDirection() {
        var tick = baseBotInternals.getCurrentTickOrNull();
        if (tick != null) {
            return tick.getBotState().getRadarDirection();
        }
        return baseBotInternals.getInitialPosition().getDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getSpeed() {
        return baseBotInternals.getSpeed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunHeat() {
        return baseBotInternals.getGunHeat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Collection<BulletState> getBulletStates() {
        return baseBotInternals.getBulletStates();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BotEvent> getEvents() {
        return baseBotInternals.getEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clearEvents() {
        baseBotInternals.clearEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getTurnRate() {
        return baseBotInternals.getTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*final*/ void setTurnRate(double turnRate) {
        baseBotInternals.setTurnRate(turnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxTurnRate() {
        return baseBotInternals.getMaxTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxTurnRate(double maxTurnRate) {
        baseBotInternals.setMaxTurnRate(maxTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunTurnRate() {
        return baseBotInternals.getGunTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*final*/ void setGunTurnRate(double gunTurnRate) {
        baseBotInternals.setGunTurnRate(gunTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxGunTurnRate() {
        return baseBotInternals.getMaxGunTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxGunTurnRate(double maxGunTurnRate) {
        baseBotInternals.setMaxGunTurnRate(maxGunTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getRadarTurnRate() {
        return baseBotInternals.getRadarTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*final*/ void setRadarTurnRate(double radarTurnRate) {
        baseBotInternals.setRadarTurnRate(radarTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxRadarTurnRate() {
        return baseBotInternals.getMaxRadarTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxRadarTurnRate(double maxRadarTurnRate) {
        baseBotInternals.setMaxRadarTurnRate(maxRadarTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getTargetSpeed() {
        Double targetSpeed = baseBotInternals.getBotIntent().getTargetSpeed();
        return targetSpeed == null ? 0 : targetSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*final*/ void setTargetSpeed(double targetSpeed) {
        baseBotInternals.setTargetSpeed(targetSpeed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxSpeed() {
        return baseBotInternals.getMaxSpeed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxSpeed(double maxSpeed) {
        baseBotInternals.setMaxSpeed(maxSpeed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean setFire(double firepower) {
        return baseBotInternals.setFire(firepower);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getFirepower() {
        Double firepower = baseBotInternals.getBotIntent().getFirepower();
        return firepower == null ? 0 : firepower;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRescan() {
        baseBotInternals.getBotIntent().setRescan(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setFireAssist(boolean enable) {
        baseBotInternals.getBotIntent().setFireAssist(enable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setInterruptible(boolean interruptible) {
        baseBotInternals.setInterruptible(interruptible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setAdjustGunForBodyTurn(boolean adjust) {
        baseBotInternals.getBotIntent().setAdjustGunForBodyTurn(adjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAdjustGunForBodyTurn() {
        Boolean adjust = baseBotInternals.getBotIntent().getAdjustGunForBodyTurn();
        return adjust != null && adjust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setAdjustRadarForBodyTurn(boolean adjust) {
        baseBotInternals.getBotIntent().setAdjustRadarForBodyTurn(adjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAdjustRadarForBodyTurn() {
        Boolean adjust = baseBotInternals.getBotIntent().getAdjustRadarForBodyTurn();
        return adjust != null && adjust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setAdjustRadarForGunTurn(boolean adjust) {
        BotIntent botIntent = baseBotInternals.getBotIntent();
        botIntent.setAdjustRadarForGunTurn(adjust);
        botIntent.setFireAssist(!adjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAdjustRadarForGunTurn() {
        Boolean adjust = baseBotInternals.getBotIntent().getAdjustRadarForGunTurn();
        return adjust != null && adjust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean addCustomEvent(Condition condition) {
        return baseBotInternals.addCondition(condition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean removeCustomEvent(Condition condition) {
        return baseBotInternals.removeCondition(condition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setStop() {
        baseBotInternals.setStop(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setStop(boolean overwrite) {
        baseBotInternals.setStop(overwrite);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setResume() {
        baseBotInternals.setResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isStopped() {
        return baseBotInternals.isStopped();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Integer> getTeammateIds() {
        return baseBotInternals.getTeammateIds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isTeammate(int botId) {
        return baseBotInternals.isTeammate(botId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void broadcastTeamMessage(Object message) {
        baseBotInternals.broadcastTeamMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void sendTeamMessage(int teammateId, Object message) {
        baseBotInternals.sendTeamMessage(teammateId, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getBodyColor() {
        return baseBotInternals.getBodyColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setBodyColor(Color color) {
        baseBotInternals.setBodyColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getTurretColor() {
        return baseBotInternals.getTurretColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurretColor(Color color) {
        baseBotInternals.setTurretColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getRadarColor() {
        return baseBotInternals.getRadarColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRadarColor(Color color) {
        baseBotInternals.setRadarColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getBulletColor() {
        return baseBotInternals.getBulletColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setBulletColor(Color color) {
        baseBotInternals.setBulletColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getScanColor() {
        return baseBotInternals.getScanColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setScanColor(Color color) {
        baseBotInternals.setScanColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getTracksColor() {
        return baseBotInternals.getTracksColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTracksColor(Color color) {
        baseBotInternals.setTracksColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getGunColor() {
        return baseBotInternals.getGunColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setGunColor(Color color) {
        baseBotInternals.setGunColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double calcMaxTurnRate(double speed) {
        return MAX_TURN_RATE - 0.75 * Math.abs(clamp(speed, -MAX_SPEED, MAX_SPEED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double calcBulletSpeed(double firepower) {
        return 20 - 3 * clamp(firepower, MIN_FIREPOWER, MAX_FIREPOWER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double calcGunHeat(double firepower) {
        return 1 + (clamp(firepower, MIN_FIREPOWER, MAX_FIREPOWER) / 5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getEventPriority(Class<BotEvent> eventClass) {
        return baseBotInternals.getPriority(eventClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setEventPriority(Class<BotEvent> eventClass, int priority) {
        baseBotInternals.setPriority(eventClass, priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDebuggingEnabled() {
        return baseBotInternals.getCurrentTickOrThrow().getBotState().isDebuggingEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Graphics2D getGraphics() {
        return baseBotInternals.getGraphics();
    }
}
