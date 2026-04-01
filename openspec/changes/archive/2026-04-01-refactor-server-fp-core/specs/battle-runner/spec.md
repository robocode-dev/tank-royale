## MODIFIED Requirements

### Requirement: Synchronous Battle Execution

The system SHALL provide a `runBattle()` method that starts a battle, blocks until completion, and returns structured
results.

The server game engine SHALL separate pure game-logic computation (collision detection, damage calculation, round-over
checks) from state mutation and event dispatch. Each simulation step SHALL produce an explicit outcome value; all state
writes SHALL be deferred to a single apply phase per turn. This ensures the simulation logic is independently testable
and the turn pipeline is auditable at the call site.

#### Scenario: Run battle to completion
- **WHEN** a user calls `runBattle()` with valid bots and configuration
- **THEN** the method SHALL block until all rounds complete
- **AND** the method SHALL return a `BattleResults` object containing per-bot scores and rankings

#### Scenario: Battle execution failure
- **WHEN** a battle cannot complete (e.g., all bots crash)
- **THEN** the method SHALL throw an exception with details about the failure

#### Scenario: Turn pipeline is compute-then-apply
- **WHEN** the server advances one turn of the simulation
- **THEN** all game-logic steps (gun firing, movement, collision detection, inactivity, defeat checks) SHALL compute outcomes without mutating shared game state
- **AND** all outcomes SHALL be applied to the model in a single subsequent phase
- **AND** the external behavior (events, scores, tick data) SHALL remain identical to the pre-refactor implementation
