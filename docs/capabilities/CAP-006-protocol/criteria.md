---
id: CAP-006-criteria
type: criteria
status: draft
links: [CAP-006]
title: Acceptance criteria for CAP-006 (protocol)
ac-prefix: PRO
provenance: inferred
reversal-cost: low
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
    Test-type: Integration
    When a message is sent or received by any client or server
    Then it MUST validate against its corresponding `.schema.yaml` definition

  # Requirement: Behavior Compatibility Metadata
  # The server SHALL expose the server-owned behavior epoch independently of the release version.

  @PRO-005
  Scenario: Server advertises the behavior compatibility version
    Test-type: Integration
    When a client opens a WebSocket connection to a current Tank Royale server
    Then the `server-handshake` SHALL contain a positive integer `behaviorVersion`
    And the value SHALL identify the game-observable compatibility epoch
    And a handshake that omits the field SHALL remain readable by a compatibility client

  # Bot team messages support ordered payload batches under explicit per-turn count and byte limits.

  @PRO-006
  Scenario: Deliver a compatible team-message batch
    Test-type: Integration
    Given a sender and its teammates advertise batch protocol version 1
    When the sender submits a broadcast or directed batch with up to 128 logical payloads
    And the complete intent uses at most 64 packets, 49,152 UTF-8 bytes per encoded packet, and 262,144 UTF-8 bytes for its compact `teamMessages` array
    Then each intended recipient SHALL receive one team-message event on the next turn
    And the event SHALL contain all batch entries in send order

  @PRO-007
  Scenario: Reject an invalid team-message intent atomically
    Test-type: Unit
    Given a bot submits an empty batch, a null entry, a malformed packet, or a message over a count or UTF-8 byte limit
    When the server validates the complete intent
    Then the server SHALL reject the intent without delivering any message from it

  @PRO-009
  Scenario: Keep the sender connected when a directed receiver has left the game
    Test-type: Unit
    Given a bot's teammate was listed in its game-started event and has since disconnected
    When the bot sends a directed team message or batch to that teammate
    Then the server SHALL NOT close the sender's connection
    And the server SHALL NOT deliver the message
    And a receiver that was not a teammate at game start SHALL still be rejected as a policy violation

  @PRO-008
  Scenario: Reject an oversized incoming WebSocket text message before parsing
    Test-type: Unit
    When a client sends a text message larger than 1,048,576 UTF-8 bytes
    Then the server SHALL close the connection with a message-too-big status before JSON parsing

  # Requirement: Team Bot Identity
  # The server SHALL provide team bots with their own and teammate names without exposing opponent names.

  @PRO-010
  Scenario: Server assigns battle names to a bot and its teammates
    Test-type: Integration
    Given a start-game request contains bot addresses in battle order
    And participants advertise a name and an optional version
    When the server sends `game-started-event-for-bot`
    Then `botNames` SHALL contain the bot's own id and every teammate id
    And each name SHALL combine the advertised `teamMemberName`, or the display name when `teamMemberName` is absent or blank, with its optional version
    And repeated full names SHALL receive ` (n)` suffixes numbered from 1 in battle order across the full roster

  @PRO-010a
  Scenario: Server hides opponent names from bot events
    Test-type: Integration
    Given a participant is not the bot itself or one of its teammates
    When the server sends `game-started-event-for-bot`
    Then `botNames` SHALL NOT contain that participant's id

  # Requirement: Team Member Identity Metadata
  # Each official Bot API SHALL send the optional unbounded teamMemberName unchanged in bot-handshake.

  @PRO-011
  Scenario: Bot API sends the configured team member name
    Test-type: Integration
    Given BotInfo has a `teamMemberName` longer than 30 characters
    When the Bot API sends `bot-handshake`
    Then `teamMemberName` SHALL preserve the configured value exactly

  @PRO-011a
  Scenario: Bot API omits an unset team member name
    Test-type: Integration
    Given BotInfo has no `teamMemberName`
    When the Bot API sends `bot-handshake`
    Then `teamMemberName` SHALL be omitted

  # Requirement: Bot Name Lookup
  # Every official Bot API SHALL expose the same lookup behavior for the name map supplied at game start.

  @PRO-012
  Scenario: Bot API resolves its own and teammate names
    Test-type: Integration
    Given `game-started-event-for-bot` contains `botNames` entries for the bot and its teammates
    When a bot calls `getBotName(botId)` for one of those ids
    Then Java, .NET, Python, and TypeScript APIs SHALL return the same corresponding name

  @PRO-012a
  Scenario: Bot API returns null for an id without a name
    Test-type: Integration
    Given `game-started-event-for-bot` has been received
    When a bot calls `getBotName(botId)` for an unknown id or an opponent id omitted from the map
    Then Java, .NET, Python, and TypeScript APIs SHALL return null or the language-equivalent `None`
```
