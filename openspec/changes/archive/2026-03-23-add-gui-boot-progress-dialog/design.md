## Context

The GUI currently has a 4-panel bot selection flow (Bot Directories → Running Bots → Joined Bots →
Selected Bots) in `BotSelectionPanel`. When the user clicks "Start Battle", `NewBattleDialog`
immediately calls `Client.startGame(botAddresses)` and disposes the dialog. There is no intermediate
feedback while bots boot and connect.

This change adds a modal progress dialog between the "Start Battle" click and the game start. It
reuses `BotIdentity` concepts from `add-identity-based-bot-matching` but operates at the GUI layer,
reading bot.json files directly (the GUI already has booter access and bot directory paths).

## Goals / Non-Goals

- Goals:
  - Show real-time boot progress with bot identity information
  - Allow cancellation during the boot wait
  - Show clear error when timeout expires
  - Auto-dismiss and start game when all bots connect

- Non-Goals:
  - Reusing the `BattleRunner` module in the GUI (GUI uses its own `BootProcess` + `Client`)
  - Changing the 4-panel bot selection flow
  - Adding boot progress to the programmatic runner (already handled by sibling proposal)

## Decisions

### Decision 1: Modal dialog approach

Use a Swing `JDialog` (modal to `NewBattleDialog`) with a table/list showing each expected bot
identity and its connection status. This blocks interaction with the battle dialog while booting.

- Alternatives: non-modal panel embedded in `BotSelectionPanel` (clutters the already complex
  4-panel layout), status bar text (too subtle for a potentially long wait).

### Decision 2: Bot identity reading in GUI

Add a utility function (or reuse from a shared module) to read `bot.json` and `team.json` from bot
directories to extract `(name, version)` identities. The GUI already validates bot directories via
`BootProcess.info()` which calls the booter JAR — this new read is lightweight and can happen
in-process without the booter.

If `BotIdentity` is placed in a shared library (e.g., `lib/common`), the GUI can depend on it
directly. Otherwise, the GUI can define its own lightweight data class.

### Decision 3: Matching connected bots to identities

The GUI already receives `BotListUpdate` events via `ClientEvents.onBotListUpdate`. The dialog
subscribes to this event, builds a connected multiset from `BotInfo.name`/`BotInfo.version`, and
compares against the expected multiset (same algorithm as the runner's `BotMatcher`, but
independent implementation or shared via library).

### Decision 4: Timeout handling

The dialog uses the same default 30-second timeout. A `javax.swing.Timer` ticks every 500ms to
update the elapsed time display and check for timeout. On timeout, the dialog shows an error panel
listing pending bots and offers "Retry" and "Cancel" buttons.

### Decision 5: Cancel behavior

"Cancel" button disposes the progress dialog, calls `BootProcess.stop()` to kill booted processes,
and returns the user to the `NewBattleDialog` with their bot selection intact.

## Risks / Trade-offs

- **Duplicate matching logic**: The GUI implements its own identity matching rather than depending
  on `runner` module. This is intentional — the GUI has its own connection model (`Client` /
  `LiveBattlePlayer`) that doesn't use `BattleRunner`. → If `BotIdentity` and `BotMatcher` are
  placed in `lib/common`, both modules can share them.

- **Modal dialog blocks UI**: If the dialog hangs (e.g., booter crashes silently), the user must
  click Cancel. → The 500ms timer ensures the dialog always shows elapsed time, and timeout
  auto-triggers error display.

## Open Questions

- Should `BotIdentity` and the matching logic live in `lib/common` so both runner and GUI share
  them? This is a packaging decision that can be deferred to implementation.
