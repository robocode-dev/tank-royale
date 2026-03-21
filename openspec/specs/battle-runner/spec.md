# battle-runner Specification

## Purpose
TBD - created by archiving change add-battle-runner-api. Update Purpose after archive.
## Requirements
### Requirement: Battle Runner Creation

The system SHALL provide a `BattleRunner` entry point that can be instantiated with a builder/DSL configuration
specifying server mode (embedded or external) and bot paths.

#### Scenario: Create with embedded server
- **WHEN** a user creates a `BattleRunner` with embedded server mode
- **THEN** the runner SHALL start a local server on the specified or default port
- **AND** the server SHALL be ready to accept connections before `create` returns

#### Scenario: Create with external server
- **WHEN** a user creates a `BattleRunner` pointing to an external server URL
- **THEN** the runner SHALL connect to the existing server
- **AND** the runner SHALL fail with a clear error if the server is unreachable

### Requirement: Bot Selection

The system SHALL allow users to specify which bots participate in a battle by providing file system
paths to bot directories (containing bot configuration files).

#### Scenario: Select bots by path
- **WHEN** a user provides a list of bot directory paths
- **THEN** the runner SHALL resolve bot entries from those directories
- **AND** the runner SHALL validate that each path contains a valid bot configuration
- **AND** the runner SHALL pre-read bot identities from the configuration files

#### Scenario: Invalid bot path
- **WHEN** a user provides a path that does not contain a valid bot
- **THEN** the runner SHALL throw a descriptive error before starting the battle

#### Scenario: Team bot path with missing members
- **WHEN** a user provides a team directory path where a team member's directory is missing
- **THEN** the runner SHALL throw a descriptive error naming the missing member before starting the battle

### Requirement: Battle Configuration

The system SHALL support game type presets (`classic`, `melee`, `1v1`, `custom`) matching the existing server rule
definitions in `dev.robocode.tankroyale.server.rules.setup`. Selecting a preset provides default values for arena
dimensions, number of participants, and other game parameters. When using `custom`, all parameters are user-supplied.
Individual parameters MAY be overridden on any preset; unset parameters fall back to preset/server defaults.

#### Scenario: Run with classic preset
- **WHEN** a user runs a battle with game type `classic`
- **THEN** the battle SHALL use 800×600 arena, standard rules, and server defaults for all other parameters

#### Scenario: Run with melee preset
- **WHEN** a user runs a battle with game type `melee`
- **THEN** the battle SHALL use 1000×1000 arena and require a minimum of 10 participants
- **AND** more than 10 participants SHALL be allowed (RoboRumble standard is exactly 10, but casual play allows more)

#### Scenario: Run with 1v1 preset
- **WHEN** a user runs a battle with game type `1v1`
- **THEN** the battle SHALL use 800×600 arena and require exactly 2 participants

#### Scenario: Override preset parameters
- **WHEN** a user selects a preset and overrides specific parameters (e.g., number of rounds)
- **THEN** the overridden parameters SHALL take effect while all other parameters use preset defaults

#### Scenario: Custom game type
- **WHEN** a user selects game type `custom`
- **THEN** all parameters SHALL be configurable
- **AND** unset parameters SHALL fall back to server default values

### Requirement: Synchronous Battle Execution

The system SHALL provide a `runBattle()` method that starts a battle, blocks until completion, and returns structured
results.

#### Scenario: Run battle to completion
- **WHEN** a user calls `runBattle()` with valid bots and configuration
- **THEN** the method SHALL block until all rounds complete
- **AND** the method SHALL return a `BattleResults` object containing per-bot scores and rankings

#### Scenario: Battle execution failure
- **WHEN** a battle cannot complete (e.g., all bots crash)
- **THEN** the method SHALL throw an exception with details about the failure

### Requirement: Event Observation

The `BattleRunner` SHALL provide an `observer` property that returns an Observer object. The Observer SHALL deliver
real-time battle events (turn ended, round ended, bot death, etc.) to registered listeners using the Event<T> system.
This maps to the Observer WebSocket role defined in ADR-0007.

#### Scenario: Observe turn events
- **WHEN** a user registers a turn-ended listener on `runner.observer` before starting a battle
- **THEN** the listener SHALL be invoked for every turn with the full observer-view tick event

#### Scenario: Observe battle results
- **WHEN** a user registers a battle-ended listener on `runner.observer`
- **THEN** the listener SHALL be invoked when the battle completes with final results

### Requirement: Battle Control

The `BattleRunner` SHALL provide a `controller` property that returns a Controller object. The Controller SHALL
provide methods to start, pause, resume, and stop a battle, mapping to the Controller WebSocket role defined in
ADR-0007. TPS control is intentionally excluded — battles run at maximum speed (TPS = -1) by default, as there is
no GUI rendering to pace.

#### Scenario: Start a battle
- **WHEN** a user calls `controller.startBattle()` with a game type preset
- **THEN** the battle SHALL start with the preset's default rules

#### Scenario: Pause and resume
- **WHEN** a user calls `controller.pause()` during a running battle
- **THEN** the battle SHALL stop advancing turns
- **AND** calling `controller.resume()` SHALL continue the battle from where it paused

#### Scenario: Stop battle early
- **WHEN** a user calls `controller.stop()` during a running battle
- **THEN** the battle SHALL end immediately
- **AND** partial results SHALL be returned if available

### Requirement: Max-Speed Execution

The system SHALL configure the server with TPS = -1 (unlimited) by default, running battles as fast as the hardware
allows without artificial throttling.

#### Scenario: Default TPS is max speed
- **WHEN** a user runs a battle without specifying TPS
- **THEN** the server SHALL run at TPS = -1 (maximum speed)

### Requirement: Bot State Inspection

The Observer SHALL deliver full bot state data each turn, including position, speed, direction, energy, gun heat, turn
rates, and debug output (stdOut/stdErr). This enables verification of bot behavior and debugging of custom Bot API
implementations by observing the *outcomes* of bot intents.

#### Scenario: Inspect bot states per turn
- **WHEN** a user registers a turn-ended listener on `runner.observer`
- **THEN** the listener SHALL receive the full bot state for every bot each turn, including position, speed,
  direction, energy, turn rates, and radar sweep

#### Scenario: Debug output from bots
- **WHEN** a bot writes to stdOut or stdErr during a turn
- **THEN** the observer tick event SHALL include the debug output for that bot

### Requirement: Intent Diagnostics

The `BattleRunner` SHALL support optional intent diagnostics via a transparent WebSocket proxy. When enabled, the proxy
sits between bots and server, capturing raw `bot-intent` messages per bot per turn without requiring any server or
observer protocol changes. Intent data is stored in memory for the current battle and is accessible after battle
completion (similar to battle results). This works for all Bot APIs regardless of programming language, since it
operates at the wire protocol level.

#### Scenario: Enable intent diagnostics
- **WHEN** a user enables intent diagnostics via `enableIntentDiagnostics()` in the builder
- **THEN** the Battle Runner SHALL start a WebSocket proxy that bots connect to instead of the server directly
- **AND** the `SERVER_URL` environment variable for bots SHALL point to the proxy

#### Scenario: Capture intents per bot
- **WHEN** a bot sends a `bot-intent` message during a battle with diagnostics enabled
- **THEN** the proxy SHALL capture and store the intent data (turn rate, target speed, firepower, etc.) per bot per turn

#### Scenario: Access captured intents after battle
- **WHEN** a battle completes with diagnostics enabled
- **THEN** the user SHALL be able to query captured intents per bot via `runner.intentDiagnostics`
- **AND** intent data SHALL include the turn number and all intent fields

#### Scenario: Diagnostics disabled by default
- **WHEN** a user runs a battle without enabling intent diagnostics
- **THEN** no WebSocket proxy SHALL be started and bots SHALL connect directly to the server

### Requirement: Battle Recording

The `BattleRunner` SHALL support optional battle recording using the same GZIP ND-JSON format as the existing Recorder
module. This enables replay, post-mortem analysis, and archival of programmatic battle runs.

#### Scenario: Record a battle
- **WHEN** a user enables recording before starting a battle
- **THEN** all observer events (game started, ticks, round ended, game ended) SHALL be written to a `.battle.gz` file

#### Scenario: Recording disabled by default
- **WHEN** a user runs a battle without enabling recording
- **THEN** no recording file SHALL be created

### Requirement: Lifecycle Management

The system SHALL implement `AutoCloseable` and provide a `close()` method that performs orderly shutdown of all
managed resources (server, bot processes, WebSocket connections).

#### Scenario: Clean shutdown
- **WHEN** a user calls `close()` on a `BattleRunner`
- **THEN** all bot processes SHALL be terminated
- **AND** the embedded server (if used) SHALL be stopped
- **AND** all WebSocket connections SHALL be closed

#### Scenario: Shutdown on JVM exit
- **WHEN** the JVM exits without explicit `close()`
- **THEN** a shutdown hook SHALL attempt to clean up bot processes and server

### Requirement: Maven Central Publication

The Battle Runner API SHALL be published to Maven Central as artifact `dev.robocode.tankroyale:robocode-tankroyale-battle-runner` so that any JVM project can depend on it.

#### Scenario: Depend via Gradle
- **WHEN** a user adds `implementation("dev.robocode.tankroyale:robocode-tankroyale-battle-runner:VERSION")` to their build
- **THEN** the library and its transitive dependencies SHALL resolve correctly from Maven Central

### Requirement: Java Client Compatibility

The Battle Runner API SHALL be usable from pure Java 11+ code without requiring knowledge of Kotlin language features
or the Kotlin standard library API.

#### Scenario: Static factory access from Java
- **WHEN** a Java client calls `BattleRunner.create()` or `BattleSetup.classic()`
- **THEN** the methods SHALL be accessible as static methods (via `@JvmStatic`)
- **AND** Java clients SHALL NOT need to go through `Companion` objects

#### Scenario: Builder configuration from Java
- **WHEN** a Java client configures a `BattleSetup` or `BattleRunner` via a builder
- **THEN** `Consumer<Builder>` overloads SHALL be available alongside Kotlin DSL lambdas
- **AND** Java clients SHALL NOT need to return `Unit.INSTANCE` from builder lambdas
- **AND** Kotlin DSL lambda overloads SHALL be hidden from Java (via `@JvmSynthetic`) to prevent overload ambiguity

#### Scenario: No-arg factory access from Java
- **WHEN** a Java client calls `BattleRunner.create()`, `BattleSetup.classic()`, or similar factories without parameters
- **THEN** explicit no-arg overloads SHALL be available that return default configurations

#### Scenario: Default parameter overloads for Java
- **WHEN** a builder method has Kotlin default parameters (e.g., `embeddedServer(port = 0)`)
- **THEN** a no-arg overload SHALL be generated (via `@JvmOverloads`) for Java callers

### Requirement: Identity-Based Bot Matching

The Battle Runner SHALL match booted bot processes to their connected bot instances using a multiset
of `(name, version)` identities read from bot configuration files, rather than counting the total
number of connected bots.

#### Scenario: Two distinct bots connect successfully
- **WHEN** a battle is started with two bot directories containing different `(name, version)` pairs
- **THEN** the runner SHALL wait until both specific identities appear in `BotListUpdate`
- **AND** the runner SHALL return the `BotAddress` for each matched identity

#### Scenario: Same bot directory used twice (duplicate identity)
- **WHEN** a battle is started with the same bot directory listed twice
- **THEN** the runner SHALL expect two connections with the same `(name, version)`
- **AND** the runner SHALL wait until two distinct `BotAddress` entries with that identity appear

#### Scenario: Team directory expands to member identities
- **WHEN** a battle is started with a team directory containing `teamMembers` in its JSON config
- **THEN** the runner SHALL read each member's `bot.json` from sibling directories
- **AND** the expected identity multiset SHALL contain one entry per team member (including duplicates)

#### Scenario: Stray bot on external server is ignored
- **WHEN** a battle is started on an external server that has pre-existing bots with different identities
- **THEN** the stray bots SHALL NOT count toward the expected identity match
- **AND** the runner SHALL wait for the correct identities to connect

#### Scenario: Pre-existing bots are filtered
- **WHEN** a battle is started on an external server with bots already connected before boot
- **THEN** the pre-existing bots SHALL be excluded from identity matching
- **AND** only freshly connected bots SHALL be matched

#### Scenario: Bot config file missing or malformed
- **WHEN** a bot directory lacks a valid JSON config file or the file is missing `name`/`version`
- **THEN** the runner SHALL throw a `BattleException` with a descriptive message before booting

#### Scenario: Team member directory missing
- **WHEN** a team config references a member whose directory does not exist
- **THEN** the runner SHALL throw a `BattleException` naming the missing member and expected path

### Requirement: Configurable Boot Timeout

The Battle Runner builder SHALL accept a `botConnectTimeout(Duration)` option that controls how long
`waitForBots()` waits for all expected identities to connect. The default SHALL remain 30 seconds
for backward compatibility.

#### Scenario: Custom timeout via builder
- **WHEN** a user creates a `BattleRunner` with `botConnectTimeout(Duration.ofSeconds(120))`
- **THEN** the runner SHALL wait up to 120 seconds for bots to connect

#### Scenario: Default timeout preserved
- **WHEN** a user creates a `BattleRunner` without specifying `botConnectTimeout`
- **THEN** the runner SHALL use the default 30-second timeout

#### Scenario: Timeout with identity-aware error
- **WHEN** the boot timeout expires before all expected identities connect
- **THEN** the runner SHALL throw a `BattleException` listing which identities are still pending

### Requirement: Boot Progress Reporting

The Battle Runner SHALL report boot progress with identity information so callers can display which
bots have connected and which are still pending.

#### Scenario: Progress event on bot connection
- **WHEN** a `BotListUpdate` arrives during the boot wait phase
- **THEN** the runner SHALL fire an `onBootProgress` event with connected and pending identity maps

#### Scenario: Periodic progress during wait
- **WHEN** the runner is waiting for bots to connect
- **THEN** the runner SHALL fire `onBootProgress` periodically (at most every 500ms) with elapsed
  time and timeout remaining

#### Scenario: Progress includes timing
- **WHEN** an `onBootProgress` event fires
- **THEN** it SHALL include `elapsedMs` (time since boot started) and `timeoutMs` (configured timeout)

