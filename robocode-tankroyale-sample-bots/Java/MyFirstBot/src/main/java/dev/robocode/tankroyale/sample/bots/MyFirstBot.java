package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.TickEvent;

public class MyFirstBot extends Bot {

  // Main method
  public static void main(String[] args) {
    // Run our bot
    new MyFirstBot().run();
  }

  // Variable used for storing next step to be taken
  private int step = 1;

  // This method is called when our bot must take action for the next turn
  @Override
  public void onTick(TickEvent event) {
    // Only do something, when the bot has finished it's movement
    if (getDistanceRemaining() == 0 && getTurnRemaining() == 0) {
      if (step == 1) {
        setForward(100); // remaining distance = 100
      } else if (step == 2 || step == 4) {
        setTurnGunLeft(360); // remaining turn = 360
      } else if (step == 3) {
        setBack(100); // remaining distance = -100
      } else {
        step = 0; // reset step, so it becomes 1 when step++ is called next
      }
      step++; // next step
    }
    // execute our set command. Our turn is over!
    go();
  }

  // This method is called when our bot has scanned another bot
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    // Fire the cannon!
    setFire(1);
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
