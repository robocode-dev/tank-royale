# Custom Game Setup

Robocode Tank Royale offers extensive customization options for creating battles with custom rules, arena sizes, and game modes. This guide covers how to configure advanced battle settings beyond the defaults.

## Game Configuration Files

Game configurations can be set through:

1. **GUI Battle Setup** - Interactive configuration through the graphical interface
2. **Config Files** - JSON configuration files for repeatable setups
3. **Server Options** - Command-line server configuration for automated battles

See [User Data & Config Files](user-data-config) for file locations and structure.

## Battle Configuration

### Basic Battle Settings

The fundamental battle parameters include:

**Number of rounds:**
- Default: 10 rounds
- Range: 1 to unlimited
- Determines how many rounds are played before a winner is declared

**Turn timeout:**
- Default: 30,000 microseconds (30 ms)
- Controls how long each bot has to process each turn
- Lower values increase game speed but may penalize complex bots

**Battlefield dimensions:**
- Default: 800 × 600 units
- Minimum: 200 × 200 units
- Larger arenas favor long-range targeting; smaller arenas favor close combat

### Game Types

Robocode supports different battle formats:

**Classic Battle (1v1):**
- Two bots compete head-to-head
- Best for testing targeting and movement algorithms
- See [Scoring](scoring) for 1v1 scoring rules

**Melee Battle:**
- 3+ bots in free-for-all combat
- Emphasizes survival and threat assessment
- Different strategies than 1v1 (avoid crossfire, pick weakest targets)

**Team Battle:**
- Teams of bots cooperate against other teams
- Bots can send messages to teammates
- See [Team Messages](team-messages) for communication protocols

For competitive battle formats and tournament structures, see [Scoring Systems & Battle Types](https://book.robocode.dev/energy-and-scoring/scoring-systems-battle-types) in The Book of Robocode.

## Advanced Configuration Options

### Initial Bot Positions

You can specify starting positions for bots:

```json
{
  "initialPositions": [
    {"x": 100, "y": 100, "angle": 0},
    {"x": 700, "y": 500, "angle": 180}
  ]
}
```

This is useful for:
- Testing specific scenarios
- Ensuring fair starting positions
- Reproducing specific battle conditions

### Custom Arena Sizes

Arena dimensions affect bot strategies:

| Arena Size | Characteristics | Favors |
|------------|----------------|--------|
| **Small** (400×300) | Quick engagements, limited maneuvering | Aggressive bots, ramming |
| **Standard** (800×600) | Balanced gameplay | General-purpose bots |
| **Large** (1200×900) | Extended battles, room to dodge | Long-range targeting, evasion |

### Inactivity Rules

Configure bot inactivity detection:

**Inactivity time:**
- Default: 450 turns (about 45 seconds at 10 TPS)
- Bots that don't move or fire are penalized
- Prevents stalling tactics

**Inactivity penalty:**
- Bot loses energy for being inactive
- Serves to eliminate non-responsive bots

## Server Configuration

### Running Battles Programmatically

For automated testing or tournaments, run the server with custom configurations:

```bash
robocode-tankroyale-server --config=battle-config.json
```

Key server options:

**`--port`** - Specify server port (default: 7654)
**`--games`** - Number of battles to run
**`--config`** - Path to battle configuration file

See the [Server documentation](https://github.com/robocode-dev/tank-royale/tree/master/server#readme) for complete server options.

### Debugging Options

Enable additional logging for testing:

```bash
robocode-tankroyale-server --debug
```

See [Debugging](debug) for more debugging techniques.

## Creating Repeatable Test Scenarios

For bot development, create standardized test configurations:

1. **Create battle config JSON** with specific bots, arena size, and rules
2. **Save bot selection** for consistent opponents
3. **Run multiple battles** to average results and reduce variance
4. **Compare performance** across code changes using same config

### Example Test Configuration

```json
{
  "gameType": "classic",
  "arenaWidth": 800,
  "arenaHeight": 600,
  "numberOfRounds": 35,
  "gunCoolingRate": 0.1,
  "maxInactivityTurns": 450,
  "turnTimeout": 30000,
  "bots": [
    {"name": "MyBot", "version": "1.0"},
    {"name": "TargetBot", "version": "1.0"}
  ]
}
```

## Visual Customization

The GUI supports visual customization:

- **Theme colors** for bots
- **Battlefield appearance** (grid, background)
- **TPS (Turns Per Second)** control for visual speed

See [GUI](gui) for interface options and [TPS](https://robocode.dev/articles/tps) for visualization speed control.

## Further Reading

- [GUI](gui) - Graphical interface features and battle management
- [User Data & Config Files](user-data-config) - Configuration file locations and structure
- [Debugging](debug) - Testing and debugging techniques
- [Scoring](scoring) - Understanding battle results and rankings
- [Competition Formats & Rankings](https://book.robocode.dev/energy-and-scoring/competition-formats-rankings) - Tournament structures and rating systems
