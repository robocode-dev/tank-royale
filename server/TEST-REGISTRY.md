# Server Test Registry

The single source of truth for **what must be tested** in the Tank Royale Server. Each row is an acceptance criterion identified by a `TR-SRV-xxx` ID.

Use this registry to:
- See which tests exist and where gaps are
- Find the acceptance ID to tag a new test with
- Track migration from LEGACY tests to new testable architecture

Governed by [ADR-0039](../docs-internal/architecture/adr/0039-server-testability.md).

---

## Rules

Every `TR-SRV-xxx` test listed here **must** include both **positive** (happy-path) and **negative** (rejection/edge) test cases under the same ID. A test is only ✅ when both sides are covered.

| Type | Verifies | Example |
|------|----------|---------|
| **Positive** | Correct behavior with valid input | Bullet hits bot -> damage applied |
| **Negative** | Correct rejection/handling of invalid input | Bullet misses bot -> no damage |

---

## PHY — Physics

| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-PHY-001 | Bullet-bot collisions (hit, miss, edge, diagonal) | ❌ |
| TR-SRV-PHY-002 | Bullet-bullet collisions (hit, near-miss) | ❌ |
| TR-SRV-PHY-003 | Bot-wall collisions (impact, safe distance) | ❌ |
| TR-SRV-PHY-004 | Bot-bot collisions (overlap, clear spacing) | ❌ |
| TR-SRV-PHY-005 | Line intersection (crossing, parallel, coincident, endpoints) | ❌ |

## ENG — Engine

| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-ENG-001 | Gun firing (cold/hot gun, energy levels) | ❌ |

## MAP — Mapping & Events

| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-MAP-001 | Events mapping (valid/unknown/malformed events) | ❌ |

## SCR — Scoring

| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-SCR-001 | Damage tracking (applied, zero-damage, overkill capping) | ❌ |

---

## Coverage Summary

| Category | Total IDs | Completed |
|----------|-----------|-----------|
| PHY | 5 | 0 |
| ENG | 1 | 0 |
| MAP | 1 | 0 |
| SCR | 1 | 0 |
| **Total** | **8** | **0** |

---

## How to add a new acceptance test

1. Choose a category (`PHY`, `ENG`, `MAP`, `SCR`, `SYS`)
2. Assign the next sequential ID: `TR-SRV-{CAT}-{NNN}`
3. Add a row to the table above with ❌ status
4. Implement the test and tag it with `TR-SRV-{CAT}-{NNN}`
5. Update this table once both positive and negative cases are covered (❌ → ✅)
