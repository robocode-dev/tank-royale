# Collision Mechanics

Collisions are a critical aspect of Robocode Tank Royale. Understanding how collisions work—and how to handle them strategically—can mean the difference between victory and defeat.

## Types of Collisions

There are three types of collisions in Robocode:

1. **Wall collisions** - When your bot hits an arena wall
2. **Bot-to-bot collisions** - When your bot collides with another bot
3. **Bullet collisions** - When bullets hit bots or walls

## Wall Collisions

When a bot collides with a wall, several things happen:

- The bot is **immediately stopped** (speed becomes 0)
- The bot takes **damage** proportional to its impact speed
- The bot may bounce slightly off the wall

### Wall Collision Damage

The damage formula for wall collisions is:

$\text{damage} = \max(|velocity| - 1, 0)$

This means:
- Moving at speeds ≤ 1 unit/turn = no damage
- Moving at 8 units/turn = 7 damage points
- The faster you hit the wall, the more it hurts

### Avoiding Wall Collisions

Basic wall avoidance strategies include:

- **Distance checking**: Monitor your distance to walls using arena boundaries
- **Wall smoothing**: Adjust heading to parallel the wall when approaching
- **Predictive braking**: Start decelerating before reaching the wall

For advanced wall avoidance techniques including wall smoothing algorithms, see [Wall Avoidance & Wall Smoothing](https://book.robocode.dev/movement/basic/wall-avoidance-wall-smoothing) in The Book of Robocode.

## Bot-to-Bot Collisions

When two bots collide:

- Both bots take **0.6 damage** per turn while in contact
- Both bots are typically stopped
- **Exception**: If a bot is moving away from the collision point, it may continue moving

### Collision Physics

Bot-to-bot collision behavior:

- Bots cannot pass through each other
- If bots are perpendicular or approaching, both stop
- If one bot is retreating from the contact point, it may continue
- Collision damage accumulates each turn bots remain in contact

### Strategic Considerations

**Ramming** can be an offensive tactic:

- Useful when you have more energy than your opponent
- Each collision inflicts 0.6 damage but costs you 0.6 energy too
- See [Physics - Ramming](physics#ramming) for ramming damage calculations

**Avoiding collisions** is often preferable:

- Maintain minimum safe distance from opponents
- Use predictive movement to avoid being trapped
- Consider collision zones in your movement algorithms

For advanced collision handling strategies, explore movement techniques in [The Book of Robocode](https://book.robocode.dev/movement/basic/movement-fundamentals-goto).

## Bullet Collisions

### Bullet-Bot Collisions

When a bullet hits a bot:

- The target bot takes damage based on bullet firepower (see [Physics - Bullet damage](physics#bullet-damage))
- The shooter gains energy (3 × firepower)
- The bullet is removed from the battlefield

### Bullet-Bullet Collisions

Bullets can collide with each other in mid-flight:

- Both bullets are destroyed
- No damage is dealt to any bot
- This is relatively rare but can happen in crowded battles

### Bullet-Wall Collisions

When bullets hit walls:

- The bullet is destroyed
- No damage is dealt
- No energy is transferred

## Working with Collision Events

The Bot API provides events for handling collisions:

**Wall collisions:**
```java
onHitWall(HitWallEvent event)
```

**Bot collisions:**
```java
onHitBot(HitBotEvent event)
onScannedBot(ScannedBotEvent event) // To avoid collisions
```

**Bullet hits:**
```java
onBulletHit(BulletHitEvent event)
onHitByBullet(HitByBulletEvent event)
```

See the [Bot API documentation](/api/apis) for complete event details.

## Collision Strategy Summary

| Situation | Recommended Action |
|-----------|-------------------|
| **Near wall** | Slow down, turn parallel, or use wall smoothing |
| **Enemy nearby** | Maintain safe distance or ram if you have energy advantage |
| **Low energy** | Avoid all collisions—every hit matters |
| **High traffic area** | Use predictive movement to navigate clear paths |

## Further Reading

- [Physics](physics) - Core physics equations and movement constraints
- [Movement Fundamentals](https://book.robocode.dev/movement/basic/movement-fundamentals-goto) - Basic movement techniques
- [Wall Avoidance & Wall Smoothing](https://book.robocode.dev/movement/basic/wall-avoidance-wall-smoothing) - Advanced wall handling
- [Anti-Gravity Movement](https://book.robocode.dev/movement/strategic-movement/anti-gravity-movement) - Collision avoidance through repulsion fields
