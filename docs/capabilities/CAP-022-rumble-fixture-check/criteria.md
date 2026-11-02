---
id: CAP-022-criteria
type: criteria
status: draft
links: [CAP-022]
title: Acceptance criteria for CAP-022 (rumble-fixture-check)
ac-prefix: RFX
---

```gherkin
Feature: rumble-fixture-check — Rumble fixture check

  @RFX-001
  Scenario: Test results are grouped into named cases and a case that ran nothing fails
    Test-type: Unit
    Given test result files of the Rumble modules and the results of the Python checks
    When the results are grouped
    Then every check belongs to exactly one case named for what a maintainer cares about, with its counts of passed, failed and skipped checks
    And a case with no checks at all is a failed case with a plain message, and a missing or unreadable result file is a failed case and not an error of the command

  @RFX-002
  Scenario: The machine-readable result states versions, environments, cases and failures
    Test-type: Unit
    Given grouped results
    When the result file is written
    Then it carries a schema version, the Tank Royale version, the engine behavior version, the Java version and the operating system, the name and repository names of each Rumble environment and never an account, one entry per case, and each failure with its test and a message
    And the checks skipped by design are listed as skipped live checks and counted apart from passed checks

  @RFX-003
  Scenario: The human report is built from the same data and says what a green run does not claim
    Test-type: Unit
    Given the same grouped results
    When the report is written
    Then it lists every case with its outcome and every failure, the versions and environments, and where the retained evidence is
    And it ends in every run, green or not, with a plain sentence that fixture success does not claim anything about the sign-in App on a real fork, public installation permissions or a real account, and it names the live checks that were skipped

  @RFX-004
  Scenario: Secrets and machine details are removed from both files automatically
    Test-type: Unit
    Given text holding GitHub token shapes, an authorization header, the value of an environment variable with a secret-sounding name, the user's home folder and name, an email address and absolute paths
    When the text is redacted
    Then none of them remains, repository paths are shown relative to the repository and other absolute paths are replaced
    And a token inside a path, inside an address and inside a stack trace is also removed, and ordinary text is left unchanged

  @RFX-005
  Scenario: The command fails closed when a secret survives redaction
    Test-type: Unit
    Given text that still looks like a secret after redaction, including a long key next to a secret-sounding word of a shape the rules do not know
    When the files are about to be written
    Then nothing is written and the command ends with its own exit code and a message that names the kind of match and never the secret

  @RFX-006
  Scenario: The command never runs when the live switch is on
    Test-type: Unit
    Given the live-check switch is on
    When the command is started
    Then it refuses before any check runs, writes no files and ends with its own exit code, and with the switch off the live checks are skipped and listed as skipped

  @RFX-007
  Scenario: A failed case fails the command and is still fully reported
    Test-type: Unit
    Given results in which one case has a failed check and the others pass
    When the command ends
    Then the exit code is not zero, both files are written, and the failed case, its failed check and its message are in both, while the passing cases are reported as passing

  @RFX-008
  Scenario: The command is documented where Tank Royale's other commands are
    Test-type: Human
    Given the development guide and the participation design
    When a maintainer reads how to run the Rumble fixture check
    Then both name the Gradle task `rumbleFixtureCheck`, say that it needs no network and writes only below the build folder, name the two files it writes, and say what a green run does not claim
```
