package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.internal.BotInternals;

import java.net.URI;

/**
 * Abstract bot class provides convenient methods for movement, turning, and firing the gun. Most
 * bots should inherit from this class.
 */
public abstract class Bot extends BaseBot implements IBot {

    private final BotInternals botInternals = new BotInternals(this, super.baseBotInternals);

    /**
     * @see BaseBot#BaseBot()
     */
    protected Bot() {
        super();
    }

    /**
     * @see BaseBot#BaseBot(BotInfo)
     */
    protected Bot(final BotInfo botInfo) {
        super(botInfo);
    }

    /**
     * @see BaseBot#BaseBot(BotInfo, URI)
     */
    protected Bot(final BotInfo botInfo, URI serverUrl) {
        super(botInfo, serverUrl);
    }

    /**
     * @see BaseBot#BaseBot(BotInfo, URI, String)
     */
    protected Bot(final BotInfo botInfo, URI serverUrl, String serverSecret) {
        super(botInfo, serverUrl, serverSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnRate(double turnRate) {
        botInternals.setTurnRate(turnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setGunTurnRate(double turnRate) {
        botInternals.setGunTurnRate(turnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRadarTurnRate(double turnRate) {
        botInternals.setRadarTurnRate(turnRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isRunning() {
        return baseBotInternals.isRunning();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTargetSpeed(double targetSpeed) {
        botInternals.setTargetSpeed(targetSpeed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setForward(double distance) {
        botInternals.setForward(distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void forward(double distance) {
        botInternals.forward(distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setBack(double distance) {
        setForward(-distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void back(double distance) {
        forward(-distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getDistanceRemaining() {
        return botInternals.getDistanceRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnLeft(double degrees) {
        botInternals.setTurnLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnLeft(double degrees) {
        botInternals.turnLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnRight(double degrees) {
        setTurnLeft(-degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnRight(double degrees) {
        turnLeft(-degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getTurnRemaining() {
        return botInternals.getTurnRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnGunLeft(double degrees) {
        botInternals.setTurnGunLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnGunLeft(double degrees) {
        botInternals.turnGunLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnGunRight(double degrees) {
        setTurnGunLeft(-degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnGunRight(double degrees) {
        turnGunLeft(-degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getGunTurnRemaining() {
        return botInternals.getGunTurnRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnRadarLeft(double degrees) {
        botInternals.setTurnRadarLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnRadarLeft(double degrees) {
        botInternals.turnRadarLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnRadarRight(double degrees) {
        setTurnRadarLeft(-degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnRadarRight(double degrees) {
        turnRadarLeft(-degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getRadarTurnRemaining() {
        return botInternals.getRadarTurnRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void fire(double firepower) {
        botInternals.fire(firepower);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void stop() {
        botInternals.stop(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void stop(boolean overwrite) {
        botInternals.stop(overwrite);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void resume() {
        botInternals.resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void rescan() {
        botInternals.rescan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void waitFor(Condition condition) {
        botInternals.waitFor(condition::test);
    }
}
