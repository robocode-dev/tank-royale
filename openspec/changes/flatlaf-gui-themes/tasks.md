## 1. Dependencies

- [ ] 1.1 Add `flatlaf = "3.5.4"` to `[versions]` in `gradle/libs.versions.toml`
- [ ] 1.2 Add `flatlaf = { module = "com.formdev:flatlaf", version.ref = "flatlaf" }` to `[libraries]` in `gradle/libs.versions.toml`
- [ ] 1.3 Add `implementation(libs.flatlaf)` to the `dependencies {}` block in `gui/build.gradle.kts`
- [ ] 1.4 Run `./gradlew :gui:compileKotlin` and confirm it succeeds with no errors

## 2. Theme Color Constants

- [ ] 2.1 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/theme/RobocodeThemeColors.kt` containing two `object` declarations:
  - `RobocodeDarkColors` with vals: `WINDOW_BG=#0a0e1a`, `COMPONENT_BG=#141824`, `FOREGROUND=#e0e8ff`, `ACCENT=#00d4ff`, `SELECTION_BG=#ff6b35`, `SELECTION_FG=#0a0e1a`, `BORDER=#1e2436`, `SCROLLBAR_THUMB=#1e2436`, `TOGGLE_ON=#00d4ff`
  - `RobocodeLightColors` with vals: `WINDOW_BG=#f0f4ff`, `COMPONENT_BG=#dce4f5`, `FOREGROUND=#1a1e2e`, `ACCENT=#007ba8`, `SELECTION_BG=#007ba8`, `SELECTION_FG=#f0f4ff`, `BORDER=#b8c4dc`, `SCROLLBAR_THUMB=#c0cce8`, `TOGGLE_ON=#007ba8`
  - Each color is a `val` of type `java.awt.Color` constructed with explicit RGB int components (e.g., `Color(0x00, 0xd4, 0xff)`)

## 3. Theme Kotlin Classes

- [ ] 3.1 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/theme/RobocodeFlatDark.kt` — extends `FlatDarkLaf`, overrides `getDefaults()` to apply all `RobocodeDarkColors` constants via `defaults.put("UIKey", RobocodeDarkColors.CONSTANT)` for every themed UIDefaults key (Panel.background, window, Label.foreground, Component.accentColor, selection keys, scrollbar keys, border keys, ToggleSwitch.onColor, etc.); companion `setup()` delegates to `FlatLaf.setup(RobocodeFlatDark())`
- [ ] 3.2 Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/theme/RobocodeFlatLight.kt` — same pattern extending `FlatLightLaf`, applying `RobocodeLightColors` constants
- [ ] 3.3 Run `./gradlew :gui:compileKotlin` and confirm all three new files compile cleanly

## 4. Settings

- [ ] 4.1 Add `private const val THEME = "theme"` to `ConfigSettings.kt` alongside the other key constants
- [ ] 4.2 Add `var theme: String` property to `ConfigSettings.kt` — getter returns `"dark"` or `"light"` (default `"dark"`), setter saves to `gui.properties`

## 5. Startup L&F Wiring

- [ ] 5.1 In `GuiApp.kt`, replace `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())` with `if (ConfigSettings.theme == "light") RobocodeFlatLight.setup() else RobocodeFlatDark.setup()`
- [ ] 5.2 Add imports for `RobocodeFlatDark` and `RobocodeFlatLight` in `GuiApp.kt`; remove the now-unused `javax.swing.UIManager` import if nothing else uses it
- [ ] 5.3 Launch the GUI and confirm the dark robocode.dev theme is applied (navy background, cyan accents)

## 6. GUI Config — Theme Selector

- [ ] 6.1 Add `option.gui.theme=Theme`, `option.gui.theme.dark=Dark`, `option.gui.theme.light=Light` to `gui/src/main/resources/Strings.properties`
- [ ] 6.2 Add the same three keys (translated or English fallback) to `Strings_ca.properties`, `Strings_da.properties`, `Strings_es.properties`
- [ ] 6.3 In `GuiConfigDialog.kt`, add `ThemeOption` data class and `themeCombo: JComboBox` field alongside the existing `LanguageOption` / `languageCombo`
- [ ] 6.4 Add `addThemeSelector()` method (label + combo row) and call it from `init` before `addOkButton`
- [ ] 6.5 Add `applyThemeLive()` method: guard for same-theme no-op → `ConfigSettings.theme = newCode` → `setup()` → `FlatLaf.updateUI()` → `Window.getWindows().forEach { SwingUtilities.updateComponentTreeUI(it) }`
- [ ] 6.6 Wire `themeCombo.addActionListener { applyThemeLive() }` in `init`
- [ ] 6.7 Add theme initialization to `syncFromSettings()` — select the combo item matching `ConfigSettings.theme`
- [ ] 6.8 Open GUI Config, switch Dark ↔ Light, confirm all open windows update immediately with no restart dialog

## 7. LogoPanel — Theme-Aware Colors

- [ ] 7.1 Remove `private val textColor = Color(0x377B37)` from `LogoPanel.kt`
- [ ] 7.2 Remove the `init { background = Color(0x282828) }` block from `LogoPanel.kt`
- [ ] 7.3 In `paintComponent()`, replace `g2.color = textColor` with `g2.color = UIManager.getColor("Label.foreground") ?: Color(0xe0, 0xe8, 0xff)`
- [ ] 7.4 Add `import javax.swing.UIManager` to `LogoPanel.kt`
- [ ] 7.5 Verify LogoPanel shows navy background in dark theme and light background in light theme

## 8. ToggleSwitch — Theme-Aware Track Color

- [ ] 8.1 Remove `background = Color(0x3f, 0x3f, 0xff)` from `ToggleSwitch.kt` `init` block
- [ ] 8.2 In `paint()`, replace `g2.color = background` with `g2.color = UIManager.getColor("ToggleSwitch.onColor") ?: Color(0x3f, 0x3f, 0xff)`
- [ ] 8.3 Add `import javax.swing.UIManager` to `ToggleSwitch.kt`
- [ ] 8.4 Verify ToggleSwitch renders cyan track in dark theme and blue track in light theme

## 9. R8 Keep Rules

- [ ] 9.1 Add to `gui/r8-rules.pro` after the MigLayout block: `-keep class com.formdev.flatlaf.** { *; }` and `-dontwarn com.formdev.flatlaf.**`
- [ ] 9.2 Run `./gradlew :gui:r8ShrinkTask` and confirm it completes without FlatLaf-related errors

## 10. Build Verification

- [ ] 10.1 Unzip the `*-all.jar` and confirm `com/formdev/flatlaf/FlatDarkLaf.class` and `dev/robocode/tankroyale/gui/ui/theme/RobocodeThemeColors.class` are present
- [ ] 10.2 Run `./gradlew clean build` and confirm full build passes
- [ ] 10.3 Verify arena rendering (canvas, tank colors, explosions) is unaffected when switching themes during a battle
- [ ] 10.4 Verify the ANSI bot console keeps its dark background regardless of active theme
- [ ] 10.5 Restart the GUI after selecting light theme — confirm light theme is restored from `gui.properties`
