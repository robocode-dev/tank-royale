# Team Messages

## Introduction

Team messages allow bots in the same team to communicate with each other during a battle. This enables coordinated
strategies where a leader bot can give orders to teammate bots, or teammates can share information like enemy positions.

This article demonstrates team messaging using the `MyFirstTeam` sample team, which consists of:

- **MyFirstLeader**: A leader bot that scans for enemies and sends commands to teammates
- **MyFirstDroid**: Droid bots (four instances) that follow the leader's orders

> **Note:** Droids are special bots that have more energy but no radar. They rely entirely on team messages from their
> leader to know where enemies are located.

## How Team Messages Work

Team messages are automatically serialized to JSON format when sent and deserialized back to typed objects when
received. This happens internally—you simply send and receive objects without worrying about the serialization details.

The key concepts are:

1. **Broadcasting**: Send a message to all teammates using `broadcastTeamMessage()`
2. **Direct messaging**: Send a message to a specific teammate using `sendTeamMessage()`
3. **Receiving**: Override the `onTeamMessage()` event handler to process incoming messages
4. **Type matching**: Message classes are matched by name between sender and receiver

## Message Types

In the `MyFirstTeam` example, two message types are used:

| Message Type  | Purpose                                             | Sent When                |
|---------------|-----------------------------------------------------|--------------------------|
| `RobotColors` | Synchronize team colors across all bots             | At the start of a round  |
| `Point`       | Share enemy position coordinates for droids to fire | When an enemy is scanned |

## Defining Message Classes

Each bot must define its own message classes. The classes are matched by name, so they must have the same name and
compatible properties on both the sender and receiver.

::: code-group

```java [Java]
// Point (x,y) class
class Point {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

// Robot colors class
class RobotColors {
    public Color bodyColor;
    public Color tracksColor;
    public Color turretColor;
    public Color gunColor;
    public Color radarColor;
    public Color scanColor;
    public Color bulletColor;
}
```

```csharp [C#]
// Point (x,y) class
class Point
{
    public double X { get; set; }
    public double Y { get; set; }

    public Point(double x, double y)
    {
        X = x;
        Y = y;
    }
}

// Robot colors class
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
```

```python [Python]
from dataclasses import dataclass
from typing import Optional
from robocode_tank_royale.bot_api import team_message_type
from robocode_tank_royale.bot_api.color import Color

# Point (x,y) class
@team_message_type
@dataclass
class Point:
    x: float
    y: float

# Robot colors class
@team_message_type
@dataclass
class RobotColors:
    body_color: Optional[Color] = None
    tracks_color: Optional[Color] = None
    turret_color: Optional[Color] = None
    gun_color: Optional[Color] = None
    radar_color: Optional[Color] = None
    scan_color: Optional[Color] = None
    bullet_color: Optional[Color] = None
```

:::

> **Python Note:** In Python, you must use the `@team_message_type` decorator to register message classes for automatic
> serialization and deserialization. The decorator enables typed team messages that work like Java and C#.

## The Leader Bot: Sending Messages

The leader bot is responsible for scanning enemies and coordinating the team. Here's how `MyFirstLeader` sends messages:

### Sending Robot Colors at Round Start

At the beginning of each round, the leader sets up team colors and broadcasts them to all teammates:

::: code-group

```java [Java]

@Override
public void run() {
    // Prepare robot colors to send to teammates
    var colors = new RobotColors();
    colors.bodyColor = Color.RED;
    colors.tracksColor = Color.CYAN;
    colors.turretColor = Color.RED;
    colors.gunColor = Color.YELLOW;
    colors.radarColor = Color.RED;
    colors.scanColor = Color.YELLOW;
    colors.bulletColor = Color.YELLOW;

    // Set our own colors
    setBodyColor(colors.bodyColor);
    setTracksColor(colors.tracksColor);
    setTurretColor(colors.turretColor);
    setGunColor(colors.gunColor);
    setRadarColor(colors.radarColor);
    setScanColor(colors.scanColor);
    setBulletColor(colors.bulletColor);

    // Send RobotColors object to every member in the team
    broadcastTeamMessage(colors);

    // Set radar to turn forever and start moving
    setTurnRadarLeft(Double.POSITIVE_INFINITY);
    while (isRunning()) {
        forward(100);
        back(100);
    }
}
```

```csharp [C#]
public override void Run()
{
    // Prepare robot colors to send to teammates
    var colors = new RobotColors();
    colors.BodyColor = Color.Red;
    colors.TracksColor = Color.Cyan;
    colors.TurretColor = Color.Red;
    colors.GunColor = Color.Yellow;
    colors.RadarColor = Color.Red;
    colors.ScanColor = Color.Yellow;
    colors.BulletColor = Color.Yellow;

    // Set our own colors
    BodyColor = colors.BodyColor;
    TracksColor = colors.TracksColor;
    TurretColor = colors.TurretColor;
    GunColor = colors.GunColor;
    RadarColor = colors.RadarColor;
    ScanColor = colors.ScanColor;
    BulletColor = colors.BulletColor;

    // Send RobotColors object to every member in the team
    BroadcastTeamMessage(colors);

    // Set radar to turn forever and start moving
    SetTurnRadarLeft(Double.PositiveInfinity);
    while (IsRunning)
    {
        Forward(100);
        Back(100);
    }
}
```

```python [Python]
async def run(self) -> None:
    # Prepare robot colors to send to teammates
    colors = RobotColors()
    colors.body_color = Color.RED
    colors.tracks_color = Color.CYAN
    colors.turret_color = Color.RED
    colors.gun_color = Color.YELLOW
    colors.radar_color = Color.RED
    colors.scan_color = Color.YELLOW
    colors.bullet_color = Color.YELLOW

    # Set our own colors
    self.body_color = colors.body_color
    self.tracks_color = colors.tracks_color
    self.turret_color = colors.turret_color
    self.gun_color = colors.gun_color
    self.radar_color = colors.radar_color
    self.scan_color = colors.scan_color
    self.bullet_color = colors.bullet_color

    # Send RobotColors object to every member in the team
    self.broadcast_team_message(colors)

    # Set radar to turn forever and start moving
    self.set_turn_radar_left(float("inf"))
    while self.running:
        await self.forward(100)
        await self.back(100)
```

:::

### Sending Enemy Positions

When the leader scans an enemy (not a teammate), it broadcasts the enemy's position to all droids:

::: code-group

```java [Java]

@Override
public void onScannedBot(ScannedBotEvent e) {
    // Ignore teammates
    if (isTeammate(e.getScannedBotId())) {
        return;
    }
    // Send enemy position to teammates
    broadcastTeamMessage(new Point(e.getX(), e.getY()));
}
```

```csharp [C#]
public override void OnScannedBot(ScannedBotEvent evt)
{
    // Ignore teammates
    if (IsTeammate(evt.ScannedBotId))
    {
        return;
    }
    // Send enemy position to teammates
    BroadcastTeamMessage(new Point(evt.X, evt.Y));
}
```

```python [Python]
async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
    # Ignore teammates
    if self.is_teammate(e.scanned_bot_id):
        return
    # Send enemy position to teammates
    self.broadcast_team_message(Point(x=e.x, y=e.y))
```

:::

## The Droid Bot: Receiving Messages

The droid bots wait for team messages and react accordingly. Since droids have no radar, they depend entirely on the
leader for target information.

### Handling Team Messages

The droid overrides `onTeamMessage()` to handle incoming messages:

::: code-group

```java [Java]

@Override
public void onTeamMessage(TeamMessageEvent e) {
    var message = e.getMessage();

    if (message instanceof Point) {
        // Message is a point towards a target
        var target = (Point) message;

        // Turn body to target
        turnRight(bearingTo(target.x, target.y));

        // Fire hard!
        fire(3);

    } else if (message instanceof RobotColors) {
        // Message contains new robot colors
        var colors = (RobotColors) message;

        setBodyColor(colors.bodyColor);
        setTracksColor(colors.tracksColor);
        setTurretColor(colors.turretColor);
        setGunColor(colors.gunColor);
        setRadarColor(colors.radarColor);
        setScanColor(colors.scanColor);
        setBulletColor(colors.bulletColor);
    }
}
```

```csharp [C#]
public override void OnTeamMessage(TeamMessageEvent evt)
{
    var message = evt.Message;

    if (message is Point target)
    {
        // Message is a point towards a target

        // Turn body to target
        TurnRight(BearingTo(target.X, target.Y));

        // Fire hard!
        Fire(3);
    }
    else if (message is RobotColors colors)
    {
        // Message contains new robot colors
        BodyColor = colors.BodyColor;
        TracksColor = colors.TracksColor;
        TurretColor = colors.TurretColor;
        GunColor = colors.GunColor;
        RadarColor = colors.RadarColor;
        ScanColor = colors.ScanColor;
        BulletColor = colors.BulletColor;
    }
}
```

```python [Python]
async def on_team_message(self, e: TeamMessageEvent) -> None:
    message = e.message

    if isinstance(message, Point):
        # Message is a point towards a target

        # Turn body to target and fire hard
        await self.turn_right(self.bearing_to(message.x, message.y))
        await self.fire(3)

    elif isinstance(message, RobotColors):
        # Message contains new robot colors
        self.body_color = message.body_color
        self.tracks_color = message.tracks_color
        self.turret_color = message.turret_color
        self.gun_color = message.gun_color
        self.radar_color = message.radar_color
        self.scan_color = message.scan_color
        self.bullet_color = message.bullet_color
```

:::

### The Droid Run Loop

The droid's main loop is simple—it just waits for messages:

::: code-group

```java [Java]

@Override
public void run() {
    System.out.println("MyFirstDroid ready");

    while (isRunning()) {
        go(); // Execute next turn, team messages drive the logic
    }
}
```

```csharp [C#]
public override void Run()
{
    Console.WriteLine("MyFirstDroid ready");

    while (IsRunning)
    {
        Go(); // Execute next turn, team messages drive the logic
    }
}
```

```python [Python]
async def run(self) -> None:
    print("MyFirstDroid ready")

    while self.running:
        await self.go()  # Execute next turn, team messages drive the logic
```

:::

## Creating a Team

To create a team, you need a team configuration file (JSON) that specifies the team members:

```json
{
    "name": "MyFirstTeam",
    "version": "1.0",
    "authors": [
        "Mathew Nelson",
        "Flemming N. Larsen"
    ],
    "description": "A sample team. MyFirstLeader scans for enemies, and orders the droids to fire.",
    "teamMembers": [
        "MyFirstLeader",
        "MyFirstDroid",
        "MyFirstDroid",
        "MyFirstDroid",
        "MyFirstDroid"
    ]
}
```

Note that `MyFirstDroid` is listed four times—this means four instances of the droid will be created in the battle.

## Implementing the Droid Interface

To create a droid bot, you must implement the `Droid` interface (or `DroidABC` in Python). This signals to the game
that your bot is a droid with the special characteristics (more energy, no radar):

::: code-group

```java [Java]
public class MyFirstDroid extends Bot implements Droid {
    // ... bot implementation
}
```

```csharp [C#]
public class MyFirstDroid : Bot, Droid
{
    // ... bot implementation
}
```

```python [Python]
from robocode_tank_royale.bot_api.droid_abc import DroidABC

class MyFirstDroid(Bot, DroidABC):
    # ... bot implementation
```

:::

## Message Sending Methods

| Method                                 | Description                           |
|----------------------------------------|---------------------------------------|
| `broadcastTeamMessage(message)`        | Send a message to all teammates       |
| `sendTeamMessage(teammateId, message)` | Send a message to a specific teammate |

## Limitations

- **Maximum messages per turn**: 10 team messages per bot per turn
- **Maximum message size**: 32,768 bytes (JSON format)
- **Serialization**: Messages must be serializable to JSON (no circular references)

## Best Practices

1. **Define identical message classes**: Both sender and receiver must have message classes with the same name and
   compatible properties

2. **Check for teammates**: Always use `isTeammate()` before processing scanned bots to avoid targeting your own team

3. **Keep messages simple**: Use simple data types that serialize well to JSON

4. **Handle all message types**: Your `onTeamMessage()` handler should handle all expected message types gracefully

5. **Consider message timing**: Messages are processed on the next turn, so factor in potential movement when
   broadcasting positions

## Summary

Team messaging in Tank Royale enables powerful team coordination:

- **Leaders** scan the battlefield and broadcast commands
- **Droids** (or other teammates) receive and act on those commands
- **Message classes** are matched by name, allowing typed communication
- **Colors, positions, and custom commands** can all be shared via team messages

Try running `MyFirstTeam` against other bots to see team coordination in action!
