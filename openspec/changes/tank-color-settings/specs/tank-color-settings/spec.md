## ADDED Requirements

### Requirement: Tank Color Mode Selector
The GUI SHALL include a **Tank Color Mode** radio-button group in the configuration dialog so that
the user can choose how tank colors are displayed during a battle.

#### Scenario: User navigates to GUI configuration
- **WHEN** the user opens the GUI configuration dialog
- **THEN** the user sees a "Tank Color Mode" section with four radio buttons:
  "Bot Colors", "Bot Colors (Once)", "Default Colors", and "Bot Colors (Debug Only)"

---

### Requirement: Bot Colors Mode
When **Bot Colors** is selected, tanks SHALL render using the colors provided by the bot. If a bot
provides no color for a component, the system default for that component is used. This is the
default mode and preserves existing behaviour.

#### Scenario: Bot Colors mode is active
- **GIVEN** Tank Color Mode is set to "Bot Colors"
- **WHEN** a battle is running
- **THEN** each tank renders with the colors supplied by its bot
- **AND** components for which the bot provides no color render with the system default color
- **AND** color changes from the bot take effect immediately

---

### Requirement: Bot Colors (Once) Mode
When **Bot Colors (Once)** is selected, the first color a bot provides for each component SHALL be
recorded and used for the entire battle, including between rounds. Subsequent color changes from the
bot are ignored. A component for which the bot has never provided a color renders with the system
default.

#### Scenario: Bot sets a color for the first time
- **GIVEN** Tank Color Mode is set to "Bot Colors (Once)"
- **WHEN** a bot sends its first non-null color for a component during the battle
- **THEN** that color is locked and used for that component for the rest of the battle

#### Scenario: Bot attempts to change a locked color
- **GIVEN** Tank Color Mode is set to "Bot Colors (Once)"
- **AND** a bot has already provided a color for a component
- **WHEN** the bot sends a different color for that component
- **THEN** the original locked color continues to be rendered (the change is ignored)

#### Scenario: Locked colors persist between rounds
- **GIVEN** Tank Color Mode is set to "Bot Colors (Once)"
- **AND** a bot's colors are locked from round 1
- **WHEN** round 2 begins
- **THEN** the same locked colors from round 1 are still used

---

### Requirement: Default Colors Mode
When **Default Colors** is selected, all tanks SHALL render with the hardcoded system-default colors
from `ColorConstant`, regardless of any colors the bot provides.

#### Scenario: Default Colors mode is active
- **GIVEN** Tank Color Mode is set to "Default Colors"
- **WHEN** a battle is running
- **THEN** all tanks render with the system-default colors
- **AND** bot-defined colors are completely ignored

---

### Requirement: Bot Colors (Debug Only) Mode
When **Bot Colors (Debug Only)** is selected, a tank SHALL render with its bot-defined colors only
while Graphical Debugging is enabled for that bot in the Bot Console. Otherwise the system defaults
are used.

#### Scenario: Debugging is enabled for a bot
- **GIVEN** Tank Color Mode is set to "Bot Colors (Debug Only)"
- **AND** the user has enabled Graphical Debugging for Bot A in the Bot Console
- **WHEN** the battle is running
- **THEN** Bot A renders with its bot-defined colors
- **AND** all other bots render with the system-default colors

#### Scenario: Debugging is disabled for a bot
- **GIVEN** Tank Color Mode is set to "Bot Colors (Debug Only)"
- **AND** Graphical Debugging is NOT enabled for Bot A
- **WHEN** the battle is running
- **THEN** Bot A renders with the system-default colors

---

### Requirement: Persistence of Tank Color Mode
The selected Tank Color Mode SHALL be saved in the GUI configuration and persist across application
restarts.

#### Scenario: User saves the Tank Color Mode
- **WHEN** the user selects a mode and clicks "OK"
- **THEN** the mode is saved to `gui.properties`
- **WHEN** the application is restarted
- **THEN** the previously saved mode is active
