package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * Crazy - a sample bot, original version by Mathew Nelson for Robocode.
 *
 * <p>This robot moves around in a crazy pattern.
 */
public class Crazy extends Bot {

  boolean movingForward;

  /** Main method starts our bot */
  public static void main(String[] args) throws IOException {
    new Crazy().start();
  }

  /** Constructor, which loads the bot settings file */
  protected Crazy() throws IOException {
    super(BotInfo.fromFile("/crazy.properties"));
  }

  /** run: Crazy's main run function */
  public void run() {
    // Set colors
    setBodyColor("#00C800");
    setGunColor("#009632");
    setRadarColor("#006464");
    setBulletColor("#FFFF64");
    setScanColor("#FFC8C8");

    // Loop while as long as the bot is running
    while (isRunning()) {
      // Tell the game we will want to move ahead 40000 -- some large number
      setForward(40000);
      movingForward = true;
      // Tell the game we will want to turn right 90
      setTurnRight(90);
      // At this point, we have indicated to the game that *when we do something*,
      // we will want to move ahead and turn right. That's what "set" means.
      // It is important to realize we have not done anything yet!
      // In order to actually move, we'll want to call a method that takes real time, such as
      // waitFor.
      // waitFor actually starts the action -- we start moving and turning.
      // It will not return until we have finished turning.
      waitFor(new TurnCompleteCondition(this));
      // Note:  We are still moving ahead now, but the turn is complete.
      // Now we'll turn the other way...
      setTurnLeft(180);
      // ... and wait for the turn to finish ...
      waitFor(new TurnCompleteCondition(this));
      // ... then the other way ...
      setTurnRight(180);
      // ... and wait for that turn to finish.
      waitFor(new TurnCompleteCondition(this));
      // then back to the top to do it all again
    }
  }

  /** onHitWall: Handle collision with wall. */
  @Override
  public void onHitWall(HitWallEvent e) {
    // Bounce off!
    reverseDirection();
  }

  /** reverseDirection: Switch from ahead to back & vice versa */
  public void reverseDirection() {
    if (movingForward) {
      setBack(40000);
      movingForward = false;
    } else {
      setForward(40000);
      movingForward = true;
    }
  }

  /** onScannedRobot: Fire! */
  @Override
  public void onScannedBot(ScannedBotEvent e) {
    fire(1);
  }

  /** onHitRobot: Back up! */
  @Override
  public void onHitBot(HitBotEvent e) {
    // If we're moving into the other robot, reverse!
    if (e.isRammed()) {
      reverseDirection();
    }
  }
}