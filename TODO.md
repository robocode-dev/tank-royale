# TODO - Bot API Issues to Fix

## C# Bot API Issues

### Issue #1: Race Condition in OnNextTurn Event Priority

**Status**: ✅ FIXED (C#, Java, Python)  
**Severity**: Medium  
**Location**:

- C#: `bot-api/dotnet/api/src/internal/BotInternals.cs` (line ~44)
- Java: `bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BotInternals.java` (line ~51)
- Python: `bot-api/python/src/robocode_tank_royale/bot_api/bot.py` (line ~41)

**Problem**:  
When `InternalEventHandlers.OnNextTurn.Publish()` is called from the WebSocket thread:

1. `BaseBotInternals.OnNextTurn` (priority 100) runs FIRST → calls `Monitor.PulseAll()` / `notifyAll()` to wake the bot
   thread
2. `BotInternals.OnNextTurn` (priority 90) runs SECOND → calls `ProcessTurn()` to update `TurnRemaining`,
   `DistanceRemaining`, etc.

After step 1 completes, the bot thread CAN wake up before step 2 runs. This means the bot thread may see stale values
for `TurnRemaining`, `DistanceRemaining`, `GunTurnRemaining`, `RadarTurnRemaining`.

**Impact**:

- Blocking commands (`Forward()`, `TurnRight()`, etc.) would get stuck in infinite `WaitFor` loops
- The condition `TurnRemaining == 0` was never becoming true because `ProcessTurn()` hadn't run yet

**Fix Applied (all languages)**:  
Changed `BotInternals.OnNextTurn` priority from 90 to 110 (higher than BaseBotInternals' 100):

```csharp
// C#: In BotInternals.cs
internalEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 110);
```

```java
// Java: In BotInternals.java
instantEventHandlers.onNextTurn.subscribe(this::onNextTurn, 110);
```

```python
# Python: In bot.py
handlers.on_next_turn.subscribe(self.on_next_turn, 110)
```

This ensures `ProcessTurn()` runs BEFORE `Monitor.PulseAll()` / `notifyAll()` wakes up the bot thread.

---

### Issue #2: Base64 Decoding Bug in Team Messages

**Status**: ✅ FIXED (C# only - Java and Python were not affected)  
**Severity**: High  
**Location**: `bot-api/dotnet/api/src/mapper/EventMapper.cs`

**Problem**:  
Team messages were incorrectly being decoded from Base64, but the server sends them as plain JSON strings.

**Fix Applied**:  
Removed the incorrect Base64 decoding. Messages are now deserialized directly from the JSON string.

---

### Issue #3: OnDeath Callback Not Being Called

**Status**: ✅ FIXED (C#, Java, Python)  
**Severity**: High  
**Location**:

- C#: `bot-api/dotnet/api/src/internal/BotInternals.cs`
- Java: `bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BotInternals.java`
- Python: `bot-api/python/src/robocode_tank_royale/bot_api/bot.py`

**Problem**:  
The internal `OnDeath` handler was subscribed to `internalEventHandlers` (instant event handlers) which fire immediately
during tick processing, BEFORE the event queue dispatches events to user callbacks. This caused the following sequence:

1. Server sends tick with `DeathEvent`
2. Internal handlers fire → `BotInternals.OnDeath()` → `stopThread()` → `isRunning = false`
3. Later, `dispatchEvents()` is called to fire user callbacks
4. `dispatchEvents()` has `while (isRunning)` check → returns immediately because `isRunning = false`
5. **User's `OnDeath` callback is NEVER called!**

**Fix Applied (all languages)**:  
Moved `OnDeath` subscription from internal handlers to public bot event handlers with priority 0 (lower than user's
default priority of 1). This ensures:

- User's `OnDeath` callback runs first (priority 1)
- Then internal `stopThread()` runs (priority 0)

```csharp
// C#: In BotInternals.cs
var botEventHandlers = baseBotInternals.BotEventHandlers;
botEventHandlers.OnDeath.Subscribe(OnDeath, 0);
```

```java
// Java: In BotInternals.java
var botEventHandlers = baseBotInternals.getBotEventHandlers();
botEventHandlers.onDeath.

subscribe(this::onDeath, 0);
```

```python
# Python: In bot.py
public_handlers = self._base_bot_internals.bot_event_handlers
public_handlers.on_death.subscribe(_stop_thread, 0)
```

---

### Issue #4: dispatchEvents Refactored to Match Original Robocode

**Status**: ✅ FIXED (C#, Java, Python)  
**Severity**: Medium  
**Location**:

- C#: `bot-api/dotnet/api/src/internal/EventQueue.cs` - `DispatchEvents()` method
- Java: `bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/EventQueue.java` - `dispatchEvents()` method
- Python: `bot-api/python/src/robocode_tank_royale/bot_api/internal/event_queue.py` - `dispatch_events()` method

**Problem**:  
The original implementation differed from the original Robocode in several key ways:

1. **Removed event before checking priority** - caused events to be lost
2. **While loop only checked `isBotRunning()`** - didn't check priority condition
3. **Didn't set `currentTopEvent = null` on exceptions**

**Original Robocode behavior** (from `EventManager.java`):

```java
while ((currentEvent = eventQueue.get(0)) != null  // PEEK, not remove!
        && currentEvent.getPriority() >= currentTopEventPriority) {  // Priority check in loop!
    
    if (currentEvent.getPriority() == currentTopEventPriority) {
        if (isInterruptible(currentTopEventPriority)) {
            throw new EventInterruptedException();
        }
        break;  // Event stays in queue - intentional loss for non-interruptible
    }
    
    // Only remove AFTER passing all checks
    eventQueue.remove(currentEvent);
    
    try {
        dispatch(currentEvent);
    } catch (EventInterruptedException e) {
        currentTopEvent = null;  // Clear on exception
    } catch (RuntimeException | Error e) {
        currentTopEvent = null;  // Clear on exception
        throw e;
    } finally {
        currentTopEventPriority = oldTopEventPriority;
    }
}
```

**Fix Applied (all languages)**:  
Refactored to match original Robocode exactly:

1. **Peek first, remove later**: Use `peekNextEvent()` to check the event, only call `removeNextEvent()` after deciding
   to dispatch
2. **Priority check in while condition**: `getPriority(currentEvent) >= currentTopEventPriority`
3. **Set `currentTopEvent = null` on exceptions**: Match original Robocode catch block behavior
4. **Move `addCustomEvents()` to `dispatchEvents()`**: Match original Robocode which processes custom events at dispatch
   time

```java
// New Java implementation (C# and Python match this pattern)
while (isBotRunning()
        && (currentEvent = peekNextEvent()) != null
        && getPriority(currentEvent) >= currentTopEventPriority) {

    if (getPriority(currentEvent) == currentTopEventPriority) {
        if (currentTopEventPriority > Integer.MIN_VALUE && isCurrentEventInterruptible()) {
            // ... throw exception
        }
        break; // Same priority but not interruptible - intentional in original Robocode
    }

    // ... set up priority tracking ...
    
    removeNextEvent(); // Remove only after we've decided to dispatch

    try {
        dispatch(currentEvent, turnNumber);
    } catch (ThreadInterruptedException ignore) {
        currentTopEvent = null; // Match original Robocode
    } catch (RuntimeException | Error e) {
        currentTopEvent = null; // Match original Robocode
        throw e;
    } finally {
        currentTopEventPriority = oldTopEventPriority;
    }
}
```

**Note**: The "loss of event" when same priority and not interruptible is **intentional** behavior from original
Robocode - the event stays in the queue and will be processed when the outer handler completes.

---

### Issue #5: Double Remove in DispatchEvents (Minor)

**Status**: ✅ FIXED (C#, Java)  
**Severity**: Low  
**Location**:

- C#: `bot-api/dotnet/api/src/internal/EventQueue.cs`
- Java: `bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/EventQueue.java`

**Problem**:  
The event was removed twice:

1. In `GetNextEvent()`: event is removed from the list/queue
2. Later in `DispatchEvents()`: `_events.Remove(currentEvent)` / `events.remove(currentEvent)`

The second remove was a no-op since the event was already gone, but it was unnecessary code and could cause confusion.

**Fix Applied**:  
Refactored to use `peekNextEvent()` + `removeNextEvent()` pattern, eliminating the double-remove issue entirely.

---

### Issue #6: Python Had Extra Inconsistent Code (Minor)

**Status**: ✅ FIXED (Python only)  
**Severity**: Low  
**Location**: `bot-api/python/src/robocode_tank_royale/bot_api/internal/event_queue.py`

**Problem**:  
Python had extra `EventInterruption.set_interruptible()` calls in `dispatch_events` that didn't exist in Java/C#:

```python
# This code was in Python but NOT in Java/C#
if self.current_top_event is not None:
    EventInterruption.set_interruptible(type(self.current_top_event), False)
```

These calls were redundant since the `dispatch()` method already clears the interruptible flag in its `finally` block.

**Fix Applied**:  
Removed the extra code to align with Java/C# behavior.

---

## Tests Created

### C# Tests

- `bot-api/dotnet/test/src/TeamMessageSerializationTest.cs`
- `bot-api/dotnet/test/src/TeamMessageRealisticTest.cs`

### Java Tests

- `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/TeamMessageSerializationTest.java`
- `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/TeamMessageRealisticTest.java`

### Python Tests

- `bot-api/python/tests/bot_api/test_team_message_serialization.py`
- `bot-api/python/tests/bot_api/test_team_message_realistic.py`

---

## Summary

### Fixed Issues:

1. ✅ Race condition in OnNextTurn priority (C#, Java, Python) - blocking commands getting stuck
2. ✅ Base64 decoding bug in team messages (C# only)
3. ✅ OnDeath callback not being called (C#, Java, Python) - internal death handler was stopping the thread before user's
   OnDeath callback could run
4. ✅ dispatchEvents refactored to match original Robocode (C#, Java, Python) - peek before remove, priority check in
   while loop, currentTopEvent nulled on exceptions
5. ✅ Double remove in DispatchEvents fixed via peek/remove pattern (C#, Java)
6. ✅ Python had extra inconsistent code (Python only) - removed redundant EventInterruption calls

## Related Files

- `bot-api/dotnet/api/src/internal/BotInternals.cs`
- `bot-api/dotnet/api/src/internal/BaseBotInternals.cs`
- `bot-api/dotnet/api/src/internal/EventQueue.cs`
- `bot-api/dotnet/api/src/mapper/EventMapper.cs`
- `bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BotInternals.java`
- `bot-api/python/src/robocode_tank_royale/bot_api/bot.py`

## Testing

Test with:

1. MyFirstTeam (MyFirstLeader + MyFirstDroid) - team messaging with two teams competing
2. Verify droids receive RobotColors and Point messages
3. Verify droids turn and fire at targets sent by leader
4. Verify colors are applied correctly
5. Run the new unit tests in each language
