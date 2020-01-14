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

    new TestBot2().Start();
  }

  private double move = 200;

  private TestBot2() : base() { }

  public void OnConnected(ConnectedEvent evt)
  {
    Console.WriteLine("OnConnected");
  }

  public void OnDisconnected(DisconnectedEvent evt)
  {
    Console.WriteLine("OnDisconnected");
  }

  public void OnGameStarted(GameStartedEvent evt)
  {
    Console.WriteLine("OnGameStarted: " + evt);

    SetMaxGunTurnRate(4);
    SetMaxRadarTurnRate(4);
    SetMaxSpeed(4);

    SetTurnRadarLeft(Double.PositiveInfinity);

    SetForward(move);
    Go();
  }

  public void Run()
  {
    while (IsRunning)
    {
      Forward(100);
      TurnGunLeft(360);
      Back(100);
      TurnGunRight(360);
    }
  }

  public void OnScannedBot(ScannedBotEvent evt)
  {
    Fire(1);
  }

  public void OnHitWall(BotHitWallEvent ev)
  {
    move = -move;
    SetForward(move);
  }

  public void OnHitBot(BotHitBotEvent evt)
  {
    move = -move;
    SetForward(move);
  }
}