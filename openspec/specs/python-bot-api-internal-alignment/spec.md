# Spec: Python Bot API Internal Alignment

## Purpose

Defines the internal structure requirements for the Python Bot API, aligning it with the Java and
C# implementations. Specifically, this governs how `BotInternals` is organised as a standalone
module, how `BaseBotInternals` holds all mutable state directly (without a `BaseBotInternalData`
indirection layer), and the consequent clean-up of production code and tests.

## Requirements

### Requirement: BotInternals is a standalone module in internal/

The Python Bot API SHALL provide a `BotInternals` class in
`bot-api/python/src/robocode_tank_royale/bot_api/internal/bot_internals.py`.
The class SHALL be public (no leading underscore) and implement `StopResumeListenerABC`,
matching the role of `BotInternals` in Java and C#.

#### Scenario: BotInternals can be imported from internal package
- **WHEN** a consumer imports `from robocode_tank_royale.bot_api.internal.bot_internals import BotInternals`
- **THEN** the import MUST succeed without error

#### Scenario: Bot class uses BotInternals from internal package
- **WHEN** a `Bot` instance is created
- **THEN** its `_bot_internals` attribute SHALL be an instance of the public `BotInternals` class
  from `internal.bot_internals`, not a private class defined inside `bot.py`

#### Scenario: bot.py contains no BotInternals class definition
- **WHEN** `bot.py` is inspected
- **THEN** it SHALL contain no class named `_BotInternals` or `BotInternals`; it SHALL only
  import and instantiate `BotInternals`

---

### Requirement: BaseBotInternals holds all bot state directly

`BaseBotInternals` SHALL hold all mutable bot state as direct instance attributes.
No separate `BaseBotInternalData` class SHALL exist.
The `data` attribute on `BaseBotInternals` SHALL not exist.

#### Scenario: BaseBotInternalData module is deleted
- **WHEN** the Python package is installed or imported
- **THEN** `internal/base_bot_internal_data.py` SHALL NOT exist

#### Scenario: BaseBotInternals exposes bot_intent directly
- **WHEN** a test constructs `BaseBotInternals(mock_bot, None, "ws://localhost", None)`
- **THEN** `internals.bot_intent` SHALL be accessible and return the current `BotIntent`

#### Scenario: BaseBotInternals exposes all formerly-delegated state fields
- **WHEN** `BaseBotInternals` is constructed
- **THEN** all of the following attributes SHALL be accessible directly on the instance:
  `bot_intent`, `my_id`, `teammate_ids`, `game_setup`, `initial_position`,
  `tick_event`, `server_handshake`, `conditions`, `is_stopped`,
  `was_current_event_interrupted`, `recording_stdout`, `recording_stderr`

---

### Requirement: No data. indirection in production code

All production source files in the Python Bot API SHALL access bot state directly on
`BaseBotInternals` — no file SHALL reference `self.data.<field>` or `internals.data.<field>`.

#### Scenario: base_bot.py has no data. accesses
- **WHEN** `base_bot.py` is inspected
- **THEN** it SHALL contain no occurrences of `.data.bot_intent`, `.data.is_stopped`, or any
  other `data.` field access pattern

#### Scenario: event_queue.py takes BaseBotInternals
- **WHEN** `EventQueue` is constructed
- **THEN** its constructor SHALL accept a `BaseBotInternals` instance, not `BaseBotInternalData`

#### Scenario: websocket_handler.py takes BaseBotInternals
- **WHEN** `WebSocketHandler` is constructed
- **THEN** its constructor SHALL accept a `BaseBotInternals` instance, not `BaseBotInternalData`

---

### Requirement: Python-specific test workarounds are removed

All test code that accessed bot state via the `data.` indirection or imported the deleted
`BaseBotInternalData` module SHALL be updated to use direct field access.

#### Scenario: test_shared.py accesses bot_intent directly
- **WHEN** the shared test runner asserts against the bot intent after a method call
- **THEN** it SHALL read `internals.bot_intent` (not `internals.data.bot_intent`)
  and the full shared test suite SHALL pass

#### Scenario: abstract_bot_test.py accesses bot_intent directly
- **WHEN** `abstract_bot_test.py` reads the firepower from the last captured bot intent
- **THEN** it SHALL access `bot._internals.bot_intent.firepower` (not
  `bot._internals.data.bot_intent.firepower`) and the test SHALL pass

#### Scenario: test_imports.py does not assert existence of deleted module
- **WHEN** `test_imports.py` runs
- **THEN** it SHALL NOT attempt to import `base_bot_internal_data` and the test SHALL pass

---

### Requirement: All existing Python tests pass after refactor

The refactor SHALL be a pure structural change with no behavior difference.

#### Scenario: Full Python test suite passes
- **WHEN** `pytest bot-api/python/` is executed after all changes
- **THEN** every test that passed before the refactor SHALL still pass, with no new failures
  and no tests skipped or xfailed that were not already skipped/xfailed before
