using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Sample.Bots
{
  /// <summary>
  /// Fire - a sample bot, original version by Mathew Nelson for Robocode.
  /// Modified by Flemming N. Larsen.
  /// 
  /// This is a sample of a robot using the turn rates and target speed to move the bot.
  /// </summary>
  public class VelocityBot : Bot
  {
    // Main method starts our bot
    static void Main_VelocityBot(string[] args)
    {
      new VelocityBot().Start();
    }

    // Constructor, which loads the bot settings file
    VelocityBot() : base(BotInfo.FromJsonFile("velocitybot-settings.json")) { }

    // VelocityBot's run method
    public override void Run()
    {
      GunTurnRate = 15;

      while (IsRunning)
      {
        if (TurnNumber % 64 == 0)
        {
          // Straighten out, if we were hit by a bullet and are turning
          TurnRate = 0;
          // Go forward with a target speed of 4
          TargetSpeed = 4;
        }
        if (TurnNumber % 64 == 32)
        {
          // Go backwards, faster
          SetTargetSpeed(-6);
        }

        // Execute (send commands to server)
        Go();
        // Wait for the next turn before continuing the loop
        WaitFor(new NextTurnCondition(this));
      }
    }

    public override void OnScannedBot(ScannedBotEvent e)
    {
      Fire(1);
    }

    public override void OnHitByBullet(BulletHitBotEvent e)
    {
      // Turn to confuse the other robot
      TurnRate = 5;
    }

    public override void OnHitWall(HitWallEvent e)
    {
      // Move away from the wall
      SetTargetSpeed(-1 * TargetSpeed);
    }
  }
}