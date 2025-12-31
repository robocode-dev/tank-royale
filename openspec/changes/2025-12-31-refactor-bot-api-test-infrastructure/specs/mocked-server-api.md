# MockedServer API Specification

## Overview

The MockedServer provides a lightweight WebSocket server that simulates the Tank Royale game server for testing
purposes. This specification defines the enhanced API that makes testing simple and deterministic.

## Core Responsibilities

1. **Protocol Simulation**: Accept bot connections and exchange messages per Tank Royale protocol
2. **State Management**: Maintain bot state (energy, position, gun heat, etc.)
3. **Intent Capture**: Record bot intents for test assertions
4. **Synchronization**: Provide blocking operations that guarantee state visibility

## Language Implementations

- Java: `bot-api/java/src/test/java/test_utils/MockedServer.java`
- .NET: `bot-api/dotnet/test/src/test_utils/MockedServer.cs`
- Python: `bot-api/python/tests/test_utils/mocked_server.py`

## Existing API (Preserved)

### Lifecycle

```
start() : void
  Start the WebSocket server on a dynamic port.
  
stop() : void
  Stop the WebSocket server and clean up resources.
  
closeConnections() : void
  Close all active WebSocket connections.
```

### State Setters (Basic)

```
setEnergy(energy: double) : void
  Set the bot's energy value for subsequent ticks.
  
setSpeed(speed: double) : void
  Set the bot's speed value for subsequent ticks.
  
setGunHeat(gunHeat: double) : void
  Set the bot's gun heat value for subsequent ticks.
  
set<Field>Increment(increment: double) : void
  Set per-tick increment for speed, turn, gunTurn, radarTurn.
  
set<Field>MinLimit(limit: double) : void
set<Field>MaxLimit(limit: double) : void
  Set min/max bounds for state fields (used to test boundary conditions).
```

### Synchronization Primitives

```
awaitConnection(timeoutMs: int) : bool
  Block until a bot connects or timeout.
  
awaitBotHandshake(timeoutMs: int) : bool
  Block until BotHandshake message received.
  
awaitGameStarted(timeoutMs: int) : bool
  Block until GameStartedEventForBot sent.
  
awaitTick(timeoutMs: int) : bool
  Block until TickEventForBot sent.
  
awaitBotIntent(timeoutMs: int) : bool
  Block until BotIntent message received.
  
resetBotIntentLatch() : void
  Reset the intent latch to await the next intent.
```

### Accessors

```
getBotHandshake() : BotHandshake
  Get the captured bot handshake message.
  
getBotIntent() : BotIntent
  Get the most recently captured bot intent.
  
getServerUrl() : URI/string
  Get the WebSocket URL for bot connection.
```

## New API (Phase 1)

### Enhanced Synchronization

```java
/**
 * Block until the bot has processed the handshake and is ready to accept commands.
 * This is a convenience method that chains:
 *   awaitBotHandshake() → awaitGameStarted() → awaitTick()
 * 
 * After this returns true, the bot will be in a state where:
 * - bot.getGameType() will not throw BotException
 * - bot.getEnergy() returns the current state
 * - bot can execute commands
 * 
 * @param timeoutMs Maximum time to wait in milliseconds
 * @return true if bot is ready, false if timeout
 */
public boolean awaitBotReady(int timeoutMs)
```

### Atomic State Setup

```java
/**
 * Atomically set bot state fields and send a tick, blocking until the bot processes it.
 * 
 * This method guarantees that:
 * 1. The specified state values are set in the server's internal state
 * 2. A TickEventForBot is sent with the new state
 * 3. The method blocks until the bot has processed the tick
 * 4. Subsequent calls to bot.getEnergy() etc. will see the new values
 * 
 * All parameters are nullable; null means "keep current value".
 * 
 * Example:
 *   server.setBotStateAndAwaitTick(
 *       5.0,    // energy = 5.0
 *       10.0,   // gunHeat = 10.0
 *       null,   // speed unchanged
 *       null,   // direction unchanged
 *       null,   // gunDirection unchanged
 *       null    // radarDirection unchanged
 *   );
 *   // Now bot.getEnergy() == 5.0, bot.getGunHeat() == 10.0
 * 
 * @param energy Bot energy (null to keep current)
 * @param gunHeat Gun heat (null to keep current)
 * @param speed Bot speed (null to keep current)
 * @param direction Bot body direction in degrees (null to keep current)
 * @param gunDirection Gun direction in degrees (null to keep current)
 * @param radarDirection Radar direction in degrees (null to keep current)
 * @return true if tick was processed within timeout, false otherwise
 */
public boolean setBotStateAndAwaitTick(
    Double energy,
    Double gunHeat,
    Double speed,
    Double direction,
    Double gunDirection,
    Double radarDirection
)
```

## Implementation Notes

### State Management

The MockedServer maintains internal state for:

- `turnNumber` - increments with each tick
- `energy` - bot's current energy
- `speed` - bot's current speed
- `gunHeat` - gun's current heat
- `direction` - bot body direction
- `gunDirection` - gun direction
- `radarDirection` - radar direction

State values are sent in each `TickEventForBot`.

### Message Flow

```
Bot connects
  ↓
Server sends ServerHandshake
  ↓
Bot sends BotHandshake
  ↓
Server sends GameStartedEventForBot
  ↓
Bot sends BotReady
  ↓
Server sends RoundStartedEvent
  ↓
Server sends TickEventForBot (turn 1)
  ↓
Bot sends BotIntent
  ↓
Server sends TickEventForBot (turn 2)
  ↓
(cycle continues)
```

### Synchronization Strategy

Each synchronization primitive uses a latch/event:

- **Java**: `CountDownLatch` (reset via `new CountDownLatch(1)`)
- **.NET**: `CountdownEvent` (reset via `Reset()`)
- **Python**: `threading.Event` (reset via `clear()`)

The `setBotStateAndAwaitTick()` method:

1. Updates internal state variables
2. Resets the tick event latch
3. Triggers a tick send (implementation-specific)
4. Blocks on `awaitTick()`
5. Returns success/failure

### Thread Safety

- **Java**: Uses `volatile` for state fields, synchronized access to latches
- **.NET**: Uses locks or concurrent collections as appropriate
- **Python**: Uses `threading.Lock` for state updates, `threading.Event` for coordination

## Usage Examples

### Basic State Setup

```java
// Java
MockedServer server = new MockedServer();
server.

start();

BaseBot bot = startBot(server);

// Set energy low, gun cool
server.

setBotStateAndAwaitTick(
    1.0,   // energy
        0.0,   // gunHeat
        null,  // speed (unchanged)
        null,  // direction (unchanged)
        null,  // gunDirection (unchanged)
        null   // radarDirection (unchanged)
);

// Now bot sees energy=1.0, gunHeat=0.0
boolean fired = bot.setFire(3.0);

assertFalse(fired); // Can't fire when energy < firepower
```

### Incremental State Updates

```java
// Initial setup
server.setBotStateAndAwaitTick(100.0,0.0,0.0,90.0,90.0,90.0);

// Later, just update gun heat
server.

setBotStateAndAwaitTick(
    null,  // energy unchanged
        5.0,   // gunHeat = 5.0
        null,  // others unchanged
        null,
        null,
        null
);
```

### Boundary Testing

```java
// Test at exact boundary
server.setBotStateAndAwaitTick(3.0,0.0,null,null,null,null);

boolean fired = bot.setFire(3.0);

assertTrue(fired); // Exactly enough energy

// Test just below boundary
server.

setBotStateAndAwaitTick(2.99,0.0,null,null,null,null);

fired =bot.

setFire(3.0);

assertFalse(fired); // Not quite enough energy
```

## Error Handling

### Timeout Behavior

If `setBotStateAndAwaitTick()` times out:

- Returns `false`
- State has been updated in the server
- Tick may or may not have been sent
- Test should fail fast (don't continue with unstable state)

Best practice:

```java
assertTrue(
    server.setBotStateAndAwaitTick(5.0, 0.0,null,null,null,null),
    "Failed to set bot state - bot may not be responding"
        );
```

### Invalid State Values

The method accepts any numeric values (including negative, NaN, etc.).
The bot API's validation is under test, so the server should not validate.

However, for test clarity:

- Use realistic values where possible
- Use extreme values intentionally to test edge cases
- Document why unusual values are used

## Language-Specific Considerations

### Java

- Use `Double` (nullable) for optional parameters
- `null` check before updating state
- `CountDownLatch` for synchronization

### .NET

- Use `double?` (nullable) for optional parameters
- `HasValue` check before updating state
- `CountdownEvent` or `ManualResetEventSlim` for synchronization

### Python

- Use `Optional[float]` for optional parameters
- `is not None` check before updating state
- `threading.Event` for synchronization
- Handle asyncio event loop properly (run in executor if needed)

## Testing the MockedServer

MockedServer itself should have unit tests:

```java
@Test
void testAwaitBotReady() {
    MockedServer server = new MockedServer();
    server.start();
    
    BaseBot bot = startBotAsync(server);
    
    assertTrue(server.awaitBotReady(2000));
    
    // Bot should now be able to call methods
    assertDoesNotThrow(() -> bot.getEnergy());
}

@Test
void testSetBotStateAndAwaitTick() {
    MockedServer server = new MockedServer();
    server.start();
    BaseBot bot = startBot(server);
    
    assertTrue(server.setBotStateAndAwaitTick(42.0, null, null, null, null, null));
    
    assertEquals(42.0, bot.getEnergy(), 0.01);
}
```

## Future Enhancements

Potential improvements (out of scope for initial implementation):

1. **Batch state updates**: `setBotStateAndAwaitTick(BotStateBuilder)`
2. **State presets**: `setLowEnergyState()`, `setHighSpeedState()`
3. **State assertions**: `assertBotState(energy=5.0, gunHeat=0.0)`
4. **Event injection**: `injectScannedBotEvent(...)`, `injectBulletHitEvent(...)`
5. **Timeline recording**: Record actual vs expected state progression

These can be considered after the core refactoring is complete.

