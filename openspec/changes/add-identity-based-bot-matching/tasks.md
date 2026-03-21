## 1. BotIdentity data class and bot.json parsing

- [x] 1.1 Create `BotIdentity` data class in `runner/src/main/kotlin/.../runner/BotIdentity.kt`
  with `name: String` and `version: String` properties. Override `toString()` for readable
  progress messages (e.g., `"My First Bot v1.0"`).

- [x] 1.2 Add `readBotIdentities(botDir: Path): List<BotIdentity>` to `BooterManager.Companion`.
  For a regular bot directory: parse `<dir>/<dir>.json`, extract `name` and `version` fields,
  return a single-element list. For a team directory (JSON contains `teamMembers` array): resolve
  each member name to a sibling directory `<parent>/<member>/<member>.json`, parse each, and
  return one `BotIdentity` per team member (preserving duplicates for the multiset).
  Throw `BattleException` with a clear message if any JSON file is missing or malformed.

- [x] 1.3 Write unit tests for `readBotIdentities()`:
  - Regular bot directory → returns `[BotIdentity("My First Bot", "1.0")]`
  - Team directory with 5 members (including duplicates) → returns 5 identities
  - Missing bot.json → throws `BattleException`
  - Malformed JSON (missing `name` field) → throws `BattleException`
  - Team with missing member directory → throws `BattleException`
  Use temp directories with crafted JSON files (no real sample bots needed).

## 2. Identity multiset matching logic

- [x] 2.1 Create `BotMatcher` internal class in `runner/src/main/kotlin/.../runner/internal/BotMatcher.kt`.
  Constructor takes `expectedIdentities: List<BotIdentity>` and `preExistingBots: Set<BotAddress>`.
  Internally builds `expectedMultiset: Map<BotIdentity, Int>` by counting occurrences.
  Expose:
  - `fun update(bots: Set<BotInfo>): MatchResult` — filters out pre-existing bots, builds a
    connected multiset from `BotInfo.name`/`BotInfo.version`, compares against expected.
  - `data class MatchResult(val matched: Set<BotAddress>, val isComplete: Boolean,
    val connected: Map<BotIdentity, Int>, val pending: Map<BotIdentity, Int>)`
  When `isComplete`, `matched` contains exactly one `BotAddress` per expected identity slot.
  If more bots than expected connect for an identity, take only the needed count (first seen).

- [x] 2.2 Write unit tests for `BotMatcher`:
  - 2 distinct bots, both connect → `isComplete = true`, `matched` has 2 addresses
  - Same bot directory twice (duplicate identity) → needs 2 connections with same name+version
  - Team with 4 droids (same identity) → needs 4 connections
  - Stray bot with different identity → filtered out, does not count toward match
  - Pre-existing bots → excluded from matching
  - Partial connection (1 of 2) → `isComplete = false`, `pending` shows missing identity
  - Extra bots beyond expected count → only expected count taken, `isComplete = true`

## 3. Configurable boot timeout

- [x] 3.1 Add `botConnectTimeoutMs: Long` field to `BattleRunner.Config` (default: `30_000L`).
  Add `botConnectTimeout(timeout: java.time.Duration): Builder` method to `BattleRunner.Builder`
  that stores `timeout.toMillis()`. Add `@JvmOverloads` no-arg overload is not needed (Duration
  is required). Annotate with KDoc explaining the default and purpose.

- [x] 3.2 Replace `BOT_CONNECT_TIMEOUT_MS` constant usage in `waitForBots()` with
  `config.botConnectTimeoutMs`. Remove the constant.

- [x] 3.3 Write unit tests for timeout configuration:
  - Default config has `botConnectTimeoutMs = 30_000`
  - `botConnectTimeout(Duration.ofSeconds(120))` → config has `120_000`
  - `botConnectTimeout(Duration.ofMillis(500))` → config has `500`
  - Builder without `botConnectTimeout()` → default preserved

## 4. Integrate identity matching into BattleRunner.waitForBots()

- [ ] 4.1 In `BattleRunner.startBattleAsync()`, after `validateBotDir()` and before `boot()`:
  call `BooterManager.readBotIdentities()` for each `BotEntry` and flatten into a single
  `List<BotIdentity>`. Pass this list to `waitForBots()`.

- [ ] 4.2 Refactor `waitForBots()` signature from
  `(conn, preExistingBots: Set<BotAddress>, expectedCount: Int): Set<BotAddress>` to
  `(conn, preExistingBots: Set<BotAddress>, expectedIdentities: List<BotIdentity>): Set<BotAddress>`.
  Internally create a `BotMatcher` and use `matcher.update(update.bots)` on each
  `BotListUpdate` event. Count down the latch when `result.isComplete`. Return
  `result.matched`.

- [ ] 4.3 Update the timeout error message to include identity-aware detail:
  `"Bot connect timeout (${timeoutMs}ms): connected N of M. Pending: [BotName v1.0 (×2)]"`.

- [ ] 4.4 Write integration-level tests for the refactored `waitForBots()`:
  - Mock `ServerConnection.onBotListUpdate` to emit `BotListUpdate` with matching identities
    → returns correct `BotAddress` set
  - Emit partial updates → only completes when all identities matched
  - Timeout scenario → throws `BattleException` with identity-aware message
  - Pre-existing bots filtered → not included in result

## 5. Boot progress reporting

- [ ] 5.1 Create `BootProgress` data class in `runner/src/main/kotlin/.../runner/BootProgress.kt`:
  ```
  data class BootProgress(
      val expected: Map<BotIdentity, Int>,
      val connected: Map<BotIdentity, Int>,
      val pending: Map<BotIdentity, Int>,
      val elapsedMs: Long,
      val timeoutMs: Long,
  )
  ```
  Include a `val totalExpected: Int` and `val totalConnected: Int` computed property.

- [ ] 5.2 Add `val onBootProgress: Event<BootProgress>` to `BattleHandle`. Fire this event
  from `waitForBots()` on every `BotListUpdate` and periodically (every 500ms via the latch
  await loop). Pass the `BotMatcher` result plus timing info.

- [ ] 5.3 Write unit tests for `BootProgress`:
  - Verify `totalExpected` and `totalConnected` computed correctly
  - Verify `pending` = expected minus connected (clamped to 0)
  - Verify progress event fires on each `BotListUpdate`

## 6. Team expansion in BotEntry validation

- [ ] 6.1 Update `BooterManager.validateBotDir()` to also validate team member directories
  when the config file contains `teamMembers`. For each member, verify that
  `<parent>/<member>/<member>.json` exists. Throw `BattleException` with a message like
  `"Team member directory not found: <member> (expected at <path>)"`.

- [ ] 6.2 Write unit tests for team validation:
  - Valid team with all member directories present → no exception
  - Team with missing member directory → throws `BattleException` naming the missing member
  - Non-team bot (no `teamMembers`) → existing validation unchanged

## 7. Update documentation

- [ ] 7.1 Update `runner/README.md` to document identity-based bot matching, configurable boot
  timeout (`botConnectTimeout(Duration)`), boot progress reporting (`onBootProgress`), and team
  member directory validation.

- [ ] 7.2 Update `runner/examples/README.md` and add/update example code in `runner/examples/`
  to demonstrate the new features (boot timeout configuration, progress event handling, team
  bot entries).

- [ ] 7.3 Update `docs/api/battle-runner.html` to document the new public API surface:
  `BotIdentity`, `BootProgress`, `botConnectTimeout()` builder method, and `onBootProgress`
  event on `BattleHandle`.

## 8. Update CHANGELOG.md

- [ ] 8.1 Add a `[0.38.0]` version entry at the top of `CHANGELOG.md` (replacing the current
  `[0.37.1]` entry which gets rolled into this release). Document under "Runner API":
  - Identity-based bot matching (fixes teams, stray bots, duplicate instances)
  - Configurable boot timeout via `botConnectTimeout(Duration)`
  - Boot progress reporting via `onBootProgress` event on `BattleHandle`
  - Team member directory validation at battle-start time
  - Include the previous `[0.37.1]` content (`suppressServerOutput()`, orphaned stdout fix)
  Follow the existing changelog format (emoji headers, bullet style, grouped under Runner API).
  Leave space for the GUI boot progress dialog entry from the sibling proposal.
