## Why

The Tank Royale GUI currently uses the OS system Look & Feel, giving a plain native appearance that clashes with the Robocode brand and looks dated next to modern developer tooling. FlatLaf is the standard modern Swing L&F and supports fully custom color themes via `.properties` files, enabling a cohesive robocode.dev dark/light palette without replacing any existing layout or component logic.

## What Changes

- Replace `UIManager.getSystemLookAndFeelClassName()` with FlatLaf (`com.formdev:flatlaf`) in `GuiApp.kt`
- Introduce a custom dark theme (default) and light theme, each backed by a `.properties` file using the robocode.dev color palette:
  - Dark: background `#0a0e1a`, accent `#00d4ff`, selection `#ff6b35`, foreground `#e0e8ff`
  - Light: background `#f0f4ff`, accent `#007ba8`, foreground `#1a1e2e`
- Two new Kotlin classes (`RobocodeFlatDark`, `RobocodeFlatLight`) in a new `ui/theme/` package
- Add a `theme` setting to `ConfigSettings` (persisted in `gui.properties`, default `"dark"`)
- Add a **Theme** selector row to the GUI Config dialog — theme switches live (no restart required)
- Update `LogoPanel` to read background and text color from UIManager instead of hardcoded values
- Update `ToggleSwitch` to read its track color from a `ToggleSwitch.onColor` UIManager key (defined per theme) instead of a hardcoded blue
- Add FlatLaf keep rules to `r8-rules.pro`
- Add `flatlaf` version entry and library to `gradle/libs.versions.toml` and `gui/build.gradle.kts`

**Not changing:**
- Arena rendering (game canvas, tank colors, explosions, bullets)
- ANSI console colors (terminal palette, intentionally dark)
- MiGLayout (stays as the layout manager)

## Capabilities

### New Capabilities

- `gui-flatlaf-theming`: FlatLaf-based theming for the GUI — custom dark (default) and light robocode.dev themes, persisted preference, live switching in GUI Config dialog

### Modified Capabilities

_(none — no existing spec-level requirements are changing)_

## Impact

- **Dependencies**: `com.formdev:flatlaf` added to `gui/build.gradle.kts` and `gradle/libs.versions.toml`
- **R8 shrinking**: `gui/r8-rules.pro` must keep all FlatLaf classes
- **Files modified**: `GuiApp.kt`, `ConfigSettings.kt`, `GuiConfigDialog.kt`, `LogoPanel.kt`, `ToggleSwitch.kt`, `Strings.properties` (+ locale variants), `r8-rules.pro`, `libs.versions.toml`, `gui/build.gradle.kts`
- **New files**: `RobocodeThemeColors.kt`, `RobocodeFlatDark.kt`, `RobocodeFlatLight.kt`
- **No API changes**: purely GUI-internal; bot API, server, and protocol are unaffected
