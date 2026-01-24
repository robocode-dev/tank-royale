# Quick Reference: Team Messages in C# Bot API

## Overview

Team messages allow bots in the same team to communicate with each other during a battle. Messages are serialized to
JSON and deserialized on the receiving end.

## How to Send Team Messages

### Send to All Teammates

```csharp
BroadcastTeamMessage(messageObject);
```

### Send to Specific Teammate

```csharp
SendTeamMessage(teammateId, messageObject);
```

## Message Classes

Message classes should be simple POCOs (Plain Old CLR Objects) with public properties:

```csharp
class Point
{
    public double X { get; set; }
    public double Y { get; set; }
}

class RobotColors
{
    public Color BodyColor { get; set; }
    public Color GunColor { get; set; }
    // ... other properties
}
```

## Important Rules

1. **Each bot defines its own message classes** - MyFirstLeader and MyFirstDroid each have their own `Point` and
   `RobotColors` classes

2. **Classes are matched by name** - The type name (e.g., "RobotColors") is used to find the corresponding class in the
   receiving bot's assembly

3. **Use the global namespace or consistent namespaces** - Classes in the global namespace work best for cross-bot
   communication

4. **Properties must be public** - JSON serialization requires public getters and setters

5. **Color properties are automatically handled** - The `Color` type has a custom JSON converter that serializes to hex
   format (#RRGGBBAA)

## Receiving Team Messages

Override the `OnTeamMessage` method:

```csharp
public override void OnTeamMessage(TeamMessageEvent evt)
{
    var message = evt.Message;
    
    if (message is Point point)
    {
        // Handle point message
        TurnRight(BearingTo(point.X, point.Y));
        Fire(3);
    }
    else if (message is RobotColors colors)
    {
        // Handle colors message
        BodyColor = colors.BodyColor;
        GunColor = colors.GunColor;
    }
}
```

## Example: MyFirstTeam

### MyFirstLeader (Sender)

```csharp
public override void Run()
{
    // Create and send colors to team
    var colors = new RobotColors {
        BodyColor = Color.Red,
        GunColor = Color.Yellow
    };
    BroadcastTeamMessage(colors);
    
    // ... bot logic ...
}

public override void OnScannedBot(ScannedBotEvent evt)
{
    if (!IsTeammate(evt.ScannedBotId))
    {
        // Send enemy position to teammates
        BroadcastTeamMessage(new Point(evt.X, evt.Y));
    }
}
```

### MyFirstDroid (Receiver)

```csharp
public override void OnTeamMessage(TeamMessageEvent evt)
{
    var message = evt.Message;
    
    if (message is Point target)
    {
        TurnRight(BearingTo(target.X, target.Y));
        Fire(3);
    }
    else if (message is RobotColors colors)
    {
        BodyColor = colors.BodyColor;
        GunColor = colors.GunColor;
    }
}
```

## Troubleshooting

### Colors Not Showing

- Ensure `RobotColors` class has the same property names on both sender and receiver
- Check that colors are set AFTER the message is received in `OnTeamMessage`

### Messages Not Received

- Verify both bots are part of the same team (check team JSON configuration)
- Use `IsTeammate(botId)` to verify team membership
- Check bot console for any error messages

### Type Not Found Error

- Make sure the message class is defined in the receiving bot's file
- Use simple class names without complex namespaces
- Ensure properties match between sender and receiver

## Limitations

- Maximum 10 team messages per turn per bot
- Maximum message size: 32,768 bytes (JSON format)
- Messages must be serializable to JSON (no circular references)
- Complex objects may require custom JSON converters

## Advanced: Custom Message Types

For complex scenarios, you can define custom message types with type discriminators:

```csharp
abstract class TeamMessage
{
    public abstract string Type { get; }
}

class MoveCommand : TeamMessage
{
    public override string Type => "Move";
    public double X { get; set; }
    public double Y { get; set; }
}

class AttackCommand : TeamMessage
{
    public override string Type => "Attack";
    public int TargetId { get; set; }
}
```

Then in `OnTeamMessage`:

```csharp
public override void OnTeamMessage(TeamMessageEvent evt)
{
    if (evt.Message is MoveCommand move)
    {
        // Handle move
    }
    else if (evt.Message is AttackCommand attack)
    {
        // Handle attack
    }
}
```
