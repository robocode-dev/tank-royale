# Tasks: Tank Color Settings

## Phase 1 — Model & Config

- [ ] Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/settings/TankColorMode.kt`
  - Kotlin enum with values `BOT_COLORS`, `DEFAULT_COLORS`, `BOT_COLORS_WHEN_DEBUGGING`
  - Include a `companion object` with a `fromString(value: String): TankColorMode` helper that
    falls back to `BOT_COLORS` for unknown values
- [ ] Update `gui/src/main/kotlin/dev/robocode/tankroyale/gui/settings/ConfigSettings.kt`
  - Add private constant `TANK_COLOR_MODE = "tank-color-mode"`
  - Add `var tankColorMode: TankColorMode` property (get/set via `load`/`save`)
  - Default value: `TankColorMode.BOT_COLORS`

## Phase 2 — GUI

- [ ] Update `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/config/GuiConfigDialog.kt`
  - Add a `ButtonGroup` with three `JRadioButton` items to `GuiConfigPanel`:
    `Bot Colors`, `Default Colors`, `Bot Colors (Debug Only)`
  - Initialise selection from `ConfigSettings.tankColorMode` in `setInitialSelections()`
  - Save selection to `ConfigSettings.tankColorMode` in `onOkClicked()`
- [ ] Update string resources
  - Add keys to `Strings.properties`, `Strings_es.properties`, `Strings_da.properties`,
    `Strings_ca.properties`:
    - `option.gui.tank_color_mode` — section label
    - `option.gui.tank_color_mode.bot_colors` — "Bot Colors"
    - `option.gui.tank_color_mode.default_colors` — "Default Colors"
    - `option.gui.tank_color_mode.bot_colors_when_debugging` — "Bot Colors (Debug Only)"

## Phase 3 — Rendering

- [ ] Update `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/arena/Tank.kt`
  - Add a private `resolveColor(botColor: String?, default: String): Color` helper that
    reads `ConfigSettings.tankColorMode` and `bot.isDebuggingEnabled`
  - Replace every inline `fromString(bot.xColor ?: DEFAULT_X_COLOR)` expression with a call to
    `resolveColor(bot.xColor, DEFAULT_X_COLOR)`
- [ ] Update `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/arena/ArenaPanel.kt`
  - Apply the same `resolveColor` pattern for bullet and scan color resolution

## Phase 4 — Tests

- [ ] Add unit tests for `TankColorMode` in `gui` module
  - `fromString()` returns correct enum value for each name
  - `fromString()` falls back to `BOT_COLORS` for unknown/null input
- [ ] Add unit tests for `Tank` color resolution (mock `ConfigSettings` and `BotState`)
  - `BOT_COLORS`: returns bot-defined color when present; falls back to default when absent
  - `BOT_COLORS_LOCKED`: first non-null color is cached and returned on subsequent calls
  - `BOT_COLORS_LOCKED`: a second different color from the bot is ignored (cache unchanged)
  - `BOT_COLORS_LOCKED`: cache is cleared on `reset()` (new battle); locked color is gone after reset
  - `BOT_COLORS_LOCKED`: cached color persists across a simulated round boundary (no reset between rounds)
  - `DEFAULT_COLORS`: always returns default regardless of bot color
  - `BOT_COLORS_WHEN_DEBUGGING`: returns bot color when `isDebuggingEnabled == true`
  - `BOT_COLORS_WHEN_DEBUGGING`: returns default when `isDebuggingEnabled == false`
- [ ] Verify `PropertiesConsistencyTest` still passes (new `tank-color-mode` key must be covered)
