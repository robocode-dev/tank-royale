---
id: CAP-016-criteria
type: criteria
status: draft
links: [CAP-016]
title: Acceptance criteria for CAP-016 (rumble-client)
ac-prefix: RCL
provenance: inferred
reversal-cost: low
---

```gherkin
Feature: rumble-client — Local ranked and practice battle client

  @RCL-001 @draft
  Scenario: Client configuration is validated before side effects
    Test-type: Integration
    Given a client configuration declares its schema version, mode, repository locations, game types, session size, local storage, and a client identity in ranked mode
    When the client starts
    Then it accepts a complete ranked configuration and a complete practice configuration without a client identity
    And it rejects an unknown schema version, unsupported value, missing ranked identity, or unsafe local path before synchronizing, running, journaling, or submitting anything

  @RCL-002 @draft
  Scenario: Ranked synchronization establishes one validated input snapshot
    Test-type: Integration
    Given the configured data repository publishes its canonical pointer, engine pin, synchronized catalog, client registration, and matchmaking advice
    When a ranked session synchronizes
    Then the client follows the canonical location and accepts one mutually consistent snapshot with supported schemas, an active registered client ID, an immutable catalog commit, and advice for every selected game type
    And it refuses ranked execution when a required document is missing, unsupported, inconsistent, or fails its declared source identity

  @RCL-003 @retired
  Scenario: Ranked selection turns published advice into a valid battle
    Test-type: Unit
    Given a validated snapshot contains active bots and matchmaking advice for `1v1`, `twinduel`, or `melee`
    When the client selects a ranked battle with a recorded random seed
    Then it selects the pinned number of distinct active participants, preferring advised pairings involving configured own bots before other high-priority advice
    And it treats advice as non-exclusive and can select a valid fallback battle when no advised pairing is available

  @RCL-010 @draft
  Scenario: Ranked selection turns published advice into a valid executable battle
    Test-type: Unit
    Given a validated snapshot contains active individual bots, active TwinDuel teams with two active members, and matchmaking advice
    When the client selects a ranked `1v1`, `twinduel`, or `melee` battle with a recorded random seed
    Then it selects the pinned number of distinct individual entries or two distinct team entries whose expanded members equal the pinned participant count
    And it prefers advised pairings involving configured own entries before selecting a valid seeded fallback

  @RCL-004 @draft
  Scenario: Ranked and practice modes cannot mix result state
    Test-type: Integration
    Given the client is configured for ranked or practice mode
    When it prepares and runs a battle
    Then ranked mode uses only pinned catalog sources and the pinned behavior version and records every completed result
    And practice mode may use local bot sources but never appends to the ranked journal or invokes a submission transport

  @RCL-005 @draft
  Scenario: A completed ranked battle becomes replay-bound result evidence
    Test-type: Integration
    Given a valid ranked selection and a running server whose behavior version matches the engine pin
    When Battle Runner completes the game type's full configured round count
    Then the client transcribes the complete participant results into the result-data envelope contract and binds the record to a locally retained replay by battle ID and SHA-256 hash
    And an aborted, incomplete, identity-mismatched, or behavior-incompatible battle creates no submittable result

  @RCL-006 @draft
  Scenario: The ranked journal survives submission failures without selective loss
    Test-type: Integration
    Given one or more completed ranked results have been durably appended to the local journal
    When submission succeeds, fails, or is interrupted
    Then it removes only records whose accepted facts were published before their successful ingestion receipts
    And unacknowledged records remain retryable while records from an obsolete behavior-version epoch are quarantined with a clear diagnostic

  @RCL-007 @draft
  Scenario: A registered client submits a bounded issue-ops batch without repository write access
    Test-type: Integration
    Given the ranked journal contains one or more compatible results and the contributor supplies an Issues-only repository credential
    When the client submits the pending results
    Then it creates labelled result issues containing between one and sixty results as required by the result-data contract and correlates each post-publication ingestion receipt to its journal records
    And it neither requests nor uses permission to modify repository contents, branches, releases, packages, Pages, facts, or projections

  @RCL-008 @draft
  Scenario: The client runtime can execute every supported bot platform within its declared boundary
    Test-type: E2E
    Given the pinned Rumble engine and catalog contain Java, .NET, Python, and TypeScript bots
    When a contributor builds the primary container or follows a supported bare-metal setup
    Then the client can boot each platform with the pinned runtime versions and run a battle through Battle Runner
    And the container restricts bot networking to the local server while client egress is limited to repository synchronization and result submission

  @RCL-009 @draft
  Scenario: A ranked result reaches the immutable Rumble facts without manual handling
    Test-type: E2E
    Given a registered contributor starts a ranked session from a clean client installation
    When the client synchronizes, selects and completes a battle, journals its result, and submits it through issue-ops
    Then the result-data ingestion workflow accepts the record as an immutable raw fact and regenerates the affected projections
    And no person copies, edits, or grants repository-content write access to deliver the result
```
