---
id: CAP-007-criteria
type: criteria
status: draft
links: [CAP-007]
title: Acceptance criteria for CAP-007 (python-bot-api-internal-alignment)
ac-prefix: PBA
provenance: inferred
---

```gherkin
Feature: python-bot-api-internal-alignment — Defines the internal structure requirements for the Python Bot API, aligning it with the Java and

  # Requirement: BotInternals is a standalone module in internal/
  # The Python Bot API SHALL provide a `BotInternals` class in
  # `bot-api/python/src/robocode_tank_royale/bot_api/internal/bot_internals.py`.
  # The class SHALL be public (no leading underscore) and implement `StopResumeListenerABC`,
  # matching the role of `BotInternals` in Java and C#.

  @PBA-001
  Scenario: BotInternals can be imported from internal package
    When a consumer imports `from robocode_tank_royale.bot_api.internal.bot_internals import BotInternals`
    Then the import MUST succeed without error

  @PBA-002
  Scenario: Bot class uses BotInternals from internal package
    When a `Bot` instance is created
    Then its `_bot_internals` attribute SHALL be an instance of the public `BotInternals` class
    # from `internal.bot_internals`, not a private class defined inside `bot.py`

  @PBA-003
  Scenario: bot.py contains no BotInternals class definition
    When `bot.py` is inspected
    Then it SHALL contain no class named `_BotInternals` or `BotInternals`; it SHALL only
    # import and instantiate `BotInternals`
    # ---

  # Requirement: BaseBotInternals holds all bot state directly
  # `BaseBotInternals` SHALL hold all mutable bot state as direct instance attributes.
  # No separate `BaseBotInternalData` class SHALL exist.
  # The `data` attribute on `BaseBotInternals` SHALL not exist.

  @PBA-004
  Scenario: BaseBotInternalData module is deleted
    When the Python package is installed or imported
    Then `internal/base_bot_internal_data.py` SHALL NOT exist

  @PBA-005
  Scenario: BaseBotInternals exposes bot_intent directly
    When a test constructs `BaseBotInternals(mock_bot, None, "ws://localhost", None)`
    Then `internals.bot_intent` SHALL be accessible and return the current `BotIntent`

  @PBA-006
  Scenario: BaseBotInternals exposes all formerly-delegated state fields
    When `BaseBotInternals` is constructed
    Then all of the following attributes SHALL be accessible directly on the instance:
    # `bot_intent`, `my_id`, `teammate_ids`, `game_setup`, `initial_position`,
    # `tick_event`, `server_handshake`, `conditions`, `is_stopped`,
    # `was_current_event_interrupted`, `recording_stdout`, `recording_stderr`
    # ---

  # Requirement: No data. indirection in production code
  # All production source files in the Python Bot API SHALL access bot state directly on
  # `BaseBotInternals` — no file SHALL reference `self.data.<field>` or `internals.data.<field>`.

  @PBA-007
  Scenario: base_bot.py has no data. accesses
    When `base_bot.py` is inspected
    Then it SHALL contain no occurrences of `.data.bot_intent`, `.data.is_stopped`, or any
    # other `data.` field access pattern

  @PBA-008
  Scenario: event_queue.py takes BaseBotInternals
    When `EventQueue` is constructed
    Then its constructor SHALL accept a `BaseBotInternals` instance, not `BaseBotInternalData`

  @PBA-009
  Scenario: websocket_handler.py takes BaseBotInternals
    When `WebSocketHandler` is constructed
    Then its constructor SHALL accept a `BaseBotInternals` instance, not `BaseBotInternalData`
    # ---

  # Requirement: Python-specific test workarounds are removed
  # All test code that accessed bot state via the `data.` indirection or imported the deleted
  # `BaseBotInternalData` module SHALL be updated to use direct field access.

  @PBA-010
  Scenario: test_shared.py accesses bot_intent directly
    When the shared test runner asserts against the bot intent after a method call
    Then it SHALL read `internals.bot_intent` (not `internals.data.bot_intent`)
    # and the full shared test suite SHALL pass

  @PBA-011
  Scenario: abstract_bot_test.py accesses bot_intent directly
    When `abstract_bot_test.py` reads the firepower from the last captured bot intent
    Then it SHALL access `bot._internals.bot_intent.firepower` (not
    # `bot._internals.data.bot_intent.firepower`) and the test SHALL pass

  @PBA-012
  Scenario: test_imports.py does not assert existence of deleted module
    When `test_imports.py` runs
    Then it SHALL NOT attempt to import `base_bot_internal_data` and the test SHALL pass
    # ---

  # Requirement: All existing Python tests pass after refactor
  # The refactor SHALL be a pure structural change with no behavior difference.

  @PBA-013
  Scenario: Full Python test suite passes
    When `pytest bot-api/python/` is executed after all changes
    Then every test that passed before the refactor SHALL still pass, with no new failures
    # and no tests skipped or xfailed that were not already skipped/xfailed before
```
