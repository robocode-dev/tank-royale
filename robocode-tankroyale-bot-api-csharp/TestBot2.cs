using System;
using Robocode.TankRoyale.BotApi;

public class TestBot2 : Bot
{
  static void Main()
  {
    var botInfo = new BotInfo(
      "TestBot2",
      "1.0",
      "fnl",
      "A sample bot",
      "dk",
      new string[] { "1v1", "melee" },
      "C#"
    );

    Uri serverUri = new Uri("ws://localhost:55000");

    new TestBot2(botInfo, serverUri).Start();
  }

  private double move = 200;

  private TestBot2(BotInfo botInfo, Uri serverUri) : base(botInfo, serverUri) { }

  public override void OnConnected(ConnectedEvent evt)
  {
    Console.WriteLine("OnConnected");
  }

  public override void OnDisconnected(DisconnectedEvent evt)
  {
    Console.WriteLine("OnDisconnected");
  }

  public override void OnGameStarted(GameStartedEvent evt)
  {
    SetMaxGunTurnRate(4);
    SetMaxRadarTurnRate(4);
    SetMaxSpeed(4);

    SetTurnRadarLeft(Double.PositiveInfinity);

    SetForward(move);
    Go();
  }

  public override void Run()
  {
    while (IsRunning)
    {
      Forward(100);
      TurnGunLeft(360);
      Back(100);
      TurnGunRight(360);
    }
  }

  public override void OnScannedBot(ScannedBotEvent evt)
  {
    Console.WriteLine("OnScannedBot");

    Fire(1);
  }

  public override void OnHitWall(BotHitWallEvent evt)
  {
    move = -move;
    SetForward(move);
  }

  public override void OnHitBot(BotHitBotEvent evt)
  {
    move = -move;
    SetForward(move);
  }
}