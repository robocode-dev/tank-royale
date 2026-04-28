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

- [ ] 11.1 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ansi/LightAnsiColors.kt` implementing `IAnsiColors` with a palette tuned for light backgrounds: darkened yellow (`#b7950b`), darkened cyan (`#1a8a8a`), golden-amber brightYellow (`#d4a017`), turquoise brightCyan (`#1abc9c`), medium-gray brightWhite (`#9e9e9e`), near-black default (`#1a1a2e`)
- [ ] 11.2 In `AnsiEditorKit.kt`, change `private val ansiColors` to `var ansiColors` so the color scheme can be swapped at runtime without recreating the document
- [ ] 11.3 In `AnsiEditorPane.kt`, add a `private var ready = false` guard; move background init out of `init {}` into a new `updateUI()` override that calls `FlatLaf.isLafDark()` to select background (`Color(0x28,0x28,0x28)` for dark; `UIManager.getColor("Panel.background")` for light) and swaps `ansiKit.ansiColors` to the matching scheme
- [ ] 11.4 Launch in dark theme, open bot/server console — verify white default text on `#282828` background
- [ ] 11.5 Switch to light theme, open bot/server console — verify near-black default text on `#dce4f5` background with no invisible colors (yellow, cyan, white all readable)
