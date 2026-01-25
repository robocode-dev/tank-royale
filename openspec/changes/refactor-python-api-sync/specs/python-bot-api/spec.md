## ADDED Requirements

### Requirement: Synchronous Public API

The Python Bot API SHALL provide a synchronous (blocking) public interface that matches the Java and C# reference
implementations exactly.

All public methods that perform game actions (movement, turning, firing) SHALL block until the action is complete,
matching the behavior of the corresponding Java methods.

#### Scenario: Bot run loop is synchronous

- **WHEN** a user creates a bot class extending `Bot`
- **AND** implements the `run()` method
- **THEN** the `run()` method SHALL be a regular synchronous method (not async)
- **AND** blocking methods like `forward()`, `turn_left()`, `fire()` SHALL block until complete

#### Scenario: Event handlers are synchronous

- **WHEN** a user implements an event handler like `on_scanned_bot()`
- **THEN** the handler SHALL be a regular synchronous method (not async)
- **AND** the handler MAY call blocking methods like `fire()` which will block until complete

#### Scenario: Bot startup is synchronous

- **WHEN** a user calls `bot.start()`
- **THEN** the method SHALL block until the game ends or the bot disconnects
- **AND** no `asyncio.run()` wrapper SHALL be required

### Requirement: Internal Async I/O

The Python Bot API SHALL use async I/O internally for WebSocket communication, but this SHALL be hidden from users.

#### Scenario: WebSocket runs in background thread

- **WHEN** `bot.start()` is called
- **THEN** a background daemon thread SHALL be created to run the async WebSocket event loop
- **AND** the main thread SHALL block until the game ends

#### Scenario: Thread synchronization matches Java

- **WHEN** a blocking method like `forward()` is called
- **THEN** it SHALL use `threading.Condition` to wait for completion
- **AND** the pattern SHALL match Java's `wait()`/`notifyAll()` synchronization

### Requirement: 1:1 Semantic Equivalence with Java

The Python Bot API public methods SHALL have identical semantics to the corresponding Java Bot API methods.

#### Scenario: Method signatures match Java (with Python naming conventions)

- **GIVEN** Java method `void forward(double distance)`
- **THEN** Python SHALL have `def forward(self, distance: float) -> None`
- **AND** the method SHALL block until `distance_remaining == 0` and `speed == 0`

#### Scenario: Docstrings match Javadoc

- **GIVEN** a public method in the Python Bot API
- **THEN** its docstring SHALL match the corresponding Javadoc in the Java Bot API
- **AND** SHALL use Python conventions (Args/Returns instead of @param/@return)

### Requirement: Sample Bots Match Cross-Platform Pattern

All Python sample bots SHALL follow the same structure as their Java and C# counterparts.

#### Scenario: SpinBot structure matches Java

- **GIVEN** the Java SpinBot sample
- **WHEN** comparing to the Python SpinBot
- **THEN** the structure SHALL be equivalent (accounting for language syntax differences)
- **AND** no `async`/`await` keywords SHALL be present
- **AND** the main entry point SHALL be `SpinBot().start()`

### Requirement: Tests Use Standard unittest

All Python Bot API tests SHALL use standard synchronous `unittest.TestCase`, not `IsolatedAsyncioTestCase`.

#### Scenario: Tests do not require async infrastructure

- **WHEN** running `pytest tests/bot_api/`
- **THEN** no tests SHALL use `IsolatedAsyncioTestCase`
- **AND** no tests SHALL be skipped due to async-related hangs
- **AND** all tests SHALL pass without timeouts
