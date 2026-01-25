## Context

The Python Bot API must provide 1:1 semantic equivalence with the Java reference implementation. The original author
chose an async/await approach for Python, but this diverges from the threading model used in Java and C#.

**Stakeholders**: Bot developers (Robocoders), maintainers of all three Bot API implementations.

**Constraints**:

- Must match Java API behavior exactly (blocking semantics for `forward()`, `turn_left()`, `fire()`, etc.)
- Must support the same programming model: `run()` loop with blocking calls
- Internal WebSocket communication naturally uses async I/O in Python
- Python's GIL provides thread-safety for shared state access

## Goals / Non-Goals

**Goals**:

- Synchronous public API matching Java/C# signatures
- Internal async WebSocket I/O hidden from users
- Simplified testing (no more `IsolatedAsyncioTestCase`, no test hangs)
- Docstrings that mirror Java's Javadoc

**Non-Goals**:

- Providing an async public API (users can wrap if needed)
- Backwards compatibility with existing async Python bots
- Supporting Python < 3.10

## Decisions

### Decision 1: Threading Model (matches Java's `BaseBotInternals`)

**What**: Use Python's `threading` module to replicate Java's threading model.

**Why**: Direct 1:1 mapping with Java makes cross-platform maintenance easier.

**Implementation**:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Main Thread                              │
│  bot = SpinBot()                                                │
│  bot.start()  ←── blocks until game ends (closedLatch.await()) │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    WebSocket Thread (daemon)                     │
│  - Runs asyncio event loop                                      │
│  - Handles all WebSocket I/O                                    │
│  - Dispatches events to bot thread via thread-safe queue        │
│  - Notifies bot thread on each tick via threading.Condition     │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Bot Thread                                 │
│  - Created on first tick (onFirstTurn)                          │
│  - Runs bot.run() method                                        │
│  - Blocking methods use threading.Condition.wait()              │
│  - Stopped on round end/death/disconnect                        │
└─────────────────────────────────────────────────────────────────┘
```

### Decision 2: Blocking Mechanism (matches Java's `nextTurnMonitor`)

**What**: Use `threading.Condition` for blocking calls like `forward()`, `go()`.

**Why**: Direct equivalent to Java's `synchronized (nextTurnMonitor) { nextTurnMonitor.wait(); }`

**Python equivalent**:

```python
# Java: synchronized (nextTurnMonitor) { nextTurnMonitor.wait(); }
with self._next_turn_condition:
    while self._is_running and turn_number == self._current_tick.turn_number:
        self._next_turn_condition.wait()

# Java: synchronized (nextTurnMonitor) { nextTurnMonitor.notifyAll(); }
with self._next_turn_condition:
    self._next_turn_condition.notify_all()
```

### Decision 3: Event Handler Invocation

**What**: Event handlers are called synchronously from the bot thread during `go()`.

**Why**: Matches Java's `dispatchEvents()` which runs on the bot thread.

**Flow**:

1. `go()` is called
2. `dispatchEvents(turnNumber)` processes queued events
3. Each event handler (e.g., `on_scanned_bot()`) is called synchronously
4. Handler can call blocking methods like `fire()` which internally calls `go()`
5. After all events dispatched, `execute()` sends intent and waits for next turn

### Decision 4: Sample Bot Structure (matches Java exactly)

**Before (async)**:

```python
class SpinBot(Bot):
    async def run(self) -> None:
        while self.running:
            self.set_turn_right(10_000)
            await self.forward(10_000)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        await self.fire(3)

async def main() -> None:
    bot = SpinBot()
    await bot.start()

if __name__ == "__main__":
    asyncio.run(main())
```

**After (sync, matches Java)**:

```python
class SpinBot(Bot):
    def run(self) -> None:
        while self.running:
            self.set_turn_right(10_000)
            self.forward(10_000)

    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        self.fire(3)

if __name__ == "__main__":
    SpinBot().start()
```

## Alternatives Considered

### Alternative A: Keep async public API

- **Pros**: No breaking change
- **Cons**: Violates 1:1 equivalence, testing remains difficult, maintenance burden
- **Decision**: Rejected - 1:1 equivalence is non-negotiable

### Alternative B: Provide both sync and async APIs

- **Pros**: Flexibility for users
- **Cons**: Double maintenance, complexity, potential for inconsistencies
- **Decision**: Rejected - users can wrap sync API if they want async

## Risks / Trade-offs

| Risk                          | Mitigation                                                                  |
|-------------------------------|-----------------------------------------------------------------------------|
| Breaking existing Python bots | Clear migration guide in VERSIONS.md; Python API is still under development |
| Threading bugs                | Follow Java implementation exactly; extensive testing                       |
| Deadlocks                     | Use same patterns as Java (single condition variable, clear lock ordering)  |
| GIL performance               | Not a concern for bot game logic; I/O is async in background thread         |

## Migration Plan

1. Update public API (remove async)
2. Update internal implementation (add threading)
3. Update all 14 sample bots
4. Update all tests
5. Update docstrings
6. Add VERSIONS.md entry with migration guide

**Rollback**: Not applicable - this is a deliberate breaking change for a pre-1.0 API.

## Open Questions

None - the Java reference implementation provides all answers.
