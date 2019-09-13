package dev.robocode.tankroyale.bot;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;

@SuppressWarnings("UnusedDeclaration")
public class TestBot extends Bot {

  public static void main(String[] args) {
    new TestBot().run();
  }

  private Double targetX;
  private Double targetY;

  private TestBot() {
    super();
  }

  @Override
  public void onConnected(ConnectedEvent event) {
    System.out.println("onConnected");
  }

  @Override
  public void onDisconnected(DisconnectedEvent event) {
    System.out.println("onDisconnected");
  }

  @Override
  public void onGameStarted(GameStartedEvent event) {
    System.out.println("onGameStarted: " + event);

    setRadarTurnRate(100);
    setTargetSpeed(targetSpeed);
    go();
  }

  private double targetSpeed = 15;

  @Override
  public void onTick(TickEvent event) {
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
