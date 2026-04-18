using System;
using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Cross-platform protocol conformance tests (TR-API-TCK-007 through TR-API-TCK-017).
///
/// <para>These tests verify that the .NET Bot API correctly handles protocol messages
/// as defined in the Tank Royale cross-platform TCK.</para>
///
/// <para>See also: bot-api/tests/TEST-REGISTRY.md</para>
/// </summary>
[TestFixture]
[Category("TCK")]
public class ProtocolConformanceTest : AbstractBotTest
{
    // -----------------------------------------------------------------------
    // TCK-007: BotHandshake contains correct sessionId, name, version, authors, isDroid
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-007")]
    public void Tck007_BotHandshake_ContainsCorrectFields()
    {
        StartAsync(new TckBot(Server.ServerUrl));
        Assert.That(Server.AwaitBotHandshake(3000), Is.True);

        var handshake = Server.Handshake;
        Assert.That(handshake.SessionId, Is.EqualTo(MockedServer.SessionId));
        Assert.That(handshake.Name, Is.EqualTo("TestBot"));
        Assert.That(handshake.Version, Is.EqualTo("1.0"));
        Assert.That(handshake.Authors, Is.EquivalentTo(new[] { "Author 1", "Author 2" }));
        Assert.That(handshake.IsDroid ?? false, Is.False);
    }

    // -----------------------------------------------------------------------
    // TCK-008: Bot sends BotReady after GameStarted
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-008")]
    public void Tck008_BotSendsBotReadyAfterGameStarted()
    {
        StartAsync(new TckBot(Server.ServerUrl));
        Assert.That(Server.AwaitBotReadyMessage(3000), Is.True);
    }

    // -----------------------------------------------------------------------
    // TCK-009: onRoundStarted fires with roundNumber==1
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-009")]
    public void Tck009_OnRoundStarted_FiresWithRoundNumber1()
    {
        var latch = new ManualResetEventSlim(false);
        var roundNumber = 0;

        var bot = new TckBot(Server.ServerUrl);
        bot.OnRoundStartedAction = e =>
        {
            roundNumber = e.RoundNumber;
            latch.Set();
        };
        StartAsync(bot);

        Assert.That(latch.Wait(3000), Is.True);
        Assert.That(roundNumber, Is.EqualTo(1));
    }

    // -----------------------------------------------------------------------
    // TCK-010: onRoundEnded fires with roundNumber==1, turnNumber==5
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-010")]
    public void Tck010_OnRoundEnded_FiresWithCorrectNumbers()
    {
        var latch = new ManualResetEventSlim(false);
        var capturedRound = 0;
        var capturedTurn = 0;

        var bot = new TckBot(Server.ServerUrl);
        bot.OnRoundEndedAction = e =>
        {
            capturedRound = e.RoundNumber;
            capturedTurn = e.TurnNumber;
            latch.Set();
        };
        StartAsync(bot);

        Assert.That(Server.AwaitBotReadyMessage(3000), Is.True);
        Server.SendRaw(BuildRoundEndedJson(1, 5));

        Assert.That(latch.Wait(3000), Is.True);
        Assert.That(capturedRound, Is.EqualTo(1));
        Assert.That(capturedTurn, Is.EqualTo(5));
    }

    // -----------------------------------------------------------------------
    // TCK-011: onGameEnded fires with numberOfRounds==10
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-011")]
    public void Tck011_OnGameEnded_FiresWithNumberOfRounds10()
    {
        var latch = new ManualResetEventSlim(false);
        var capturedRounds = 0;

        var bot = new TckBot(Server.ServerUrl);
        bot.OnGameEndedAction = e =>
        {
            capturedRounds = e.NumberOfRounds;
            latch.Set();
        };
        StartAsync(bot);

        Assert.That(Server.AwaitBotReadyMessage(3000), Is.True);
        Server.SendRaw(BuildGameEndedJson(10));

        Assert.That(latch.Wait(3000), Is.True);
        Assert.That(capturedRounds, Is.EqualTo(10));
    }

    // -----------------------------------------------------------------------
    // TCK-012: onSkippedTurn fires with turnNumber==1
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-012")]
    public void Tck012_OnSkippedTurn_FiresWithTurnNumber1()
    {
        var latch = new ManualResetEventSlim(false);
        var capturedTurn = 0;

        var bot = new TckBot(Server.ServerUrl);
        bot.OnSkippedTurnAction = e =>
        {
            capturedTurn = e.TurnNumber;
            latch.Set();
        };
        StartAsync(bot);

        AwaitGameStarted(bot);
        AwaitTick(bot);
        Server.SendRaw("{\"type\":\"SkippedTurnEvent\",\"turnNumber\":1}");
        Thread.Sleep(100); // Allow WebSocket receive thread to process the event before GoAsync
        GoAsync(bot);
        Server.ContinueBotIntent();
        AwaitBotIntent();

        Assert.That(latch.Wait(3000), Is.True);
        Assert.That(capturedTurn, Is.EqualTo(1));
    }

    // -----------------------------------------------------------------------
    // TCK-013: Unknown server message type triggers onConnectionError
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-013")]
    public void Tck013_UnknownMessageType_TriggersOnConnectionError()
    {
        var latch = new ManualResetEventSlim(false);
        var capturedMessage = string.Empty;

        var bot = new TckBot(Server.ServerUrl);
        bot.OnConnectionErrorAction = e =>
        {
            capturedMessage = e.Exception.Message;
            latch.Set();
        };
        StartAsync(bot);

        Assert.That(Server.AwaitBotReadyMessage(3000), Is.True);
        Server.SendRaw("{\"type\":\"UnknownMessageType\",\"data\":\"test\"}");

        Assert.That(latch.Wait(3000), Is.True);
        Assert.That(capturedMessage, Does.Contain("Unsupported WebSocket message type"));
    }

    // -----------------------------------------------------------------------
    // TCK-014: BotDeathEvent(victimId==myId) triggers onDeath
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-014")]
    public void Tck014_BotDeathEventSelf_TriggersOnDeath()
    {
        var latch = new ManualResetEventSlim(false);

        var bot = new TckBot(Server.ServerUrl);
        bot.OnDeathAction = _ => latch.Set();
        StartAsync(bot);
        AwaitGameStarted(bot);
        AwaitTick(bot);
        Server.SendRaw(BuildTickWithEvent(
            $"{{\"type\":\"BotDeathEvent\",\"turnNumber\":2,\"victimId\":{MockedServer.MyId}}}"));
        AwaitTurnNumber(bot, 2);
        GoAsync(bot);
        Server.ContinueBotIntent();
        AwaitBotIntent();

        Assert.That(latch.Wait(3000), Is.True);
    }

    // -----------------------------------------------------------------------
    // TCK-015: BotDeathEvent(victimId!=myId) triggers onBotDeath
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-015")]
    public void Tck015_BotDeathEventOther_TriggersOnBotDeath()
    {
        var latch = new ManualResetEventSlim(false);
        var capturedId = 0;

        var bot = new TckBot(Server.ServerUrl);
        bot.OnBotDeathAction = e =>
        {
            capturedId = e.VictimId;
            latch.Set();
        };
        StartAsync(bot);
        AwaitGameStarted(bot);
        AwaitTick(bot);
        Server.SendRaw(BuildTickWithEvent(
            "{\"type\":\"BotDeathEvent\",\"turnNumber\":2,\"victimId\":99}"));
        AwaitTurnNumber(bot, 2);
        GoAsync(bot);
        Server.ContinueBotIntent();
        AwaitBotIntent();

        Assert.That(latch.Wait(3000), Is.True);
        Assert.That(capturedId, Is.EqualTo(99));
    }

    // -----------------------------------------------------------------------
    // TCK-016: BulletHitBotEvent(victimId==myId) triggers onHitByBullet
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-016")]
    public void Tck016_BulletHitBotEventSelf_TriggersOnHitByBullet()
    {
        var latch = new ManualResetEventSlim(false);

        var bot = new TckBot(Server.ServerUrl);
        bot.OnHitByBulletAction = _ => latch.Set();
        StartAsync(bot);
        AwaitGameStarted(bot);
        AwaitTick(bot);
        Server.SendRaw(BuildTickWithEvent(BuildBulletHitBotEventJson(MockedServer.MyId)));
        AwaitTurnNumber(bot, 2);
        GoAsync(bot);
        Server.ContinueBotIntent();
        AwaitBotIntent();

        Assert.That(latch.Wait(3000), Is.True);
    }

    // -----------------------------------------------------------------------
    // TCK-017: BulletHitBotEvent(victimId!=myId) triggers onBulletHit
    // -----------------------------------------------------------------------

    [Test]
    [Property("ID", "TR-API-TCK-017")]
    public void Tck017_BulletHitBotEventOther_TriggersOnBulletHit()
    {
        var latch = new ManualResetEventSlim(false);

        var bot = new TckBot(Server.ServerUrl);
        bot.OnBulletHitAction = _ => latch.Set();
        StartAsync(bot);
        AwaitGameStarted(bot);
        AwaitTick(bot);
        Server.SendRaw(BuildTickWithEvent(BuildBulletHitBotEventJson(99)));
        AwaitTurnNumber(bot, 2);
        GoAsync(bot);
        Server.ContinueBotIntent();
        AwaitBotIntent();

        Assert.That(latch.Wait(3000), Is.True);
    }

    // -----------------------------------------------------------------------
    // JSON builders
    // -----------------------------------------------------------------------

    private static string BuildTickWithEvent(string eventJson)
    {
        return "{\"type\":\"TickEventForBot\",\"roundNumber\":1,\"turnNumber\":2,"
            + "\"botState\":{\"isDroid\":false,\"energy\":100.0,\"x\":100.0,\"y\":100.0,"
            + "\"direction\":0.0,\"gunDirection\":0.0,\"radarDirection\":0.0,\"radarSweep\":0.0,"
            + "\"speed\":0.0,\"turnRate\":0.0,\"gunTurnRate\":0.0,\"radarTurnRate\":0.0,"
            + "\"gunHeat\":0.0,\"enemyCount\":0,\"isDebuggingEnabled\":false},"
            + "\"bulletStates\":[],"
            + "\"events\":[" + eventJson + "]}";
    }

    private static string BuildRoundEndedJson(int roundNumber, int turnNumber)
    {
        return "{\"type\":\"RoundEndedEventForBot\","
            + $"\"roundNumber\":{roundNumber},"
            + $"\"turnNumber\":{turnNumber},"
            + "\"results\":{\"rank\":1,\"survival\":0,\"lastSurvivorBonus\":0,"
            + "\"bulletDamage\":0,\"bulletKillBonus\":0,\"ramDamage\":0,\"ramKillBonus\":0,"
            + "\"totalScore\":0,\"firstPlaces\":0,\"secondPlaces\":0,\"thirdPlaces\":0}}";
    }

    private static string BuildGameEndedJson(int numberOfRounds)
    {
        return "{\"type\":\"GameEndedEventForBot\","
            + $"\"numberOfRounds\":{numberOfRounds},"
            + "\"results\":{\"rank\":1,\"survival\":0,\"lastSurvivorBonus\":0,"
            + "\"bulletDamage\":0,\"bulletKillBonus\":0,\"ramDamage\":0,\"ramKillBonus\":0,"
            + "\"totalScore\":0,\"firstPlaces\":0,\"secondPlaces\":0,\"thirdPlaces\":0}}";
    }

    private static string BuildBulletHitBotEventJson(int victimId)
    {
        return $"{{\"type\":\"BulletHitBotEvent\",\"turnNumber\":2,\"victimId\":{victimId},"
            + "\"bullet\":{\"bulletId\":1,\"ownerId\":2,\"power\":1.0,\"x\":50.0,\"y\":50.0,\"direction\":90.0},"
            + "\"damage\":4.0,\"energy\":96.0}";
    }

    // -----------------------------------------------------------------------
    // TckBot: BaseBot with delegate hooks for each callback under test
    // -----------------------------------------------------------------------

    private class TckBot : BaseBot
    {
        public Action<RoundStartedEvent> OnRoundStartedAction { get; set; }
        public Action<RoundEndedEvent> OnRoundEndedAction { get; set; }
        public Action<GameEndedEvent> OnGameEndedAction { get; set; }
        public Action<SkippedTurnEvent> OnSkippedTurnAction { get; set; }
        public Action<ConnectionErrorEvent> OnConnectionErrorAction { get; set; }
        public Action<DeathEvent> OnDeathAction { get; set; }
        public Action<BotDeathEvent> OnBotDeathAction { get; set; }
        public Action<HitByBulletEvent> OnHitByBulletAction { get; set; }
        public Action<BulletHitBotEvent> OnBulletHitAction { get; set; }

        public TckBot(Uri serverUrl) : base(BotInfo, serverUrl) { }

        public override void OnRoundStarted(RoundStartedEvent botEvent) => OnRoundStartedAction?.Invoke(botEvent);
        public override void OnRoundEnded(RoundEndedEvent botEvent) => OnRoundEndedAction?.Invoke(botEvent);
        public override void OnGameEnded(GameEndedEvent botEvent) => OnGameEndedAction?.Invoke(botEvent);
        public override void OnSkippedTurn(SkippedTurnEvent botEvent) => OnSkippedTurnAction?.Invoke(botEvent);
        public override void OnConnectionError(ConnectionErrorEvent botEvent) => OnConnectionErrorAction?.Invoke(botEvent);
        public override void OnDeath(DeathEvent botEvent) => OnDeathAction?.Invoke(botEvent);
        public override void OnBotDeath(BotDeathEvent botEvent) => OnBotDeathAction?.Invoke(botEvent);
        public override void OnHitByBullet(HitByBulletEvent botEvent) => OnHitByBulletAction?.Invoke(botEvent);
        public override void OnBulletHit(BulletHitBotEvent botEvent) => OnBulletHitAction?.Invoke(botEvent);
    }
}
