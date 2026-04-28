## 1. Dependencies


## 2. Theme Color Constants

  - `RobocodeDarkColors` with vals: `WINDOW_BG=#0a0e1a`, `COMPONENT_BG=#141824`, `FOREGROUND=#e0e8ff`, `ACCENT=#00d4ff`, `SELECTION_BG=#ff6b35`, `SELECTION_FG=#0a0e1a`, `BORDER=#1e2436`, `SCROLLBAR_THUMB=#1e2436`, `TOGGLE_ON=#00d4ff`
  - `RobocodeLightColors` with vals: `WINDOW_BG=#f0f4ff`, `COMPONENT_BG=#dce4f5`, `FOREGROUND=#1a1e2e`, `ACCENT=#007ba8`, `SELECTION_BG=#007ba8`, `SELECTION_FG=#f0f4ff`, `BORDER=#b8c4dc`, `SCROLLBAR_THUMB=#c0cce8`, `TOGGLE_ON=#007ba8`
  - Each color is a `val` of type `java.awt.Color` constructed with explicit RGB int components (e.g., `Color(0x00, 0xd4, 0xff)`)

## 3. Theme Kotlin Classes


## 4. Settings


## 5. Startup L&F Wiring


## 6. GUI Config — Theme Selector


## 7. LogoPanel — Theme-Aware Colors


## 8. ToggleSwitch — Theme-Aware Track Color


## 9. R8 Keep Rules

- [x] 9.1 Add to `gui/r8-rules.pro` after the MigLayout block: `-keep class com.formdev.flatlaf.** { *; }` and `-dontwarn com.formdev.flatlaf.**`
- [x] 9.2 Run `./gradlew :gui:r8ShrinkTask` and confirm it completes without FlatLaf-related errors

## 10. Build Verification

- [x] 10.1 Unzip the `*-all.jar` and confirm `com/formdev/flatlaf/FlatDarkLaf.class` and `dev/robocode/tankroyale/gui/ui/theme/RobocodeThemeColors.class` are present
- [x] 10.2 Run `./gradlew clean build` and confirm full build passes
- [x] 10.3 Verify arena rendering (canvas, tank colors, explosions) is unaffected when switching themes during a battle
- [x] 10.4 Verify the ANSI bot console keeps its dark background regardless of active theme
- [x] 10.5 Restart the GUI after selecting light theme — confirm light theme is restored from `gui.properties`

## 11. Theme-Aware ANSI Console Colors

- [x] 11.1 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ansi/LightAnsiColors.kt` implementing `IAnsiColors` with a full 17-color palette tuned for light backgrounds (WCAG AA ≥ 4.5:1 contrast): `black=#3d3d3d`, `red=#b03030`, `green=#1a6b30`, `yellow=#8a6000`, `blue=#1a5276`, `magenta=#6c3182`, `cyan=#0e6655`, `white=#7f7f7f`; bright variants: `brightBlack=#555555`, `brightRed=#c0392b`, `brightGreen=#1a6b30`, `brightYellow=#8a6000`, `brightBlue=#1a5276`, `brightMagenta=#7d3c98`, `brightCyan=#0e6655`, `brightWhite=#7f7f7f`; `default=#1a1a1a`
- [x] 11.2 In `AnsiEditorKit.kt`, declare `ansiColors` as `var ansiColors: IAnsiColors = DefaultAnsiColors` (constructor parameter) so the color scheme can be swapped at runtime without recreating the document
- [x] 11.3 In `AnsiEditorPane.kt`, add a `private var initialized = false` guard; keep background init in `init {}` via `applyThemeColors()`; add `updateUI()` override that calls `super.updateUI()` then `applyThemeColors()` when `initialized`; detect dark LAF via luminance of `UIManager.getColor("Panel.background")` (threshold 0.5) — no FlatLaf API dependency; dark BG = `Color(0x28,0x28,0x28)`, light BG = `UIManager.getColor("Panel.background")` (fallback `Color(0xf0,0xf4,0xff)`); swap `ansiKit.ansiColors` to `DefaultAnsiColors` (dark) or `LightAnsiColors` (light)
- [x] 11.4 Launch in dark theme, open bot/server console — verify default text is `#ffffff` on `#282828` background
- [x] 11.5 Switch to light theme, open bot/server console — verify default text is `#1a1a1a` on `#f6f6f7` background; confirm problematic colors are readable: yellow/brightYellow (`#8a6000`), cyan/brightCyan (`#0e6655`), white/brightWhite (`#7f7f7f`)
