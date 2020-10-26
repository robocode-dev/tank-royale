package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.Event;

import java.net.URI;
import java.util.Collection;

/**
 * Abstract bot class that takes care of communication between the bot and the server, and sends
 * notifications through the event handlers. Most bots can inherit from this class to get access to
 * basic methods.
 */
public abstract class BaseBot implements IBaseBot {

  final BaseBotInternals __baseBotInternals;

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * both BotInfo and server URL is provided through environment variables, i.e., when starting up
   * the bot using a bootstrap. These environment variables must be set to provide the server URL
   * and bot information, and are automatically set by the bootstrap tool for Robocode.
   *
   * <p><b>Example of how to set the predefined environment variables:</b>
   *
   * <p>ROBOCODE_SERVER_URL=ws://localhost<br>
   * BOT_NAME=MyBot<br>
   * BOT_VERSION=1.0<br>
   * BOT_AUTHOR=fnl<br>
   * BOT_DESCRIPTION=Sample bot<br>
   * BOT_URL=https://mybot.somewhere.net
   * BOT_COUNTRY_CODE=DK<br>
   * BOT_GAME_TYPES=melee,1v1<br>
   * BOT_PLATFORM=Java<br>
   * BOT_PROG_LANG=Java 8<br>
   */
  public BaseBot() {
    __baseBotInternals = new BaseBotInternals(this,null, null);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * server URL is provided through the environment variable ROBOCODE_SERVER_URL.
   *
   * @param botInfo is the bot info containing information about your bot.
   */
  public BaseBot(final BotInfo botInfo) {
    __baseBotInternals = new BaseBotInternals(this, botInfo, null);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used
   * providing both the bot information and server URL for your bot.
   *
   * @param botInfo is the bot info containing information about your bot.
   * @param serverUrl is the server URL
   */
  public BaseBot(final BotInfo botInfo, URI serverUrl) {
    __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl);
  }

  /** {@inheritDoc} */
  @Override
  public final void start() {
    __baseBotInternals.connect();
  }

  /** {@inheritDoc} */
  @Override
  public final void go() {
    // Send the bot intent to the server
    __baseBotInternals.sendIntent();
  }

  /** {@inheritDoc} */
  @Override
  public final String getVariant() {
    return __baseBotInternals.getServerHandshake().getVariant();
  }

  /** {@inheritDoc} */
  @Override
  public final String getVersion() {
    return __baseBotInternals.getServerHandshake().getVersion();
  }

  /** {@inheritDoc} */
  @Override
  public final int getMyId() {
    return __baseBotInternals.getMyId();
  }

  /** {@inheritDoc} */
  @Override
  public final String getGameType() {
    return __baseBotInternals.getGameSetup().getGameType();
  }

  /** {@inheritDoc} */
  @Override
  public final int getArenaWidth() {
    return __baseBotInternals.getGameSetup().getArenaWidth();
  }

  /** {@inheritDoc} */
  @Override
  public final int getArenaHeight() {
    return __baseBotInternals.getGameSetup().getArenaHeight();
  }

  /** {@inheritDoc} */
  @Override
  public final int getNumberOfRounds() {
    return __baseBotInternals.getGameSetup().getNumberOfRounds();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunCoolingRate() {
    return __baseBotInternals.getGameSetup().getGunCoolingRate();
  }

  /** {@inheritDoc} */
  @Override
  public final int getMaxInactivityTurns() {
    return __baseBotInternals.getGameSetup().getMaxInactivityTurns();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTurnTimeout() {
    return __baseBotInternals.getGameSetup().getTurnTimeout();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTimeLeft() {
    long passesMicroSeconds = (System.nanoTime() - __baseBotInternals.getTicksStart()) / 1000;
    return (int) (__baseBotInternals.getGameSetup().getTurnTimeout() - passesMicroSeconds);
  }

  /** {@inheritDoc} */
  @Override
  public final int getRoundNumber() {
    return __baseBotInternals.getCurrentTick().getRoundNumber();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTurnNumber() {
    return __baseBotInternals.getCurrentTick().getTurnNumber();
  }

  /** {@inheritDoc} */
  @Override
  public final int getEnemyCount() {
    return __baseBotInternals.getCurrentTick().getEnemyCount();
  }

  /** {@inheritDoc} */
  @Override
  public final double getEnergy() {
    return __baseBotInternals.getCurrentTick().getBotState().getEnergy();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isDisabled() {
    return getEnergy() == 0;
  }

  /** {@inheritDoc} */
  @Override
  public final double getX() {
    return __baseBotInternals.getCurrentTick().getBotState().getX();
  }

  /** {@inheritDoc} */
  @Override
  public final double getY() {
    return __baseBotInternals.getCurrentTick().getBotState().getY();
  }

  /** {@inheritDoc} */
  @Override
  public final double getDirection() {
    return __baseBotInternals.getCurrentTick().getBotState().getDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunDirection() {
    return __baseBotInternals.getCurrentTick().getBotState().getGunDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarDirection() {
    return __baseBotInternals.getCurrentTick().getBotState().getRadarDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getSpeed() {
    return __baseBotInternals.getCurrentTick().getBotState().getSpeed();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunHeat() {
    return __baseBotInternals.getCurrentTick().getBotState().getGunHeat();
  }

  /** {@inheritDoc} */
  @Override
  public final Collection<BulletState> getBulletStates() {
    return __baseBotInternals.getCurrentTick().getBulletStates();
  }

  /** {@inheritDoc} */
  @Override
  public final Collection<? extends Event> getEvents() {
    return __baseBotInternals.getCurrentTick().getEvents();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRate(double turnRate) {
    if (Double.isNaN(turnRate)) {
      throw new IllegalArgumentException("turnRate cannot be NaN");
    }
    __baseBotInternals.botIntent.setTurnRate(turnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getTurnRate() {
    return __baseBotInternals.getCurrentTick().getBotState().getTurnRate();
  }

  /** {@inheritDoc} */
  @Override
  public final void setGunTurnRate(double gunTurnRate) {
    if (Double.isNaN(gunTurnRate)) {
      throw new IllegalArgumentException("gunTurnRate cannot be NaN");
    }
    __baseBotInternals.botIntent.setGunTurnRate(gunTurnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunTurnRate() {
    return __baseBotInternals.getCurrentTick().getBotState().getGunTurnRate();
  }

  /** {@inheritDoc} */
  @Override
  public final void setRadarTurnRate(double radarTurnRate) {
    if (Double.isNaN(radarTurnRate)) {
      throw new IllegalArgumentException("radarTurnRate cannot be NaN");
    }
    __baseBotInternals.botIntent.setRadarTurnRate(radarTurnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarTurnRate() {
    return __baseBotInternals.getCurrentTick().getBotState().getRadarTurnRate();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTargetSpeed(double targetSpeed) {
    if (Double.isNaN(targetSpeed)) {
      throw new IllegalArgumentException("targetSpeed cannot be NaN");
    }
    __baseBotInternals.botIntent.setTargetSpeed(targetSpeed);
  }

  /** {@inheritDoc} */
  @Override
  public final double getTargetSpeed() {
    return __baseBotInternals.botIntent.getTargetSpeed();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean setFire(double firepower) {
    if (Double.isNaN(firepower)) {
      throw new IllegalArgumentException("firepower cannot be NaN");
    }
    if (getGunHeat() > 0) {
      return false; // cannot fire yet
    }
    __baseBotInternals.botIntent.setFirepower(firepower);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final double getFirepower() {
    return __baseBotInternals.botIntent.getFirepower();
  }

  /** {@inheritDoc} */
  @Override
  public final void setAdjustGunForBodyTurn(boolean adjust) {
    __baseBotInternals.botIntent.setAdjustGunForBodyTurn(adjust);
  }

  /** {@inheritDoc} */
  @Override
  public final boolean doAdjustGunForBodyTurn() {
    Boolean adjust = __baseBotInternals.botIntent.getAdjustGunForBodyTurn();
    return adjust != null && adjust;
  }

  /** {@inheritDoc} */
  @Override
  public final void setAdjustRadarForGunTurn(boolean adjust) {
    __baseBotInternals.botIntent.setAdjustRadarForGunTurn(adjust);
  }

  /** {@inheritDoc} */
  @Override
  public final boolean doAdjustRadarForGunTurn() {
    Boolean adjust = __baseBotInternals.botIntent.getAdjustRadarForGunTurn();
    return adjust != null && adjust;
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getBodyColor() {
    return __baseBotInternals.getCurrentTick().getBotState().getBodyColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBodyColor(String bodyColor) {
    __baseBotInternals.botIntent.setBodyColor(bodyColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getTurretColor() {
    return __baseBotInternals.getCurrentTick().getBotState().getTurretColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurretColor(String turretColor) {
    __baseBotInternals.botIntent.setTurretColor(turretColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getRadarColor() {
    return __baseBotInternals.getCurrentTick().getBotState().getRadarColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setRadarColor(String radarColor) {
    __baseBotInternals.botIntent.setRadarColor(radarColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getBulletColor() {
    return __baseBotInternals.getCurrentTick().getBotState().getBulletColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBulletColor(String bulletColor) {
    __baseBotInternals.botIntent.setBulletColor(bulletColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getScanColor() {
    return __baseBotInternals.getCurrentTick().getBotState().getScanColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setScanColor(String scanColor) {
    __baseBotInternals.botIntent.setScanColor(scanColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getTracksColor() {
    return __baseBotInternals.getCurrentTick().getBotState().getTracksColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTracksColor(String tracksColor) {
    __baseBotInternals.botIntent.setTracksColor(tracksColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getGunColor() {
    return __baseBotInternals.getCurrentTick().getBotState().getGunColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setGunColor(String gunColor) {
    __baseBotInternals.botIntent.setGunColor(gunColor);
  }

  /** {@inheritDoc} */
  @Override
  public final double calcMaxTurnRate(double speed) {
    return MAX_TURN_RATE - 0.75 * Math.abs(speed);
  }

  /** {@inheritDoc} */
  @Override
  public final double calcBulletSpeed(double firepower) {
    return 20 - 3 * firepower;
  }

  /** {@inheritDoc} */
  @Override
  public final double calcGunHeat(double firepower) {
    return 1 + (firepower / 5);
  }
}
