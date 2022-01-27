import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.io.IOException;

/**
 * MyFirstBot - a sample bot, original version by Mathew Nelson for Robocode.
 *
 * <p>Probably the first bot you will learn about.
 *
 * <p>Moves in a seesaw motion, and spins the gun around at each end.
 */
public class MyFirstBot extends Bot {

    /**
     * Constructor, which loads the bot settings file
     */
    protected MyFirstBot() throws IOException {
        super(BotInfo.fromFile("MyFirstBot.json"));
    }

    /**
     * Main method starts our bot
     */
    public static void main(String[] args) throws IOException {
        new MyFirstBot().start();
    }

    /**
     * This method runs our bot program, where each command is executed one at a time in a loop.
     */
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

    /**
     * Our bot scanned another bot. Fire when we see another bot!
     */
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
    }

    /**
     * Our bot has been hit by a bullet. Turn perpendicular to the bullet so our seesaw might avoid a
     * future shot.
     */
    @Override
    public void onHitByBullet(BulletHitBotEvent e) {
        // Calculate the bearing to the direction of the bullet
        double bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnLeft(90 - bearing);
    }
}
