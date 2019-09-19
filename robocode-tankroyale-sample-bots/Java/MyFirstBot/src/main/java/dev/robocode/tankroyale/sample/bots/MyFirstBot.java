package dev.robocode.tankroyale.sample.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.TickEvent;

public class MyFirstBot extends Bot {

  // Main method for running our bot
  public static void main(String[] args) {
    new MyFirstBot().run();
  }

  @Override
  public void onTick(TickEvent event) {
    int mode = (event.getTurnNumber() % 40) / 10;

    switch (mode) {
      case 0:
        setTargetSpeed(MAX_FORWARD_SPEED);
        setGunTurnRate(0);
        break;
      case 1:
      case 3:
        setTargetSpeed(0);
        setGunTurnRate(360.0 / 10);
        break;
      case 2:
        setTargetSpeed(MAX_BACKWARD_SPEED);
        setGunTurnRate(0);
        break;
    }
    go();
  }

  // A bot has been scanned by out bot => fire!
  @Override
  public void onScannedBot(ScannedBotEvent event) {
    setFire(1);
  }
}
