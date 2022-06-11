package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.BotEvent;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.internal.BaseBotInternals;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import static dev.robocode.tankroyale.botapi.Constants.MAX_TURN_RATE;

/**
 * Abstract bot class that takes care of communication between the bot and the server, and sends
 * notifications through the event handlers. Most bots can inherit from this class to get access to
 * basic methods.
 */
public abstract class BaseBot implements IBaseBot {

    final BaseBotInternals __baseBotInternals;

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
     * <li>{@code BOT_GAME_TYPES}</li>
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
    public BaseBot() {
        __baseBotInternals = new BaseBotInternals(this, null, null, null);
    }

    /**
     * Constructor for initializing a new instance of the BaseBot class.
     * This constructor assumes the server URL and secret is provided by the environment
     * variables SERVER_URL and SERVER_SECRET.
     *
     * @param botInfo is the bot info containing information about your bot.
     */
    public BaseBot(final BotInfo botInfo) {
        __baseBotInternals = new BaseBotInternals(this, botInfo, null, null);
    }

    /**
     * Constructor for initializing a new instance of the BaseBot class.
     *
     * @param botInfo   is the bot info containing information about your bot.
     * @param serverUrl is the server URL
     */
    public BaseBot(final BotInfo botInfo, URI serverUrl) {
        __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, null);
    }

    /**
     * Constructor for initializing a new instance of the BaseBot class.
     *
     * @param botInfo      is the bot info containing information about your bot.
     * @param serverUrl    is the server URL
     * @param serverSecret is the server secret for bots
     */
    public BaseBot(final BotInfo botInfo, URI serverUrl, String serverSecret) {
        __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, serverSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void start() {
        __baseBotInternals.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void go() {
        __baseBotInternals.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getVariant() {
        return __baseBotInternals.getVariant();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getVersion() {
        return __baseBotInternals.getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMyId() {
        return __baseBotInternals.getMyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getGameType() {
        return __baseBotInternals.getGameSetup().getGameType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getArenaWidth() {
        return __baseBotInternals.getGameSetup().getArenaWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getArenaHeight() {
        return __baseBotInternals.getGameSetup().getArenaHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNumberOfRounds() {
        return __baseBotInternals.getGameSetup().getNumberOfRounds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunCoolingRate() {
        return __baseBotInternals.getGameSetup().getGunCoolingRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxInactivityTurns() {
        return __baseBotInternals.getGameSetup().getMaxInactivityTurns();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTurnTimeout() {
        return __baseBotInternals.getGameSetup().getTurnTimeout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTimeLeft() {
        return __baseBotInternals.getTimeLeft();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getRoundNumber() {
        return __baseBotInternals.getCurrentTick().getRoundNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTurnNumber() {
        return __baseBotInternals.getCurrentTick().getTurnNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getEnemyCount() {
        return __baseBotInternals.getCurrentTick().getEnemyCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getEnergy() {
        return __baseBotInternals.getCurrentTick().getBotState().getEnergy();
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
        return __baseBotInternals.getCurrentTick().getBotState().getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getY() {
        return __baseBotInternals.getCurrentTick().getBotState().getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getDirection() {
        return __baseBotInternals.getCurrentTick().getBotState().getDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunDirection() {
        return __baseBotInternals.getCurrentTick().getBotState().getGunDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getRadarDirection() {
        return __baseBotInternals.getCurrentTick().getBotState().getRadarDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getSpeed() {
        return __baseBotInternals.getCurrentTick().getBotState().getSpeed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunHeat() {
        return __baseBotInternals.getCurrentTick().getBotState().getGunHeat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Collection<BulletState> getBulletStates() {
        return __baseBotInternals.getCurrentTick().getBulletStates();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BotEvent> getEvents() {
        return __baseBotInternals.getEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clearEvents() {
        __baseBotInternals.clearEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getTurnRate() {
        return __baseBotInternals.getCurrentTick().getBotState().getTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnRate(double turnRate) {
        if (Double.isNaN(turnRate)) {
            throw new IllegalArgumentException("turnRate cannot be NaN");
        }
        __baseBotInternals.getBotIntent().setTurnRate(turnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxTurnRate() {
        return __baseBotInternals.getMaxTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxTurnRate(double maxTurnRate) {
        __baseBotInternals.setMaxTurnRate(maxTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunTurnRate() {
        return __baseBotInternals.getCurrentTick().getBotState().getGunTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setGunTurnRate(double gunTurnRate) {
        if (Double.isNaN(gunTurnRate)) {
            throw new IllegalArgumentException("gunTurnRate cannot be NaN");
        }
        __baseBotInternals.getBotIntent().setGunTurnRate(gunTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxGunTurnRate() {
        return __baseBotInternals.getMaxGunTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxGunTurnRate(double maxGunTurnRate) {
        __baseBotInternals.setMaxGunTurnRate(maxGunTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getRadarTurnRate() {
        return __baseBotInternals.getCurrentTick().getBotState().getRadarTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRadarTurnRate(double radarTurnRate) {
        if (Double.isNaN(radarTurnRate)) {
            throw new IllegalArgumentException("radarTurnRate cannot be NaN");
        }
        __baseBotInternals.getBotIntent().setRadarTurnRate(radarTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxRadarTurnRate() {
        return __baseBotInternals.getMaxRadarTurnRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxRadarTurnRate(double maxRadarTurnRate) {
        __baseBotInternals.setMaxRadarTurnRate(maxRadarTurnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getTargetSpeed() {
        Double targetSpeed = __baseBotInternals.getBotIntent().getTargetSpeed();
        return targetSpeed == null ? 0 : targetSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetSpeed(double targetSpeed) {
        if (Double.isNaN(targetSpeed)) {
            throw new IllegalArgumentException("targetSpeed cannot be NaN");
        }
        __baseBotInternals.getBotIntent().setTargetSpeed(targetSpeed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getMaxSpeed() {
        return __baseBotInternals.getMaxSpeed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxSpeed(double maxSpeed) {
        __baseBotInternals.setMaxSpeed(maxSpeed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean setFire(double firepower) {
        return __baseBotInternals.setFire(firepower);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getFirepower() {
        Double firepower = __baseBotInternals.getBotIntent().getFirepower();
        return firepower == null ? 0 : firepower;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRescan() {
        __baseBotInternals.getBotIntent().setRescan(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setInterruptible(boolean interruptible) {
        __baseBotInternals.setInterruptible(interruptible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setAdjustGunForBodyTurn(boolean adjust) {
        __baseBotInternals.getBotIntent().setAdjustGunForBodyTurn(adjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAdjustGunForBodyTurn() {
        Boolean adjust = __baseBotInternals.getBotIntent().getAdjustGunForBodyTurn();
        return adjust != null && adjust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setAdjustRadarForBodyTurn(boolean adjust) {
        __baseBotInternals.getBotIntent().setAdjustRadarForBodyTurn(adjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAdjustRadarForBodyTurn() {
        Boolean adjust = __baseBotInternals.getBotIntent().getAdjustRadarForBodyTurn();
        return adjust != null && adjust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setAdjustRadarForGunTurn(boolean adjust) {
        __baseBotInternals.getBotIntent().setAdjustRadarForGunTurn(adjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAdjustRadarForGunTurn() {
        Boolean adjust = __baseBotInternals.getBotIntent().getAdjustRadarForGunTurn();
        return adjust != null && adjust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addCustomEvent(Condition condition) {
        __baseBotInternals.addCondition(condition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void removeCustomEvent(Condition condition) {
        __baseBotInternals.removeCondition(condition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStop() {
        __baseBotInternals.setStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResume() {
        __baseBotInternals.setResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStopped() {
        return __baseBotInternals.isStopped();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getBodyColor() {
        return __baseBotInternals.getCurrentTick().getBotState().getBodyColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setBodyColor(Color bodyColor) {
        __baseBotInternals.getBotIntent().setBodyColor(toIntentColor(bodyColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getTurretColor() {
        return __baseBotInternals.getCurrentTick().getBotState().getTurretColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurretColor(Color turretColor) {
        __baseBotInternals.getBotIntent().setTurretColor(toIntentColor(turretColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getRadarColor() {
        return __baseBotInternals.getCurrentTick().getBotState().getRadarColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRadarColor(Color radarColor) {
        __baseBotInternals.getBotIntent().setRadarColor(toIntentColor(radarColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getBulletColor() {
        return __baseBotInternals.getCurrentTick().getBotState().getBulletColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setBulletColor(Color bulletColor) {
        __baseBotInternals.getBotIntent().setBulletColor(toIntentColor(bulletColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getScanColor() {
        return __baseBotInternals.getCurrentTick().getBotState().getScanColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setScanColor(Color scanColor) {
        __baseBotInternals.getBotIntent().setScanColor(toIntentColor(scanColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getTracksColor() {
        return __baseBotInternals.getCurrentTick().getBotState().getTracksColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTracksColor(Color tracksColor) {
        __baseBotInternals.getBotIntent().setTracksColor(toIntentColor(tracksColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Color getGunColor() {
        return __baseBotInternals.getCurrentTick().getBotState().getGunColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setGunColor(Color gunColor) {
        __baseBotInternals.getBotIntent().setGunColor(toIntentColor(gunColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double calcMaxTurnRate(double speed) {
        return MAX_TURN_RATE - 0.75 * Math.abs(speed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double calcBulletSpeed(double firepower) {
        return 20 - 3 * firepower;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double calcGunHeat(double firepower) {
        return 1 + (firepower / 5);
    }

    private static String toIntentColor(Color color) {
        return color == null ? null : "#" + color.toHex();
    }
}
