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
```
