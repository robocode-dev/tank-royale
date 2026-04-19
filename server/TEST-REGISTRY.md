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
| TR-SRV-PHY-001 | Bullet-bot collisions (hit, miss, edge, diagonal) | ✅ |
| TR-SRV-PHY-002 | Bullet-bullet collisions (hit, near-miss) | ✅ |
| TR-SRV-PHY-003 | Bot-wall collisions (impact, safe distance) | ✅ |
| TR-SRV-PHY-004 | Bot-bot collisions (overlap, clear spacing) | ✅ |
| TR-SRV-PHY-005 | Line intersection (crossing, parallel, coincident, endpoints) | ✅ |

## ENG — Engine

| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-ENG-001 | Gun firing (cold/hot gun, energy levels) | ✅ |

## MAP — Mapping & Events

| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-MAP-001 | Events mapping (valid/unknown/malformed events) | ✅ |

## SCR — Scoring

| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-SCR-001 | Damage tracking (applied, zero-damage, overkill capping) | ✅ |
| TR-SRV-SCR-002 | End-to-end scoring (multiple rounds, survival/kill bonus) | ✅ |

## PLN — Pipeline & Turn Processing
| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-PLN-001 | Turn-step pipeline (sequential steps, state mutations) | ✅ |

## LIF — Lifecycle
| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-LIF-001 | Game/round state machine transitions | ✅ |

## CON — Connection
| ID | Description | Status |
|----|-------------|--------|
| TR-SRV-CON-001 | Bot connect/disconnect/reconnect | ✅ |

---

## Coverage Summary

| Category | Total IDs | Completed |
|----------|-----------|-----------|
| PHY | 5 | 5 |
| ENG | 1 | 1 |
| MAP | 1 | 1 |
| SCR | 2 | 2 |
| PLN | 1 | 1 |
| LIF | 1 | 1 |
| CON | 1 | 1 |
| **Total** | **12** | **12** |

---

## How to add a new acceptance test

1. Choose a category (`PHY`, `ENG`, `MAP`, `SCR`, `SYS`)
2. Assign the next sequential ID: `TR-SRV-{CAT}-{NNN}`
3. Add a row to the table above with ❌ status
4. Implement the test and tag it with `TR-SRV-{CAT}-{NNN}`
5. Update this table once both positive and negative cases are covered (❌ → ✅)
