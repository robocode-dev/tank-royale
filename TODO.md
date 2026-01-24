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

### Issue #4: Events Lost When IsSameEvent Returns True

**Status**: 🟡 Investigate  
**Severity**: Medium  
**Location**: `bot-api/dotnet/api/src/internal/EventQueue.cs` - `DispatchEvents()` method

**Problem**:  
In `DispatchEvents()`, when processing events during a nested `Go()` call from an event handler:

```csharp
BotEvent currentEvent = GetNextEvent();  // REMOVES event from queue
if (IsSameEvent(currentEvent))  // Checks if same priority as current top event
{
    if (IsCurrentEventInterruptible)
    {
        throw new ThreadInterruptedException();
    }
    break;  // ⚠️ EVENT IS LOST! Removed but never dispatched
}
```

**Note**: Java has the same pattern. Need to verify if this is intentional behavior or a bug in both implementations.

---

### Issue #5: Double Remove in DispatchEvents (Minor)

**Status**: 🟡 Investigate  
**Severity**: Low  
**Location**: `bot-api/dotnet/api/src/internal/EventQueue.cs`

**Problem**:  
The event is removed twice:

1. In `GetNextEvent()`: `_events.Remove(botEvent);`
2. Later in `DispatchEvents()`: `_events.Remove(currentEvent);`

The second remove is a no-op since the event is already gone, but it's unnecessary code.

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

### Issues to Investigate:

4. 🟡 Events lost when IsSameEvent returns true
5. 🟡 Double remove in DispatchEvents

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
