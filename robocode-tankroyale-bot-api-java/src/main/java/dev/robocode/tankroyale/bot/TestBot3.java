package dev.robocode.tankroyale.bot;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.*;

import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
public class TestBot3 extends Bot {

  public static void main(String[] args) throws IOException {
    new TestBot3().start();
  }

  protected TestBot3() throws IOException {
    super(BotInfo.fromFile("testbot3.properties"));
  }

  public void run() {
    double delta = (360 + (90 - getDirection())) % 360;

    if (delta < 180)
      turnLeft(delta);
    else
      turnRight(360 - delta);
  }
}
