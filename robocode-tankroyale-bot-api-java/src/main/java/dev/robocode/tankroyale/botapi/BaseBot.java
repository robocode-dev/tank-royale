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

  final BaseBotInternals __internals;

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
    __internals = new BaseBotInternals(this,null, null);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * server URL is provided through the environment variable ROBOCODE_SERVER_URL.
   *
   * @param botInfo is the bot info containing information about your bot.
   */
  public BaseBot(final BotInfo botInfo) {
    __internals = new BaseBotInternals(this, botInfo, null);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used
   * providing both the bot information and server URL for your bot.
   *
   * @param botInfo is the bot info containing information about your bot.
   * @param serverUrl is the server URL
   */
  public BaseBot(final BotInfo botInfo, URI serverUrl) {
    __internals = new BaseBotInternals(this, botInfo, serverUrl);
  }

  /** {@inheritDoc} */
  @Override
  public final void start() {
    __internals.connect();
  }

  /** {@inheritDoc} */
  @Override
  public final void go() {
    // Send the bot intent to the server
    __internals.sendIntent();
  }

  /** {@inheritDoc} */
  @Override
  public final String getVariant() {
    return __internals.getServerHandshake().getVariant();
  }

  /** {@inheritDoc} */
  @Override
  public final String getVersion() {
    return __internals.getServerHandshake().getVersion();
  }

  /** {@inheritDoc} */
  @Override
  public final int getMyId() {
    return __internals.getMyId();
  }

  /** {@inheritDoc} */
  @Override
  public final String getGameType() {
    return __internals.getGameSetup().getGameType();
  }

  /** {@inheritDoc} */
  @Override
  public final int getArenaWidth() {
    return __internals.getGameSetup().getArenaWidth();
  }

  /** {@inheritDoc} */
  @Override
  public final int getArenaHeight() {
    return __internals.getGameSetup().getArenaHeight();
  }

  /** {@inheritDoc} */
  @Override
  public final int getNumberOfRounds() {
    return __internals.getGameSetup().getNumberOfRounds();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunCoolingRate() {
    return __internals.getGameSetup().getGunCoolingRate();
  }

  /** {@inheritDoc} */
  @Override
  public final int getMaxInactivityTurns() {
    return __internals.getGameSetup().getMaxInactivityTurns();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTurnTimeout() {
    return __internals.getGameSetup().getTurnTimeout();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTimeLeft() {
    long passesMicroSeconds = (System.nanoTime() - __internals.getTicksStart()) / 1000;
    return (int) (__internals.getGameSetup().getTurnTimeout() - passesMicroSeconds);
  }

  /** {@inheritDoc} */
  @Override
  public final int getRoundNumber() {
    return __internals.getCurrentTurn().getRoundNumber();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTurnNumber() {
    return __internals.getCurrentTurn().getTurnNumber();
  }

  /** {@inheritDoc} */
  @Override
  public final double getEnergy() {
    return __internals.getCurrentTurn().getBotState().getEnergy();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isDisabled() {
    return getEnergy() == 0;
  }

  /** {@inheritDoc} */
  @Override
  public final double getX() {
    return __internals.getCurrentTurn().getBotState().getX();
  }

  /** {@inheritDoc} */
  @Override
  public final double getY() {
    return __internals.getCurrentTurn().getBotState().getY();
  }

  /** {@inheritDoc} */
  @Override
  public final double getDirection() {
    return __internals.getCurrentTurn().getBotState().getDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunDirection() {
    return __internals.getCurrentTurn().getBotState().getGunDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarDirection() {
    return __internals.getCurrentTurn().getBotState().getRadarDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getSpeed() {
    return __internals.getCurrentTurn().getBotState().getSpeed();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunHeat() {
    return __internals.getCurrentTurn().getBotState().getGunHeat();
  }

  /** {@inheritDoc} */
  @Override
  public final Collection<BulletState> getBulletStates() {
    return __internals.getCurrentTurn().getBulletStates();
  }

  /** {@inheritDoc} */
  @Override
  public final Collection<? extends Event> getEvents() {
    return __internals.getCurrentTurn().getEvents();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRate(double turnRate) {
    if (Double.isNaN(turnRate)) {
      throw new IllegalArgumentException("turnRate cannot be NaN");
    }
    if (Math.abs(turnRate) > MAX_TURN_RATE) {
      turnRate = MAX_TURN_RATE * (turnRate > 0 ? 1 : -1);
    }
    __internals.botIntent.setTurnRate(turnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getTurnRate() {
    Double turnRate = __internals.botIntent.getTurnRate();
    if (turnRate == null) {
      return 0;
    }
    return turnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setGunTurnRate(double gunTurnRate) {
    if (Double.isNaN(gunTurnRate)) {
      throw new IllegalArgumentException("gunTurnRate cannot be NaN");
    }
    if (isAdjustGunForBodyTurn()) {
      gunTurnRate -= getTurnRate();
    }
    if (Math.abs(gunTurnRate) > MAX_GUN_TURN_RATE) {
      gunTurnRate = MAX_GUN_TURN_RATE * (gunTurnRate > 0 ? 1 : -1);
    }
    __internals.botIntent.setGunTurnRate(gunTurnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunTurnRate() {
    Double turnRate = __internals.botIntent.getGunTurnRate();
    if (turnRate == null) {
      return 0;
    }
    return turnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setRadarTurnRate(double radarTurnRate) {
    if (Double.isNaN(radarTurnRate)) {
      throw new IllegalArgumentException("radarTurnRate cannot be NaN");
    }
    if (isAdjustRadarForGunTurn()) {
      radarTurnRate -= getGunTurnRate();
    }
    if (Math.abs(radarTurnRate) > MAX_RADAR_TURN_RATE) {
      radarTurnRate = MAX_RADAR_TURN_RATE * (radarTurnRate > 0 ? 1 : -1);
    }
    __internals.botIntent.setRadarTurnRate(radarTurnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarTurnRate() {
    Double turnRate = __internals.botIntent.getRadarTurnRate();
    if (turnRate == null) {
      return 0;
    }
    return turnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTargetSpeed(double targetSpeed) {
    if (Double.isNaN(targetSpeed)) {
      throw new IllegalArgumentException("targetSpeed cannot be NaN");
    }
    if (targetSpeed > MAX_FORWARD_SPEED) {
      targetSpeed = MAX_FORWARD_SPEED;
    } else if (targetSpeed < MAX_BACKWARD_SPEED) {
      targetSpeed = MAX_BACKWARD_SPEED;
    }
    __internals.botIntent.setTargetSpeed(targetSpeed);
  }

  /** {@inheritDoc} */
  @Override
  public final double getTargetSpeed() {
    return __internals.botIntent.getTargetSpeed();
  }

  /** {@inheritDoc} */
  @Override
  public final void setFirepower(double firepower) {
    if (Double.isNaN(firepower)) {
      throw new IllegalArgumentException("firepower cannot be NaN");
    }
    if (getGunHeat() == 0) {
      if (firepower < MIN_FIREPOWER) {
        firepower = 0;
      } else if (firepower > MAX_FIREPOWER) {
        firepower = MAX_FIREPOWER;
      }
      __internals.botIntent.setFirepower(firepower);
    }
  }

  /** {@inheritDoc} */
  @Override
  public final double getFirepower() {
    return __internals.botIntent.getFirepower();
  }

  /** {@inheritDoc} */
  @Override
  public final void setAdjustGunForBodyTurn(boolean adjust) {
    __internals.doAdjustGunForBodyTurn = adjust;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isAdjustGunForBodyTurn() {
    return __internals.doAdjustGunForBodyTurn;
  }

  /** {@inheritDoc} */
  @Override
  public final void setAdjustRadarForGunTurn(boolean adjust) {
    __internals.doAdjustRadarForGunTurn = adjust;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isAdjustRadarForGunTurn() {
    return __internals.doAdjustRadarForGunTurn;
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getBodyColor() {
    return __internals.getCurrentTurn().getBotState().getBodyColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBodyColor(String bodyColor) {
    __internals.botIntent.setBodyColor(bodyColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getTurretColor() {
    return __internals.getCurrentTurn().getBotState().getTurretColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurretColor(String turretColor) {
    __internals.botIntent.setTurretColor(turretColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getRadarColor() {
    return __internals.getCurrentTurn().getBotState().getRadarColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setRadarColor(String radarColor) {
    __internals.botIntent.setRadarColor(radarColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getBulletColor() {
    return __internals.getCurrentTurn().getBotState().getBulletColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBulletColor(String bulletColor) {
    __internals.botIntent.setBulletColor(bulletColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getScanColor() {
    return __internals.getCurrentTurn().getBotState().getScanColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setScanColor(String scanColor) {
    __internals.botIntent.setScanColor(scanColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getTracksColor() {
    return __internals.getCurrentTurn().getBotState().getTracksColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTracksColor(String tracksColor) {
    __internals.botIntent.setTracksColor(tracksColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getGunColor() {
    return __internals.getCurrentTurn().getBotState().getGunColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setGunColor(String gunColor) {
    __internals.botIntent.setGunColor(gunColor);
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
