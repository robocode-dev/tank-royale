# TPS (Turns Per Second)

## Introduction

_Turns Per Second_ is a term used in Robocode that can be compared to [FPS] (Frames Per Second). TPS is the _visualization
speed_ for observers watching a battle. It does **not** affect how much time bots have to compute or the outcome of battles.

## Turn Timeout

The game has a _turn timeout_ defined as a part of the game rules. The turn timeout is specified in microseconds (µs),
where a microsecond (µs) is a millionth of a second. The turn timeout is the **fixed duration** of every turn in a battle.

### Important: Deterministic Battles

**Every turn takes exactly the turn timeout duration**, regardless of when bots send their intents. This ensures that:

- Battle outcomes depend **only** on turn timeout, not on TPS settings
- Running the same battle at different TPS values produces **identical results**
- All bots get the **same computation time** in every turn

This deterministic behavior is critical for competitive play and reproducible simulations.

### Skipped Turns

If a bot fails to send its _intent_ before the turn timeout expires, the bot will be _skipping the turn_, meaning it
missed the opportunity to update its actions. This means that the bot cannot fire its cannon, turn, or change its
speed, etc. in that turn. The server will use the bot's previous intent (or default values) and proceed to the next turn.

## TPS as Visualization Speed Only

TPS controls the **maximum rate at which turns are displayed** to observers. It does not affect bot computation time:

- **Low TPS** (e.g., 30 TPS): Slow-motion visualization for detailed observation
- **High TPS** (e.g., 500 TPS): Fast-forward visualization for quick battles
- **TPS = -1**: Unlimited speed (as fast as hardware allows)

Regardless of TPS setting, every turn gives bots the full turn timeout to compute.

## Maximum Turn Rate

The maximum turn rate is determined by the turn timeout:

$TPS_{max} = \frac{1,000,000}{timeout_{turn}}$

So if the turn timeout is set to 30,000 µs (30 ms), the maximum turn rate will be:

$TPS_{max} = \frac{1,000,000}{30,000} = 33.33$ TPS

**You cannot run faster than this maximum rate** because each turn requires the full timeout duration.

### Example Configurations

| Turn Timeout | Max TPS | Description |
|--------------|---------|-------------|
| 10,000 µs (10 ms) | 100 TPS | Fast bots, limited visualization speed |
| 30,000 µs (30 ms) | 33.33 TPS | Standard competitive play |
| 100,000 µs (100 ms) | 10 TPS | Slower bots, more thinking time |

## Choosing TPS Settings

When setting TPS, remember:

1. **TPS ≤ Max TPS**: You can set TPS lower than the maximum for slower visualization
2. **TPS > Max TPS**: Has no effect - turns still take turn timeout duration
3. **Battle outcomes**: Are **always** determined by turn timeout, **never** by TPS
4. **For competitive battles**: Use the same turn timeout. TPS is a viewer preference.

## Note About Performance

Be careful when setting very low turn timeouts (< 1000 µs)! This can cause issues on some systems, meaning that bots
might skip turns due to hardware or OS limitations, not because of their code.


[FPS]: https://en.wikipedia.org/wiki/Frame_rate "Frame Rate and frames per second (FPS)"
