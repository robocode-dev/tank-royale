---
id: CAP-021-criteria
type: criteria
status: draft
links: [CAP-021]
title: Acceptance criteria for CAP-021 (rumble-battle-worker)
ac-prefix: RBW
---

```gherkin
Feature: rumble-battle-worker — Rumble battle worker

  @RBW-001
  Scenario: A job and every worker event travel as one versioned message per line
    Test-type: Unit
    Given a job and each kind of worker event
    When each is written as a line and read back
    Then the message read equals the message written and carries its schema version
    And a message is exactly one line with no line break inside it

  @RBW-002
  Scenario: A message the reader does not understand is refused whole and in plain words
    Test-type: Unit
    Given a line with a schema version the reader does not know, a line that is not valid, a line of an unknown kind and a line missing a required field
    When each is read
    Then each is refused with a message that says what was wrong and, for an unknown version, which version was read and which are understood
    And no partial message is returned

  @RBW-003
  Scenario: The worker runs the job it was given and reports it in order
    Test-type: Unit
    Given a valid job on the worker's standard input and an engine that completes the battle
    When the worker runs
    Then it writes a started event, then one result event with every bot's rank and score components, then a finished event, all on standard output
    And it exits with the code for finished, and writes nothing else to standard output

  @RBW-004
  Scenario: A job that cannot be run is refused or fails with a stable code, never silently
    Test-type: Unit
    Given a job with an unknown schema version, a job that is not valid and a job whose battle the engine fails
    When the worker runs
    Then the first two end with a refused event and the exit code for refused, and the third ends with a failed event carrying a stable code and a plain message and the exit code for failed
    And the outcome can be read from the events and the exit code alone, without any diagnostic text

  @RBW-005
  Scenario: A job whose engine pin differs from the running server is refused before any bot starts
    Test-type: Unit
    Given a job that pins a behavior version
    When the worker prepares the battle
    Then the engine is asked to require exactly that behavior version
    And a mismatch reported by the engine ends the run with a refused event before any bot process is started

  @RBW-006
  Scenario: The worker cannot reach credentials by construction
    Test-type: Unit
    Given the worker's project dependencies and its source
    When the build checks its dependency rules
    Then the worker depends on the battle runner and the message module only, and not on the shared Rumble library, the bot project library, the desktop program or any GitHub code
    And its source names no token, user home folder or container socket, and a deliberate violation makes the check fail

  @RBW-007
  Scenario: A real battle through the worker gives a ranked result for every bot
    Test-type: Integration
    Given a job for a one-round battle with two real bots and the pin of the server in use
    When the worker runs with the real battle runner
    Then the result event ranks both bots, the finished event follows and the exit code is the one for finished
```
