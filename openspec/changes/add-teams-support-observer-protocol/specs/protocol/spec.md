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

The protocol SHALL use **competition ranking** (1224 style) for all results in observer messages, consistent with classic Robocode: tied scores share the same rank, and subsequent ranks skip positions equal to the number of ties.

#### Scenario: Sequential rank assignment

- **WHEN** battle results are generated for observers
- **THEN** rank values SHALL start from 1
- **AND** participants with equal scores SHALL receive the same rank
- **AND** the next rank after a tie SHALL skip positions (e.g., two 1st places → next is 3rd)

#### Scenario: Competition ranking example

- **GIVEN** participants with scores: Bot1=370, Team1=370, Bot3=230, Bot4=230, Bot5=220
- **WHEN** ranks are assigned
- **THEN** ranks SHALL be: Bot1=1, Team1=1, Bot3=3, Bot4=3, Bot5=5

#### Scenario: All equal scores

- **WHEN** all participants have equal scores
- **THEN** all participants SHALL receive rank 1
