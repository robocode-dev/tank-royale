using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using Newtonsoft.Json;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Internal;
using Color = Robocode.TankRoyale.BotApi.Graphics.Color;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

using ApiBotInfo = Robocode.TankRoyale.BotApi.BotInfo;
using ApiBotIntent = Robocode.TankRoyale.Schema.BotIntent;

[TestFixture]
public class SharedTestRunner
{
    private static readonly string SharedTestsDir = Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "../../../../tests/shared"));

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

        public override string ToString() => $"{Id}: {Description}";
    }

    public static IEnumerable<TestCaseData> GetSharedTestCases()
    {
        var dir = SharedTestsDir;
        if (!Directory.Exists(dir))
        {
            dir = Path.GetFullPath(Path.Combine(Directory.GetCurrentDirectory(), "bot-api/tests/shared"));
        }

        if (!Directory.Exists(dir)) yield break;

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
                        (string)args[0], (string)args[1], ((Newtonsoft.Json.Linq.JArray)args[2]).ToObject<List<string>>(),
                        args.Length > 3 ? (string)args[3] : null, args.Length > 4 ? (string)args[4] : null,
                        args.Length > 5 ? ((Newtonsoft.Json.Linq.JArray)args[5]).ToObject<List<string>>() : null,
                        args.Length > 6 ? ((Newtonsoft.Json.Linq.JArray)args[6]).ToObject<HashSet<string>>() : null,
                        args.Length > 7 ? (string)args[7] : null, args.Length > 8 ? (string)args[8] : null, null);
                    break;
                case "fromRgb": lastActionValue = Color.FromRgb(Convert.ToInt32(args[0]), Convert.ToInt32(args[1]), Convert.ToInt32(args[2])); break;
                case "fromRgba": lastActionValue = Color.FromRgba(Convert.ToInt32(args[0]), Convert.ToInt32(args[1]), Convert.ToInt32(args[2]), Convert.ToInt32(args[3])); break;
                case "colorToHex": lastActionValue = IntentValidator.ColorToHex((Color)args[0]); break;
                case "getColorConstant": lastActionValue = GetStaticField(typeof(Color), (string)args[0]); break;
                case "getConstant": lastActionValue = GetStaticField(typeof(Constants), (string)args[0]); break;
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
                if (parsed is double d1 && lastActionValue is double d2) Assert.That(d2, Is.EqualTo(d1).Within(1e-6));
                else if (parsed is string s1 && lastActionValue is string s2 && s1.StartsWith("#")) Assert.That(s2, Is.EqualTo(s1).IgnoreCase);
                else Assert.That(lastActionValue, Is.EqualTo(parsed));
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
            return Color.FromRgba(jobj["r"].Value<int>(), jobj["g"].Value<int>(), jobj["b"].Value<int>(), jobj["a"]?.Value<int>() ?? 255);
        }
        return arg is long l ? (double)l : arg;
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
        var flags = BindingFlags.Public | BindingFlags.Static | BindingFlags.IgnoreCase;
        return (object)type.GetField(fieldName, flags)?.GetValue(null) ?? type.GetProperty(fieldName, flags)?.GetValue(null);
    }
}
