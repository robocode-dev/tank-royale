using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.Sample.Bots
{
  /// <summary>
  /// SpinBot - a sample bot.
  ///
  /// Moves in a circle, firing hard when an enemy is detected.
  /// </summary>
  public class SpinBot : Bot
  {
    /// Main method starts our bot
    static void Main(string[] args)
    {
      new SpinBot().Start();
    }

    /// Constructor, which loads the bot settings file
    SpinBot() : base(BotInfo.FromJsonFile("bot-settings.json")) { }

    /// SpinBot's run method - Move in a circle
    public override void Run()
    {
      SetBodyColor("#00F"); // blue
      SetTurretColor("#00F"); // blue
      SetRadarColor("#000"); // black
      SetScanColor("#FF0"); // yellow

      // Repeat while bot is running
      while (IsRunning)
      {
        // Tell the game that when we take move,
        // we'll also want to turn right... a lot.
        SetTurnRight(10000);
        // Limit our speed to 5
        SetMaxSpeed(5);
        // Start moving (and turning)
        Forward(10000);
        // Repeat
      }
    }

    /// Fire hard when scanning another bot!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
      Firepower = 3; // Fire the cannon!
    }

    /// We hit another bot. If we rammed the bot, we'll stop turning and moving, so we need to turn
    /// again to keep spinning.
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