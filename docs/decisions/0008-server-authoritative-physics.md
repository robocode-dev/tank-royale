# ADR-0008: Server-Authoritative Deterministic Physics

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Tank Royale is a competitive programming game where fairness and reproducibility are essential.

**Problem:** Where should physics run, and how to ensure deterministic outcomes?

---

## Decision

All physics run **exclusively on the server** using a **fixed 13-step update order** per turn:

1. Cool down and fire guns
2. Execute bot intents (apply movement)
3. Check bot-wall collisions
4. Check bot-bot collisions
5. Constrain bot positions
6. Check and handle scans
7. Update bullet positions
8. Check bullet-wall collisions
9. Check bullet hits
10. Check inactivity
11. Check disabled bots
12. Check defeated bots
13. Check round/game over

Uses **double-precision floating-point** with epsilon comparison (`1E-6`) for near-equality checks.

---

## Rationale

- ✅ Deterministic — same inputs always produce same outputs
- ✅ Fair — no bot can manipulate physics locally
- ✅ Reproducible — enables meaningful battle recordings and replays
- ✅ Fixed update order prevents order-dependent bugs
- ❌ Bots cannot predict exact physics outcomes client-side
- ❌ Double-precision (not fixed-point) means cross-platform edge cases theoretically possible

---

## References

- [ModelUpdater.kt](/server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt)
- [math.kt](/server/src/main/kotlin/dev/robocode/tankroyale/server/model/math.kt)
- [ADR-0011: Real-Time Game Loop Architecture](./0011-realtime-game-loop-architecture.md)
