# ADR-0005: Participant ID as Unified Team Identifier

**Status:** Accepted  
**Date:** 2026-02-14

---

## Context

All battle participants are treated as "teams" internally. A solo bot is a "team of one."

**Problem:** Real team IDs (from booter) and bot IDs (from server) could collide if both use positive integers.

**Example collision:**
- Booter assigns Team ID = 1
- Server assigns Bot ID = 1 to a solo bot
- Observer cannot distinguish them in results

---

## Decision

Use **negative bot IDs** for solo bots to avoid collision with real team IDs.

| Participant Type | ID in Results |
|------------------|---------------|
| Real team | Positive (from booter: 1, 2, 3...) |
| Solo bot | Negative (negated botId: -1, -2, -3...) |

**Implementation** (`ParticipantId.kt`):
```kotlin
val id: Int = teamId?.id ?: -botId.value
```

---

## Consequences

- ✅ No ID collision between teams and solo bots
- ✅ Observers can distinguish: `id > 0` = real team, `id < 0` = solo bot
- ✅ No schema changes required
- ✅ Booter unchanged (keeps positive team IDs)

---

## References

- [ParticipantId.kt](/server/src/main/kotlin/dev/robocode/tankroyale/server/model/ParticipantId.kt)
- [ADR-0007: Teams Support in Observer Protocol](./0007-teams-support-observer-protocol.md)
