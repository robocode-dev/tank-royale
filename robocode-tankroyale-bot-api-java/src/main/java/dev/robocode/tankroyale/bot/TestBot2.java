package dev.robocode.tankroyale.bot;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.MDC;

import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
@Log4j
public class TestBot2 extends Bot {

  static {
    MDC.put("myUuid", UUID.randomUUID());
  }

  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e));
    new TestBot2().run();
  }

  private Double targetX;
  private Double targetY;
  private double move = 200;

  private TestBot2() {
    super();
  }

  @Override
  public void onConnected(ConnectedEvent event) {
    log.info("onConnected");
  }

  @Override
  public void onDisconnected(DisconnectedEvent event) {
    log.info("onDisconnected");
  }

  @Override
  public void onGameStarted(GameStartedEvent event) {
    log.info("onGameStarted: " + event);

    setMaxGunTurnRate(4);
    setMaxRadarTurnRate(4);

    setTurnRadarLeft(Double.POSITIVE_INFINITY);

    setForward(move);
    go();
  }

  @Override
  public void onTick(TickEvent event) {
    log.info("onTick: " + event.getTurnNumber());

    if (Math.abs(getDistanceRemaining()) < 1) {
      move = -move;
      setForward(move);
    }

    if (targetX != null) {
      double dx = targetX - getX();
      double dy = targetY - getY();
      double angle = Math.toDegrees(Math.atan2(dy, dx));

      setTurnGunLeft(normalRelativeDegrees(angle - getGunDirection()));
    }
    setFire(0.1 + Math.random() * 2.9);

    go();
  }

  @Override
  public void onScannedBot(ScannedBotEvent event) {
    log.info("onScannedBot");

    targetX = event.getX();
    targetY = event.getY();
  }

  @Override
  public void onHitWall(BotHitWallEvent event) {
    move = -move;
    setForward(move);
  }

  @Override
  public void onHitBot(BotHitBotEvent event) {
    move = -move;
    setForward(move);
  }
}
