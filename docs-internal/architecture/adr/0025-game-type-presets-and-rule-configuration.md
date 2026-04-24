# ADR-0025: Game Type Presets and Rule Configuration

**Status:** Accepted  
**Date:** 2026-02-28 (Documenting historical decision)

---

## Context

Tank Royale battles are governed by configurable rules: arena dimensions, number of rounds, gun cooling rate, inactivity
limits, timeouts, and participant constraints. Different play styles need different defaults — a 1v1 duel has different
arena and participant requirements than a 10-bot melee.

### Origins: RoboRumble Competition Formats

The game type presets originate from the classic Robocode community's standardized competition formats, established
through **RoboRumble** (created by Albert Perez) and **LiteRumble** (created by Julian Kent / "Skilgannon"). These
formats ensure fair, reproducible battle conditions for community rankings:

- **1v1** — Two bots duel on an 800×600 arena, 35 rounds. The core competitive format for measuring targeting and
  movement skill. ([RoboRumble 1v1 standard](https://book.robocode.dev/energy-and-scoring/competition-formats-rankings.html#_1v1-standard-format))
- **Melee** — 10+ bots battle on a 1000×1000 arena, 10 rounds. The "Battle Royale" format — survival matters more
  than raw damage. The name "Tank Royale" is inspired by this format.
  ([RoboRumble melee standard](https://book.robocode.dev/energy-and-scoring/competition-formats-rankings.html#melee-standard-format))
- **Teams** — Coordinated team battles (e.g., Twin Duel 2v2). Not yet supported in Tank Royale but planned for the
  future. ([Team formats](https://book.robocode.dev/energy-and-scoring/competition-formats-rankings.html#team-standard-formats))

The **`classic`** preset represents how Robocode runs when it is *not* used for competitions — the default mode for
casual play, bot development, and testing. It uses the same 800×600 arena as 1v1 but without the strict participant
constraints of competition formats.

**Problem:** How should game rules be configured, and how should common configurations be made easy to select?

---

## Decision

Define a **game type preset system** with four named presets and a `GameSetup` data class that holds all rule
parameters with per-field "locked" flags.

### Game Type Presets

| Preset | Arena | Min Bots | Max Bots | Key Locked Fields | Purpose |
|--------|-------|----------|----------|-------------------|---------|
| **`classic`** | 800×600 | 2 | unlimited | arena, participants | Default non-competition mode |
| **`melee`** | 1000×1000 | 10 | unlimited | arena | Free-for-all battles (RoboRumble standard: exactly 10) |
| **`1v1`** | 800×600 | 2 | 2 | arena, participants | Head-to-head duels (RoboRumble 1v1) |
| **`custom`** | 800×600 | 2 | unlimited | none | Full user control |
| **`teams`** | — | — | — | — | *Planned — not yet supported* |

> **Note:** The RoboRumble melee standard uses exactly 10 bots. The GUI's melee preset enforces a *minimum* of 10 but
> allows more for casual/local play. The Battle Runner API should follow the same approach — `min=10, max=unlimited` —
> since users may want larger melees outside competition contexts.

All presets share common defaults: `gunCoolingRate=0.1`, `maxInactivityTurns=450`, `turnTimeout=5ms` (server) /
`30ms` (GUI), `readyTimeout=10s`, `defaultTurnsPerSecond=30`.

### GameSetup Data Class

All rule parameters live in `GameSetup` (`dev.robocode.tankroyale.server.model.GameSetup`) with:

- **Default values** sourced from constants in `dev.robocode.tankroyale.server.rules.setup`
- **Locked flags** (`isArenaWidthLocked`, `isNumberOfRoundsLocked`, etc.) that indicate which fields a preset
  considers fixed — the GUI uses these to disable input fields; other consumers can use them as guidance

### Server-Side Defaults (`setup.kt`)

```kotlin
DEFAULT_GAME_TYPE = "classic"
DEFAULT_ARENA_WIDTH = 800
DEFAULT_ARENA_HEIGHT = 600
DEFAULT_MIN_NUMBER_OF_PARTICIPANTS = 2
DEFAULT_NUMBER_OF_ROUNDS = 35
DEFAULT_GUN_COOLING_RATE = 0.1
DEFAULT_INACTIVITY_TURNS = 450
DEFAULT_TURN_TIMEOUT = 5.milliseconds
DEFAULT_READY_TIMEOUT = 10.seconds
DEFAULT_TURNS_PER_SECOND = 30
```

> **Note:** `DEFAULT_READY_TIMEOUT` was raised from 1 second to 10 seconds. See [ADR-0040](./0040-ready-timeout-default.md).

### Preset Definitions

Presets are currently defined in the GUI (`GamesSettings.kt`) and applied client-side. The server itself only sees the
final `GameSetup` values — it does not have built-in knowledge of presets. This means:

- The server accepts any valid `GameSetup` regardless of game type label
- Preset enforcement (locked fields, default overrides) happens in the client
- The `gameType` field is a string label, not an enum — extensible without server changes

---

## Rationale

- ✅ **Ease of use** — Selecting "classic" gives sensible defaults without specifying every parameter
- ✅ **Flexibility** — "custom" allows full control; presets can be overridden per-field
- ✅ **Extensibility** — `gameType` is a string, not an enum — new presets can be added without schema changes
- ✅ **Separation of concerns** — Server validates ranges; clients enforce preset semantics
- ✅ **Locked flags** — UI and API consumers know which fields a preset considers fixed
- ❌ **Client-side presets** — Preset definitions are duplicated if multiple clients exist (GUI, Battle Runner, etc.)
- ❌ **String-typed game type** — No compile-time validation of preset names

---

## Consequences

### Positive

- ✅ Common battle configurations are one selection away
- ✅ New game types can be added without protocol changes
- ✅ Server remains simple — it just runs whatever `GameSetup` it receives

### Negative

- ⚠️ Preset definitions currently live only in GUI code — other consumers (e.g., Battle Runner API) must replicate them
  or extract them to a shared library
- ⚠️ Rule defaults exist in three places: `server/rules/setup.kt` (constants), `lib/client/model/GameSetup.kt`
  (client data class), and `gui/settings/GamesSettings.kt` (preset definitions) — this should be consolidated
- ⚠️ No server-side validation that a "classic" game actually uses classic parameters — a client could label anything
  "classic"

---

## References

- [Server rules constants](/server/src/main/kotlin/dev/robocode/tankroyale/server/rules/setup.kt)
- [Server GameSetup model](/server/src/main/kotlin/dev/robocode/tankroyale/server/model/GameSetup.kt)
- [GUI preset definitions](/gui/src/main/kotlin/dev/robocode/tankroyale/gui/settings/GamesSettings.kt)
- [ADR-0008: Server-Authoritative Physics](./0008-server-authoritative-physics.md) — Server owns physics rules
- [ADR-0024: Battle Runner API](./0024-battle-runner-api.md) — Consumes presets via programmatic API
- [The Book of Robocode: Competition Formats & Rankings](https://book.robocode.dev/energy-and-scoring/competition-formats-rankings.html) — Origins of game type standards
- [RoboWiki: RoboRumble](https://robowiki.net/wiki/RoboRumble) — Community competition system
