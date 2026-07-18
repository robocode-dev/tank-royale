---
id: CAP-001-criteria
type: criteria
status: draft
links: [CAP-001]
title: Acceptance criteria for CAP-001 (battle-runner)
ac-prefix: BR
provenance: inferred
---

```gherkin
Feature: battle-runner — TBD - created by archiving change add-battle-runner-api. Update Purpose after archive.

  # Requirement: Battle Runner Creation
  # The system SHALL provide a `BattleRunner` entry point that can be instantiated with a builder/DSL configuration
  # specifying server mode (embedded or external) and bot paths.

  @BR-001
  Scenario: Create with embedded server
    When a user creates a `BattleRunner` with embedded server mode
    Then the runner SHALL start a local server on the specified or default port
    And the server SHALL be ready to accept connections before `create` returns

  @BR-002
  Scenario: Create with external server
    When a user creates a `BattleRunner` pointing to an external server URL
    Then the runner SHALL connect to the existing server
    And the runner SHALL fail with a clear error if the server is unreachable

  # Requirement: Bot Selection
  # The system SHALL allow users to specify which bots participate in a battle by providing file system
  # paths to bot directories (containing bot configuration files).

  @BR-003
  Scenario: Select bots by path
    When a user provides a list of bot directory paths
    Then the runner SHALL resolve bot entries from those directories
    And the runner SHALL validate that each path contains a valid bot configuration
    And the runner SHALL pre-read bot identities from the configuration files

  @BR-004
  Scenario: Invalid bot path
    When a user provides a path that does not contain a valid bot
    Then the runner SHALL throw a descriptive error before starting the battle

  @BR-005
  Scenario: Team bot path with missing members
    When a user provides a team directory path where a team member's directory is missing
    Then the runner SHALL throw a descriptive error naming the missing member before starting the battle

  # Requirement: Battle Configuration
  # The system SHALL support game type presets (`classic`, `melee`, `1v1`, `custom`) matching the existing server rule
  # definitions in `dev.robocode.tankroyale.server.rules.setup`. Selecting a preset provides default values for arena
  # dimensions, number of participants, and other game parameters. When using `custom`, all parameters are user-supplied.
  # Individual parameters MAY be overridden on any preset; unset parameters fall back to preset/server defaults.

  @BR-006
  Scenario: Run with classic preset
    When a user runs a battle with game type `classic`
    Then the battle SHALL use 800×600 arena, standard rules, and server defaults for all other parameters

  @BR-007
  Scenario: Run with melee preset
    When a user runs a battle with game type `melee`
    Then the battle SHALL use 1000×1000 arena and require a minimum of 10 participants
    And more than 10 participants SHALL be allowed (RoboRumble standard is exactly 10, but casual play allows more)

  @BR-008
  Scenario: Run with 1v1 preset
    When a user runs a battle with game type `1v1`
    Then the battle SHALL use 800×600 arena and require exactly 2 participants

  @BR-009
  Scenario: Override preset parameters
    When a user selects a preset and overrides specific parameters (e.g., number of rounds)
    Then the overridden parameters SHALL take effect while all other parameters use preset defaults

  @BR-010
  Scenario: Custom game type
    When a user selects game type `custom`
    Then all parameters SHALL be configurable
    And unset parameters SHALL fall back to server default values

  # Requirement: Synchronous Battle Execution
  # The system SHALL provide a `runBattle()` method that starts a battle, blocks until completion, and returns structured
  # results.

  @BR-011
  Scenario: Run battle to completion
    When a user calls `runBattle()` with valid bots and configuration
    Then the method SHALL block until all rounds complete
    And the method SHALL return a `BattleResults` object containing per-bot scores and rankings

  @BR-012
  Scenario: Battle execution failure
    When a battle cannot complete (e.g., all bots crash)
    Then the method SHALL throw an exception with details about the failure

  # Requirement: Event Observation
  # The `BattleRunner` SHALL provide an `observer` property that returns an Observer object. The Observer SHALL deliver
  # real-time battle events (turn ended, round ended, bot death, etc.) to registered listeners using the Event<T> system.
  # This maps to the Observer WebSocket role defined in ADR-0007.

  @BR-013
  Scenario: Observe turn events
    When a user registers a turn-ended listener on `runner.observer` before starting a battle
    Then the listener SHALL be invoked for every turn with the full observer-view tick event

  @BR-014
  Scenario: Observe battle results
    When a user registers a battle-ended listener on `runner.observer`
    Then the listener SHALL be invoked when the battle completes with final results

  # Requirement: Battle Control
  # The `BattleRunner` SHALL provide a `controller` property that returns a Controller object. The Controller SHALL
  # provide methods to start, pause, resume, and stop a battle, mapping to the Controller WebSocket role defined in
  # ADR-0007. TPS control is intentionally excluded — battles run at maximum speed (TPS = -1) by default, as there is
  # no GUI rendering to pace.

  @BR-015
  Scenario: Start a battle
    When a user calls `controller.startBattle()` with a game type preset
    Then the battle SHALL start with the preset's default rules

  @BR-016
  Scenario: Pause and resume
    When a user calls `controller.pause()` during a running battle
    Then the battle SHALL stop advancing turns
    And calling `controller.resume()` SHALL continue the battle from where it paused

  @BR-017
  Scenario: Stop battle early
    When a user calls `controller.stop()` during a running battle
    Then the battle SHALL end immediately
    And partial results SHALL be returned if available

  # Requirement: Max-Speed Execution
  # The system SHALL configure the server with TPS = -1 (unlimited) by default, running battles as fast as the hardware
  # allows without artificial throttling.

  @BR-018
  Scenario: Default TPS is max speed
    When a user runs a battle without specifying TPS
    Then the server SHALL run at TPS = -1 (maximum speed)

  # Requirement: Bot State Inspection
  # The Observer SHALL deliver full bot state data each turn, including position, speed, direction, energy, gun heat, turn
  # rates, and debug output (stdOut/stdErr). This enables verification of bot behavior and debugging of custom Bot API
  # implementations by observing the *outcomes* of bot intents.

  @BR-019
  Scenario: Inspect bot states per turn
    When a user registers a turn-ended listener on `runner.observer`
    Then the listener SHALL receive the full bot state for every bot each turn, including position, speed,
    # direction, energy, turn rates, and radar sweep

  @BR-020
  Scenario: Debug output from bots
    When a bot writes to stdOut or stdErr during a turn
    Then the observer tick event SHALL include the debug output for that bot

  # Requirement: Intent Diagnostics
  # The `BattleRunner` SHALL support optional intent diagnostics via a transparent WebSocket proxy. When enabled, the proxy
  # sits between bots and server, capturing raw `bot-intent` messages per bot per turn without requiring any server or
  # observer protocol changes. Intent data is stored in memory for the current battle and is accessible after battle
  # completion (similar to battle results). This works for all Bot APIs regardless of programming language, since it
  # operates at the wire protocol level.

  @BR-021
  Scenario: Enable intent diagnostics
    When a user enables intent diagnostics via `enableIntentDiagnostics()` in the builder
    Then the Battle Runner SHALL start a WebSocket proxy that bots connect to instead of the server directly
    And the `SERVER_URL` environment variable for bots SHALL point to the proxy

  @BR-022
  Scenario: Capture intents per bot
    When a bot sends a `bot-intent` message during a battle with diagnostics enabled
    Then the proxy SHALL capture and store the intent data (turn rate, target speed, firepower, etc.) per bot per turn

  @BR-023
  Scenario: Access captured intents after battle
    When a battle completes with diagnostics enabled
    Then the user SHALL be able to query captured intents per bot via `runner.intentDiagnostics`
    And intent data SHALL include the turn number and all intent fields

  @BR-024
  Scenario: Diagnostics disabled by default
    When a user runs a battle without enabling intent diagnostics
    Then no WebSocket proxy SHALL be started and bots SHALL connect directly to the server

  # Requirement: Battle Recording
  # The `BattleRunner` SHALL support optional battle recording using the same GZIP ND-JSON format as the existing Recorder
  # module. This enables replay, post-mortem analysis, and archival of programmatic battle runs.

  @BR-025
  Scenario: Record a battle
    When a user enables recording before starting a battle
    Then all observer events (game started, ticks, round ended, game ended) SHALL be written to a `.battle.gz` file

  @BR-026
  Scenario: Recording disabled by default
    When a user runs a battle without enabling recording
    Then no recording file SHALL be created

  # Requirement: Lifecycle Management
  # The system SHALL implement `AutoCloseable` and provide a `close()` method that performs orderly shutdown of all
  # managed resources (server, bot processes, WebSocket connections).

  @BR-027
  Scenario: Clean shutdown
    When a user calls `close()` on a `BattleRunner`
    Then all bot processes SHALL be terminated
    And the embedded server (if used) SHALL be stopped
    And all WebSocket connections SHALL be closed

  @BR-028
  Scenario: Shutdown on JVM exit
    When the JVM exits without explicit `close()`
    Then a shutdown hook SHALL attempt to clean up bot processes and server

  # Requirement: Maven Central Publication
  # The Battle Runner API SHALL be published to Maven Central as artifact `dev.robocode.tankroyale:robocode-tankroyale-battle-runner` so that any JVM project can depend on it.

  @BR-029
  Scenario: Depend via Gradle
    When a user adds `implementation("dev.robocode.tankroyale:robocode-tankroyale-battle-runner:VERSION")` to their build
    Then the library and its transitive dependencies SHALL resolve correctly from Maven Central

  # Requirement: Java Client Compatibility
  # The Battle Runner API SHALL be usable from pure Java 11+ code without requiring knowledge of Kotlin language features
  # or the Kotlin standard library API.

  @BR-030
  Scenario: Static factory access from Java
    When a Java client calls `BattleRunner.create()` or `BattleSetup.classic()`
    Then the methods SHALL be accessible as static methods (via `@JvmStatic`)
    And Java clients SHALL NOT need to go through `Companion` objects

  @BR-031
  Scenario: Builder configuration from Java
    When a Java client configures a `BattleSetup` or `BattleRunner` via a builder
    Then `Consumer<Builder>` overloads SHALL be available alongside Kotlin DSL lambdas
    And Java clients SHALL NOT need to return `Unit.INSTANCE` from builder lambdas
    And Kotlin DSL lambda overloads SHALL be hidden from Java (via `@JvmSynthetic`) to prevent overload ambiguity

  @BR-032
  Scenario: No-arg factory access from Java
    When a Java client calls `BattleRunner.create()`, `BattleSetup.classic()`, or similar factories without parameters
    Then explicit no-arg overloads SHALL be available that return default configurations

  @BR-033
  Scenario: Default parameter overloads for Java
    When a builder method has Kotlin default parameters (e.g., `embeddedServer(port = 0)`)
    Then a no-arg overload SHALL be generated (via `@JvmOverloads`) for Java callers

  # Requirement: Identity-Based Bot Matching
  # The Battle Runner SHALL match booted bot processes to their connected bot instances using a multiset
  # of `(name, version)` identities read from bot configuration files, rather than counting the total
  # number of connected bots.

  @BR-034
  Scenario: Two distinct bots connect successfully
    When a battle is started with two bot directories containing different `(name, version)` pairs
    Then the runner SHALL wait until both specific identities appear in `BotListUpdate`
    And the runner SHALL return the `BotAddress` for each matched identity

  @BR-035
  Scenario: Same bot directory used twice (duplicate identity)
    When a battle is started with the same bot directory listed twice
    Then the runner SHALL expect two connections with the same `(name, version)`
    And the runner SHALL wait until two distinct `BotAddress` entries with that identity appear

  @BR-036
  Scenario: Team directory expands to member identities
    When a battle is started with a team directory containing `teamMembers` in its JSON config
    Then the runner SHALL read each member's `bot.json` from sibling directories
    And the expected identity multiset SHALL contain one entry per team member (including duplicates)

  @BR-037
  Scenario: Stray bot on external server is ignored
    When a battle is started on an external server that has pre-existing bots with different identities
    Then the stray bots SHALL NOT count toward the expected identity match
    And the runner SHALL wait for the correct identities to connect

  @BR-038
  Scenario: Pre-existing bots are filtered
    When a battle is started on an external server with bots already connected before boot
    Then the pre-existing bots SHALL be excluded from identity matching
    And only freshly connected bots SHALL be matched

  @BR-039
  Scenario: Bot config file missing or malformed
    When a bot directory lacks a valid JSON config file or the file is missing `name`/`version`
    Then the runner SHALL throw a `BattleException` with a descriptive message before booting

  @BR-040
  Scenario: Team member directory missing
    When a team config references a member whose directory does not exist
    Then the runner SHALL throw a `BattleException` naming the missing member and expected path

  # Requirement: Configurable Boot Timeout
  # The Battle Runner builder SHALL accept a `botConnectTimeout(Duration)` option that controls how long
  # `waitForBots()` waits for all expected identities to connect. The default SHALL remain 30 seconds
  # for backward compatibility.

  @BR-041
  Scenario: Custom timeout via builder
    When a user creates a `BattleRunner` with `botConnectTimeout(Duration.ofSeconds(120))`
    Then the runner SHALL wait up to 120 seconds for bots to connect

  @BR-042
  Scenario: Default timeout preserved
    When a user creates a `BattleRunner` without specifying `botConnectTimeout`
    Then the runner SHALL use the default 30-second timeout

  @BR-043
  Scenario: Timeout with identity-aware error
    When the boot timeout expires before all expected identities connect
    Then the runner SHALL throw a `BattleException` listing which identities are still pending

  # Requirement: Boot Progress Reporting
  # The Battle Runner SHALL report boot progress with identity information so callers can display which
  # bots have connected and which are still pending.

  @BR-044
  Scenario: Progress event on bot connection
    When a `BotListUpdate` arrives during the boot wait phase
    Then the runner SHALL fire an `onBootProgress` event with connected and pending identity maps

  @BR-045
  Scenario: Periodic progress during wait
    When the runner is waiting for bots to connect
    Then the runner SHALL fire `onBootProgress` periodically (at most every 500ms) with elapsed
    # time and timeout remaining

  @BR-046
  Scenario: Progress includes timing
    When an `onBootProgress` event fires
    Then it SHALL include `elapsedMs` (time since boot started) and `timeoutMs` (configured timeout)
```
