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

  @RBC-011 @retired
  Scenario: Moderators distinguish strategic contributions from cosmetic near-duplicates
    Test-type: Human
    Given a submission is reported as a near-duplicate of an existing catalog bot
    When an impartial moderator compares the relevant source and battle strategy and considers the author's explanation
    Then the moderator may reject a near-duplicate whose differences are primarily cosmetic and make no substantial contribution to battle strategy
    And licensed reuse with a substantial strategic contribution is eligible under this rule
    And similar behavior alone is insufficient grounds for rejection
    And the decision identifies the compared entry, evidence and reasoning and allows reconsideration by a moderator without a conflict of interest

  @RBC-012
  Scenario: Rewritten derivatives need a substantial independent strategic contribution
    Test-type: Human
    Given a submission is reported as derived from an existing catalog bot
    When an impartial moderator compares the underlying implementation and strategic decisions using source and battle behavior and considers the author's explanation
    Then the moderator may reject a derivative that retains substantially the same strategy without a substantial independent strategic contribution even if its source is extensively rewritten, reorganized or translated
    And cosmetic changes or small parameter adjustments do not qualify when the underlying strategy remains substantially unchanged
    And licensed reuse with a substantial strategic contribution is eligible under this rule whether or not the bot's score improves
    And similar behavior alone is insufficient evidence of derivation from the compared bot
    And the decision records the compared entry, evidence of derivation and reasoning about the strategic contribution and allows reconsideration by a moderator without a conflict of interest

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

  @RBC-005 @draft
  Scenario: Hourly catalog polling publishes only changed reviewed content
    Test-type: Integration
    Given the normalized local catalog and the reviewed source catalog
    When the scheduled synchronization checks the source
    Then a changed bot or version is stored and regenerated into current rankings
    And identical content is not rewritten or aggregated
    And, when no independent publication state changed, no commit or deployment occurs
    And catalog and result writers cannot overlap their publication sections

  @RBC-006 @draft
  Scenario: TypeScript API runtime peers are distinguished from unrelated bot libraries
    Test-type: Integration
    Given a source-only TypeScript bot declares the official Tank Royale Bot API and its API-declared runtime dependencies
    When the submission validator checks its dependency declarations and committed lockfile where required
    Then the official API and its required runtime peers are accepted
    And an unrelated declared bot library is rejected with an actionable diagnostic
    And generated dependency directories remain outside the submitted bot source tree

  @RBC-007 @retired
  Scenario: Every incoming pull request receives a first moderation decision
    Test-type: Integration
    Given an incoming pull request from any GitHub contributor
    When trusted moderation checks its exact head commit
    Then a single-bot source submission is assessed without giving its code write credentials
    And a team, mixed-file, or non-bot pull request is held for Flemming with the specific reason

  @RBC-010 @draft
  Scenario: Every incoming pull request receives a first decision and exceptions reach the moderator team
    Test-type: Integration
    Given an incoming pull request from any GitHub contributor
    When trusted moderation checks its exact head commit
    Then a single-bot source submission is assessed without giving its code write credentials
    And a team, mixed-file, suspicious, or non-bot pull request stays pending with a reason for the Tank Royale Rumble moderators
    And only an active member of the moderator team may clear a pending moderation status with a recorded reason for that commit
    And a non-member, pending member, or failed membership check cannot clear the status
    And a human-cleared status does not merge the pull request or bypass its required review

  @RBC-008 @draft
  Scenario: A bot must safely complete a real battle before approval
    Test-type: Integration
    Given a bot-only pull request with permitted source, dependencies, and launchers
    When its bot runs for one complete round against a trusted reference in an isolated container
    Then successful source, security, and battle checks approve its exact commit
    And an unstartable or failing bot requests changes with an actionable diagnostic
    And a suspicious source pattern or unavailable test infrastructure is held for human judgment

  @RBC-009 @draft
  Scenario: Only clean bot submissions merge and publish automatically
    Test-type: Integration
    Given a clean moderation verdict and required status for the current pull request head
    When the trusted moderation App merges the bot-only pull request
    Then the post-merge publisher regenerates the catalog from trusted main without running bot source
    And generated catalog files enter main only through a pull request whose exact contents match trusted regeneration and whose head the moderator App approves
    And a newer commit, failing or pending moderation status, or changes outside one bot directory cannot auto-merge
```
