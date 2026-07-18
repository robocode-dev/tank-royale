---
id: CAP-004-criteria
type: criteria
status: draft
links: [CAP-004]
title: Acceptance criteria for CAP-004 (gui-boot-progress)
ac-prefix: GBP
provenance: inferred
---

```gherkin
Feature: gui-boot-progress — TBD - created by archiving change add-gui-boot-progress-dialog. Update Purpose after archive.

  # Requirement: Boot Progress Dialog
  # The GUI SHALL display a modal progress dialog between the "Start Battle" action and the game start,
  # showing which bots have connected to the server and which are still pending.

  @GBP-001
  Scenario: Dialog shown on battle start
    When the user clicks "Start Battle" in the New Battle dialog
    Then a modal progress dialog SHALL appear showing all expected bot identities
    And each bot SHALL be displayed with its name and version from `bot.json`

  @GBP-002
  Scenario: Bot connects during wait
    When a bot connects to the server while the progress dialog is open
    Then the dialog SHALL update that bot's status from pending to connected
    And the connected count SHALL increment

  @GBP-003
  Scenario: All bots connected
    When all expected bot identities have connected
    Then the progress dialog SHALL auto-close
    And the battle SHALL start automatically

  @GBP-004
  Scenario: Team identity expansion
    When a team directory is among the selected bots
    Then the dialog SHALL show each team member's identity individually
    And duplicate members (e.g., multiple droids) SHALL show aggregated counts like "2/4 connected"

  # Requirement: Boot Progress Timing Display
  # The boot progress dialog SHALL display elapsed time and timeout information so the user knows how
  # long the system has been waiting and when it will give up.

  @GBP-005
  Scenario: Elapsed time updates
    When the progress dialog is open
    Then an elapsed time indicator SHALL update at least every second
    And the configured timeout SHALL be displayed alongside the elapsed time

  @GBP-006
  Scenario: Timeout reached
    When the boot timeout expires while bots are still pending
    Then the dialog SHALL display an error message listing the pending bot identities
    And the dialog SHALL offer "Retry" and "Cancel" options

  # Requirement: Boot Progress Cancellation
  # The boot progress dialog SHALL provide a way to cancel the boot process and return to bot selection.

  @GBP-007
  Scenario: User cancels during boot
    When the user clicks "Cancel" on the progress dialog
    Then the progress dialog SHALL close
    And all booted bot processes SHALL be terminated
    And the user SHALL be returned to the New Battle dialog with their bot selection intact

  @GBP-008
  Scenario: User retries after timeout
    When the user clicks "Retry" after a boot timeout
    Then the timeout timer SHALL reset
    And the dialog SHALL continue waiting for pending bots to connect

  # Requirement: Pre-existing Bot Filtering in GUI
  # The boot progress dialog SHALL exclude bots that were already connected to the server before the
  # boot process started, so that only freshly booted bots are tracked.

  @GBP-009
  Scenario: External server with existing bots
    When a battle is started on an external server that has pre-existing bots
    Then the progress dialog SHALL only track bots that were booted for this battle
    And pre-existing bots SHALL NOT appear in the progress display
```
