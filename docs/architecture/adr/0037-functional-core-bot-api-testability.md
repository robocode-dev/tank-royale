# ADR-0037: Functional Core Extraction for Bot API Testability

**Status:** Proposed  
**Date:** 2026-04-14

---

## Context

`BaseBotInternals` is the central class in every Bot API implementation (Java, C#, Python, TypeScript). It manages the bot's connection to the server, processes ticks, builds intents, and sends them over WebSocket. At 700+ lines (Java), it mixes three distinct concerns:

1. **Pure validation and intent-building logic** — NaN checks, energy/gunHeat guards, turn-rate clamping, optimal velocity calculation, movement state management
2. **I/O and threading** — WebSocket connect/send, thread lifecycle, stdout/stderr capture, event dispatch
3. **Synchronization** — monitor-based blocking (`waitForNextTurn`), atomic flags, turn coordination

**The problem:** Testing intent-building logic (e.g., "does `setFire(1.5)` produce `firepower=1.5` in the intent?") requires spinning up a WebSocket connection, a MockedServer, multiple threads, and a 5-step semaphore-gated capture protocol. This makes tests:

- **Slow** — each test creates a WebSocket server and connection
- **Fragile** — synchronization bugs in the test harness mask bugs in the code under test
- **Hard to write** — the intent-capture protocol is non-obvious and undocumented (fixed separately)
- **Platform-isolated** — each platform writes its own tests with no shared validation

A deep architecture audit confirmed that all validation rules are **identical** across Java, C#, Python, and TypeScript. The pure logic is extractable: it has no dependencies on WebSocket, threads, or I/O.

**References:**

- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md) — requires 1:1 semantic equivalence
- [ADR-0004: Java as Reference Implementation](./0004-java-reference-implementation.md) — Java is authoritative
- [ADR-0010: Declarative Bot Intent Model](./0010-declarative-bot-intent-model.md) — intent-based architecture

---

## Decision

Extract pure validation and intent-building logic from `BaseBotInternals` into a dedicated **functional core** module in each platform. `BaseBotInternals` becomes a thin I/O shell that delegates to the core.

### What Gets Extracted

| Pure Logic | Current Location (Java) | Description |
|-----------|------------------------|-------------|
| Fire validation | `setFire()` lines 519–528 | NaN check, energy/gunHeat guards |
| Turn rate clamping | `setTurnRate()` lines 538–543 | NaN check, clamp to ±maxTurnRate |
| Gun turn rate clamping | `setGunTurnRate()` lines 545–550 | NaN check, clamp to ±maxGunTurnRate |
| Radar turn rate clamping | `setRadarTurnRate()` lines 552–557 | NaN check, clamp to ±maxRadarTurnRate |
| Target speed clamping | `setTargetSpeed()` lines 559–564 | NaN check, clamp to ±maxSpeed |
| Optimal velocity | `getNewTargetSpeed()` lines 629–649 | Voidious/Skilgannon algorithm |
| Stopping distance | `getDistanceTraveledUntilStop()` lines 658–665 | Braking distance calculation |
| Movement update | `BotInternals.updateMovement()` lines 371–400 | Distance/speed state machine |
| Turn processing | `BotInternals.processTurn()` lines 112–122 | Remaining-angle updates |

### What Stays in BaseBotInternals (I/O Shell)

- WebSocket connection lifecycle (`connect`, `close`)
- Thread lifecycle (`startThread`, `stopThread`, `waitForNextTurn`)
- Event marshaling and dispatch (`addEventsFromTick`, `dispatchEvents`)
- Stdout/stderr redirection and transfer
- `execute()` orchestration — calls the pure core, then sends via WebSocket

### Naming Convention

| Platform | Module | Location |
|----------|--------|----------|
| Java | `IntentValidator` | `botapi/internal/IntentValidator.java` |
| C# | `IntentValidator` | `BotApi/Internal/IntentValidator.cs` |
| Python | `intent_validator` | `bot_api/internal/intent_validator.py` |
| TypeScript | `intentValidator` | `bot-api/internal/intentValidator.ts` |

### Known Structural Divergence: Python

Python's internal architecture differs structurally from Java/C#/TypeScript while remaining **behaviorally identical**:

| Aspect | Java / C# / TypeScript | Python |
|--------|----------------------|--------|
| Movement tracking | Public `BotInternals` class | Private `_BotInternals` inner class inside `Bot` |
| State fields | Direct fields in `BaseBotInternals` | Extracted into separate `BaseBotInternalData` dataclass |
| BotInternals visibility | Package-private / internal | Name-mangled private (`_BotInternals`) |

The validation and intent-building logic targeted for extraction into `IntentValidator` lives in `base_bot_internals.py` — the same location as the other platforms. The extraction is unaffected by these differences. However, when implementing `intent_validator.py`, be aware that Python accesses intent state via `self.data.bot_intent` rather than `self.botIntent` directly.

### Example: Before and After

**Before (Java):**
```java
// BaseBotInternals.java — mixed concerns
void setFire(double firepower) {
    if (Double.isNaN(firepower))
        throw new IllegalArgumentException("'firepower' cannot be NaN");
    if (baseBot.getEnergy() < firepower || baseBot.getGunHeat() > 0)
        return;
    botIntent.setFirepower(firepower);
}
```

**After (Java):**
```java
// IntentValidator.java — pure function, no I/O
static boolean validateAndSetFire(BotIntent intent, double firepower, double energy, double gunHeat) {
    if (Double.isNaN(firepower))
        throw new IllegalArgumentException("'firepower' cannot be NaN");
    if (energy < firepower || gunHeat > 0)
        return false;
    intent.setFirepower(firepower);
    return true;
}

// BaseBotInternals.java — thin shell delegates to core
void setFire(double firepower) {
    IntentValidator.validateAndSetFire(botIntent, firepower, baseBot.getEnergy(), baseBot.getGunHeat());
}
```

---

## Rationale

**Why extract, not rewrite:**

- ✅ **Zero public API changes** — `BaseBot.setFire()`, `setTurnRate()`, etc. keep their signatures
- ✅ **Trivially testable** — pure functions with no I/O dependencies, no threads, no WebSocket
- ✅ **Enables shared test definitions** — all platforms can test the same pure logic against the same test data (see ADR-0038)
- ✅ **Aligns with FP-FUNCTIONAL-CORE-IMPERATIVE-SHELL** — pure core + thin I/O shell
- ✅ **Aligns with SOLID-SRP** — BaseBotInternals currently has at least 3 reasons to change
- ✅ **Low risk** — extraction is mechanical; behavior doesn't change

**Alternatives considered:**

1. **Keep current structure, improve tests only** — Rejected. The fundamental issue is that testing pure logic requires I/O infrastructure. Better tests don't fix this coupling.
2. **Full rewrite of BaseBotInternals** — Rejected. Too risky, too much effort. Extraction achieves the goal with minimal disruption.
3. **Generate code from a single source** — Rejected (per ADR-0003). Generated code is hard to debug and customize.

---

## Consequences

### Positive

- Pure validation logic is testable without WebSocket, threads, or MockedServer
- Cross-platform semantic parity becomes verifiable via shared test data
- `BaseBotInternals` shrinks from 700+ lines to ~400 lines of I/O orchestration
- New validation rules only need to be added to `IntentValidator` — single responsibility
- Existing MockedServer-based integration tests remain valid and unchanged

### Negative

- One-time migration effort across 4 platforms
- Slight indirection: `BaseBotInternals.setFire()` → `IntentValidator.validateAndSetFire()`
- Must maintain symmetry of `IntentValidator` across platforms (same as all Bot API code)

---

## References

- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Reference Implementation](./0004-java-reference-implementation.md)
- [ADR-0010: Declarative Bot Intent Model](./0010-declarative-bot-intent-model.md)
- [BaseBotInternals.java](/bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BaseBotInternals.java)
- [BotInternals.java](/bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BotInternals.java)
- [bot-api/tests/TESTING-GUIDE.md](/bot-api/tests/TESTING-GUIDE.md)
