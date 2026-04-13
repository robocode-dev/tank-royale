# Team Strategies

Team battles add a cooperative dimension to Robocode, where bots work together to defeat opposing teams. Success requires not only strong individual bot capabilities but also effective communication and coordination.

## Team Battle Basics

### Team Composition

Teams typically consist of 2-5 bots:

**Standard roles:**
- **Leader** - Coordinates team strategy, tracks all enemies
- **Soldier** - General-purpose combat bot
- **Droid** - Specialized bot with reduced radar (cannot win alone)

**Droid special properties:**
- Droids score 2× points for their team
- Droids cannot win battles (team needs at least one non-droid alive)
- Droids often have limited radar range (120° instead of 360°)

### Team Messages

Bots communicate via team messages to share information and coordinate:

```java
// Send message to teammates
sendTeamMessage("enemy.spotted", enemyData);

// Receive message from teammate
onTeamMessage(TeamMessageEvent event) {
    String message = event.getMessage();
    // Process teammate's information
}
```

See [Team Messages](team-messages) for complete messaging API and protocols.

## Communication Strategies

### Information Sharing

**What to share:**
- Enemy positions and headings
- Enemy energy levels (track damage dealt)
- Your current target (avoid duplicate targeting)
- Your position and status

**Message frequency:**
- Send updates when significant changes occur
- Avoid flooding with messages every turn
- Balance information timeliness with communication overhead

### Message Protocols

Establish consistent message formats:

```java
// Example protocol
"ENEMY|Tracker|x:250,y:300,h:45,e:80,t:150"
"TARGET|Tracker|MyBot"
"STATUS|MyBot|x:100,y:100,e:100"
"HELP|MyBot|x:100,y:100,e:20"
```

**Components:**
- **Type** - Message category (ENEMY, TARGET, STATUS, HELP)
- **Sender** - Which teammate sent it
- **Data** - Relevant information (position, energy, etc.)

## Basic Team Tactics

### Target Selection

Coordinate targeting to avoid overkill:

**Distributed targeting:**
```java
private String myTarget = null;

void selectTarget() {
    for (EnemyData enemy : enemies) {
        if (!isTargetedByTeammate(enemy.name)) {
            myTarget = enemy.name;
            sendTeamMessage("TARGET:" + myTarget);
            break;
        }
    }
}
```

**Focus fire (when advantageous):**
- Multiple bots target the same enemy
- Useful for quickly eliminating dangerous opponents
- Announce target to team: "FOCUS|EnemyName"

### Positioning

**Crossfire formation:**
- Position bots at different angles to enemy
- Enemy cannot dodge both bots simultaneously
- Requires coordination and positioning awareness

**Cover formation:**
- One bot engages while another provides support
- Useful when one bot has low energy

**Corner trapping:**
- Coordinate to push enemy into corner
- Reduces enemy's movement options

### Energy Management

**Share energy status:**
- Announce when low on energy
- Request support from teammates
- Protect low-energy teammates

**Example:**
```java
if (getEnergy() < 20) {
    sendTeamMessage("HELP|" + getX() + "," + getY());
}
```

**Sacrifice plays:**
- Droid can sacrifice itself to damage enemy (2× scoring bonus)
- Low-energy bot can ram to benefit team

## Advanced Team Strategies

Team-specific content is currently being developed for The Book of Robocode. In the meantime, explore advanced movement and targeting techniques that apply to team battles:

- [Anti-Gravity Movement](https://book.robocode.dev/movement/strategic-movement/anti-gravity-movement) - Positioning based on multiple threats
- [GuessFactor Targeting](https://book.robocode.dev/targeting/statistical-targeting/guessfactor-targeting) - Advanced targeting for coordinated fire

### Shared Enemy Tracking

Build a team-wide view of the battlefield:

**Leader maintains enemy database:**
- Receives scan data from all teammates
- Maintains most recent position/status for each enemy
- Broadcasts consolidated enemy data to team

**Teammates contribute scans:**
- Send scan data to leader
- Receive processed enemy data back
- Use shared data for targeting calculations

### Role Specialization

**Example team composition:**

**1. Scout/Leader**
- Wide radar coverage
- Tracks all enemies
- Coordinates team strategy
- Low firepower (focus on information)

**2. Sniper**
- Advanced targeting algorithms
- High firepower
- Relies on scout for enemy data
- Limited radar (can be a droid)

**3. Protector/Soldier**
- Protects sniper
- Engages close-range threats
- Anti-ramming capabilities
- Medium firepower

## Team Battle Scoring

Team scoring differs from 1v1:

**Points awarded to team:**
- Damage dealt by any team member
- Survival bonuses (last team standing)
- Droid bonuses (droids score 2× points)

**Winning strategy:**
- Maximize total team score
- Consider sacrificing low-value bots for team advantage
- Protect high-value bots (strong performers)

See [Scoring](scoring) for detailed scoring rules.

## Debugging Team Bots

**Testing team coordination:**
- Run team battles with debug output enabled
- Verify messages are being sent and received
- Check that shared data is used correctly
- Ensure bots aren't targeting same enemy when using distributed targeting

**Common team issues:**
- **Message overload** - Too many messages slow down bots
- **Targeting conflicts** - Multiple bots targeting same enemy unintentionally
- **Poor positioning** - Bots blocking each other's shots or movement
- **Communication failures** - Messages not reaching teammates

See [Testing & Debugging Guide](testing-guide) for debugging techniques.

## Team Development Workflow

1. **Build strong individual bots first** - Each bot should be competent alone
2. **Add basic messaging** - Share enemy positions
3. **Implement target coordination** - Avoid duplicate targeting
4. **Add positioning awareness** - Maintain formation, avoid friendly fire
5. **Optimize communication** - Reduce message overhead
6. **Test extensively** - Run team battles against various opponents

## Further Reading

- [Team Messages](team-messages) - Team messaging API and implementation
- [Scoring](scoring) - Team scoring rules and calculations
- [Testing & Debugging Guide](testing-guide) - Testing team coordination
- [Anti-Gravity Movement](https://book.robocode.dev/movement/strategic-movement/anti-gravity-movement) - Positioning with multiple threats
- [Energy Management](https://book.robocode.dev/energy-and-scoring/energy-management-1v1-melee) - Resource allocation strategies
