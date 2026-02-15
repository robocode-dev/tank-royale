# ADR-0020: Teams Support in Observer Protocol

**Status:** Approved  
**Date:** 2026-02-14

---

## Context

Observers (web viewers, GUIs) cannot reliably distinguish team results from solo bot results in
`game-ended-event-for-observer`.

**Issues reported:**

1. No explicit field indicates if a result is for a team or bot
2. `rank` values are inconsistent (sometimes 0 for teams, sometimes inherited)

---

## Decision

### 1. Add `isTeam` field to results

Add `isTeam: boolean` to `results-for-observer.schema.yaml`:

```yaml
isTeam:
    description: True if this result represents a team, false for solo bot
    type: boolean
```

### 2. Fix rank values

Use **competition ranking** (also known as "1224" ranking) for consistency with classic Robocode:

- Tied scores share the same rank
- Subsequent ranks skip positions equal to the number of ties

Example with 5 participants:
| Rank | Score | Participant |
|------|-------|-------------|
| 1 | 370 | Bot1 |
| 1 | 370 | Team1 |
| 3 | 230 | Bot3 |
| 3 | 230 | Bot4 |
| 5 | 220 | Bot5 |

This fixes the previous bug that produced inconsistent values like `[0,0,3,0,8]`.

### 3. Use ID sign convention (from ADR-0015)

Observers can also use: `id > 0` = real team, `id < 0` = solo bot.

See [ADR-0015](./0015-bot-id-team-id-namespace-separation.md) for details.

---

## Consequences

- ✅ Explicit `isTeam` flag for clarity
- ✅ Consistent rank values (1..N)
- ✅ Backward compatible (new optional field)
- ✅ Two discrimination methods: `isTeam` field OR ID sign convention

---

## References

- [ADR-0015: Participant ID as Unified Team Identifier](./0015-bot-id-team-id-namespace-separation.md)
- [results-for-observer.schema.yaml](/schema/schemas/results-for-observer.schema.yaml)
