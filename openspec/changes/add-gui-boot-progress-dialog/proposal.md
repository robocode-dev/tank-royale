# Change: GUI Boot Progress Dialog

## Why

When a user clicks "Start Battle" in the GUI, bots are booted and the GUI waits for them to connect
to the server. Currently there is no feedback during this waiting period — the user sees nothing
until all bots have connected and the game starts. On slow machines or with Python bots that install
dependencies, this wait can exceed 30 seconds with no indication of what is happening. The user
cannot tell whether bots are still booting, have crashed, or are simply slow.

The `add-identity-based-bot-matching` change introduces `BotIdentity`, identity-aware matching, and
the `BootProgress` event in the Battle Runner. This proposal brings that information to the GUI.

## What Changes

- **Boot progress dialog**: A modal dialog displayed between "Start Battle" click and game start,
  showing which bots have connected and which are still pending, with elapsed time and a timeout
  indicator.
- **Identity-aware status**: Each bot is shown by its `(name, version)` identity (from `bot.json`),
  with a visual indicator (e.g., checkmark vs spinner/hourglass) for connected vs pending.
- **Timeout and cancel**: The dialog shows time remaining. A "Cancel" button aborts the boot and
  returns to the bot selection panel. If timeout expires, the dialog shows an error message listing
  pending bots.
- **Auto-dismiss on success**: When all bots connect, the dialog auto-closes and the battle starts.
- **CHANGELOG.md entry**: Document the GUI boot progress dialog in the changelog.

## Impact

- Affected specs: `gui-boot-progress` (new capability)
- Affected code:
  - `gui/src/main/kotlin/.../gui/ui/newbattle/BootProgressDialog.kt` (new)
  - `gui/src/main/kotlin/.../gui/ui/newbattle/NewBattleDialog.kt` (show dialog on start)
  - `gui/src/main/kotlin/.../gui/booter/BootProcess.kt` (read bot.json for identities)
  - `gui/src/main/kotlin/.../gui/client/Client.kt` (pass identity info)
  - `CHANGELOG.md`
- Depends on: `add-identity-based-bot-matching` (for `BotIdentity` type and matching concepts)
- No protocol, schema, server, or Bot API changes required (GUI-only)
