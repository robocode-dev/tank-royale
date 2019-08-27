package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

public class TestBot extends Bot {

  public static void main(String[] args) {
    new TestBot().run();
  }

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
//    System.out.println("onTick: " + event);

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

  private Double targetX;
  private Double targetY;

  @Override
  public void onScannedBot(ScannedBotEvent event) {
//    System.out.println("onScannedBot: " + event);

    targetX = event.getX();
    targetY = event.getY();
  }

  @Override
  public void onHitWall(BotHitWallEvent event) {
    targetSpeed =- targetSpeed;
  }

  @Override
  public void onSkippedTurn(SkippedTurnEvent event) {
//    System.out.println("onSkippedTurn: turn: " + event.getTurnNumber() + "/" + getRoundNumber());
  }

  @Override
  public void onHitByBullet(BulletHitBotEvent event) {
//    System.out.println("-> onHitByBullet: " + event.getTurnNumber() + "/" + getRoundNumber());
  }

  @Override
  public void onBulletHit(BulletHitBotEvent event) {
//    System.out.println("<- onBulletHit: " + event.getTurnNumber() + "/" + getRoundNumber());
  }

  @Override
  public void onHitBot(BotHitBotEvent event) {
//    System.out.println("BotHitBotEvent: " + event);

    targetSpeed =- targetSpeed;
  }
}
