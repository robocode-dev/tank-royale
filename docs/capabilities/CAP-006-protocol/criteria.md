---
id: CAP-006-criteria
type: criteria
status: draft
links: [CAP-006]
title: Acceptance criteria for CAP-006 (protocol)
ac-prefix: PRO
provenance: inferred
---

```gherkin
Feature: protocol — WebSocket protocol

  # Requirement: Bot Connection Lifecycle
  # The protocol SHALL support a defined bot connection lifecycle, including handshaking and session identification.

  @PRO-001
  Scenario: Bot joins the server
    When a Bot opens a WebSocket connection
    Then the Server SHALL send a `server-handshake` containing a `session-id`
    And the Bot SHALL respond with a `bot-handshake` containing that `session-id`

  # Requirement: Game Start Synchronization
  # The protocol SHALL ensure all participating bots are ready before starting a turn-based battle.

  @PRO-002
  Scenario: All bots become ready
    When the Server sends `game-started-event-for-bot` to all selected bots
    And all bots respond with `bot-ready` within the `ready-timeout`
    Then the Server SHALL transition to `GAME_RUNNING` state

  # Requirement: Turn-Based Main Loop
  # The protocol SHALL advance the game in discrete turns (ticks), where each turn requires a reactive exchange of state and
  # intent.

  @PRO-003
  Scenario: Running next turn
    When the Server state is `GAME_RUNNING`
    Then the Server SHALL send a `tick-event-for-bot` to each bot
    And each Bot SHOULD respond with a `bot-intent` before the turn timeout
    And the Server SHALL advance the physics simulation based on received intents

  # Requirement: Language Agnostic Schemas
  # All protocol messages SHALL follow the JSON schemas defined in `schema/schemas/`, ensuring cross-language compatibility
  # for all Bot APIs.

  @PRO-004
  Scenario: Schema validation
    When a message is sent or received by any client or server
    Then it MUST validate against its corresponding `.schema.yaml` definition
```
