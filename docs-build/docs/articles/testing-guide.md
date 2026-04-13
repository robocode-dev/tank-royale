# Testing & Debugging Guide

Testing and debugging are essential parts of bot development. This guide covers strategies for testing your bot's behavior, diagnosing issues, and improving performance through systematic testing.

## Testing Strategies

### Local Testing Workflow

A typical bot testing workflow includes:

1. **Unit testing** - Test individual algorithms in isolation
2. **Integration testing** - Test bot behavior in battles
3. **Performance testing** - Measure win rates against reference bots
4. **Regression testing** - Ensure changes don't break existing behavior

### Choosing Test Opponents

Select appropriate opponents for different testing phases:

**Static targets (sample bots):**
- **Target** - Doesn't move, useful for testing targeting accuracy
- **Walls** - Moves along walls, tests wall avoidance
- **Corners** - Stays in corners, tests corner targeting

**Reference bots:**
- Test against known-strength bots to measure improvement
- Use consistent opponent set for comparable results
- Include both strong and weak opponents in your suite

**Specialized opponents:**
- **Rammers** - Test collision avoidance
- **Pattern movers** - Test adaptive targeting
- **Strong dodgers** - Test targeting accuracy against evasive bots

### Battle Configuration for Testing

Use consistent battle settings:

```json
{
  "numberOfRounds": 35,
  "arenaWidth": 800,
  "arenaHeight": 600,
  "turnTimeout": 30000
}
```

**Why 35 rounds?** Multiple rounds reduce variance and provide statistically meaningful results.

See [Custom Game Setup](custom-game-setup) for battle configuration details.

## Debugging Techniques

### Print Debugging

The simplest debugging technique is printing to console:

**Java:**
```java
System.out.println("Debug: currentHeading=" + getDirection());
```

**Python:**
```python
print(f"Debug: currentHeading={self.direction}")
```

**C#:**
```csharp
Console.WriteLine($"Debug: currentHeading={Direction}");
```

### Conditional Debug Output

Avoid excessive logging by using debug flags:

```java
private static final boolean DEBUG = true;

private void debug(String message) {
    if (DEBUG) {
        System.out.println(message);
    }
}
```

### Visualizing Bot State

Print key variables at decision points:

- Current position and heading
- Target enemy position
- Calculated angles and distances
- Decision variables (which algorithm is active, etc.)

### Server Debug Mode

Run the server with debug logging enabled:

```bash
robocode-tankroyale-server --debug
```

This shows:
- Bot registration and communication
- Server-side events
- Network protocol details

See [Debugging](debug) for complete debugging options.

## Performance Profiling

### Measuring Execution Time

Track algorithm execution time to avoid timeout penalties:

**Java:**
```java
long startTime = System.nanoTime();
// Your algorithm here
long duration = System.nanoTime() - startTime;
System.out.println("Algorithm took: " + duration + " ns");
```

**Python:**
```python
import time
start_time = time.perf_counter()
# Your algorithm here
duration = time.perf_counter() - start_time
print(f"Algorithm took: {duration*1000:.3f} ms")
```

### Turn Timeout Considerations

Each bot has limited time per turn (default: 30ms):

- **Simple bots**: Usually complete in < 1ms
- **Complex targeting**: May take 5-15ms
- **Slow algorithms**: Risk timeout penalties

If your bot times out:
- Optimize algorithms (better data structures, fewer loops)
- Cache calculations when possible
- Reduce complexity of per-turn operations

For advanced performance optimization techniques, see [Performance Optimization](performance-optimization).

## Test Harness Development

### Automated Testing Scripts

Create scripts to run multiple battles and aggregate results:

**Example test script (Bash):**
```bash
#!/bin/bash
for i in {1..10}; do
  robocode-tankroyale-server --config=test-config.json >> results.txt
done
```

### Recording Battle Statistics

Track key metrics across battles:

- **Win rate percentage** - Overall success rate
- **Average score per round** - Consistent performance measure
- **Survival rate** - Ability to avoid destruction
- **Bullet hit percentage** - Targeting accuracy

### Regression Testing

After code changes:

1. Run test suite against saved bot versions
2. Compare win rates and scores
3. Investigate any significant performance drops
4. Keep baseline results for comparison

## Common Testing Pitfalls

**Insufficient sample size:**
- Running too few battles leads to misleading results
- Use at least 35 rounds per opponent

**Testing against weak opponents only:**
- Your bot may overfit to weak strategies
- Include strong, diverse opponents

**Optimizing for specific opponents:**
- Avoid hardcoding behaviors for test bots
- Focus on generalizable strategies

**Ignoring edge cases:**
- Test in different arena sizes
- Test with various starting positions
- Test against multiple opponent types

## Debugging Common Issues

### Bot Doesn't Move

**Possible causes:**
- Forgetting to call movement methods (`setForward()`, `setTurnRate()`)
- Setting contradictory commands
- Bot timing out due to slow code

**Debugging:**
- Print movement commands each turn
- Check for exceptions in console output
- Verify `run()` loop executes each turn

### Bot Doesn't Fire

**Possible causes:**
- Gun heat > 0 (gun still cooling)
- No target scanned
- Invalid firepower (< 0.1 or > 3)

**Debugging:**
- Print gun heat each turn
- Verify enemy is being scanned
- Check firepower calculations

### Erratic Behavior

**Possible causes:**
- Unhandled events overwriting planned actions
- State variables not updating correctly
- Calculation errors in targeting or movement

**Debugging:**
- Add extensive logging around decision points
- Visualize bot's "thought process" through debug output
- Test individual algorithms in isolation

## Further Reading

- [Debugging](debug) - Debug options and print debugging
- [Custom Game Setup](custom-game-setup) - Configure test battles
- [Performance Optimization](performance-optimization) - Optimize bot execution speed
- [APIs](/api/apis) - Bot API documentation for all languages
- [The Book of Robocode](https://book.robocode.dev/) - Advanced strategies and testing methodologies
