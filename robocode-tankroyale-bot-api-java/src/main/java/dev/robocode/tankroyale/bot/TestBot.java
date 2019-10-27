package dev.robocode.tankroyale.bot;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
@Log4j
public class TestBot extends Bot {

  static {
    MDC.put("myUuid", UUID.randomUUID());
  }

  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e));
    new TestBot().run();
  }

  private Double targetX;
  private Double targetY;

  private TestBot() {
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

    setRadarTurnRate(100);
    setTargetSpeed(targetSpeed);
    go();
  }

  private double targetSpeed = 15;

  @Override
  public void onTick(TickEvent event) {
    log.info("onTick: " + event.getTurnNumber());

    if (getSpeed() == targetSpeed) {
      targetSpeed = -targetSpeed;
    }
    setTargetSpeed(targetSpeed);

    if (targetX != null) {

      double dx = targetX - getX();
      double dy = targetY - getY();
      double angle = Math.toDegrees(Math.atan2(dy, dx));

      double gunTurnRate = normalRelativeDegrees(angle - getGunDirection());

      setGunTurnRate(gunTurnRate);
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
    targetSpeed =- targetSpeed;
  }

  @Override
  public void onHitBot(BotHitBotEvent event) {
    targetSpeed =- targetSpeed;
  }
}
