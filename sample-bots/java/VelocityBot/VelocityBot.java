import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// VelocityBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Joshua Galecki.
//
// Example bot of how to use turn rates.
// ------------------------------------------------------------------
public class VelocityBot extends Bot {

    int turnCounter;

    // The main method starts our bot
    public static void main(String[] args) {
        new VelocityBot().start();
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
		turnCounter = 0;

		setGunTurnRate(15);
		
		while (isRunning()) {
			if (turnCounter % 64 == 0) {
				// Straighten out, if we were hit by a bullet (ends turning)
				setTurnRate(0);

                // Go forward with a target speed of 4
				setTargetSpeed(4);
			}
			if (turnCounter % 64 == 32) {
				// Go backwards, faster
                setTargetSpeed(-6);
			}
			turnCounter++;
			go(); // execute turn
		}
	}

    // We scanned another bot -> fire!
    @Override
	public void onScannedBot(ScannedBotEvent e) {
		fire(1);
	}

    // We were hit by a bullet -> set turn rate
    @Override
	public void onHitByBullet(HitByBulletEvent e) {
		// Turn to confuse the other bots
		setTurnRate(5);
	}
	
    // We hit a wall -> move in the opposite direction
    @Override
	public void onHitWall(HitWallEvent e) {
		// Move away from the wall by reversing the target speed.
		// Note that current speed is 0 as the bot just hit the wall.
		setTargetSpeed(-1 * getTargetSpeed());
	}
}