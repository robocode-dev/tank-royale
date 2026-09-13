---
id: CAP-015-criteria
type: criteria
status: draft
links: [CAP-015]
title: Acceptance criteria for CAP-015 (rumble-result-data)
ac-prefix: RDA
provenance: inferred
reversal-cost: low
---

```gherkin
Feature: rumble-result-data — Rumble result data

  @RDA-001 @draft
  Scenario: A registered contributor's valid result becomes an immutable fact and derived advice
    Test-type: Integration
    Given a registered forge account submits a labelled batch with a supported engine pin, active cataloged bots, and plausible Battle Runner results
    When the serialized ingestion workflow drains the batch
    Then each valid result is written once as a content-addressed raw fact
    And the leaderboard, pairing statistics, and matches-needed projections are regenerated from the accepted facts

  @RDA-002 @draft
  Scenario: Invalid or duplicate results never become facts
    Test-type: Integration
    Given a submission has a malformed envelope, unregistered client, incompatible engine pin, unknown or disqualified bot, implausible score set, or duplicate battle ID
    When validation processes the submission
    Then the invalid result is rejected with a diagnostic
    And it is absent from the raw facts and every derived projection

  @RDA-003 @draft
  Scenario: Projections remain reproducible after retention and moderation changes
    Test-type: Integration
    Given accepted facts are compacted into a monthly rollup or a current ban, registration, disqualification, or exclusion changes
    When aggregation runs from repository-tracked inputs
    Then it produces the same projection for equivalent facts
    And it excludes facts disallowed by the current moderation and registration records without deleting them

  @RDA-004 @draft
  Scenario: The published dashboard displays generated, versioned leaderboard data
    Test-type: E2E
    Given GitHub Pages is deployed from the static dashboard artifact
    When a visitor selects a ranked game type
    Then the dashboard requests its generated leaderboard projection and links each entry to its generated detail shard
    And it does not require a live application backend

  @RDA-005 @draft
  Scenario: Result eligibility is derived from immutable catalog team membership
    Test-type: Integration
    Given a supported engine pin and a synchronized catalog containing active individual entries and TwinDuel teams with immutable `teamMembers`
    When validation processes a ranked result and aggregation prepares matchmaking advice
    Then `1v1` and `melee` admit only the pinned number of distinct active individual entries
    And `twinduel` admits exactly two distinct active teams whose catalog members are active individuals, expand to the pinned participant count, and have disjoint member identities
    And validation rejects a team in an individual game type, an individual in TwinDuel, or a result whose catalog membership, eligibility, or member disjointness is invalid

  @RDA-006 @draft
  Scenario: APS gives each distinct pairing equal weight within the active ranking epoch
    Test-type: Integration
    Given eligible facts for the current behavior version and the catalog's active bot versions
    When a game type's leaderboard is generated
    Then repeated battles are averaged within their exact participant pairing and APS is 100 times the mean of those pairing averages
    And each pairing has equal weight regardless of its battle count
    And a superseded participant or another behavior version does not contribute, while an active version with no samples has APS zero

  @RDA-007 @draft
  Scenario: Publication freshness changes only when current ranking data changes
    Test-type: Integration
    Given a current leaderboard and its recorded ranking-tree hash and publication time
    When publication regenerates or reconciles the current leaderboard
    Then a changed ranking tree advances `lastUpdatedAt`
    And identical ranking output, deployment, or snapshot creation preserves the previous time

  @RDA-008 @draft
  Scenario: The first writer after a UTC month boundary preserves immutable cumulative history
    Test-type: Integration
    Given the current cumulative leaderboard and its month cursor
    When a serialized writer first runs in a later UTC month
    Then it copies current leaderboard and bot-detail JSON byte for byte for every completed month before accepting new input
    And it never resets the live ranking or alters an existing snapshot

  @RDA-009 @draft
  Scenario: A dashboard viewer can distinguish current rankings from read-only monthly history
    Test-type: E2E
    Given current and archived leaderboard data listed in the publication history manifest
    When a viewer selects Current or a completed month and a game type
    Then the dashboard loads the corresponding leaderboard and bot details
    And it shows the ranking update time and identifies an archived month as read-only
```
