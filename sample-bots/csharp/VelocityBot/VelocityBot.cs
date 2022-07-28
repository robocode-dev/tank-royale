using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// VelocityBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Joshua Galecki.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Example bot of how to use turn rates.
// ------------------------------------------------------------------
public class VelocityBot : Bot {

    int turnCounter;

    // The main method starts our bot
    static void Main(string[] args)
    {
        new VelocityBot().Start();
    }

    // Constructor, which loads the bot config file
    VelocityBot() : base(BotInfo.FromFile("VelocityBot.json")) { }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
		turnCounter = 0;

		GunTurnRate = 15;
		
		while (IsRunning) {
			if (turnCounter % 64 == 0) {
				// Straighten out, if we were hit by a bullet (ends turning)
				TurnRate = 0;

                // Go forward with a target speed of 4
				TargetSpeed = 4;
			}
			if (turnCounter % 64 == 32) {
				// Go backwards, faster
                TargetSpeed = -6;
			}
			turnCounter++;
			Go(); // execute turn
		}
	}

    // We scanned another bot -> fire!
	public override void OnScannedBot(ScannedBotEvent e) {
		Fire(1);
	}

    // We were hit by a bullet -> set turn rate
	public override void OnHitByBullet(HitByBulletEvent e) {
		// Turn to confuse the other bots
		TurnRate = 5;
	}
	
    // We hit a wall -> move in the opposite direction
	public override void OnHitWall(HitWallEvent e) {
		// Move away from the wall by reversing the target speed.
		// Note that current speed is 0 as the bot just hit the wall.
		TargetSpeed = -1 * TargetSpeed;
	}
}