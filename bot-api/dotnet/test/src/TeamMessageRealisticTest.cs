using System;
using System.Reflection;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Graphics;
using Robocode.TankRoyale.BotApi.Internal.Json;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Simulates RobotColors and Point classes as they would appear in actual bot source files
/// (not nested classes, defined at top level with no namespace like in MyFirstLeader.cs and MyFirstDroid.cs)
/// </summary>
///

// These classes simulate what's in MyFirstLeader.cs (global namespace, top-level classes)
class RobotColors
{
    public Color BodyColor { get; set; }
    public Color TracksColor { get; set; }
    public Color TurretColor { get; set; }
    public Color GunColor { get; set; }
    public Color RadarColor { get; set; }
    public Color ScanColor { get; set; }
    public Color BulletColor { get; set; }
}

class Point
{
    public double X { get; set; }
    public double Y { get; set; }
}

[TestFixture]
public class TeamMessageRealisticTest
{
    [Test]
    public void TestRealWorldScenario()
    {
        Console.WriteLine("=== Testing Real-World Team Message Scenario ===\n");

        // SENDER SIDE (MyFirstLeader)
        Console.WriteLine("--- SENDER SIDE (MyFirstLeader) ---");
        var leaderColors = new RobotColors
        {
            BodyColor = Color.Red,
            TracksColor = Color.Cyan,
            TurretColor = Color.Red,
            GunColor = Color.Yellow,
            RadarColor = Color.Red,
            ScanColor = Color.Yellow,
            BulletColor = Color.Yellow
        };

        // This is what BaseBotInternals.SendTeamMessage does
        var messageType = leaderColors.GetType().ToString();
        var json = JsonConverter.ToJson(leaderColors);

        Console.WriteLine($"Message Type (ToString): {messageType}");
        Console.WriteLine($"Message Type (FullName): {leaderColors.GetType().FullName}");
        Console.WriteLine($"Message Type (Name): {leaderColors.GetType().Name}");
        Console.WriteLine($"JSON Length: {json.Length} bytes");
        Console.WriteLine($"JSON: {json}\n");

        // RECEIVER SIDE (MyFirstDroid)
        Console.WriteLine("--- RECEIVER SIDE (MyFirstDroid) ---");
        Console.WriteLine("Simulating EventMapper.Map(TeamMessageEvent, baseBot)...\n");

        // Get the receiver's assembly (in real scenario, this would be MyFirstDroid.exe)
        var receiverAssembly = Assembly.GetExecutingAssembly();
        Console.WriteLine($"Receiver Assembly: {receiverAssembly.GetName().Name}");

        // Try to find the type using our strategies
        Type? foundType = null;

        // Strategy 1: Direct lookup
        Console.WriteLine($"\nStrategy 1: botAssembly.GetType(\"{messageType}\")");
        foundType = receiverAssembly.GetType(messageType);
        Console.WriteLine($"  Result: {foundType?.FullName ?? "NULL"}");

        // Strategy 2: Search by name
        if (foundType == null)
        {
            Console.WriteLine($"\nStrategy 2: Search all types in assembly");
            var simpleTypeName = messageType.Contains('.') ? messageType.Substring(messageType.LastIndexOf('.') + 1) : messageType;
            Console.WriteLine($"  Simple type name: {simpleTypeName}");

            foreach (var t in receiverAssembly.GetTypes())
            {
                if (t.Name == simpleTypeName || t.FullName == messageType)
                {
                    foundType = t;
                    Console.WriteLine($"  Found: {t.FullName}");
                    break;
                }
            }

            if (foundType == null)
            {
                Console.WriteLine($"  Result: NULL");
            }
        }

        // Strategy 3: Assembly-qualified name
        if (foundType == null)
        {
            Console.WriteLine($"\nStrategy 3: Type.GetType with assembly name");
            var typeName = messageType + "," + receiverAssembly.GetName().Name;
            Console.WriteLine($"  Looking for: {typeName}");
            foundType = Type.GetType(typeName);
            Console.WriteLine($"  Result: {foundType?.FullName ?? "NULL"}");
        }

        // Strategy 4: All assemblies
        if (foundType == null)
        {
            Console.WriteLine($"\nStrategy 4: Type.GetType across all assemblies");
            foundType = Type.GetType(messageType);
            Console.WriteLine($"  Result: {foundType?.FullName ?? "NULL"}");
        }

        // Verify we found it
        Assert.That(foundType, Is.Not.Null, "Should find RobotColors type");
        Console.WriteLine($"\n✓ Successfully found type: {foundType.FullName}");

        // Deserialize
        Console.WriteLine($"\n--- DESERIALIZATION ---");
        var receivedObject = JsonConverter.FromJson(json, foundType);

        Assert.That(receivedObject, Is.Not.Null);
        Assert.That(receivedObject, Is.InstanceOf<RobotColors>(), "Should deserialize to RobotColors");

        var receivedColors = (RobotColors)receivedObject;
        Console.WriteLine($"✓ Deserialized successfully");
        Console.WriteLine($"  BodyColor: {receivedColors.BodyColor} (expected: {Color.Red})");
        Console.WriteLine($"  GunColor: {receivedColors.GunColor} (expected: {Color.Yellow})");
        Console.WriteLine($"  TracksColor: {receivedColors.TracksColor} (expected: {Color.Cyan})");

        Assert.That(receivedColors.BodyColor, Is.EqualTo(Color.Red));
        Assert.That(receivedColors.GunColor, Is.EqualTo(Color.Yellow));
        Assert.That(receivedColors.TracksColor, Is.EqualTo(Color.Cyan));

        Console.WriteLine($"\n✓✓✓ TEST PASSED - Team messages work correctly! ✓✓✓");
    }

    [Test]
    public void TestPointMessage()
    {
        Console.WriteLine("=== Testing Point Message ===\n");

        var leaderPoint = new Point { X = 100.5, Y = 200.7 };

        var messageType = leaderPoint.GetType().ToString();
        var json = JsonConverter.ToJson(leaderPoint);

        Console.WriteLine($"Message Type: {messageType}");
        Console.WriteLine($"JSON: {json}\n");

        // Find type in receiver's assembly
        var receiverAssembly = Assembly.GetExecutingAssembly();
        Type? foundType = receiverAssembly.GetType(messageType);

        if (foundType == null)
        {
            var simpleTypeName = messageType.Contains('.') ? messageType.Substring(messageType.LastIndexOf('.') + 1) : messageType;
            foreach (var t in receiverAssembly.GetTypes())
            {
                if (t.Name == simpleTypeName || t.FullName == messageType)
                {
                    foundType = t;
                    break;
                }
            }
        }

        Assert.That(foundType, Is.Not.Null, "Should find Point type");
        Console.WriteLine($"Found type: {foundType.FullName}");

        var receivedObject = JsonConverter.FromJson(json, foundType);
        Assert.That(receivedObject, Is.InstanceOf<Point>());

        var receivedPoint = (Point)receivedObject;
        Console.WriteLine($"Point X: {receivedPoint.X}, Y: {receivedPoint.Y}");

        Assert.That(receivedPoint.X, Is.EqualTo(100.5));
        Assert.That(receivedPoint.Y, Is.EqualTo(200.7));

        Console.WriteLine($"\n✓ TEST PASSED");
    }
}
