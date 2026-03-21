## Context

ADR-0026 selects Option 4: Identity-Based Multiset Matching (Runner-Only). The runner pre-reads
bot configuration files from disk, builds an expected multiset of `(name, version)` identities, and
matches incoming `BotListUpdate` messages by identity counts rather than flat totals.

This is a cross-cutting change within the `runner` module: it touches the builder (timeout config),
the matching logic (core algorithm), bot directory handling (JSON parsing), the async handle
(progress events), and the public `BotEntry` class (identity exposure).

## Goals / Non-Goals

- Goals:
  - Fix all three count-based detection failure modes (teams, stray bots, duplicates)
  - Make boot timeout configurable
  - Report boot progress with identity information
  - Maintain backward compatibility (no API breaks, no protocol changes)

- Non-Goals:
  - Boot correlation tokens (requires protocol changes — rejected in ADR-0026)
  - GUI boot progress dialog (separate future work; this change enables it)
  - External-server bot identity verification (accepted limitation per ADR-0026)

## Decisions

### Decision 1: BotIdentity value class

Introduce a `BotIdentity(name: String, version: String)` data class in the runner module. This is
the key type for the identity multiset. It maps to `BotInfo.name` / `BotInfo.version` from
`BotListUpdate` messages.

- Alternatives: reuse `BotInfo` (too heavy, includes host/port), use a `Pair<String, String>`
  (no semantic meaning, poor API surface).

### Decision 2: Pre-scan in BotEntry or BooterManager

Add a `readBotIdentities(path: Path): List<BotIdentity>` utility to `BooterManager.Companion`.
For a regular bot directory, it reads `<dir>/<dir>.json` and returns a single identity. For a team
directory (detected by `teamMembers` key), it resolves each member subdirectory's `bot.json` and
returns one identity per member.

The scan happens in `BattleRunner.startBattleAsync()` after `validateBotDir()` and before
`boot()`. This keeps `BotEntry` as a simple path wrapper (no file I/O in data class).

- Alternatives: scan in `BotEntry.init` (side effects in data class constructor),
  scan lazily on first `waitForBots()` (delays error reporting).

### Decision 3: Multiset matching algorithm

Build `Map<BotIdentity, Int>` from the pre-scan. On each `BotListUpdate`, build a matching multiset
from the update's `BotInfo` entries (excluding pre-existing bots). The bots are "ready" when every
identity in the expected multiset has a count ≥ the expected count.

When matched, collect the `BotAddress` for each matched `BotInfo` entry. If more bots connect than
expected for an identity (e.g., stray bots), only take the expected count.

### Decision 4: Configurable timeout via Builder

Add `botConnectTimeout(timeout: Duration)` to `BattleRunner.Builder`. Store as
`botConnectTimeoutMs: Long` in `Config` (default: 30_000L, preserving current behavior).
Remove `BOT_CONNECT_TIMEOUT_MS` constant and use `config.botConnectTimeoutMs` in `waitForBots()`.

### Decision 5: Boot progress event

Add a `BootProgress` data class: `(expected: Map<BotIdentity, Int>, connected: Map<BotIdentity, Int>,
elapsedMs: Long, timeoutMs: Long)`. Fire `onBootProgress` on a periodic check within the
`waitForBots()` loop (e.g., every 500ms) and on each `BotListUpdate`. Expose on `BattleHandle`.

## Risks / Trade-offs

- **bot.json format coupling**: Runner now depends on the `name` and `version` fields in bot.json.
  This is stable and unlikely to change. If it does, runner parsing and Bot API handshake both need
  updating anyway. → Low risk.

- **Name+version collision on external servers**: Two different bots with the same name+version
  could confuse matching. → Accepted limitation per ADR-0026; documented in API Javadoc.

- **Team member directory resolution**: The runner assumes team members are sibling directories
  of the team directory (same parent). This matches the current Booter convention. → Document
  assumption; fail with a clear error if a member directory is missing.

## Open Questions

None — all decisions captured in ADR-0026.
