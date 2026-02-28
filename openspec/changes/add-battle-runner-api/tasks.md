## 1. Extract Shared Rules to lib/common
- [x] 1.1 Move game rule default constants from `server/rules/setup.kt` to `lib/common`
- [x] 1.2 Create game type preset definitions in `lib/common` (currently only in `gui/settings/GamesSettings.kt`)
- [x] 1.3 Update server to import rule defaults from `lib/common` instead of its own `setup.kt`
- [x] 1.4 Update GUI (`GamesSettings.kt`) to use shared preset definitions from `lib/common`
- [x] 1.5 Ensure `lib/client` GameSetup remains compatible with shared definitions
- [x] 1.6 Verify server and GUI build and tests pass after extraction

## 2. Module Setup
- [ ] 2.1 Create `runner/` directory with `build.gradle.kts`
- [ ] 2.2 Add `runner` to `settings.gradle.kts`
- [ ] 2.3 Configure Maven Central publishing (nexus-publish)
- [ ] 2.4 Add dependencies on server, booter, and common libraries
- [ ] 2.5 Add `copyServerJar` and `copyBooterJar` Gradle tasks (same pattern as `gui/build.gradle.kts`)
- [ ] 2.6 Configure fat JAR to include embedded server and booter JARs as classpath resources
- [ ] 2.7 Reuse `ResourceUtil` extraction logic for runtime JAR extraction to temp files

## 3. Core API
- [ ] 3.1 Implement `BattleRunner` main entry point (builder pattern)
- [ ] 3.2 Implement `BattleSetup` configuration (game type presets from `lib/common`, with overrides)
- [ ] 3.3 Implement `BotEntry` for selecting bots by path or class
- [ ] 3.4 Implement `BattleResults` structured result type

## 4. Server Management
- [ ] 4.1 Implement embedded server startup (in-process)
- [ ] 4.2 Implement external server connection mode
- [ ] 4.3 Implement server lifecycle management (start, stop, health check)

## 5. Booter Integration
- [ ] 5.1 Implement Booter process spawning via stdin/stdout protocol
- [ ] 5.2 Implement bot path resolution and validation
- [ ] 5.3 Implement bot process cleanup on battle end or failure

## 6. WebSocket Client
- [ ] 6.1 Implement Observer connection and handshake
- [ ] 6.2 Implement Controller connection and handshake
- [ ] 6.3 Implement battle control commands (start, stop, pause, resume)
- [ ] 6.4 Implement event deserialization and delivery via Event<T> system

## 7. Intent Diagnostics (WebSocket Proxy)
- [ ] 7.1 Implement transparent WebSocket proxy that forwards all messages between bots and server
- [ ] 7.2 Capture `bot-intent` messages per bot per turn in memory
- [ ] 7.3 Set `SERVER_URL` env var to proxy address when diagnostics enabled
- [ ] 7.4 Expose `intentDiagnostics` API for querying captured intents per bot after battle
- [ ] 7.5 Ensure proxy is opt-in and not started when diagnostics are disabled

## 8. Battle Orchestration
- [ ] 8.1 Implement synchronous `runBattle()` — block until results available
- [ ] 8.2 Implement async battle execution with event callbacks
- [ ] 8.3 Implement graceful shutdown (close server, kill bot processes, close WebSocket)
- [ ] 8.4 Implement error handling and timeout management

## 9. Battle Recording
- [ ] 9.1 Extract `GameRecorder` class from `recorder` module into `lib/common` (GZIP ND-JSON file writer)
- [ ] 9.2 Update `recorder` module to depend on extracted `GameRecorder` from `lib/common`
- [ ] 9.3 Wire Battle Runner's observer events to `GameRecorder` when recording is enabled
- [ ] 9.4 Recording disabled by default; enabled via builder config with output path

## 10. Testing
- [ ] 10.1 Unit tests for configuration and result types
- [ ] 10.2 Integration tests — run a real battle with sample bots, assert on results
- [ ] 10.3 Test embedded vs external server modes
- [ ] 10.4 Test error scenarios (bot crash, server unavailable, timeout)
- [ ] 10.5 Test battle recording produces valid .battle.gz files
- [ ] 10.6 Test intent diagnostics captures correct bot-intent data

## 11. Documentation
- [ ] 11.1 Write `runner/README.md` with usage examples
- [ ] 11.2 Add KDoc/Javadoc to all public API types
- [ ] 11.3 Update root `README.md` to mention Battle Runner API
- [ ] 10.4 Update `VERSIONS.md` when releasing
