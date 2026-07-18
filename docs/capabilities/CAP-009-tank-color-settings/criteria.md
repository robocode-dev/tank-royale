---
id: CAP-009-criteria
type: criteria
status: draft
links: [CAP-009]
title: Acceptance criteria for CAP-009 (tank-color-settings)
ac-prefix: TCS
provenance: inferred
---

```gherkin
Feature: tank-color-settings — Defines how the GUI controls and persists the **Tank Color Mode** — the setting that governs which

  # Requirement: Tank Color Mode Selector
  # The GUI SHALL include a **Tank Color Mode** radio-button group in the configuration dialog so that
  # the user can choose how tank colors are displayed during a battle.

  @TCS-001
  Scenario: User navigates to GUI configuration
    When the user opens the GUI configuration dialog
    Then the user sees a "Tank Color Mode" section with four radio buttons:
    # "Bot Colors", "Bot Colors (Once)", "Default Colors", and "Bot Colors (Debug Only)"
    # ---

  # Requirement: Bot Colors Mode
  # When **Bot Colors** is selected, tanks SHALL render using the colors provided by the bot. If a bot
  # provides no color for a component, the system default for that component is used. This is the
  # default mode and preserves existing behaviour.

  @TCS-002
  Scenario: Bot Colors mode is active
    Given Tank Color Mode is set to "Bot Colors"
    When a battle is running
    Then each tank renders with the colors supplied by its bot
    And components for which the bot provides no color render with the system default color
    And color changes from the bot take effect immediately
    # ---

  # Requirement: Bot Colors (Once) Mode
  # When **Bot Colors (Once)** is selected, the first color a bot provides for each component SHALL be
  # recorded and used for the entire battle, including between rounds. Subsequent color changes from the
  # bot are ignored. A component for which the bot has never provided a color renders with the system
  # default.

  @TCS-003
  Scenario: Bot sets a color for the first time
    Given Tank Color Mode is set to "Bot Colors (Once)"
    When a bot sends its first non-null color for a component during the battle
    Then that color is locked and used for that component for the rest of the battle

  @TCS-004
  Scenario: Bot attempts to change a locked color
    Given Tank Color Mode is set to "Bot Colors (Once)"
    And a bot has already provided a color for a component
    When the bot sends a different color for that component
    Then the original locked color continues to be rendered (the change is ignored)

  @TCS-005
  Scenario: Locked colors persist between rounds
    Given Tank Color Mode is set to "Bot Colors (Once)"
    And a bot's colors are locked from round 1
    When round 2 begins
    Then the same locked colors from round 1 are still used
    # ---

  # Requirement: Default Colors Mode
  # When **Default Colors** is selected, all tanks SHALL render with the hardcoded system-default colors
  # from `ColorConstant`, regardless of any colors the bot provides.

  @TCS-006
  Scenario: Default Colors mode is active
    Given Tank Color Mode is set to "Default Colors"
    When a battle is running
    Then all tanks render with the system-default colors
    And bot-defined colors are completely ignored
    # ---

  # Requirement: Bot Colors (Debug Only) Mode
  # When **Bot Colors (Debug Only)** is selected, a tank SHALL render with its bot-defined colors only
  # while Graphical Debugging is enabled for that bot in the Bot Console. Otherwise the system defaults
  # are used.

  @TCS-007
  Scenario: Debugging is enabled for a bot
    Given Tank Color Mode is set to "Bot Colors (Debug Only)"
    And the user has enabled Graphical Debugging for Bot A in the Bot Console
    When the battle is running
    Then Bot A renders with its bot-defined colors
    And all other bots render with the system-default colors

  @TCS-008
  Scenario: Debugging is disabled for a bot
    Given Tank Color Mode is set to "Bot Colors (Debug Only)"
    And Graphical Debugging is NOT enabled for Bot A
    When the battle is running
    Then Bot A renders with the system-default colors
    # ---

  # Requirement: Persistence of Tank Color Mode
  # The selected Tank Color Mode SHALL be saved in the GUI configuration and persist across application
  # restarts.

  @TCS-009
  Scenario: User saves the Tank Color Mode
    When the user selects a mode and clicks "OK"
    Then the mode is saved to `gui.properties`
    When the application is restarted
    Then the previously saved mode is active
```
