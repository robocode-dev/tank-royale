## Context

Currently, tank colors in Robocode Tank Royale are exclusively defined by the bot itself. While this
allows for unique bot identities, it can lead to accessibility and visibility issues. ADR-0032
establishes the architectural decision to introduce a **Tank Color Display Mode** — a single global
GUI setting that governs how the rendering engine resolves colors for all tank components.

**The end-user must never be presented with a color picker or any mechanism to choose specific
colors.** The three modes below are the only configuration surface.

## Goals / Non-Goals

**Goals:**
- Introduce a `TankColorMode` enum with values `BOT_COLORS`, `BOT_COLORS_LOCKED`,
  `DEFAULT_COLORS`, and `BOT_COLORS_WHEN_DEBUGGING`.
- Add a radio-button group to the existing `GuiConfigDialog` for mode selection.
- Persist the selected mode in `ConfigSettings` (key: `tank-color-mode`, default: `BOT_COLORS`).
- Update `Tank.kt` and `ArenaPanel.kt` to resolve colors through a mode-aware helper instead of
  inline `bot.xColor ?: DEFAULT_X_COLOR` expressions.
- For `BOT_COLORS_LOCKED`: maintain a per-bot, per-component first-color cache in the rendering
  layer. The cache is reset when a new battle starts, but persists between rounds.

**Non-Goals:**
- Color pickers or user-defined color values of any kind.
- Per-bot or per-component mode overrides.
- Server, protocol, or Bot API changes.

## Decisions

### 1. `TankColorMode` Enum

A new Kotlin enum `TankColorMode` in `gui/settings/`:

```
BOT_COLORS                  // bot-defined colors, change freely (default, current behaviour)
BOT_COLORS_LOCKED           // first color per component locks for the entire battle
DEFAULT_COLORS              // always use ColorConstant defaults
BOT_COLORS_WHEN_DEBUGGING   // bot colors only when isDebuggingEnabled == true
```

**Rationale:** A typed enum prevents invalid config values and makes exhaustive `when` expressions
possible in rendering code.

### 2. `ConfigSettings` — Single New Property

```
private const val TANK_COLOR_MODE = "tank-color-mode"

var tankColorMode: TankColorMode
    get() = TankColorMode.fromString(load(TANK_COLOR_MODE, TankColorMode.BOT_COLORS.name))
    set(value) { save(TANK_COLOR_MODE, value.name) }
```

**Rationale:** Consistent with the existing pattern for all other `ConfigSettings` properties.

### 3. GUI — Radio Buttons in `GuiConfigDialog`

Add a labelled radio-button group beneath the existing options in `GuiConfigPanel`. Four
`JRadioButton` items (`Bot Colors`, `Bot Colors (Once)`, `Default Colors`,
`Bot Colors (Debug Only)`) grouped with a `ButtonGroup`. On OK the selected mode is written to
`ConfigSettings.tankColorMode`.

**Rationale:** Radio buttons are the natural control for a single-choice enum setting. Adding them
to the existing dialog avoids introducing a new tab or dialog, keeping the UI footprint small.

### 4. Color Resolution in `Tank.kt` and `ArenaPanel.kt`

Replace every inline `bot.xColor ?: DEFAULT_X_COLOR` expression with calls to a private
`resolveColor(botColor: String?, default: String): Color` helper:

```kotlin
private fun resolveColor(botColor: String?, default: String): Color {
    val useBot = when (ConfigSettings.tankColorMode) {
        TankColorMode.BOT_COLORS -> true
        TankColorMode.BOT_COLORS_LOCKED -> true   // actual locking handled by firstColorCache
        TankColorMode.DEFAULT_COLORS -> false
        TankColorMode.BOT_COLORS_WHEN_DEBUGGING -> bot.isDebuggingEnabled
    }
    return fromString(if (useBot) botColor ?: default else default)
}
```

For `BOT_COLORS_LOCKED`, `Tank` maintains a `firstColorCache: MutableMap<String, String>` — keyed
by component name — populated on the first non-null color received. The cache is cleared via a
`reset()` call when a new battle starts. `resolveColor` reads from the cache in this mode rather
than from the live `botColor`.

The same logic applies to `ArenaPanel.kt` for bullet and scan colors.

**Rationale:** Centralises mode logic in one place per class; all paint calls remain unchanged.

## Risks / Trade-offs

- **[Negligible] Performance** → `ConfigSettings.tankColorMode` is read every frame during rendering.
  Since it is a simple properties lookup backed by an in-memory store, there is no measurable
  overhead.
- **[Low] Rendering lag** → Mode changes take effect immediately on the next repaint with no
  explicit invalidation needed.

## Migration Plan

No migration needed. `BOT_COLORS` is the default and preserves existing behaviour for all current
users. The new key is simply absent from existing `gui.properties` files and will be written on
first save.
