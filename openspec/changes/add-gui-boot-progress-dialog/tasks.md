## 1. Bot identity reading in GUI

- [ ] 1.1 Add a utility to read `(name, version)` from a bot directory's JSON config file. For team
  directories (containing `teamMembers`), expand to member identities by reading each member's
  `bot.json` from sibling directories. This may reuse `BotIdentity` from `lib/common` (if the
  sibling proposal places it there) or define a local equivalent.
  Location: `gui/src/main/kotlin/.../gui/booter/BotIdentityReader.kt` (or shared module).

- [ ] 1.2 Write unit tests for the identity reader:
  - Regular bot directory → returns single `(name, version)`
  - Team directory with duplicates → returns correct multiset
  - Missing or malformed JSON → throws with descriptive message
  - Use temp directories with crafted JSON files.

## 2. BootProgressDialog UI component

- [ ] 2.1 Create `BootProgressDialog` as a modal `JDialog` owned by `NewBattleDialog`.
  Layout:
  - Title: "Waiting for bots to connect..."
  - Center: A list/table showing each expected bot identity with a status icon
    (pending=hourglass/spinner, connected=checkmark). For duplicate identities (e.g., 4 droids),
    show "MyFirstDroid v1.0 (2/4 connected)".
  - Bottom-left: Elapsed time label, e.g., "Elapsed: 12s / 30s"
  - Bottom-right: "Cancel" button

- [ ] 2.2 Add a `javax.swing.Timer` (500ms interval) that updates the elapsed time label and
  checks whether the timeout has been reached. On timeout, replace the status area with an error
  message listing pending bots and show "Retry" / "Cancel" buttons.

- [ ] 2.3 Subscribe to `ClientEvents.onBotListUpdate` when the dialog opens. On each update,
  rebuild the connected multiset from `BotInfo.name`/`BotInfo.version`, update each row's
  status icon, and check for completion. When all identities matched, auto-close the dialog
  and proceed to start the game.

- [ ] 2.4 Write unit tests for the matching logic within the dialog (or delegate to a shared
  `BotMatcher`):
  - All bots connected → `isComplete = true`
  - Partial connection → shows correct counts per identity
  - Stray bot with different identity → ignored

## 3. Integrate dialog into NewBattleDialog

- [ ] 3.1 Modify `NewBattleDialog.startGame()`: instead of immediately calling
  `Client.startGame(botAddresses)`, first read bot identities from the selected bot directories,
  then show `BootProgressDialog`. The dialog receives the expected identity multiset and the
  set of pre-existing bot addresses.

- [ ] 3.2 On dialog success (all bots connected): call `Client.startGame(matchedBotAddresses)`
  and dispose `NewBattleDialog` as before.

- [ ] 3.3 On dialog cancel: dispose only the progress dialog, return to `NewBattleDialog` with
  bot selection intact, and kill booted bot processes via `BootProcess.stop()`.

- [ ] 3.4 On dialog timeout: show error listing pending bots. "Retry" resets the timer and
  continues waiting. "Cancel" behaves as above.

- [ ] 3.5 Manual test plan:
  - Boot 2 distinct bots → dialog shows both, auto-closes when connected, battle starts
  - Boot a team → dialog shows expanded member identities with correct counts
  - Cancel during boot → returns to selection, bots killed
  - Slow bot (add artificial delay) → elapsed timer updates, timeout triggers error
  - External server with stray bots → stray bots ignored in progress display

## 4. Update CHANGELOG.md

- [ ] 4.1 Add GUI section to the v0.38.0 changelog entry documenting:
  - Boot progress dialog showing per-bot identity status during battle start
  - Cancel button to abort boot and return to bot selection
  - Timeout error with list of pending bots
