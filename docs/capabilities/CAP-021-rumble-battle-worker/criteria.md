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

  @RBW-008
  Scenario: The worker starts in a clean environment built from an allowlist
    Test-type: Unit
    Given a host environment holding a token, a home folder, a container engine address and ordinary variables
    When the worker's environment is built
    Then it holds only the allowlisted variables that exist in the host environment, with its own empty working folder and temporary folder
    And no variable outside the allowlist is passed, whatever its name, and the worker's home folder setting points at its own empty folder

  @RBW-009
  Scenario: A real child process cannot read the host's secrets through its environment
    Test-type: Unit
    Given a recognisable fake token and a fake home folder placed in the test's own environment
    When a real worker process is started by the host and asked to report what it can see
    Then the fake token, the fake home folder and every variable outside the allowlist are absent from what the process reports
    And a process the worker starts itself sees the same clean environment

  @RBW-010
  Scenario: The host runs one job and returns one typed outcome for every way a worker can end
    Test-type: Unit
    Given a job and a worker that finishes, refuses, fails, ends without a final event, prints something that is not an event, or cannot be started
    When the host runs the job
    Then the outcome is the result, a refusal with its code, a failure with its code, the worker ending without a final event, unreadable output or the worker being unable to start, each in plain words
    And the outcome is decided from the events and the exit code alone, never from the worker's diagnostic output

  @RBW-011
  Scenario: Output and line ceilings stop a worker that says too much
    Test-type: Unit
    Given a worker that writes more output than the ceiling allows, a worker that writes one line longer than the line ceiling, and a worker that fills its diagnostic output
    When the host runs the job
    Then the first two are stopped and reported as exceeding a limit, and no result is returned from them
    And the diagnostic output is read and discarded within a small bound so that it can never block the worker or grow without limit

  @RBW-012
  Scenario: The time limit stops the whole process tree
    Test-type: Unit
    Given a worker that starts a long-running child process and then does not finish
    When the total time allowed for the battle has passed
    Then the host reports a timeout and no process the worker started is left running
    And a worker that finishes in time is not stopped

  @RBW-013
  Scenario: The usual worker command is built from the shared Java helper with a memory limit
    Test-type: Unit
    Given the jar of the worker, a heap limit and a folder for the worker
    When the command is built
    Then it runs the Java program with the heap limit, the worker's home and temporary folder settings and the jar, followed by the worker argument
    And a caller may pass any other command and folder, which the host then uses unchanged

  @RBW-014
  Scenario: The documentation says what the clean environment does not protect against
    Test-type: Human
    Given the participation design and this capability's design notes
    When a maintainer reads what the worker boundary protects
    Then they state plainly that a bot running as the participant's own operating system user can still read files by their full path, including the saved sign-in file, and use a container engine socket whose path it knows
    And they say that only a container or a separate operating system account would stop that, and that a passing test of the clean environment is not a promise of more

  @RBW-015
  Scenario: A real battle through the host with sample bots gives a ranked result in a clean environment
    Test-type: Integration
    Given the real worker started by the host with two sample bots and the pin of the server in use
    When the host runs a one-round job
    Then the outcome is the ranked result of both bots and no process of the worker is left running afterwards
```
