using System;
using System.Reflection;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Graphics;
using Robocode.TankRoyale.BotApi.Internal.Json;
using Robocode.TankRoyale.BotApi.Mapper;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Test to simulate team message serialization/deserialization between MyFirstLeader and MyFirstDroid.
/// This test reproduces the exact scenario where colors are sent from leader to droid.
/// </summary>
/// (TR-API-TCK-006)
/// </summary>
[TestFixture]
[Category("TCK")]
[Property("ID", "TR-API-TCK-006")]
public class TeamMessageSerializationTest
{
    // Simulated message classes from MyFirstLeader
    class LeaderBot {
        public class Point
        {
            public double X { get; set; }
            public double Y { get; set; }
        }
        public class RobotColors
        {
            public Color BodyColor { get; set; }
            public Color TracksColor { get; set; }
            public Color TurretColor { get; set; }
            public Color GunColor { get; set; }
            public Color RadarColor { get; set; }
            public Color ScanColor { get; set; }
            public Color BulletColor { get; set; }
        }
    }

    // Simulated message classes from MyFirstDroid (separate definitions, SAME class name)
    class DroidBot {
        public class Point
        {
            public double X { get; set; }
            public double Y { get; set; }
        }
        public class RobotColors
        {
            public Color BodyColor { get; set; }
            public Color TracksColor { get; set; }
            public Color TurretColor { get; set; }
            public Color GunColor { get; set; }
            public Color RadarColor { get; set; }
            public Color ScanColor { get; set; }
            public Color BulletColor { get; set; }
        }
    }

    [Test]
    public void TestColorsSerialization()
    {
        Console.WriteLine("=== Testing RobotColors Serialization ===");

        // Create colors message as MyFirstLeader would
        var leaderColors = new LeaderBot.RobotColors
        {
            BodyColor = Color.Red,
            TracksColor = Color.Cyan,
            TurretColor = Color.Red,
            GunColor = Color.Yellow,
            RadarColor = Color.Red,
            ScanColor = Color.Yellow,
            BulletColor = Color.Yellow
        };

        // Simulate what BaseBotInternals.SendTeamMessage does
        var messageType = leaderColors.GetType().ToString();
        var json = JsonConverter.ToJson(leaderColors);

        Console.WriteLine($"Message Type: {messageType}");
        Console.WriteLine($"Message Type FullName: {leaderColors.GetType().FullName}");
        Console.WriteLine($"Message Type Name: {leaderColors.GetType().Name}");
        Console.WriteLine($"Message Type AssemblyQualifiedName: {leaderColors.GetType().AssemblyQualifiedName}");
        Console.WriteLine($"JSON: {json}");

        // Now simulate receiving on MyFirstDroid side
        Console.WriteLine("\n=== Attempting to deserialize as DroidBot.RobotColors ===");

        // Try to find the type in the current assembly (simulating the droid's assembly)
        var currentAssembly = Assembly.GetExecutingAssembly();
        Console.WriteLine($"Current Assembly: {currentAssembly.GetName().Name}");

        // Strategy 1: Direct lookup
        var type1 = currentAssembly.GetType(messageType);
        Console.WriteLine($"Strategy 1 (Direct lookup): {type1?.FullName ?? "NULL"}");

        // Strategy 2: Search by name
        Type? type2 = null;
        var simpleTypeName = messageType.Contains('.') ? messageType.Substring(messageType.LastIndexOf('.') + 1) : messageType;
        if (simpleTypeName.Contains('+')) simpleTypeName = simpleTypeName.Substring(simpleTypeName.LastIndexOf('+') + 1);
        Console.WriteLine($"Simple type name: {simpleTypeName}");

        foreach (var t in currentAssembly.GetTypes())
        {
            // Specifically look for DroidBot.RobotColors to simulate the droid's local type
            if (t.DeclaringType?.Name == "DroidBot" && t.Name == simpleTypeName)
            {
                type2 = t;
                Console.WriteLine($"Strategy 2 (Search by name): Found {t.FullName}");
                break;
            }
        }

        if (type2 == null)
        {
            Console.WriteLine("Strategy 2 (Search by name): NULL");
        }

        // Strategy 3: Assembly-qualified name
        var typeName3 = messageType + "," + currentAssembly.GetName().Name;
        var type3 = Type.GetType(typeName3);
        Console.WriteLine($"Strategy 3 (Assembly-qualified): {type3?.FullName ?? "NULL"} using '{typeName3}'");

        // Strategy 4: All loaded assemblies
        var type4 = Type.GetType(messageType);
        Console.WriteLine($"Strategy 4 (All assemblies): {type4?.FullName ?? "NULL"}");

        // Use the found type
        var foundType = type2 ?? type1 ?? type3 ?? type4;

        if (foundType != null)
        {
            Console.WriteLine($"\n=== Successfully found type: {foundType.FullName} ===");

            // Try to deserialize
            var droidColors = JsonConverter.FromJson(json, foundType);
            Console.WriteLine($"Deserialized object: {droidColors}");

            if (droidColors is DroidBot.RobotColors colors)
            {
                Console.WriteLine($"BodyColor: {colors.BodyColor}");
                Console.WriteLine($"GunColor: {colors.GunColor}");
                Assert.That(colors.BodyColor, Is.EqualTo(Color.Red));
                Assert.That(colors.GunColor, Is.EqualTo(Color.Yellow));
            }
            else
            {
                Console.WriteLine($"WARNING: Deserialized to wrong type: {droidColors.GetType()}");
            }
        }
        else
        {
            Console.WriteLine("\n=== FAILED: Could not find type ===");
            Assert.Fail($"Could not find type '{messageType}' in any strategy");
        }
    }

    [Test]
    public void TestPointSerialization()
    {
        Console.WriteLine("=== Testing Point Serialization ===");

        // Create point message as MyFirstLeader would
        var leaderPoint = new LeaderBot.Point { X = 100.5, Y = 200.7 };

        // Simulate what BaseBotInternals.SendTeamMessage does
        var messageType = leaderPoint.GetType().ToString();
        var json = JsonConverter.ToJson(leaderPoint);

        Console.WriteLine($"Message Type: {messageType}");
        Console.WriteLine($"JSON: {json}");

        // Now simulate receiving on MyFirstDroid side
        var currentAssembly = Assembly.GetExecutingAssembly();

        // Try all strategies
        Type? foundType = null;

        // Strategy 1
        foundType = currentAssembly.GetType(messageType);

        // Strategy 2
        if (foundType == null || foundType.DeclaringType?.Name == "LeaderBot")
        {
            var simpleTypeName = messageType.Contains('.') ? messageType.Substring(messageType.LastIndexOf('.') + 1) : messageType;
            if (simpleTypeName.Contains('+')) simpleTypeName = simpleTypeName.Substring(simpleTypeName.LastIndexOf('+') + 1);

            foreach (var t in currentAssembly.GetTypes())
            {
                // Specifically look for DroidBot.Point to simulate the droid's local type
                if (t.DeclaringType?.Name == "DroidBot" && t.Name == simpleTypeName)
                {
                    foundType = t;
                    break;
                }
            }
        }

        // Strategy 3
        if (foundType == null)
        {
            var typeName = messageType + "," + currentAssembly.GetName().Name;
            foundType = Type.GetType(typeName);
        }

        // Strategy 4
        if (foundType == null)
        {
            foundType = Type.GetType(messageType);
        }

        Assert.That(foundType, Is.Not.Null, $"Could not find type '{messageType}'");
        Console.WriteLine($"Found type: {foundType.FullName}");

        // Deserialize
        var droidPoint = JsonConverter.FromJson(json, foundType);

        if (droidPoint is DroidBot.Point point)
        {
            Console.WriteLine($"Point X: {point.X}, Y: {point.Y}");
            Assert.That(point.X, Is.EqualTo(100.5));
            Assert.That(point.Y, Is.EqualTo(200.7));
        }
        else
        {
            Assert.Fail($"Deserialized to wrong type: {droidPoint.GetType()}");
        }
    }

    [Test]
    public void TestGetTypeToStringBehavior()
    {
        Console.WriteLine("=== Testing GetType().ToString() Behavior ===");

        var colors = new LeaderBot.RobotColors();
        var point = new LeaderBot.Point();

        Console.WriteLine($"LeaderBot.RobotColors.GetType().ToString(): '{colors.GetType().ToString()}'");
        Console.WriteLine($"LeaderBot.RobotColors.GetType().FullName: '{colors.GetType().FullName}'");
        Console.WriteLine($"LeaderBot.RobotColors.GetType().Name: '{colors.GetType().Name}'");
        Console.WriteLine($"LeaderBot.RobotColors.GetType().AssemblyQualifiedName: '{colors.GetType().AssemblyQualifiedName}'");

        Console.WriteLine($"\nLeaderBot.Point.GetType().ToString(): '{point.GetType().ToString()}'");
        Console.WriteLine($"LeaderBot.Point.GetType().FullName: '{point.GetType().FullName}'");
        Console.WriteLine($"LeaderBot.Point.GetType().Name: '{point.GetType().Name}'");
        Console.WriteLine($"LeaderBot.Point.GetType().AssemblyQualifiedName: '{point.GetType().AssemblyQualifiedName}'");

        // Test finding these types
        var assembly = Assembly.GetExecutingAssembly();
        var foundColors = assembly.GetType(colors.GetType().ToString());
        var foundPoint = assembly.GetType(point.GetType().ToString());

        Console.WriteLine($"\nFound LeaderBot.RobotColors using ToString(): {foundColors != null}");
        Console.WriteLine($"Found LeaderBot.Point using ToString(): {foundPoint != null}");

        Assert.That(foundColors, Is.Not.Null, "Should find LeaderBot.RobotColors using GetType().ToString()");
        Assert.That(foundPoint, Is.Not.Null, "Should find LeaderBot.Point using GetType().ToString()");
    }
}
