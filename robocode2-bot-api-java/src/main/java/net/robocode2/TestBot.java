package net.robocode2;

import net.robocode2.events.ConnectedEvent;
import net.robocode2.events.DisconnectedEvent;
import net.robocode2.events.ScannedBotEvent;
import net.robocode2.events.TickEvent;

import java.net.URI;
import java.util.Collections;

public class TestBot extends Bot {

  private static final BotInfo botInfo =
      BotInfo.builder()
          .name("Test")
          .version("1")
          .author("fnl")
          .gameTypes(Collections.singletonList(GameType.MELEE.toString()))
          .build();

  private TestBot() throws Exception {
    super(botInfo, new URI("ws://localhost:55000"));
  }

  public static void main(String[] args) throws Exception {
    new TestBot().run();
  }

  private double targetSpeed = 8;

  @Override
  public void onTick(TickEvent event) {
    System.out.println("onTick: " + event);

    setRadarTurnRate(100);

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

  Double targetX;
  Double targetY;

  @Override
  public void onScannedBot(ScannedBotEvent event) {
    System.out.println("onScannedBot: " + event);

    targetX = event.getX();
    targetY = event.getY();
  }

  @Override
  public void onConnected(ConnectedEvent event) {
    System.out.println("onConnected");
  }

  @Override
  public void onDisconnected(DisconnectedEvent event) {
    System.out.println("onDisconnected");
  }
}
