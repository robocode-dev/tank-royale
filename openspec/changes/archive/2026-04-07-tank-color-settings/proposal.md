## Why

Currently, tanks can change their colors (body, turret, radar, etc.) through the Bot API, but there
is no way for a user to control how those colors are displayed in the GUI. Providing a simple,
mode-based setting to govern tank color rendering improves the user experience and allows for better
visual distinction between tanks in a battle, especially when multiple tanks have similar or
low-contrast colors.

## What Changes

- Add a **Tank Color Mode** selector to the GUI configuration dialog.
- The user picks **one of four modes** that applies globally to all tanks:
    - **Bot Colors** — bots freely set and change their own colors at any time (current behavior, default).
    - **Bot Colors (Once)** — the first color a bot sets for each component is locked for the entire
      battle, including between rounds. Subsequent changes from the bot are ignored.
    - **Default Colors** — all tanks always render with the hardcoded system-default colors;
      bot-defined colors are ignored entirely.
    - **Bot Colors (Debug Only)** — a bot's colors are displayed only while its Graphical Debugging
      flag is enabled in the Bot Console; otherwise the system defaults are used.
- The selected mode is saved in `gui.properties` and persists across sessions.

**Non-goals (explicitly out of scope):**
- End-users picking or inputting specific colors for any tank component — this is a NO-GO.
- Per-bot color rules.
- Per-component (body vs. turret vs. radar) separate modes.
- Any changes to the server, protocol, or Bot API.

## Capabilities

### New Capabilities
- `tank-color-settings`: Select and persist a global Tank Color Mode (Bot Colors / Default Colors /
  Bot Colors (Debug Only)) through the GUI.

### Modified Capabilities
- None. No protocol, server, or Bot API changes.

## Impact

- `gui`: New radio-button group in the GUI configuration dialog for mode selection.
- `gui`: Update tank rendering logic in `Tank.kt` and `ArenaPanel.kt` to resolve colors based on the
  active mode.
- `gui`: Configuration persistence via `ConfigSettings` / `PropertiesStore`.

## References

- [Issue #201: UI settings to define the rules for changing the colors of the tanks](https://github.com/robocode-dev/tank-royale/issues/201)
- [ADR-0032: Tank Color Display Mode](/docs-internal/architecture/adr/0032-user-defined-visual-overrides-for-tanks.md)
