# ADR-0032: Tank Color Display Mode

**Status:** Accepted
**Date:** 2026-04-06

## Context and Problem Statement

In Robocode Tank Royale, tank colors are currently defined by the bot itself via the `BotInfo` sent
during the handshake or updated during the battle. While this allows bot authors to express their
bot's "identity" through color, it can create issues for users watching a battle:

1. **Accessibility:** Some color combinations chosen by bots might have low contrast or be difficult
   to distinguish for users with color vision deficiencies.
2. **Visibility:** In dense battles with many tanks, certain colors might blend into the arena
   background or other tanks, making it hard to follow the action.
3. **Developer debugging:** Bot developers using the Graphical Debugging feature in the Bot Console
   may benefit from seeing their bot's custom colors during a debugging session, while keeping colors
   uniform for the rest of the field.

Currently, the GUI has no mechanism to control how tank colors are displayed.

**Explicitly out of scope:** Allowing end-users to pick specific custom colors for any tank component
is a non-goal. The user must never be exposed to a color picker for tank colors.

## Decision Outcome

The GUI will introduce a **Tank Color Display Mode** — a single global setting that controls how
the rendering engine resolves colors for all tank components (body, turret, radar, tracks, gun,
bullet, scan).

### The Four Modes

| Mode | Enum Value | Rendering Behavior |
|------|-----------|-------------------|
| **Bot Colors** | `BOT_COLORS` | Bot-defined colors are used as supplied, and may change freely throughout the battle. Falls back to system defaults when a bot provides no color. This is the default and preserves existing behavior. |
| **Bot Colors (Once)** | `BOT_COLORS_LOCKED` | The first color a bot provides for each component is recorded and locked for the entire battle, including between rounds. Subsequent color changes from the bot are ignored. Falls back to system defaults until a bot provides an initial color. |
| **Default Colors** | `DEFAULT_COLORS` | All tanks always render with the hardcoded system-default colors from `ColorConstant`. Bot-defined colors are completely ignored. |
| **Bot Colors (Debug Only)** | `BOT_COLORS_WHEN_DEBUGGING` | A bot's colors are shown only while its Graphical Debugging flag is enabled in the Bot Console. When debugging is off, that bot renders with system-default colors. |

### Key Rules

1. **Single global setting:** One mode applies to all tanks in the arena simultaneously.
2. **End-user display preference, client-side only:** This is a local display preference set by
   the GUI user. The end-user decides how colors appear on *their* screen. The server, the Bot API,
   bots, and any other connected observer are completely unaware of this setting and always work
   with the real, unmodified bot colors. The wire protocol is never altered.
   - **The recorder** captures the raw `BotState` data received from the server, which always
     contains the bot's true colors. A recording therefore preserves the real colors regardless of
     the display mode active at the time of recording. When the recording is replayed — by the
     original user or shared with someone else — the recipient sees the real bot colors and applies
     their own display mode preference independently.
3. **Persistence:** The mode is stored in the GUI's local configuration (`ConfigSettings`) and
   persists across sessions.
4. **Default:** `BOT_COLORS` is the default, so existing behavior is preserved for all current
   users without migration.
5. **No color picker:** The end-user does not select, choose, or input any color value. The four
   modes are the only configuration surface.

## Pros and Cons of the Options

### Option A — Bot Colors (default)
* **Good:** Zero behaviour change for existing users.
* **Good:** Bot authors' intended visual identity is respected, including dynamic color changes.
* **Neutral:** Offers no mitigation for accessibility or visibility concerns.

### Option B — Bot Colors (Once)
* **Good:** Bot identity is preserved through the battle without flickering or rapid changes.
* **Good:** Addresses the distraction concern from Issue #201 while still honouring the bot's intended color.
* **Trade-off:** Requires the GUI to maintain a per-bot, per-component "first color" cache across rounds.

### Option C — Default Colors
* **Good:** Guaranteed uniform appearance; no color-clash or distraction issues.
* **Good:** Simple implementation — ignore all bot color fields during rendering.
* **Trade-off:** Bot visual identity is lost entirely.

### Option D — Bot Colors (Debug Only)
* **Good:** Developers can visually identify their bot by its color during a Graphical Debugging
  session without affecting other observers' default display.
* **Good:** Leverages the existing `isDebuggingEnabled` flag on `BotState` — no protocol change.
* **Trade-off:** Requires understanding the relationship between Bot Console and arena rendering.

## Rationale

This decision balances the **observer's experience** and **accessibility** against the bot author's
right to express a visual identity, without introducing the complexity and user-confusion risk of a
free-form color picker. The four discrete modes cover the full range of practical use cases
identified in Issue #201.

The `BOT_COLORS_LOCKED` mode directly addresses the core concern in Issue #201 (flickering bots)
while still respecting the bot's intended color.

The `BOT_COLORS_WHEN_DEBUGGING` mode is a natural extension of the existing Graphical Debugging
workflow: a developer opens the Bot Console, enables debugging for their bot, and can now also see
the bot's intended colors simultaneously — the same workflow, zero new concepts.

## Consequences

* A `TankColorMode` enum is introduced in the `gui/settings` package with four values.
* `ConfigSettings` gains a single new property `tankColorMode`.
* `Tank.kt` introduces a per-bot first-color cache (used by `BOT_COLORS_LOCKED`) and a
  mode-aware color resolution helper replacing all raw `bot.xColor ?: DEFAULT_X_COLOR` expressions.
* `ArenaPanel.kt` receives the same treatment for bullet and scan colors.
* The first-color cache is reset when a new battle starts (not between rounds).
* `GuiConfigDialog` gains a radio-button group for the four modes.
* String resources are updated for all four supported locales.
* No new dependencies. No schema changes.

## References

* [Issue #201: UI settings to define the rules for changing the colors of the tanks](https://github.com/robocode-dev/tank-royale/issues/201)
* [ADR-0018: Custom SVG Rendering](0018-custom-svg-rendering.md)
* [Tank Color Settings Proposal](/openspec/changes/tank-color-settings/proposal.md)
