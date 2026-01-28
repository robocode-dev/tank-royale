# .NET MockedServer Thread-Safety Implementation Notes

## Overview
This document describes the thread-safety implementation in `MockedServer.cs` completed as part of Task 1.5.2.

## Thread-Safety Guarantees

### Lock Hierarchy
```
_stateLock (protects bot state fields)
  └─> _clients (protects WebSocket connection list)
```

**Rule**: Always acquire `_stateLock` before `_clients` to prevent deadlock.

### Protected State Fields
All access to these fields must occur within `lock (_stateLock)`:
- `_turnNumber`
- `_energy`
- `_gunHeat`
- `_speed`
- `_direction`
- `_gunDirection`
- `_radarDirection`
- `_speedIncrement`, `_turnIncrement`, `_gunTurnIncrement`, `_radarTurnIncrement`
- All limit fields (`_speedMinLimit`, etc.)

### Synchronization Primitives

#### ManualResetEvent (Used for signaling)
- `_openedEvent` - Signals connection opened
- `_botHandshakeEvent` - Signals bot handshake received
- `_gameStartedEvent` - Signals game started
- `_tickEvent` - Signals tick event sent
- `_botIntentEvent` - Signals bot intent received
- `_botIntentContinueEvent` - Controls intent processing flow

**Note**: Unlike Java's `CountDownLatch` which must be replaced, .NET's `ManualResetEvent` can be reset via `.Reset()`.

## Critical Code Paths

### 1. BotIntent Message Handler (OnMessage)
```csharp
case MessageType.BotIntent:
    _botIntentContinueEvent.WaitOne();  // Wait for test to allow intent
    
    lock (_stateLock) {
        // Check limit conditions (may return early)
        if (_speedMinLimit != null && _speed < _speedMinLimit) return;
        // ... other checks ...
        
        // Parse intent
        _botIntent = JsonConverter.FromJson<BotIntent>(messageJson);
        _botIntentEvent.Set();  // Signal intent received
        
        // Send tick with current state
        SendTickEventForBot(conn, _turnNumber++);
        
        // Update state AFTER sending tick
        _speed += _speedIncrement;
        _direction += _turnIncrement;
        _gunDirection += _gunTurnIncrement;
        _radarDirection += _radarTurnIncrement;
    }
    break;
```

**Key Points**:
- All state access protected by lock
- State updates happen AFTER tick is sent (ensures consistency)
- Early returns for limit violations happen inside lock
- Signal `_botIntentEvent` before sending tick (allows test synchronization)

### 2. BotReady Message Handler (OnMessage)
```csharp
case MessageType.BotReady:
    SendRoundStarted(conn);
    
    lock (_stateLock) {
        SendTickEventForBot(conn, _turnNumber++);
    }
    _tickEvent.Set();
    break;
```

**Fix Applied**: Previously missed sending tick event, violating protocol specification.

### 3. AwaitBotReady Method
```csharp
public bool AwaitBotReady(int timeoutMs = 1000)
{
    // ... wait for handshake and game started ...
    
    _tickEvent.Reset();
    
    lock (_stateLock) {
        lock (_clients) {
            foreach (var conn in _clients) {
                SendTickEventForBot(conn, _turnNumber++);
            }
        }
    }
    
    return AwaitTick(remaining);
}
```

**Key Points**:
- Proactively sends tick to avoid race with BOT_READY message
- Nested locking: `_stateLock` → `_clients`
- Matches Java's synchronized approach

### 4. SetBotStateAndAwaitTick Method
```csharp
public bool SetBotStateAndAwaitTick(double? energy = null, ...)
{
    lock (_stateLock) {
        // Update state fields
        if (energy.HasValue) _energy = energy.Value;
        // ... other updates ...
        
        _tickEvent.Reset();
        
        lock (_clients) {
            foreach (var conn in _clients) {
                SendTickEventForBot(conn, _turnNumber++);
            }
        }
    }
    
    return AwaitTick(1000);
}
```

**Key Points**:
- State updates and tick sending happen atomically
- Test thread waits for tick acknowledgment
- Used for manual game progression in tests

### 5. SetInitialBotState Method (NEW)
```csharp
public void SetInitialBotState(double? energy = null, ...)
{
    lock (_stateLock) {
        if (energy.HasValue) _energy = energy.Value;
        // ... other updates ...
    }
}
```

**Purpose**: Set state before bot starts running (no tick sent).
**Use Case**: Test setup that needs specific initial conditions.

## Happens-Before Relationships

### Java Memory Model (via C# equivalent)
1. **Lock acquisition** happens-before **any action in lock block**
2. **Lock release** happens-before **subsequent lock acquisition** (by any thread)
3. **Signal/Set on ManualResetEvent** happens-before **WaitOne returns**

### Applied to MockedServer
```
Test Thread                    WebSocket Thread
──────────────────            ──────────────────
SetBotStateAndAwaitTick()
  lock(_stateLock) {
    _energy = 75.0
    SendTick()              ──>  OnMessage(BotIntent)
  }                                lock(_stateLock) {
unlock(_stateLock) ───────────>      read _energy (sees 75.0)
                                     ...
                                  }
```

## Protocol Compliance

### Bot Joining Sequence
1. Bot connects → Server sends `server-handshake`
2. Bot sends `bot-handshake` → Server sends `game-started-event-for-bot`
3. Bot sends `bot-ready` → Server sends `round-started-event` + **tick-event-for-bot** ✓
4. Bot sends `bot-intent` → Server sends `tick-event-for-bot`

**Fix**: Step 3 previously missed sending tick event.

### Running Next Turn Sequence
```
Server sends tick-event-for-bot
Bot processes tick
Bot sends bot-intent
Server updates state
Server sends next tick-event-for-bot
```

All state updates happen within lock to ensure consistency.

## Testing Strategy

### Thread-Safety Tests (MockedServerThreadSafetyTest.cs)

1. **Concurrent State Updates**
   - 10 threads calling `SetInitialBotState()` simultaneously
   - Verifies no race conditions or exceptions

2. **Sequential State Updates with Ticks**
   - 3 consecutive calls to `SetBotStateAndAwaitTick()`
   - Verifies proper synchronization of state and tick events

3. **Bot Ready State**
   - Tests `AwaitBotReady()` concurrent tick sending
   - Verifies initialization without race conditions

4. **Deadlock Prevention**
   - Concurrent state updates while bot sends intents
   - Verifies proper lock ordering prevents deadlock

### Why These Tests Would Have Been Flaky Before
Without proper locking:
- `_speed += _speedIncrement` could be lost (read-modify-write race)
- Tick could be sent with inconsistent state (partial updates visible)
- `_turnNumber++` could produce duplicate or skipped turn numbers
- State reads in `SendTickEventForBot()` could see torn values

## Comparison with Java Implementation

| Aspect | Java | C# (.NET) |
|--------|------|-----------|
| Lock mechanism | `synchronized` method | `lock (_stateLock)` |
| State visibility | `volatile` fields implied | Lock provides memory barrier |
| Latch | `CountDownLatch` (recreate) | `ManualResetEvent` (reset) |
| State updates | In synchronized blocks | In `lock (_stateLock)` blocks |
| Protocol compliance | ✓ Sends tick on BotReady | ✓ **Fixed** in this task |

Both implementations now provide equivalent thread-safety guarantees.

## Future Maintenance Guidelines

### Adding New State Fields
1. Declare field as private instance variable
2. Protect ALL access (read/write) with `lock (_stateLock)`
3. Update `SetInitialBotState()` and `SetBotStateAndAwaitTick()` if needed
4. Document in this file

### Modifying Message Handlers
1. Identify which state fields are accessed
2. Ensure access is within `lock (_stateLock)`
3. Maintain lock ordering: `_stateLock` before `_clients`
4. Consider early returns (must be inside lock if state was read)

### Adding New Synchronization Points
1. Use existing ManualResetEvent pattern
2. Document the happens-before relationship
3. Add corresponding `Await*` method for test convenience
4. Consider timeout handling

## References
- Sequence diagrams: `schema/schemas/README.md`
- Java implementation: `bot-api/java/src/test/java/test_utils/MockedServer.java`
- Task description: `openspec/changes/2025-12-31-refactor-bot-api-test-infrastructure/tasks.md`
