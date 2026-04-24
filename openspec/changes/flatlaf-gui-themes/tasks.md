## 1. Dependencies

- [x] 1.1 Add `flatlaf = "3.5.4"` to `[versions]` in `gradle/libs.versions.toml`
- [x] 1.2 Add `flatlaf = { module = "com.formdev:flatlaf", version.ref = "flatlaf" }` to `[libraries]` in `gradle/libs.versions.toml`
- [x] 1.3 Add `implementation(libs.flatlaf)` to the `dependencies {}` block in `gui/build.gradle.kts`
- [x] 1.4 Run `./gradlew :gui:compileKotlin` and confirm it succeeds with no errors

## 2. Theme Color Constants

- [x] 2.1 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/theme/RobocodeThemeColors.kt` containing two `object` declarations:
  - `RobocodeDarkColors` with vals: `WINDOW_BG=#0a0e1a`, `COMPONENT_BG=#141824`, `FOREGROUND=#e0e8ff`, `ACCENT=#00d4ff`, `SELECTION_BG=#ff6b35`, `SELECTION_FG=#0a0e1a`, `BORDER=#1e2436`, `SCROLLBAR_THUMB=#1e2436`, `TOGGLE_ON=#00d4ff`
  - `RobocodeLightColors` with vals: `WINDOW_BG=#f0f4ff`, `COMPONENT_BG=#dce4f5`, `FOREGROUND=#1a1e2e`, `ACCENT=#007ba8`, `SELECTION_BG=#007ba8`, `SELECTION_FG=#f0f4ff`, `BORDER=#b8c4dc`, `SCROLLBAR_THUMB=#c0cce8`, `TOGGLE_ON=#007ba8`
  - Each color is a `val` of type `java.awt.Color` constructed with explicit RGB int components (e.g., `Color(0x00, 0xd4, 0xff)`)

## 3. Theme Kotlin Classes

- [x] 3.1 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/theme/RobocodeFlatDark.kt` — extends `FlatDarkLaf`, overrides `getDefaults()` to apply all `RobocodeDarkColors` constants via `defaults.put("UIKey", RobocodeDarkColors.CONSTANT)` for every themed UIDefaults key (Panel.background, window, Label.foreground, Component.accentColor, selection keys, scrollbar keys, border keys, ToggleSwitch.onColor, etc.); companion `setup()` delegates to `FlatLaf.setup(RobocodeFlatDark())`
- [x] 3.2 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/theme/RobocodeFlatLight.kt` — same pattern extending `FlatLightLaf`, applying `RobocodeLightColors` constants
- [x] 3.3 Run `./gradlew :gui:compileKotlin` and confirm all three new files compile cleanly

## 4. Settings

- [x] 4.1 Add `private const val THEME = "theme"` to `ConfigSettings.kt` alongside the other key constants
- [x] 4.2 Add `var theme: String` property to `ConfigSettings.kt` — getter returns `"dark"` or `"light"` (default `"dark"`), setter saves to `gui.properties`

## 5. Startup L&F Wiring

- [x] 5.1 In `GuiApp.kt`, replace `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())` with `if (ConfigSettings.theme == "light") RobocodeFlatLight.setup() else RobocodeFlatDark.setup()`
- [x] 5.2 Add imports for `RobocodeFlatDark` and `RobocodeFlatLight` in `GuiApp.kt`; remove the now-unused `javax.swing.UIManager` import if nothing else uses it
- [x] 5.3 Launch the GUI and confirm the dark robocode.dev theme is applied (navy background, cyan accents)

## 6. GUI Config — Theme Selector

- [x] 6.1 Add `option.gui.theme=Theme`, `option.gui.theme.dark=Dark`, `option.gui.theme.light=Light` to `gui/src/main/resources/Strings.properties`
- [x] 6.2 Add the same three keys (translated or English fallback) to `Strings_ca.properties`, `Strings_da.properties`, `Strings_es.properties`
- [x] 6.3 In `GuiConfigDialog.kt`, add `ThemeOption` data class and `themeCombo: JComboBox` field alongside the existing `LanguageOption` / `languageCombo`
- [x] 6.4 Add `addThemeSelector()` method (label + combo row) and call it from `init` before `addOkButton`
- [x] 6.5 Add `applyThemeLive()` method: guard for same-theme no-op → `ConfigSettings.theme = newCode` → `setup()` → `FlatLaf.updateUI()` → `Window.getWindows().forEach { SwingUtilities.updateComponentTreeUI(it) }`
- [x] 6.6 Wire `themeCombo.addActionListener { applyThemeLive() }` in `init`
- [x] 6.7 Add theme initialization to `syncFromSettings()` — select the combo item matching `ConfigSettings.theme`
- [x] 6.8 Open GUI Config, switch Dark ↔ Light, confirm all open windows update immediately with no restart dialog

## 7. LogoPanel — Theme-Aware Colors

- [x] 7.1 Remove `private val textColor = Color(0x377B37)` from `LogoPanel.kt`
- [x] 7.2 Remove the `init { background = Color(0x282828) }` block from `LogoPanel.kt`
- [x] 7.3 In `paintComponent()`, replace `g2.color = textColor` with `g2.color = UIManager.getColor("Label.foreground") ?: Color(0xe0, 0xe8, 0xff)`
- [x] 7.4 Add `import javax.swing.UIManager` to `LogoPanel.kt`
- [x] 7.5 Verify LogoPanel shows navy background in dark theme and light background in light theme

## 8. ToggleSwitch — Theme-Aware Track Color

- [x] 8.1 Remove `background = Color(0x3f, 0x3f, 0xff)` from `ToggleSwitch.kt` `init` block
- [x] 8.2 In `paint()`, replace `g2.color = background` with `g2.color = UIManager.getColor("ToggleSwitch.onColor") ?: Color(0x3f, 0x3f, 0xff)`
- [x] 8.3 Add `import javax.swing.UIManager` to `ToggleSwitch.kt`
- [x] 8.4 Verify ToggleSwitch renders cyan track in dark theme and blue track in light theme

## 11. Theme-Aware ANSI Console Colors

- [x] 11.1 Create `LightAnsiColors.kt` — light-optimized ANSI palette implementing `IAnsiColors` (darkened yellow/cyan, golden amber/turquoise for bright variants, near-black default)
- [x] 11.2 Change `private val ansiColors` → `var ansiColors` in `AnsiEditorKit.kt` so it can be swapped on theme change
- [x] 11.3 Override `updateUI()` in `AnsiEditorPane.kt` — call `applyThemeColors()` on theme switch; use luminance-based LAF detection (no FlatLaf API dependency); call `applyThemeColors()` directly from `init` (not `updateUI()`) to avoid `super.updateUI()` interference during construction
- [x] 11.4 Verify dark theme console — white default text on dark (#282828) background
- [x] 11.5 Verify light theme console — near-black default text on light (#dce4f5) background

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
