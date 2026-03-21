# Change: Identity-Based Bot Matching in Battle Runner

## Why

The Battle Runner's count-based bot detection has three failure modes: (1) teams produce multiple
bot connections but the runner expects one per directory, (2) stray/leftover bot processes on an
external server can satisfy the count check incorrectly, and (3) running the same bot directory
twice produces identical `(name, version)` pairs that are miscounted. Additionally, the hard-coded
30-second boot timeout is not configurable, which causes failures on slow machines (e.g., Python
bots installing dependencies). See ADR-0026 for full analysis.

## What Changes

- **Identity multiset matching**: Runner pre-reads `bot.json` (and `team.json` for teams) from bot
  directories, builds an expected multiset of `(name, version)` identities, and matches incoming
  `BotListUpdate` entries by identity counts instead of flat totals.
- **Team expansion**: When a directory contains a `team.json`, the runner reads `teamMembers` and
  resolves each member's `bot.json` to expand the expected identity multiset correctly.
- **Pre-existing bot filtering**: On external servers, pre-existing bots are subtracted from the
  identity multiset so only freshly booted bots are matched.
- **Configurable boot timeout**: New `botConnectTimeout(Duration)` builder option replaces the
  hard-coded `BOT_CONNECT_TIMEOUT_MS = 30_000L`.
- **Boot progress callback**: New `onBootProgress` event on `BattleHandle` (and optionally on the
  builder) reports which bots have connected vs. pending, elapsed time, and timeout remaining.
- **CHANGELOG.md entry**: Document all changes in the changelog.

## Impact

- Affected specs: `battle-runner`
- Affected code:
  - `runner/src/main/kotlin/.../runner/BattleRunner.kt` (matching logic, builder, timeout)
  - `runner/src/main/kotlin/.../runner/internal/BooterManager.kt` (bot.json/team.json reading)
  - `runner/src/main/kotlin/.../runner/BattleHandle.kt` (progress event)
  - `runner/src/main/kotlin/.../runner/BotEntry.kt` (identity data carrier)
  - `runner/src/test/kotlin/...` (new and updated tests)
  - `CHANGELOG.md`
- No protocol, schema, server, or Bot API changes required (runner-only)
