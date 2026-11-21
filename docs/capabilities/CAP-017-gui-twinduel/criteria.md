---
id: CAP-017-criteria
type: criteria
status: active
links: [CAP-017]
title: Acceptance criteria for CAP-017 (gui-twinduel)
ac-prefix: GTD
provenance: inferred
reversal-cost: low
---

```gherkin
Feature: gui-twinduel — Select and start the shared TwinDuel preset from the Tank Royale GUI.

  @GTD-001
  Scenario: Select TwinDuel for a GUI battle
    Test-type: Unit
    Given the GUI game-type selector is available
    When a user selects `twinduel`
    Then the selector SHALL retain the `twinduel` selection
    And the GUI SHALL use the common 800×800, four-participant TwinDuel preset when starting the battle

  @GTD-002
  Scenario: Two teams of two bots fill the four TwinDuel places in New Battle
    Test-type: Unit
    Given the TwinDuel preset with four participants and bots chosen in New Battle
    When the chosen bots are counted against the preset
    Then every bot SHALL count as a participant, also a team's member, as the server counts them, so two teams of two bots can start a TwinDuel
    And fewer or more than four bots SHALL NOT be able to start it

  @GTD-003
  Scenario: The desktop program's own server accepts TwinDuel
    Test-type: Unit
    Given the desktop program starts its local server
    When it names the game types the server accepts
    Then they SHALL include every game type New Battle offers, TwinDuel among them, so a TwinDuel started from New Battle is not refused
    And they SHALL name only known game types, each once
```
