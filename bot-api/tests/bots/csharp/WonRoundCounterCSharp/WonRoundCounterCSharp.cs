using System.IO;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Instrumented bot that counts received WonRoundEvents and writes the count to a temp file.
/// Used by runner integration tests to verify bot-side WonRoundEvent delivery.
/// </summary>
public class WonRoundCounterCSharp : Bot
{
    private int _wonRoundCount = 0;
    private readonly string _countFile = Path.Combine(Path.GetTempPath(), "won_round_csharp.txt");

    static void Main() => new WonRoundCounterCSharp().Start();

    public override void Run()
    {
        while (IsRunning)
            TurnRight(10);
    }

    public override void OnWonRound(WonRoundEvent e)
    {
        _wonRoundCount++;
        File.WriteAllText(_countFile, _wonRoundCount.ToString());
    }

    public override void OnScannedBot(ScannedBotEvent e)
    {
        SetFire(1);
    }
}
