using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using Newtonsoft.Json;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Internal;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Util;
using Color = Robocode.TankRoyale.BotApi.Graphics.Color;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

using ApiBotInfo = Robocode.TankRoyale.BotApi.BotInfo;
using ApiBotIntent = Robocode.TankRoyale.Schema.BotIntent;

[TestFixture]
public class SharedTestRunner
{
    private static string FindSharedTestsDir()
    {
        var current = AppDomain.CurrentDomain.BaseDirectory;
        while (current != null)
        {
            var path = Path.Combine(current, "bot-api/tests/shared");
            if (Directory.Exists(path)) return path;
            path = Path.Combine(current, "tests/shared"); // if we are already in bot-api
            if (Directory.Exists(path)) return path;
            current = Path.GetDirectoryName(current);
        }
        return null;
    }

    public class TestSuite
    {
        [JsonProperty("suite")] public string Suite { get; set; }
        [JsonProperty("description")] public string Description { get; set; }
        [JsonProperty("tests")] public List<TestCase> Tests { get; set; }
    }

    public class TestCase
    {
        [JsonProperty("id")] public string Id { get; set; }
        [JsonProperty("description")] public string Description { get; set; }
        [JsonProperty("type")] public string Type { get; set; }
        [JsonProperty("method")] public string Method { get; set; }
        [JsonProperty("setup")] public Dictionary<string, object> Setup { get; set; }
        [JsonProperty("args")] public List<object> Args { get; set; }
        [JsonProperty("expected")] public Dictionary<string, object> Expected { get; set; }
        [JsonProperty("steps")] public List<Dictionary<string, object>> Steps { get; set; }
        [JsonProperty("expectAfter")] public Dictionary<string, object> ExpectAfter { get; set; }

        public override string ToString() => $"{Id}: {Description}";
    }

    public static IEnumerable<TestCaseData> GetSharedTestCases()
    {
        var dir = FindSharedTestsDir();
        if (dir == null || !Directory.Exists(dir)) yield break;

        foreach (var file in Directory.GetFiles(dir, "*.json"))
        {
            if (file.EndsWith("schema.json")) continue;
            var suite = JsonConvert.DeserializeObject<TestSuite>(File.ReadAllText(file));
            foreach (var testCase in suite.Tests)
            {
                yield return new TestCaseData(suite.Suite, testCase).SetName($"{suite.Suite} | {testCase}");
            }
        }
    }

    [TestCaseSource(nameof(GetSharedTestCases))]
    public void RunSharedTest(string suiteName, TestCase testCase)
    {
        if (testCase.Type == "scenario")
        {
            ExecuteScenario(testCase);
            return;
        }
        if (testCase.Type == "botDefault")
        {
            ExecuteBotDefault(testCase);
            return;
        }

        var mockBot = new TestBot();
        var internals = new BaseBotInternals(mockBot, null, null, null);

        if (testCase.Setup != null)
        {
            if (testCase.Setup.TryGetValue("energy", out var energy)) mockBot.Energy = Convert.ToDouble(energy);
            if (testCase.Setup.TryGetValue("gunHeat", out var gunHeat)) mockBot.GunHeat = Convert.ToDouble(gunHeat);
            if (testCase.Setup.TryGetValue("maxSpeed", out var maxSpeed)) internals.MaxSpeed = Convert.ToDouble(maxSpeed);
            if (testCase.Setup.TryGetValue("maxTurnRate", out var maxTurnRate)) internals.MaxTurnRate = Convert.ToDouble(maxTurnRate);
            if (testCase.Setup.TryGetValue("maxGunTurnRate", out var maxGunTurnRate)) internals.MaxGunTurnRate = Convert.ToDouble(maxGunTurnRate);
            if (testCase.Setup.TryGetValue("maxRadarTurnRate", out var maxRadarTurnRate)) internals.MaxRadarTurnRate = Convert.ToDouble(maxRadarTurnRate);
        }

        // Mock a tick event so methods that depend on it (like SetFire) don't throw
        var tickEvent = new Robocode.TankRoyale.BotApi.Events.TickEvent(
            0, 0, new Robocode.TankRoyale.BotApi.BotState(
                false, mockBot.Energy, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, mockBot.GunHeat, 0, null, null, null, null, null, null, null, false),
            new List<Robocode.TankRoyale.BotApi.BulletState>(), new List<Robocode.TankRoyale.BotApi.Events.BotEvent>());
        typeof(BaseBotInternals).GetField("_tickEvent", BindingFlags.NonPublic | BindingFlags.Instance).SetValue(internals, tickEvent);

        object lastActionValue = null;

        Action action = () =>
        {
            var args = testCase.Args?.Select(ParseArg).ToArray() ?? Array.Empty<object>();
            switch (testCase.Method)
            {
                case "setFire": lastActionValue = internals.SetFire((double)args[0]); break;
                case "setTurnRate": internals.TurnRate = (double)args[0]; break;
                case "setGunTurnRate": internals.GunTurnRate = (double)args[0]; break;
                case "setRadarTurnRate": internals.RadarTurnRate = (double)args[0]; break;
                case "setTargetSpeed": internals.TargetSpeed = (double)args[0]; break;
                case "setMaxSpeed": internals.MaxSpeed = (double)args[0]; break;
                case "setMaxTurnRate": internals.MaxTurnRate = (double)args[0]; break;
                case "getNewTargetSpeed": lastActionValue = IntentValidator.GetNewTargetSpeed((double)args[0], (double)args[1], (double)args[2]); break;
                case "getDistanceTraveledUntilStop": lastActionValue = IntentValidator.GetDistanceTraveledUntilStop((double)args[0], (double)args[1]); break;
                case "BotInfo":
                    lastActionValue = new ApiBotInfo(
                        (string)args[0], (string)args[1], (List<string>)args[2],
                        args.Length > 3 ? (string)args[3] : null, args.Length > 4 ? (string)args[4] : null,
                        args.Length > 5 ? (List<string>)args[5] : null,
                        args.Length > 6 ? (args[6] != null ? new HashSet<string>((List<string>)args[6]) : null) : null,
                        args.Length > 7 ? (string)args[7] : null, args.Length > 8 ? (string)args[8] : null, null);
                    break;
                case "fromRgb": lastActionValue = Color.FromRgb(Convert.ToUInt32(args[0]), Convert.ToUInt32(args[1]), Convert.ToUInt32(args[2])); break;
                case "fromRgba": lastActionValue = Color.FromRgba(Convert.ToUInt32(args[0]), Convert.ToUInt32(args[1]), Convert.ToUInt32(args[2]), Convert.ToUInt32(args[3])); break;
                case "colorToHex": lastActionValue = IntentValidator.ColorToHex((Color)args[0]); break;
                case "getColorConstant": lastActionValue = GetStaticField(typeof(Color), (string)args[0]); break;
                case "getConstant": lastActionValue = GetStaticField(typeof(Constants), (string)args[0]); break;
                case "isCritical": lastActionValue = CreateEvent((string)args[0]).IsCritical; break;
                case "getDefaultPriority": lastActionValue = GetStaticField(typeof(DefaultEventPriority), (string)args[0]); break;
                case "calcBulletSpeed": lastActionValue = mockBot.CalcBulletSpeed(Convert.ToDouble(args[0])); break;
                case "calcMaxTurnRate": lastActionValue = mockBot.CalcMaxTurnRate(Convert.ToDouble(args[0])); break;
                case "calcGunHeat": lastActionValue = mockBot.CalcGunHeat(Convert.ToDouble(args[0])); break;
                case "calcBearing":
                    if (args.Length == 2) {
                        mockBot.Direction = Convert.ToDouble(args[0]);
                        lastActionValue = mockBot.CalcBearing(Convert.ToDouble(args[1]));
                    } else {
                        lastActionValue = mockBot.CalcBearing(Convert.ToDouble(args[0]));
                    }
                    break;
                case "normalizeAbsoluteAngle": lastActionValue = mockBot.NormalizeAbsoluteAngle(Convert.ToDouble(args[0])); break;
                case "normalizeRelativeAngle": lastActionValue = mockBot.NormalizeRelativeAngle(Convert.ToDouble(args[0])); break;
                default: throw new NotSupportedException($"Method {testCase.Method} not implemented");
            }
        };

        if (testCase.Expected.TryGetValue("throws", out var expectedEx))
        {
            Assert.Throws<ArgumentException>(() => action());
        }
        else
        {
            action();
            if (testCase.Expected.TryGetValue("returns", out var expected))
            {
                var parsed = ParseArg(expected);
                var actual = lastActionValue;
                if (parsed is double d1 && actual is int i1) actual = (double)i1;
                if (parsed is double d2 && actual is double d3) Assert.That(d3, Is.EqualTo(d2).Within(1e-6));
                else if (parsed is string s1 && actual is string s2 && s1.StartsWith("#")) Assert.That(s2, Is.EqualTo(s1).IgnoreCase);
                else Assert.That(actual, Is.EqualTo(parsed));
            }
            foreach (var entry in testCase.Expected)
            {
                if (entry.Key == "returns" || entry.Key == "throws") continue;
                var actual = GetActualValue(entry.Key, lastActionValue, internals.BotIntent, internals);
                var expectedVal = ParseArg(entry.Value);
                if (expectedVal is double ed && actual is double ad) Assert.That(ad, Is.EqualTo(ed).Within(1e-6));
                else if (expectedVal is IEnumerable<string> ee && actual is IEnumerable<string> ae) Assert.That(ae, Is.EquivalentTo(ee));
                else Assert.That(actual, Is.EqualTo(expectedVal));
            }
        }
    }

    private object ParseArg(object arg)
    {
        if (arg is string s)
        {
            if (s == "NaN") return double.NaN;
            if (s == "Infinity") return double.PositiveInfinity;
            if (s == "-Infinity") return double.NegativeInfinity;
            return s;
        }
        if (arg is Newtonsoft.Json.Linq.JObject jobj && jobj["r"] != null)
        {
            return Color.FromRgba((uint)jobj["r"], (uint)jobj["g"], (uint)jobj["b"], (uint?)jobj["a"] ?? 255u);
        }
        if (arg is Newtonsoft.Json.Linq.JArray jarr)
        {
            return jarr.ToObject<List<string>>();
        }
        if (arg is long l) return (double)l;
        return arg;
    }

    private object GetActualValue(string key, object lastActionValue, ApiBotIntent intent, BaseBotInternals internals)
    {
        switch (key)
        {
            case "firepower": return intent.Firepower ?? 0.0;
            case "turnRate": return intent.TurnRate;
            case "gunTurnRate": return intent.GunTurnRate;
            case "radarTurnRate": return intent.RadarTurnRate;
            case "targetSpeed": return intent.TargetSpeed;
            case "maxSpeed": return internals.MaxSpeed;
            case "maxTurnRate": return internals.MaxTurnRate;
        }
        if (lastActionValue is Color c)
        {
            switch (key)
            {
                case "r": return (double)c.R; case "g": return (double)c.G; case "b": return (double)c.B; case "a": return (double)c.A;
            }
        }
        if (lastActionValue is ApiBotInfo info)
        {
            switch (key)
            {
                case "name": return info.Name; case "version": return info.Version; case "authors": return info.Authors; case "countryCodes": return info.CountryCodes;
            }
        }
        return null;
    }

    private object GetStaticField(Type type, string fieldName)
    {
        var normalized = fieldName.Replace("_", "");
        var flags = BindingFlags.Public | BindingFlags.Static | BindingFlags.IgnoreCase;
        return type.GetField(normalized, flags)?.GetValue(null) ?? type.GetProperty(normalized, flags)?.GetValue(null);
    }

    private BotEvent CreateEvent(string eventName)
    {
        return eventName switch
        {
            "BotDeathEvent" => new BotDeathEvent(0, 0),
            "WonRoundEvent" => new WonRoundEvent(0),
            "SkippedTurnEvent" => new SkippedTurnEvent(0),
            "BotHitBotEvent" => new HitBotEvent(0, 0, 0, 0, 0, false),
            "BotHitWallEvent" => new HitWallEvent(0),
            "BulletFiredEvent" => new BulletFiredEvent(0, new BulletState(0, 0, 0, 0, 0, 0, null)),
            "BulletHitBotEvent" => new BulletHitBotEvent(0, 0, new BulletState(0, 0, 0, 0, 0, 0, null), 0, 0),
            "BulletHitBulletEvent" => new BulletHitBulletEvent(0, new BulletState(0, 0, 0, 0, 0, 0, null), new BulletState(0, 0, 0, 0, 0, 0, null)),
            "BulletHitWallEvent" => new BulletHitWallEvent(0, new BulletState(0, 0, 0, 0, 0, 0, null)),
            "HitByBulletEvent" => new HitByBulletEvent(0, new BulletState(0, 0, 0, 0, 0, 0, null), 0, 0),
            "ScannedBotEvent" => new ScannedBotEvent(0, 0, 0, 0, 0, 0, 0, 0),
            "CustomEvent" => new CustomEvent(0, new TestCondition()),
            "TeamMessageEvent" => new TeamMessageEvent(0, "test", 0),
            "TickEvent" => new TickEvent(0, 0, null, new List<BulletState>(), new List<BotEvent>()),
            "DeathEvent" => new DeathEvent(0),
            "HitWallEvent" => new HitWallEvent(0),
            "HitBotEvent" => new HitBotEvent(0, 0, 0, 0, 0, false),
            _ => throw new ArgumentException($"Unknown event: {eventName}")
        };
    }

    private BotEvent CreateEventAt(string eventName, int turnNumber) => eventName switch
    {
        "WonRoundEvent"    => new WonRoundEvent(turnNumber),
        "DeathEvent"       => new DeathEvent(turnNumber),
        "ScannedBotEvent"  => new ScannedBotEvent(turnNumber, 0, 0, 0, 0, 0, 0, 0),
        "SkippedTurnEvent" => new SkippedTurnEvent(turnNumber),
        "BotDeathEvent"    => new BotDeathEvent(turnNumber, 0),
        _ => throw new ArgumentException($"Unknown event for scenario: {eventName}")
    };

    private void ExecuteBotDefault(TestCase testCase)
    {
        var bot = new BotDefaultStub();
        Func<object> callMethod = testCase.Method switch
        {
            "getMyId"                  => () => bot.MyId,
            "getVariant"               => () => bot.Variant,
            "getVersion"               => () => bot.Version,
            "getEnergy"                => () => bot.Energy,
            "getX"                     => () => bot.X,
            "getY"                     => () => bot.Y,
            "getDirection"             => () => bot.Direction,
            "getGunDirection"          => () => bot.GunDirection,
            "getRadarDirection"        => () => bot.RadarDirection,
            "getSpeed"                 => () => (object)bot.Speed,
            "getGunHeat"               => () => (object)bot.GunHeat,
            "getBulletStates"          => () => bot.BulletStates,
            "getEvents"                => () => bot.Events,
            "getArenaWidth"            => () => (object)bot.ArenaWidth,
            "getArenaHeight"           => () => (object)bot.ArenaHeight,
            "getGameType"              => () => bot.GameType,
            "isAdjustGunForBodyTurn"   => () => (object)bot.AdjustGunForBodyTurn,
            "isAdjustRadarForBodyTurn" => () => (object)bot.AdjustRadarForBodyTurn,
            "isAdjustRadarForGunTurn"  => () => (object)bot.AdjustRadarForGunTurn,
            _ => throw new NotSupportedException($"Unknown botDefault method: {testCase.Method}")
        };

        if (testCase.Expected.ContainsKey("throws"))
        {
            Assert.Throws<BotException>(() => callMethod());
        }
        else
        {
            var result = callMethod();
            if (testCase.Expected.TryGetValue("returns", out var expected))
            {
                var parsed = ParseArg(expected);
                if (parsed is double d && result is double rd)
                    Assert.That(rd, Is.EqualTo(d).Within(1e-6));
                else
                    Assert.That(result, Is.EqualTo(parsed));
            }
            else if (testCase.Expected.ContainsKey("returnsEmpty"))
            {
                var enumerable = result as System.Collections.IEnumerable;
                Assert.That(enumerable?.Cast<object>(), Is.Empty);
            }
        }
    }

    private class BotDefaultStub : BaseBot
    {
        public BotDefaultStub() : base(new ApiBotInfo(
            "StubBot", "1.0", new List<string> { "Author" },
            null, null, null, null, null, null, null))
        { }
    }

    private void ExecuteScenario(TestCase testCase)
    {
        var botStub = new ScenarioBotStub();
        var botInfo = new ApiBotInfo("dummy", "1.0", new List<string> { "dummy" }, null, null, null, null, null, null, null);
        var internals = new BaseBotInternals(botStub, botInfo, new Uri("ws://localhost:7654"), null);
        var queue = new EventQueue(internals, internals.BotEventHandlers);

        foreach (var step in testCase.Steps)
        {
            var action = (string)step["action"];
            if (action == "addEvent")
            {
                var eventType = (string)step["eventType"];
                var turnNumber = Convert.ToInt32(step["turnNumber"]);
                var repeat = step.ContainsKey("repeat") ? Convert.ToInt32(step["repeat"]) : 1;
                for (int i = 0; i < repeat; i++)
                    queue.AddEvent(CreateEventAt(eventType, turnNumber));
            }
            else if (action == "dispatchEvents")
            {
                var atTurn = Convert.ToInt32(step["atTurn"]);
                queue.DispatchEvents(atTurn);
            }
        }

        if (testCase.ExpectAfter.TryGetValue("dispatchOrder", out var dispatchOrderRaw))
        {
            var expectedOrder = ((Newtonsoft.Json.Linq.JArray)dispatchOrderRaw).ToObject<List<string>>();
            var fired = botStub.FiredEvents;
            Assert.That(fired, Has.Count.EqualTo(expectedOrder.Count), "Dispatch count mismatch");
            for (int i = 0; i < expectedOrder.Count; i++)
                Assert.That(fired[i].GetType().Name, Is.EqualTo(expectedOrder[i]), $"Event at index {i} mismatch");
        }
        if (testCase.ExpectAfter.TryGetValue("queueSize", out var queueSizeRaw))
        {
            var expectedSize = Convert.ToInt32(queueSizeRaw);
            Assert.That(queue.Events(999), Has.Count.EqualTo(expectedSize), "Queue size mismatch");
        }
    }

    private class ScenarioBotStub : IBaseBot
    {
        public List<BotEvent> FiredEvents { get; } = new();
        public void Start() {}
        public void Go() {}
        public int MyId => 1;
        public string Variant => "";
        public string Version => "";
        public string GameType => "";
        public int ArenaWidth => 800;
        public int ArenaHeight => 600;
        public int NumberOfRounds => 1;
        public double GunCoolingRate => 0.1;
        public int? MaxInactivityTurns => 450;
        public int TurnTimeout => 30000;
        public int TimeLeft => 0;
        public int RoundNumber => 1;
        public int TurnNumber => 1;
        public int EnemyCount => 0;
        public double Energy { get; set; } = 100;
        public bool IsDisabled => Energy <= 0;
        public double X => 0;
        public double Y => 0;
        public double Direction => 0;
        public double GunDirection => 0;
        public double RadarDirection => 0;
        public double Speed => 0;
        public double GunHeat { get; set; } = 0;
        public IEnumerable<Robocode.TankRoyale.BotApi.BulletState> BulletStates => new List<Robocode.TankRoyale.BotApi.BulletState>();
        public IList<BotEvent> Events => new List<BotEvent>();
        public void ClearEvents() {}
        public double TurnRate { get; set; }
        public double MaxTurnRate { get; set; } = 10;
        public double GunTurnRate { get; set; }
        public double MaxGunTurnRate { get; set; } = 20;
        public double RadarTurnRate { get; set; }
        public double MaxRadarTurnRate { get; set; } = 45;
        public double TargetSpeed { get; set; }
        public double MaxSpeed { get; set; } = 8;
        public bool SetFire(double fp) => false;
        public double Firepower => 0;
        public void SetRescan() {}
        public void SetFireAssist(bool e) {}
        public bool Interruptible { set {} }
        public bool AdjustGunForBodyTurn { get; set; }
        public bool AdjustRadarForBodyTurn { get; set; }
        public bool AdjustRadarForGunTurn { get; set; }
        public void SetStop() {}
        public void SetStop(bool o) {}
        public void SetResume() {}
        public bool IsStopped => false;
        public ICollection<int> TeammateIds => new List<int>();
        public bool IsTeammate(int id) => false;
        public void BroadcastTeamMessage(object m) {}
        public void SendTeamMessage(int id, object m) {}
        public Robocode.TankRoyale.BotApi.Graphics.Color? BodyColor { get; set; }
        public Robocode.TankRoyale.BotApi.Graphics.Color? TurretColor { get; set; }
        public Robocode.TankRoyale.BotApi.Graphics.Color? RadarColor { get; set; }
        public Robocode.TankRoyale.BotApi.Graphics.Color? BulletColor { get; set; }
        public Robocode.TankRoyale.BotApi.Graphics.Color? ScanColor { get; set; }
        public Robocode.TankRoyale.BotApi.Graphics.Color? TracksColor { get; set; }
        public Robocode.TankRoyale.BotApi.Graphics.Color? GunColor { get; set; }
        public bool IsDebuggingEnabled => false;
        public Robocode.TankRoyale.BotApi.Graphics.IGraphics Graphics => null;
        public void OnConnected(ConnectedEvent e) {}
        public void OnDisconnected(DisconnectedEvent e) {}
        public void OnConnectionError(ConnectionErrorEvent e) {}
        public void OnGameStarted(GameStartedEvent e) {}
        public void OnGameEnded(GameEndedEvent e) {}
        public void OnRoundStarted(RoundStartedEvent e) {}
        public void OnRoundEnded(RoundEndedEvent e) {}
        public void OnTick(TickEvent e)                      { FiredEvents.Add(e); }
        public void OnBotDeath(BotDeathEvent e)              { FiredEvents.Add(e); }
        public void OnDeath(DeathEvent e)                    { FiredEvents.Add(e); }
        public void OnHitBot(HitBotEvent e)                  { FiredEvents.Add(e); }
        public void OnHitWall(HitWallEvent e)                { FiredEvents.Add(e); }
        public void OnBulletFired(BulletFiredEvent e)        { FiredEvents.Add(e); }
        public void OnHitByBullet(HitByBulletEvent e)       { FiredEvents.Add(e); }
        public void OnBulletHit(BulletHitBotEvent e)        { FiredEvents.Add(e); }
        public void OnBulletHitBullet(BulletHitBulletEvent e){ FiredEvents.Add(e); }
        public void OnBulletHitWall(BulletHitWallEvent e)   { FiredEvents.Add(e); }
        public void OnScannedBot(ScannedBotEvent e)         { FiredEvents.Add(e); }
        public void OnSkippedTurn(SkippedTurnEvent e)       { FiredEvents.Add(e); }
        public void OnWonRound(WonRoundEvent e)             { FiredEvents.Add(e); }
        public void OnCustomEvent(CustomEvent e)            { FiredEvents.Add(e); }
        public void OnTeamMessage(TeamMessageEvent e)       { FiredEvents.Add(e); }
        public bool AddCustomEvent(Condition c) => false;
        public bool RemoveCustomEvent(Condition c) => false;
        public int GetEventPriority(Type t) => 0;
        public void SetEventPriority(Type t, int p) {}
        public double CalcMaxTurnRate(double s) => 10 - 0.75 * Math.Abs(Math.Clamp(s, -8, 8));
        public double CalcBulletSpeed(double f) => 20 - 3 * Math.Clamp(f, 0.1, 3.0);
        public double CalcGunHeat(double f) => 1 + Math.Clamp(f, 0.1, 3.0) / 5;
        public double CalcBearing(double d) => NormalizeRelativeAngle(d - Direction);
        public double CalcGunBearing(double d) => NormalizeRelativeAngle(d - GunDirection);
        public double CalcRadarBearing(double d) => NormalizeRelativeAngle(d - RadarDirection);
        public double DirectionTo(double x, double y) => NormalizeAbsoluteAngle(180 * Math.Atan2(y - Y, x - X) / Math.PI);
        public double BearingTo(double x, double y) => NormalizeRelativeAngle(DirectionTo(x, y) - Direction);
        public double GunBearingTo(double x, double y) => NormalizeRelativeAngle(DirectionTo(x, y) - GunDirection);
        public double RadarBearingTo(double x, double y) => NormalizeRelativeAngle(DirectionTo(x, y) - RadarDirection);
        public double DistanceTo(double x, double y) => Math.Sqrt(Math.Pow(x - X, 2) + Math.Pow(y - Y, 2));
        public double NormalizeAbsoluteAngle(double a) => (a %= 360) >= 0 ? a : (a + 360);
        public double NormalizeRelativeAngle(double a) => (a %= 360) >= 0 ? ((a < 180) ? a : (a - 360)) : ((a >= -180) ? a : (a + 360));
        public double CalcDeltaAngle(double t, double s) { var a = t - s; a += (a > 180) ? -360 : (a < -180) ? 360 : 0; return a; }
    }

    private class TestCondition : Condition
    {
        public TestCondition() : base("test") {}
        public override bool Test() => true;
    }
}
