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
    go();
  }

  @Override
  public void onScannedBot(ScannedBotEvent event) {
    System.out.println("onScannedBot: " + event);

    double dx = event.getX() - getX();
    double dy = event.getY() - getY();
    double angle = Math.toDegrees(Math.atan2(dy, dx));

    setGunTurnRate(angle - getGunDirection());
    setFire(3);
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
