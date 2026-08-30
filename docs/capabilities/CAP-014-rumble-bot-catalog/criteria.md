---
id: CAP-014-criteria
type: criteria
status: draft
links: [CAP-014]
title: Acceptance criteria for CAP-014 (rumble-bot-catalog)
ac-prefix: RBC
provenance: inferred
reversal-cost: low
---

```gherkin
Feature: rumble-bot-catalog — Rumble bot catalog

  @RBC-001 @draft
  Scenario: A valid source-only submission becomes an active catalog entry
    Test-type: Integration
    Given a pull request contains a booter-convention bot directory with an allowed SPDX license and official Bot API dependency
    When the submission validator and source-run smoke check succeed and the pull request is merged
    Then the generated `bots/index.json` contains one active entry with its name, version, platform, path, source hash, owner, and authors
    And the catalog declares its schema version and generating commit

  @RBC-002 @draft
  Scenario: An invalid submission cannot enter the catalog
    Test-type: Integration
    Given a pull request contains a bot with an invalid structure, disallowed executable artifact, missing or disallowed license, unapproved dependency, or failed smoke boot
    When the submission validator runs
    Then the validation check fails with a diagnostic that identifies the violated rule
    And neither the bot nor an altered generated catalog is merged

  @RBC-003 @draft
  Scenario: Ownership and version rules protect the active catalog
    Test-type: Integration
    Given a bot name is already owned and represented by an active catalog entry
    When an unregistered account submits that name or an owner changes its source without increasing its version
    Then validation rejects the submission
    And an approved version increase supersedes the old active version without deleting its historical record

  @RBC-004 @draft
  Scenario: A TwinDuel team is published with immutable member identities
    Test-type: Integration
    Given a valid TwinDuel team entry names exactly two active member bots
    When the generated catalog is synchronized for ranked clients
    Then the team entry carries both member identities in `teamMembers` while individual entries carry an empty list
    And generation rejects a missing, inactive, unknown, or nested team member
```
