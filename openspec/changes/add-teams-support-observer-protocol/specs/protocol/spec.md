## ADDED Requirements

### Requirement: Observer Team Result Identification

The protocol SHALL provide explicit identification of team vs. bot results in observer messages to enable clear result display and leaderboard generation.

#### Scenario: Team battle results for observers

- **WHEN** a battle with teams ends
- **THEN** the `game-ended-event-for-observer` SHALL include an `isTeam: boolean` field for each result
- **AND** results representing teams SHALL have `isTeam: true`
- **AND** results representing individual bots SHALL have `isTeam: false`

#### Scenario: Mixed team and bot battle results

- **WHEN** a battle has both teams and solo bots
- **THEN** each result in the observer event SHALL have the appropriate `isTeam` value
- **AND** observers can distinguish team achievements from individual bot achievements

## MODIFIED Requirements

### Requirement: Observer Result Ranking

The protocol SHALL provide consistent sequential rank values (1..N) for all results in observer messages, regardless of team or bot composition.

#### Scenario: Sequential rank assignment

- **WHEN** battle results are generated for observers
- **THEN** rank values SHALL be sequential integers starting from 1
- **AND** no rank value SHALL be 0 or duplicated
- **AND** rank order SHALL reflect the actual battle placement

#### Scenario: Team vs bot ranking consistency

- **WHEN** teams and solo bots compete in the same battle
- **THEN** both teams and bots SHALL receive proper sequential rank values
- **AND** rank assignment SHALL follow consistent placement logic
