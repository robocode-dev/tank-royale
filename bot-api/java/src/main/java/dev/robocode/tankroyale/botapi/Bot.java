package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.internal.BotInternals;

import java.net.URI;

/**
 * Abstract bot class provides convenient methods for movement, turning, and firing the gun. Most
 * bots should inherit from this class.
 */
public abstract class Bot extends BaseBot implements IBot {

    private final BotInternals __botInternals = new BotInternals(this, super.__baseBotInternals);

    /**
     * @see BaseBot#BaseBot()
     */
    public Bot() {
        super();
    }

    /**
     * @see BaseBot#BaseBot(BotInfo)
     */
    public Bot(final BotInfo botInfo) {
        super(botInfo);
    }

    /**
     * @see BaseBot#BaseBot(BotInfo, URI)
     */
    public Bot(final BotInfo botInfo, URI serverUrl) {
        super(botInfo, serverUrl);
    }

    /**
     * @see BaseBot#BaseBot(BotInfo, URI, String)
     */
    public Bot(final BotInfo botInfo, URI serverUrl, String serverSecret) {
        super(botInfo, serverUrl, serverSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isRunning() {
        return __botInternals.isRunning();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetSpeed(double targetSpeed) {
        __botInternals.setTargetSpeed(targetSpeed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setForward(double distance) {
        __botInternals.setForward(distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void forward(double distance) {
        __botInternals.forward(distance);
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
        return __botInternals.getDistanceRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnLeft(double degrees) {
        __botInternals.setTurnLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnLeft(double degrees) {
        __botInternals.turnLeft(degrees);
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
        return __botInternals.getTurnRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnGunLeft(double degrees) {
        __botInternals.setTurnGunLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnGunLeft(double degrees) {
        __botInternals.turnGunLeft(degrees);
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
        return __botInternals.getGunTurnRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setTurnRadarLeft(double degrees) {
        __botInternals.setTurnRadarLeft(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void turnRadarLeft(double degrees) {
        __botInternals.turnRadarLeft(degrees);
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
        return __botInternals.getRadarTurnRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void fire(double firepower) {
        __botInternals.fire(firepower);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        __botInternals.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume() {
        __botInternals.resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rescan() {
        __botInternals.rescan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitFor(Condition condition) {
        __botInternals.waitFor(condition);
    }
}
