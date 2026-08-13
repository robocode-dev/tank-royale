--- id: CAP-015-criteria type: criteria status: draft links: [CAP-015] title: Acceptance criteria for CAP-015 (rumble-result-data) ac-prefix: RDA provenance: inferred reversal-cost: low ---

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
```
