## ADDED Requirements

### Requirement: FlatLaf is used as the Look and Feel
The GUI SHALL use FlatLaf (`com.formdev:flatlaf`) as its Swing Look and Feel instead of the OS system L&F. FlatLaf SHALL be initialized before any Swing window is shown.

#### Scenario: GUI launches with FlatLaf active
- **WHEN** the GUI application starts
- **THEN** the active Look and Feel is a FlatLaf instance (not the OS system L&F)

#### Scenario: FlatLaf initializes before the main window appears
- **WHEN** the GUI application starts
- **THEN** FlatLaf is installed before `MainFrame` is made visible

---

### Requirement: Dark theme is the default
The GUI SHALL apply the Robocode dark theme on first launch (i.e., when no theme preference has been saved). The dark theme SHALL use the robocode.dev dark color palette: background `#0a0e1a`, component background `#141824`, foreground `#e0e8ff`, accent `#00d4ff`, selection `#ff6b35`.

#### Scenario: First launch with no saved preference
- **WHEN** the GUI starts and no `theme` key exists in `gui.properties`
- **THEN** the dark robocode.dev theme is applied

#### Scenario: Dark theme palette applied
- **WHEN** the dark theme is active
- **THEN** panel backgrounds render as `#141824`, text as `#e0e8ff`, and focused borders as `#00d4ff`

---

### Requirement: Theme preference is persisted
The active theme SHALL be saved to `gui.properties` under the key `theme` with values `"dark"` or `"light"`. On subsequent launches the saved value SHALL be applied.

#### Scenario: Dark theme saved and restored
- **WHEN** the user selects the dark theme and restarts the application
- **THEN** the dark theme is applied on startup without user interaction

#### Scenario: Light theme saved and restored
- **WHEN** the user selects the light theme and restarts the application
- **THEN** the light theme is applied on startup without user interaction

#### Scenario: Invalid or missing value falls back to dark
- **WHEN** the `theme` key in `gui.properties` contains an unrecognized value
- **THEN** the dark theme is applied

---

### Requirement: Light theme is available
The GUI SHALL provide a Robocode light theme variant using the robocode.dev light palette: background `#f0f4ff`, component background `#dce4f5`, foreground `#1a1e2e`, accent `#007ba8`, selection `#007ba8`.

#### Scenario: Light theme palette applied
- **WHEN** the light theme is active
- **THEN** panel backgrounds render as `#dce4f5`, text as `#1a1e2e`, and focused borders as `#007ba8`

---

### Requirement: Theme can be switched live in GUI Config
The GUI Config dialog SHALL include a Theme selector (combo box with "Dark" and "Light" options). Selecting a different theme SHALL apply it immediately to all open windows without requiring a restart.

#### Scenario: User switches from dark to light
- **WHEN** the user opens GUI Config, selects "Light" in the Theme combo, and clicks OK (or the combo fires immediately)
- **THEN** all open windows update to the light theme without the application restarting

#### Scenario: User switches from light to dark
- **WHEN** the user opens GUI Config and selects "Dark" in the Theme combo
- **THEN** all open windows update to the dark theme immediately

#### Scenario: Theme selector reflects the active theme on open
- **WHEN** the user opens the GUI Config dialog
- **THEN** the Theme combo shows the currently active theme

#### Scenario: No restart message shown for theme change
- **WHEN** the user changes the theme in GUI Config
- **THEN** no "restart required" message is shown

---

### Requirement: LogoPanel adapts to the active theme
The `LogoPanel` splash screen SHALL use the current theme's `Panel.background` UIManager color for its background and `Label.foreground` for its text color. These SHALL be read at paint time so that a live theme switch is reflected without restarting.

#### Scenario: LogoPanel background matches dark theme
- **WHEN** the dark theme is active and the LogoPanel is visible
- **THEN** the LogoPanel background is the dark theme's panel background color (`#141824`)

#### Scenario: LogoPanel background matches light theme
- **WHEN** the light theme is active and the LogoPanel is visible
- **THEN** the LogoPanel background is the light theme's panel background color (`#dce4f5`)

#### Scenario: LogoPanel text color matches active theme
- **WHEN** the theme is switched while the LogoPanel is visible
- **THEN** on the next repaint the text color reflects the new theme's `Label.foreground`

---

### Requirement: ToggleSwitch track color adapts to the active theme
The `ToggleSwitch` component SHALL use the UIManager key `ToggleSwitch.onColor` for its "on" track color. Each theme SHALL define this key. The color SHALL be read at paint time, not cached at construction.

#### Scenario: ToggleSwitch track is cyan in dark theme
- **WHEN** the dark theme is active and a ToggleSwitch is in the "on" state
- **THEN** the track renders in `#00d4ff`

#### Scenario: ToggleSwitch track is blue in light theme
- **WHEN** the light theme is active and a ToggleSwitch is in the "on" state
- **THEN** the track renders in `#007ba8`

#### Scenario: ToggleSwitch falls back if key is missing
- **WHEN** the UIManager key `ToggleSwitch.onColor` is absent
- **THEN** the track renders in the original fallback color `#3f3fff`

---

### Requirement: Game rendering is unaffected by theming
The battle arena canvas, tank colors, bullet rendering, explosion effects, and ANSI console colors SHALL NOT be controlled by the theme system. These elements SHALL remain visually independent of the active theme.

#### Scenario: Arena canvas stays dark during light theme
- **WHEN** the light theme is active and a battle is running
- **THEN** the arena background remains `Color.DARK_GRAY` / `Color.BLACK` (game canvas colors, unchanged)

#### Scenario: Explosion colors are unchanged by theme switch
- **WHEN** the theme is switched during a battle
- **THEN** explosion particle colors are unaffected

#### Scenario: ANSI console background stays dark in light theme
- **WHEN** the light theme is active and a bot console is open
- **THEN** the console background remains the intentional dark terminal color (`#282828`)
