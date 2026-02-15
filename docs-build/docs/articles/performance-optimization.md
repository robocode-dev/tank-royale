# Performance Optimization

Optimizing your bot's performance ensures it executes quickly, avoids timeouts, and can implement sophisticated strategies without slowdowns. This guide covers techniques for writing efficient bot code.

## Understanding Turn Time Limits

Each bot has a limited time budget per turn (default: **30,000 microseconds** or **30 milliseconds**).

### Turn Timeout Consequences

If your bot exceeds the turn timeout:

- The bot **skips that turn** (no commands executed)
- Repeated timeouts can result in penalties
- In extreme cases, the bot may be disqualified

### Typical Execution Times

| Bot Complexity | Typical Execution Time |
|----------------|------------------------|
| **Simple bot** (basic movement, linear targeting) | < 1 ms |
| **Intermediate bot** (pattern detection, statistical targeting) | 2-10 ms |
| **Advanced bot** (wave surfing, guess-factor targeting) | 10-25 ms |
| **Complex statistical bot** (large data structures, clustering) | 15-30 ms |

The goal: **Stay well under 30ms** to maintain consistent performance.

## TPS vs. Computation Time

**TPS (Turns Per Second)** controls visualization speed, not computation limits:

- **Visual speed**: How fast you see the battle (adjustable in GUI)
- **Computation time**: The actual limit per bot per turn (fixed at 30ms default)

See [TPS](https://robocode.dev/articles/tps) for visualization speed control.

**Important**: Even at 60 TPS visual speed, your bot still has 30ms per turn. TPS affects display only, not bot execution.

## Optimization Techniques

### Algorithm Efficiency

**Choose appropriate algorithms:**

- **Linear targeting** - O(1), very fast
- **Circular targeting** - O(1) with trigonometry, still fast
- **Statistical targeting** - O(n) where n = data points, potentially slow
- **Pattern matching** - O(n×m) where n = history length, m = pattern length, can be very slow

**Optimize loops:**

```java
// Inefficient - creates ArrayList every turn
for (Wave wave : new ArrayList<>(waves)) {
    // Process wave
}

// Better - iterate directly
Iterator<Wave> it = waves.iterator();
while (it.hasNext()) {
    Wave wave = it.next();
    if (wave.hasHit()) {
        it.remove();
    }
}
```

### Data Structure Selection

Choose efficient data structures:

**For frequent insertions/removals:**
- `LinkedList` (Java)
- `deque` (Python)
- `LinkedList<T>` (C#)

**For quick lookups:**
- `HashMap` (Java)
- `dict` (Python)
- `Dictionary<TKey, TValue>` (C#)

**For sorted data:**
- `TreeMap` (Java)
- `SortedDict` (Python's `sortedcontainers`)
- `SortedDictionary<TKey, TValue>` (C#)

### Caching Calculations

Avoid recalculating values that don't change:

```java
// Bad - calculates every turn
double enemyDistance = Math.hypot(
    enemy.getX() - getX(),
    enemy.getY() - getY()
);

// Better - cache until enemy moves
private double cachedEnemyDistance;
private long lastEnemyUpdate;

void onScannedBot(ScannedBotEvent e) {
    cachedEnemyDistance = e.getDistance();
    lastEnemyUpdate = getTime();
}
```

### Trigonometry Optimization

Trigonometric functions are relatively expensive:

**Pre-compute where possible:**

```java
// Pre-compute common angles
private static final double[] SIN_TABLE = new double[360];
private static final double[] COS_TABLE = new double[360];

static {
    for (int i = 0; i < 360; i++) {
        double radians = Math.toRadians(i);
        SIN_TABLE[i] = Math.sin(radians);
        COS_TABLE[i] = Math.cos(radians);
    }
}

// Fast lookup (approximate)
public double fastSin(double degrees) {
    return SIN_TABLE[(int) degrees % 360];
}
```

**Use built-in optimizations:**

```java
// Slower
double distance = Math.sqrt(dx * dx + dy * dy);

// Faster (when available)
double distance = Math.hypot(dx, dy);
```

### Limiting Data Storage

Large data structures slow down iteration:

**Limit history size:**

```java
private static final int MAX_HISTORY = 1000;

void recordData(ScanData data) {
    history.add(data);
    if (history.size() > MAX_HISTORY) {
        history.remove(0); // Remove oldest
    }
}
```

**Use circular buffers:**

```java
private ScanData[] history = new ScanData[MAX_HISTORY];
private int historyIndex = 0;

void recordData(ScanData data) {
    history[historyIndex++ % MAX_HISTORY] = data;
}
```

### Lazy Evaluation

Defer expensive calculations until necessary:

```java
// Bad - calculates even if not needed
double angle = calculateBestAngle();
if (shouldFire()) {
    setFire(angle);
}

// Better - only calculate when needed
if (shouldFire()) {
    double angle = calculateBestAngle();
    setFire(angle);
}
```

## Profiling Your Bot

### Identifying Bottlenecks

Measure execution time of different sections:

```java
long start = System.nanoTime();
performTargeting();
long targetingTime = System.nanoTime() - start;

start = System.nanoTime();
performMovement();
long movementTime = System.nanoTime() - start;

if (getTime() % 100 == 0) {
    System.out.println("Targeting: " + targetingTime/1000 + "µs");
    System.out.println("Movement: " + movementTime/1000 + "µs");
}
```

### Finding Performance Issues

Common performance bottlenecks:

- **Excessive object creation** - Creates garbage collection pressure
- **Nested loops** - O(n²) or worse complexity
- **Large data structure iteration** - Processing thousands of entries per turn
- **Unnecessary calculations** - Computing values that won't be used

## Memory Management

### Garbage Collection

Minimize object creation in the main loop:

```java
// Bad - creates new objects every turn
Point2D.Double myPos = new Point2D.Double(getX(), getY());

// Better - reuse objects
private Point2D.Double myPos = new Point2D.Double();

public void run() {
    while (true) {
        myPos.setLocation(getX(), getY());
        // Use myPos
    }
}
```

### Memory Leaks

Remove unused data:

```java
// Don't forget to clean up old waves
waves.removeIf(Wave::hasPassedTarget);

// Don't store references to destroyed bots
if (enemy.getEnergy() <= 0) {
    enemyData.remove(enemy.getName());
}
```

## Platform-Specific Considerations

### Java/Kotlin

- JIT compilation warms up over time (first few rounds may be slower)
- Use `ArrayList` for fast iteration, `HashMap` for lookups
- Avoid `Stream` API in hot loops (adds overhead)

### Python

- Use `numpy` for numerical calculations (much faster than pure Python)
- List comprehensions faster than manual loops
- Consider `__slots__` for data classes to reduce memory

### C#

- Structs faster than classes for small data (no heap allocation)
- Use `List<T>` and `Dictionary<TKey, TValue>` for performance
- LINQ convenient but adds overhead in tight loops

## Testing Performance

### Benchmarking Improvements

Compare execution times before/after optimization:

1. Add timing measurements to key sections
2. Run 35+ round battles for statistical validity
3. Compare average execution times
4. Ensure win rate doesn't decrease with optimizations

### Avoiding Premature Optimization

Follow this priority:

1. **Correctness first** - Make it work
2. **Clarity second** - Make it readable
3. **Performance third** - Make it fast (only where needed)

**Profile before optimizing** - Don't guess what's slow, measure it.

## Further Reading

- [TPS](https://robocode.dev/articles/tps) - Visualization speed vs. computation time
- [Testing & Debugging Guide](testing-guide) - Profiling and performance testing
- [Physics](physics) - Understanding game mechanics for efficient calculations
- [APIs](/api/apis) - Bot API documentation for each language
- [The Book of Robocode](https://book.robocode.dev/) - Advanced algorithmic techniques and implementations
