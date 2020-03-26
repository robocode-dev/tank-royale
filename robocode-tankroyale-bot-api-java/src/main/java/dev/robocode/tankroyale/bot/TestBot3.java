package dev.robocode.tankroyale.bot;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;

@SuppressWarnings("UnusedDeclaration")
public class TestBot3 extends Bot {

  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    new TestBot3().start();
  }


  public void run() {
    double delta = (360 + (90 - getDirection())) % 360;

    if (delta < 180)
      turnLeft(delta);
    else
      turnRight(360 - delta);
  }
}
