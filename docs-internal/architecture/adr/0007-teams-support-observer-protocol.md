# ADR-0007: Teams Support in Observer Protocol

**Status:** Proposed  
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

Ensure `rank` is always 1..N (sequential placement). Current bug produces inconsistent values like `[0,0,3,0,8]`.

### 3. Use ID sign convention (from ADR-0005)

Observers can also use: `id > 0` = real team, `id < 0` = solo bot.

See [ADR-0005](./0005-bot-id-team-id-namespace-separation.md) for details.

---

## Consequences

- ✅ Explicit `isTeam` flag for clarity
- ✅ Consistent rank values (1..N)
- ✅ Backward compatible (new optional field)
- ✅ Two discrimination methods: `isTeam` field OR ID sign convention

---

## References

- [ADR-0005: Participant ID as Unified Team Identifier](./0005-bot-id-team-id-namespace-separation.md)
- [results-for-observer.schema.yaml](/schema/schemas/results-for-observer.schema.yaml)

