package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * MyFirstBot - a sample bot.
 *
 * <p>Probably the first bot you will learn about.
 */
public class MyFirstBot extends Bot {

  public static void main(String[] args) throws IOException {
    new MyFirstBot().start();
  }

  protected MyFirstBot() throws IOException {
    super(BotInfo.fromFile("bot.properties"));
  }

  // This method runs our bot program, where each command is executed one at a time
  @Override
  public void run() {
    // Repeat while bot is running
    while (isRunning()) {
      forward(100);
      turnGunRight(360);
      back(100);
      turnGunRight(360);
    }
  }

  // This method is called when our bot has scanned another bot
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    setFirepower(1); // Fire the cannon!
  }

  // This method is called when our bot is hit by a bullet
  @Override
  public void onHitByBullet(BulletHitBotEvent e) {
    // Calculate the bearing to the direction of the bullet
    double bearing = e.getBullet().getDirection() - getDirection();

    // Turn 90 degrees to the bullet direction based on the bearing
    setTurnLeft(90 - bearing);
  }
}
