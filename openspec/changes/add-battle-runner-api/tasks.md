## 1. Extract Shared Rules to lib/common
- [x] 1.1 Move game rule default constants from `server/rules/setup.kt` to `lib/common`
- [x] 1.2 Create game type preset definitions in `lib/common` (currently only in `gui/settings/GamesSettings.kt`)
- [x] 1.3 Update server to import rule defaults from `lib/common` instead of its own `setup.kt`
- [x] 1.4 Update GUI (`GamesSettings.kt`) to use shared preset definitions from `lib/common`
- [x] 1.5 Ensure `lib/client` GameSetup remains compatible with shared definitions
- [x] 1.6 Verify server and GUI build and tests pass after extraction

## 2. Module Setup
- [x] 2.1 Create `runner/` directory with `build.gradle.kts`
- [x] 2.2 Add `runner` to `settings.gradle.kts`
- [x] 2.3 Configure Maven Central publishing (nexus-publish)
- [x] 2.4 Add dependencies on server, booter, and common libraries
- [x] 2.5 Add `copyServerJar` and `copyBooterJar` Gradle tasks (same pattern as `gui/build.gradle.kts`)
- [x] 2.6 Configure fat JAR to include embedded server and booter JARs as classpath resources
- [x] 2.7 Reuse `ResourceUtil` extraction logic for runtime JAR extraction to temp files

## 3. Core API
- [x] 3.1 Implement `BattleRunner` main entry point (builder pattern)
- [x] 3.2 Implement `BattleSetup` configuration (game type presets from `lib/common`, with overrides)
- [x] 3.3 Implement `BotEntry` for selecting bots by path or class
- [x] 3.4 Implement `BattleResults` structured result type

## 4. Server Management
- [x] 4.1 Implement embedded server startup: extract bundled server JAR from classpath resource to temp file,
        launch via `java -jar <server.jar> --port=<port> --controller-secrets=<secret> --bot-secrets=<secret>`,
        register `deleteOnExit()` for the temp file
- [x] 4.2 Support dynamic port allocation: when user specifies port 0 (or omits port), pick an available port
        (e.g. bind a `ServerSocket(0)`, read its port, close it, then pass to server `--port`)
- [x] 4.3 Implement server readiness detection: after process start, attempt WebSocket connection with retries
        (e.g. 10 attempts, 500ms interval) — server is ready when handshake (`server-handshake`) is received
- [x] 4.4 Implement external server connection mode: skip process startup, connect directly to user-provided URL,
        validate reachability by completing the `server-handshake` exchange
- [x] 4.5 Implement server shutdown: send `quit` to process stdin, wait up to 2s for graceful exit, then
        `destroyForcibly()` if still alive; clean up temp JAR file
- [x] 4.6 Implement server reuse across battles: keep server process alive between `runBattle()` calls on the
        same `BattleRunner` instance; only stop server on `close()`
- [x] 4.7 Generate and manage controller secret: create random secret at startup, pass to server via
        `--controller-secrets`, use same secret for Observer and Controller handshakes (`session-id` + `secret`)
- [x] 4.8 Configure server for max-speed: always pass `--tps=-1` to embedded server (unlimited turns per second)

## 5. Booter Integration
- [x] 5.1 Implement Booter process startup: extract bundled booter JAR from classpath resource to temp file,
        launch via `java -Dserver.url=<url> -Dserver.secret=<secret> -jar <booter.jar> boot <bot_dirs...>`,
        set `SERVER_URL` to proxy address when intent diagnostics enabled (otherwise to server address)
- [x] 5.2 Implement stdout parsing: read `<pid>;<bot_directory>` lines to track booted bot processes,
        detect `stopped <pid>` and `lost <pid>` status messages
- [x] 5.3 Implement stdin commands: send `boot <directory>` for additional bots, `stop <pid>` for individual
        bot termination, `quit` for full Booter shutdown
- [x] 5.4 Implement bot path resolution and validation: verify each bot directory contains a valid bot
        configuration file before passing to Booter
- [x] 5.5 Implement bot process cleanup on battle end or failure: send `quit` to Booter stdin, wait for
        graceful shutdown, then `destroyForcibly()` if Booter process still alive; track PIDs for cleanup

## 6. WebSocket Client
- [x] 6.1 Implement Observer connection: open WebSocket to server, receive `server-handshake` (extract
        `session-id`), send `observer-handshake` (with `session-id` + `secret`), receive `bot-list-update`
- [x] 6.2 Implement Controller connection: open WebSocket to server, receive `server-handshake` (extract
        `session-id`), send `controller-handshake` (with `session-id` + `secret`), receive `bot-list-update`
- [x] 6.3 Implement `controller.startBattle()`: send `start-game` message with selected bot IDs and game setup,
        wait for `game-started-event-for-observer` (confirms enough bots sent `bot-ready`), handle
        `game-aborted-event` if insufficient participants
- [x] 6.4 Implement `controller.stop()`: send `stop-game`, await `game-aborted-event` confirmation
- [x] 6.5 Implement `controller.pause()`: send `pause-game`, await `game-paused-event-for-observers`
- [x] 6.6 Implement `controller.resume()`: send `resume-game`, await `game-resumed-event-for-observers`
- [x] 6.7 Implement `controller.nextTurn()`: send `next-turn` while paused (single-step debugging)
- [x] 6.8 Implement event deserialization and delivery via Event<T> system: deserialize
        `tick-event-for-observer`, `round-started-event`, `round-ended-event`, `game-ended-event-for-observer`,
        `game-aborted-event`, `game-paused-event-for-observers`, `game-resumed-event-for-observers`,
        `tps-changed-event`, `bot-list-update` into typed Kotlin objects and fire via Event<T> listeners
- [x] 6.9 Implement `BattleResults` extraction from `game-ended-event-for-observer` results array:
        map each entry to typed result object (rank, survival, bulletDamage, ramDamage, killBonuses, totalScore)

## 7. Intent Diagnostics (WebSocket Proxy)
- [x] 7.1 Implement transparent WebSocket proxy that forwards all messages between bots and server
- [x] 7.2 Capture `bot-intent` messages per bot per turn in memory
- [x] 7.3 Set `SERVER_URL` env var to proxy address when diagnostics enabled
- [x] 7.4 Expose `intentDiagnostics` API for querying captured intents per bot after battle
- [x] 7.5 Ensure proxy is opt-in and not started when diagnostics are disabled

## 8. Battle Orchestration
- [x] 8.1 Implement synchronous `runBattle()` flow:
        1. Ensure server is running and Observer+Controller are connected
        2. Send bot directories to Booter, wait for all bots to connect (detected via `bot-list-update`)
        3. Send `start-game` via Controller with selected bot IDs and `GameSetup`
        4. Wait for `game-started-event-for-observer` (battle running) or `game-aborted-event` (failed)
        5. Block until `game-ended-event-for-observer` — extract and return `BattleResults`
        6. Send `quit` to Booter to stop bot processes; keep server alive for next battle
- [x] 8.2 Implement async battle execution: non-blocking variant that fires Event<T> callbacks for each
        protocol event; user controls flow via `controller` object
- [x] 8.3 Implement multi-battle support: `runBattle()` can be called repeatedly on same `BattleRunner`;
        between battles, stop bot processes via Booter, restart Booter with new bot selection if needed,
        reuse server and WebSocket connections
- [x] 8.4 Implement graceful shutdown (`close()`): send `quit` to Booter, send `stop-game` if battle
        running, close Observer and Controller WebSocket connections, stop embedded server process,
        register JVM shutdown hook as fallback
- [x] 8.5 Implement error handling: timeout if `game-started-event-for-observer` not received within
        configurable deadline, detect `game-aborted-event`, handle Booter/server process crashes,
        propagate meaningful exceptions to caller

## 9. Battle Recording
- [x] 9.1 Extract `GameRecorder` class from `recorder` module into `lib/common` (GZIP ND-JSON file writer)
- [x] 9.2 Update `recorder` module to depend on extracted `GameRecorder` from `lib/common`
- [x] 9.3 Wire Battle Runner's observer events to `GameRecorder` when recording is enabled
- [x] 9.4 Recording disabled by default; enabled via builder config with output path

## 10. Testing
- [x] 10.1 Unit tests for configuration and result types
- [x] 10.2 Integration tests — run a real battle with sample bots, assert on results
- [x] 10.3 Test embedded vs external server modes
- [x] 10.4 Test error scenarios (bot crash, server unavailable, timeout)
- [x] 10.5 Test battle recording produces valid .battle.gz files
- [x] 10.6 Test intent diagnostics captures correct bot-intent data

## 11. Documentation
- [x] 11.1 Write `runner/README.md` with usage examples
- [x] 11.2 Add KDoc/Javadoc to all public API types
- [x] 11.3 Update root `README.md` to mention Battle Runner API
- [ ] 11.4 Update `CHANGELOG.md` when releasing
- [x] 11.5 Add API reference to generated Javadoc site
- [x] 11.6 Add VitePress documentation page (`docs-build/docs/api/battle-runner.md`) with dual Kotlin/Java examples and sidebar entry in `config.mts`
- [x] 11.7 Update C4 Container View (`docs-internal/architecture/c4-views/container.md`) to include Runner
        in the containers table, Mermaid diagrams, and Embedded Mode section
- [x] 11.8 Add Runner Components (L3) view (`docs-internal/architecture/c4-views/runner-components.md`)
        describing ServerManager, BooterManager, WebSocket client, and orchestration internals
