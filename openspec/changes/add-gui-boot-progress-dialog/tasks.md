## 1. Bot identity reading in GUI

- [x] 1.1 Add a utility to read `(name, version)` from a bot directory's JSON config file. For team
  directories (containing `teamMembers`), expand to member identities by reading each member's
  `bot.json` from sibling directories. This may reuse `BotIdentity` from `lib/common` (if the
  sibling proposal places it there) or define a local equivalent.
  Location: `gui/src/main/kotlin/.../gui/booter/BotIdentityReader.kt` (or shared module).

- [x] 1.2 Write unit tests for the identity reader:
  - Regular bot directory → returns single `(name, version)`
  - Team directory with duplicates → returns correct multiset
  - Missing or malformed JSON → throws with descriptive message
  - Use temp directories with crafted JSON files.

## 2. BootProgressDialog UI component

- [x] 2.1 Create `BootProgressDialog` as a modal `JDialog`.
  Layout:
  - Title: "Waiting for bots to connect..."
  - Center: A list/table showing each expected bot identity with a status icon
    (pending=hourglass/spinner, connected=checkmark). For duplicate identities (e.g., 4 droids),
    show "MyFirstDroid v1.0 (2/4 connected)".
  - Bottom-left: Elapsed time label, e.g., "Elapsed: 12s / 30s"
  - Bottom-right: "Cancel" button

- [x] 2.2 Add a `javax.swing.Timer` (500ms interval) that updates the elapsed time label and
  checks whether the timeout has been reached. On timeout, replace the status area with an error
  message listing pending bots and show "Retry" / "Cancel" buttons.
  Note: timer fires every 500ms; elapsed seconds are computed as `elapsedHalfSeconds / 2` to
  keep the displayed time accurate.

- [x] 2.3 Subscribe to `ClientEvents.onBotListUpdate` when the dialog opens. On each update,
  match all currently connected bots against the expected identity multiset, update each row's
  status icon, and check for completion. When all identities matched, auto-close the dialog.

- [x] 2.4 Write unit tests for the matching logic (`BotMatcher`):
  - All bots connected → `isComplete = true`
  - Partial connection → shows correct counts per identity
  - Stray bot with different identity → ignored

## 3. Integrate dialog into BotSelectionPanel boot flow

- [x] 3.1 The boot progress dialog is shown when bots are booted from `BotSelectionPanel`, not
  from `startGame()`. Selected bots in `NewBattleDialog` always come from the "Joined Bots" panel
  (already connected), so the dialog would close instantly if shown from `startGame()`. Instead:
  - `handleBootBots()` now calls `bootAndShowProgress(selected)` instead of `BootProcess.boot()`
    directly. Dialog is shown while the booted bots connect.
  - `runFromBotDirectoryAtIndex()` (double-click) similarly delegates to `bootAndShowProgress()`.
  - `bootAndShowProgress()` reads identities, boots, then shows `BootProgressDialog`.
  - `NewBattleDialog.startGame()` is restored to the original direct call: `Client.startGame()` →
    `NewBattleDialog.dispose()`.

- [x] 3.2 On dialog success (all bots connected): dialog auto-closes; bots now appear in the
  "Joined Bots" panel via the existing `updateJoinedBots()` / `ClientEvents.onBotListUpdate` flow.

- [x] 3.3 On dialog cancel: dispose only the progress dialog and call `BootProcess.stop()` to
  kill all booted bot processes. Bot selection in `NewBattleDialog` is unaffected.

- [x] 3.4 On dialog timeout: show error listing pending bots. "Retry" resets the timer and
  continues waiting. "Cancel" behaves as above.

- [ ] 3.5 Manual test plan (human task):
  - [x] 3.5.1 Boot 2 distinct bots → dialog shows both, auto-closes when connected, bots appear in joined list
  - [x] 3.5.2 Boot a team → dialog shows expanded member identities with correct counts
  - [x] 3.5.3 Cancel during boot → bots killed
  - [x] 3.5.4 Slow bot (add artificial delay) → elapsed timer updates, timeout triggers error
  - [ ] 3.5.5 External server with stray bots → stray bots ignored in progress display

## 4. Update documentation (human task)

- [ ] 4.1 Capture screenshots of the boot progress dialog (connecting, partial progress,
  timeout error, cancel flow) and add to GUI documentation.

- [ ] 4.2 Update GUI documentation with screenshots showing the new dialog states and
  user-facing behavior.

## 5. Update CHANGELOG.md

- [x] 5.1 Add GUI section to the v0.38.0 changelog entry documenting:
  - Boot progress dialog showing per-bot identity status during battle start
  - Cancel button to abort boot and return to bot selection
  - Timeout error with list of pending bots
