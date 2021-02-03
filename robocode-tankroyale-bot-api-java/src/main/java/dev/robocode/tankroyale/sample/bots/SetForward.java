package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.NextTurnCondition;

import java.io.IOException;

public class SetForward extends Bot {

  public static void main(String[] args) throws IOException {
    new SetForward().start();
  }

  protected SetForward() throws IOException {
    super(BotInfo.fromFile("/setforward.properties"));
  }

  public void run() {

    // Spin the gun around slowly... forever
    while (isRunning()) {
      System.out.println("turn: " + getTurnNumber() + ", speed: " + getSpeed());
      setTargetSpeed(10);

      // Execute (send commands to server)
      go();
      // Wait for the next turn before continuing the loop
      waitFor(new NextTurnCondition(this));
    }
  }
}
