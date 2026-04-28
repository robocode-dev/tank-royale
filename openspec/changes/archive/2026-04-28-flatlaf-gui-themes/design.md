## Context

The GUI currently installs the OS system L&F at startup (`UIManager.getSystemLookAndFeelClassName()`). No third-party L&F library is present. All Swing chrome (buttons, dialogs, menus, scrollbars) renders with native OS widgets. Custom painting in `ArenaPanel`, `Tank`, `LogoPanel`, `ToggleSwitch`, and `SkullComponent` bypasses L&F entirely via `Graphics2D`.

FlatLaf (`com.formdev:flatlaf`) is a widely-used, actively maintained Swing L&F that supports fully custom color themes via `.properties` files, rounded corners, and per-component style overrides — all without replacing layout managers or component APIs. It is the approach used by IntelliJ IDEA and several JetBrains tools.

The GUI fat JAR is processed by R8 (class-file shrinker). FlatLaf uses `Class.forName()` strings stored in `UIDefaults` to load its UI delegates, so all FlatLaf classes must be kept by R8.

## Goals / Non-Goals

**Goals:**
- Replace system L&F with FlatLaf, dark robocode.dev theme as default
- Provide a light theme variant; user can switch live (no restart)
- Persist theme preference in `gui.properties`
- `LogoPanel` and `ToggleSwitch` adapt colors to the active theme
- Build continues to pass with R8 shrinking

**Non-Goals:**
- Theming of game rendering (`ArenaPanel`, `Tank`, `CircleBurst`, `SkullComponent`) — game art is domain-specific and intentionally fixed
- Theming of the ANSI console — terminal colors are a separate, intentionally dark surface
- IntelliJ `.theme.json` format — `.properties` is sufficient and simpler
- Font changes — system font stack is acceptable
- Additional FlatLaf extras (FlatLaf Extras, IntelliJ theme packs) — keep dependency surface minimal

## Decisions

### D1 — Custom FlatLaf subclasses with direct `defaults.put()` from named constants

**Chosen:** Two Kotlin classes (`RobocodeFlatDark : FlatDarkLaf`, `RobocodeFlatLight : FlatLightLaf`) overriding `getDefaults()` and applying colors via `defaults.put("UIKey", ColorConstant)` using values from `RobocodeDarkColors` / `RobocodeLightColors` objects. No `.properties` files.

**Alternatives considered:**

| Option | Reason rejected |
|---|---|
| `.properties` files with raw hex strings | Magic hex strings scattered in text files — same problem as inline literals, harder to find and refactor |
| `FlatPropertiesLaf(File)` | Reads from filesystem path, not classpath — breaks in fat JAR |
| `UIManager.put("key", value)` after setup | FlatLaf resolves some defaults at `getDefaults()` time; overriding inside `getDefaults()` is the correct hook |
| `IntelliJTheme.setup(stream)` | Requires `.theme.json` format — more verbose, no benefit over constants |

The subclass approach with named constants is the cleanest option: colors are defined once with meaningful names, type-checked at compile time, discoverable via IDE navigation, and applied at the correct FlatLaf lifecycle point.

### D2 — No ThemeManager singleton

**Chosen:** Theme apply logic lives inline — two call sites only:
1. `GuiApp.kt` startup: one `if/else` on `ConfigSettings.theme`
2. `GuiConfigPanel.applyThemeLive()`: `setup()` + `FlatLaf.updateUI()` + `updateComponentTreeUI` loop

A `ThemeManager` would add indirection for three lines of code. The codebase's existing pattern (logic local to where it fires) is the right call here.

### D3 — Live theme switching via FlatLaf.updateUI()

**Chosen:** On theme combo change, call:
```kotlin
RobocodeFlatDark.setup()           // or Light
FlatLaf.updateUI()                  // refreshes UIDefaults
Window.getWindows().forEach {
    SwingUtilities.updateComponentTreeUI(it)
}
```

This is FlatLaf's documented no-restart switching pattern. It reinstalls UI delegates on every live component without requiring a restart. Theme combo fires its `ActionListener` on the EDT, so no `invokeLater` wrapping is needed.

**Tradeoff:** `updateComponentTreeUI` walks every component tree synchronously. For the Tank Royale window count (main frame + a handful of dialogs), this is imperceptible. Not a concern.

### D4 — ToggleSwitch.onColor custom UIManager key

**Chosen:** Each theme class sets `defaults.put("ToggleSwitch.onColor", RobocodeDarkColors.TOGGLE_ON)` (or `RobocodeLightColors.TOGGLE_ON`) inside `getDefaults()`. Read via `UIManager.getColor("ToggleSwitch.onColor") ?: Color(0x3f, 0x3f, 0xff)` inside `paint()`.

**Alternative rejected:** `UIManager.getColor("Component.accentColor")` — FlatLaf maps this to an internal variable, not a stable UIManager key accessible from external code after installation. Behavior is L&F-version-dependent.

The explicit custom key gives predictable values regardless of FlatLaf version and makes the intent clear.

### D5 — LogoPanel colors from UIManager, read at paint time

**Chosen:** Remove the `init { background = Color(0x282828) }` block. FlatLaf's `updateComponentTreeUI` sets `Panel.background` on `LogoPanel` automatically. Read text color from `UIManager.getColor("Label.foreground")` inside `paintComponent()` (not cached in a field).

Reading in `paintComponent()` ensures the color is always current after a live theme switch. Caching in `init` or a field would freeze the color at construction time — exactly the bug we'd introduce by not thinking carefully here.

### D6 — R8 keep rules scope

**Chosen:** Broad `-keep class com.formdev.flatlaf.** { *; }` — keeps all FlatLaf classes and members.

FlatLaf uses `UIDefaults.get("SomeComponentUI")` → `Class.forName(string)` to instantiate its UI delegates. With `-dontobfuscate` already in `r8-rules.pro`, class names are preserved, but `-keep` is still needed to prevent dead-code elimination of UI delegate classes that appear unreachable (they are only reached via string lookup). The broad rule is safe because FlatLaf doesn't expose any classes that conflict with the rest of the codebase.

### D7 — All theme colors in named Kotlin constants

**Chosen:** Two Kotlin objects in the `ui/theme/` package:

```
RobocodeDarkColors   — WINDOW_BG, COMPONENT_BG, FOREGROUND, ACCENT,
                       SELECTION_BG, SELECTION_FG, BORDER, SCROLLBAR_THUMB, TOGGLE_ON
RobocodeLightColors  — (same keys, light palette values)
```

Both are `object` declarations (not companion objects) in a single file `RobocodeThemeColors.kt`. Each color is a `val` of type `java.awt.Color`, constructed with explicit RGB components (e.g., `Color(0x00, 0xd4, 0xff)`), not `Color.decode()` — avoiding runtime parsing.

**Alternative rejected:** Single `RobocodeColors` object with nested `Dark`/`Light` sub-objects — marginally cleaner call sites (`RobocodeColors.Dark.ACCENT`) but requires one extra layer of nesting with no real benefit for two themes.

**Why this matters:** A single authoritative source for all palette values means color changes are one-line edits, colors are named (not opaque hex), and the compiler enforces that `Color` is the type — not `String`.

## Risks / Trade-offs

- **FlatLaf UIDefaults key stability** — UIDefaults key names (e.g., `Component.accentColor`, `Component.focusedBorderColor`) are FlatLaf-internal and may change across major versions. Mitigation: pin the `flatlaf` version in `libs.versions.toml`; review keys on upgrade.

- **R8 fat-JAR resource merging** — FlatLaf ships its own `META-INF/services` and internal `.properties` files. `FatJar` uses `DuplicatesStrategy.EXCLUDE` (first-wins). No other dependency in this project overlaps FlatLaf's paths, so no conflict is expected. Mitigation: unzip the `*-all.jar` after build and confirm `com/formdev/flatlaf/FlatDarkLaf.class` exists.

- **ToggleSwitch knob color** — `foreground = Color.WHITE` stays hardcoded for the knob. White is readable on both cyan (`#00d4ff`) and blue (`#007ba8`) backgrounds. If a future light theme uses a lighter accent, the knob may need updating. Acceptable for now.

- **LogoPanel text color** — `Label.foreground` resolves to `RobocodeDarkColors.FOREGROUND` (`#e0e8ff`) when the dark theme is active. If the key is absent, the fallback `Color(0xe0, 0xe8, 0xff)` ensures the text is always visible on the dark background.

## Migration Plan

1. Add FlatLaf dependency — build verification (no change to user-visible behavior yet)
2. Create `RobocodeThemeColors.kt` and theme classes — no runtime effect until step 3
3. Replace `UIManager.setLookAndFeel(...)` in `GuiApp.kt` — first visible change; GUI launches with FlatLaf dark theme
4. Add `theme` setting + GUI Config row — enables user control
5. Fix `LogoPanel` and `ToggleSwitch` — complete theme-awareness

No database migrations, no protocol changes, no rollback complexity. If FlatLaf causes rendering issues, reverting step 3 (`GuiApp.kt`) restores the old behavior completely.

## Open Questions

_(none — all decisions resolved during planning)_
