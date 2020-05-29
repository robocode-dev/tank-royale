using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.Sample.Bots
{
  /// <summary>
  /// SpinBot - a sample robot.
  ///
  /// Moves in a circle, firing hard when an enemy is detected.
  /// </summary>
  public class SpinBot : Bot
  {
    static void Main(string[] args)
    {
      new SpinBot().Start();
    }

    SpinBot() : base(BotInfo.FromJsonFile("bot-settings.json")) { }

    // This method runs our bot program, where each command is executed one at a time
    public override void Run()
    {
      // Repeat while bot is running
      while (IsRunning)
      {
        // Tell the game that when we take move, we'll also want to turn right... a lot.
        SetTurnRight(10000);
        // Limit our speed to 5
        SetMaxSpeed(5);
        // Start moving (and turning)
        Forward(10000);
      }
    }

    // OnScannedBot: Fire hard when scanning another bot!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
      Firepower = 3; // Fire the cannon!
    }

    // OnHitBot: If it's our fault, we'll stop turning and moving, so we need to turn again to keep spinning.
    public override void OnHitBot(BotHitBotEvent e)
    {
      double direction = CalcDirection(e.X, e.Y);
      double bearing = CalcBearing(direction);
      if (bearing > -10 && bearing < 10)
      {
        Fire(3);
      }
      if (e.Rammed)
      {
        TurnRight(10);
      }
    }
  }
}