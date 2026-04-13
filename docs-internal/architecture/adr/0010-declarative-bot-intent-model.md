# ADR-0010: Declarative Bot Intent Model

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Bots need to communicate their desired actions to the server each turn. The original Robocode used imperative commands
(`ahead(100)`, `turnRight(45)`) executed in bot threads within the same JVM.

**Problem:** How should bots express actions in a network-based, turn-based architecture?

---

## Decision

Use a **declarative intent model**: bots set desired state (turn rates, target speed, firepower) rather than issuing
imperative commands. One `bot-intent` message per turn.

**Key properties:**

- **Accumulative** — null fields preserve previous values; bots only send changes
- **Server-constrained** — server applies physics limits (clamping, acceleration) to intents
- **Stateless wire format** — each message is self-contained; server maintains accumulated state

---

## Rationale

- ✅ Natural fit for tick-based protocol (one message per turn)
- ✅ Bandwidth-efficient (only send what changed)
- ✅ Simple cross-language implementation (no threading/blocking semantics)
- ✅ Server enforces physics constraints uniformly
- ✅ Enables deterministic simulation (all intents collected before physics runs)
- ❌ Less intuitive than imperative `ahead(100)` for beginners
- ❌ Bot APIs must translate imperative convenience methods to intents internally

---

## References

- [IBotIntent.kt](/server/src/main/kotlin/dev/robocode/tankroyale/server/model/IBotIntent.kt)
- [BaseBotInternals.java](/bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BaseBotInternals.java)
- [ADR-0011: Real-Time Game Loop Architecture](./0011-realtime-game-loop-architecture.md)
- [Tank Royale vs Original Robocode](/docs-build/docs/articles/tank-royale.md)
