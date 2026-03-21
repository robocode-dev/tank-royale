# ADR-0026: Identity-Based Bot Matching in Battle Runner

**Status:** Proposed
**Date:** 2026-03-21

---

## Context

The Battle Runner API ([ADR-0024](./0024-battle-runner-api.md)) waits for bot processes to connect before starting a
battle. The current implementation in `BattleRunner.waitForBots()` uses **count-based** detection:

```kotlin
if (update.bots.size - preExistingBots.size >= expectedCount) {
    botsReadyLatch.countDown()
}
```

Where `expectedCount = bots.size` — the number of bot **directories** passed to `runBattle()`.

This has three failure modes:

1. **Teams under-count.** A single bot directory containing a `team.json` launches N member bots (e.g., 5). The runner
   expects 1 connection per directory, but the team produces 5. With two 5-member teams, the runner expects 2 bots but
   needs 10. `waitForBots()` returns after 2 connections and the battle starts with an incomplete roster.

2. **Stray bots over-count.** When using an external server, bots from other clients or leftover processes can already
   be connected. The count-based check `update.bots.size - preExistingBots.size` only filters by connection time, not
   identity. A stray bot connecting at the right moment satisfies the count, causing the runner to start a battle with
   the wrong participants.

3. **Duplicate instances miscounted.** Running the same bot directory multiple times (e.g., two instances of "MyBot"
   for a 1v1 self-play benchmark) produces multiple bots with identical `(name, version)`. Simple set-based identity
   matching would see these as one bot and return early. The matching must be **count-aware per identity**.

All three issues stem from the same root cause: the runner does not know **which** bots it expects — only **how many**
directories it was given.

Additionally, the current hard-coded timeout (`BOT_CONNECT_TIMEOUT_MS = 30_000L`) is not configurable. Bots with
heavy first-time initialization — particularly Python bots that must install dependencies on first launch — can exceed
this timeout on slower machines, causing spurious `BattleException` failures. The GUI has the same problem: users see
no feedback during bot booting and may assume bots have failed when they are still initializing.

**Problem:** How should the Battle Runner reliably determine that all of its intended bots have connected, and how
should it communicate boot progress?

See [GitHub Issue #195](https://github.com/robocode-dev/tank-royale/issues/195).

---

## Options Considered

### Option 1: Refactor Booter as an Embeddable Library

Refactor the Booter into a library with a thin CLI wrapper. The Battle Runner would call the Booter directly, gaining
access to process handles and exact bot identities.

**Rejected because:**

- Disproportionate scope — the Booter works well as a subprocess and this would require a significant refactor
- Doesn't solve the stray-bot problem on external servers (stray bots are not launched by the Booter)
- Tight coupling between runner and booter internals

### Option 2: Richer Booter Stdout Protocol

Extend the Booter's stdout protocol (currently `<pid>;<dir>`) to include bot metadata: name, version, and
`memberCount` for teams.

**Rejected because:**

- Fixes team under-count (runner would know the expected member count) but not stray-bot identity matching
- Fragile string-based parsing protocol — adding fields requires coordinated changes across Booter and all consumers
- Still count-based at its core

### Option 3: Boot Correlation Token

Generate a UUID per boot request, pass it through the Booter → environment variable → bot handshake →
`BotListUpdate`. The runner matches bots by correlation token.

**Rejected because:**

- Requires changes to all three Bot APIs (Java, Python, .NET), the WebSocket schema, and the server
- Disproportionate scope for a runner-only problem
- Introduces a new protocol concept with cross-cutting impact

### Option 4: Identity-Based Multiset Matching (Runner-Only) — Selected

The runner pre-reads `bot.json` (and `team.json`) from each bot directory before launching bots. It builds an expected
**multiset** of `(name, version)` identities — preserving duplicate counts — expanding teams into their individual
member bots. When `BotListUpdate` arrives, the runner matches entries by `(name, version)` counts instead of a flat
total.

**Advantages:**

- All changes are confined to the `runner` module — no protocol, schema, server, or Bot API changes
- Uses data already present in `BotListUpdate` (bot name and version are included in the handshake)
- Correctly handles teams by reading `memberCount` from `team.json` and individual bot identities from member
  `bot.json` files
- Correctly handles duplicate instances (e.g., two instances of "MyBot 1.0") via count-per-identity matching
- Immune to stray bots: only bots matching expected identities are considered

---

## Decision

**Option 4: Identity-based multiset matching, runner-only.**

### Matching Logic

The `waitForBots()` method is changed from count-based to identity-based multiset matching:

1. **Pre-scan phase.** Before launching bots, the runner reads `bot.json` from each bot directory. For team directories
   (containing `team.json`), it reads the team members' `bot.json` files to determine individual bot identities.

2. **Expected identity multiset.** The runner builds a `Map<BotIdentity, Int>` of expected `(name, version)` pairs
   with their required counts. Examples:
   - Two instances of "MyBot 1.0" → `{("MyBot", "1.0"): 2}`
   - One "MyBot 1.0" + one team of 3 distinct bots → `{("MyBot", "1.0"): 1, ("TeamA", "1.0"): 1, ("TeamB", "1.0"): 1, ("TeamC", "1.0"): 1}`
   - Two instances of the same 3-member team → each member identity has count 2

3. **Matching phase.** On each `BotListUpdate`, the runner filters out pre-existing bots (by `BotAddress`), then
   counts remaining bots per `(name, version)`. The latch counts down when every expected identity meets or exceeds
   its required count.

This replaces the current logic:

```kotlin
// Before (count-based):
if (update.bots.size - preExistingBots.size >= expectedCount)

// After (identity-based multiset):
val newBots = update.bots.filter { it.botAddress !in preExistingBots }
val connectedCounts = newBots.groupingBy { BotIdentity(it.name, it.version) }.eachCount()
if (expectedCounts.all { (id, required) -> (connectedCounts[id] ?: 0) >= required })
```

### Configurable Boot Timeout

The hard-coded `BOT_CONNECT_TIMEOUT_MS = 30_000L` is replaced with a configurable timeout on the `BattleRunner`
builder:

```kotlin
BattleRunner.create {
    embeddedServer()
    botConnectTimeout(Duration.ofSeconds(120)) // default: 30s
}
```

This is important for bots with heavy first-time initialization (e.g., Python bots installing dependencies via pip
on first launch). The default remains 30 seconds for backward compatibility, but users with slow-starting bots can
increase it.

### GUI Boot Progress Feedback

The identity-based matching model naturally enables **progress reporting**: the runner (and GUI) knows exactly which
bots it is waiting for and which have already connected. This enables:

- **Battle Runner API:** A progress callback or event that reports which bots have connected and which are still
  pending, allowing programmatic consumers to log or display boot progress.
- **GUI:** A boot progress dialog showing:
  - Which bots are being booted (from the expected identity set)
  - Which have connected (checkmark or status change as each `BotListUpdate` arrives)
  - Which are still pending (spinner/waiting indicator)
  - Elapsed time and timeout remaining

This addresses a real usability problem: when Python bots (or any bot with slow first-time initialization) take a
long time to start, users currently see no feedback and may assume the bots have failed. A progress dialog makes the
wait transparent.

---

## Consequences

### Positive

- ✅ **Fixes team under-count** — Teams are expanded into individual bot identities; the runner waits for all members
- ✅ **Fixes stray-bot over-count** — Only bots matching expected `(name, version)` pairs are considered
- ✅ **Fixes duplicate-instance miscounting** — Multiset matching correctly handles multiple instances of the same bot
- ✅ **Runner-only changes** — No modifications to the server, Bot APIs, WebSocket protocol, or schema
- ✅ **Uses existing data** — Bot name and version are already included in `BotListUpdate` from the server handshake
- ✅ **Backward compatible** — No API surface changes for `BattleRunner` callers (timeout is additive)
- ✅ **Enables progress reporting** — Knowing which specific bots are expected unlocks per-bot status feedback in both
  the runner API and the GUI
- ✅ **Configurable timeout** — Users with slow-starting bots can increase the boot timeout without code changes

### Negative

- ⚠️ **Runner reads `bot.json`/`team.json`** — Introduces coupling between the runner and the bot configuration file
  format. If the config format changes, the runner's parser must be updated. Mitigated: the format is stable and
  already documented.
- ⚠️ **Name+version collision risk** — On an external server, two different bots could theoretically share the same
  `(name, version)` pair, causing a false-positive match. This is unlikely in practice and **impossible** with an
  embedded server (where the runner controls all bot launches).
- ⚠️ **GUI changes required** — The boot progress dialog is a separate GUI enhancement, not part of the runner-only
  change. It requires wiring the identity-based progress model into the GUI's boot flow.

### Residual Risk: Name+Version Collision on External Servers

The name+version collision on external servers is accepted as a known limitation, not addressed by this ADR.

**Why not tackle it now:**

- The primary use case is the **embedded server**, where the runner is the sole bot launcher — collisions are
  impossible by construction.
- External server usage is a niche scenario. A collision requires two independently-developed bots to share the
  exact same `(name, version)` pair *and* be connected to the same server simultaneously.
- The only clean fix is Option 3 (boot correlation token), which requires cross-cutting changes to all three Bot
  APIs, the WebSocket schema, and the server — disproportionate cost for an unlikely scenario.
- Users of external servers are already expected to manage their own bot roster.

**If it becomes a real problem:** Option 3 remains available as a future enhancement. It can be layered on top of
the identity-based matching introduced here without breaking changes.

---

## References

- [GitHub Issue #195](https://github.com/robocode-dev/tank-royale/issues/195) — Count-based waiting in Runner API is
  not reliable
- [ADR-0024: Battle Runner API](./0024-battle-runner-api.md) — The Battle Runner architecture this decision extends
- [ADR-0016: Session ID for Bot Process Identification](./0016-session-id-bot-process-identification.md) — Related bot
  identification mechanism (session-level, not directory-level)
