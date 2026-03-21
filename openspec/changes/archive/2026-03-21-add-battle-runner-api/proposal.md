# Change: Add Battle Runner API

## Why

Tank Royale lacks a programmatic API for running battles outside the GUI. Classic Robocode provided `robocode.control`
(`RobocodeEngine`) for this purpose. Users need to run battles from code for integration testing, bot development,
tournament systems, education, and benchmarking — without launching the full GUI or manually wiring WebSocket
connections.

## What Changes

- **EXTRACT** game type presets and rule defaults into `lib/common` — currently scattered across `server/rules/setup.kt`
  (constants), `lib/client/model/GameSetup.kt` (client data class), and `gui/settings/GamesSettings.kt` (preset
  definitions). Consolidating into the shared `lib/common` library enables reuse by GUI, Battle Runner, and future
  consumers without duplication.
- **NEW module** `runner` — top-level Gradle module (artifact: `robocode-tankroyale-battle-runner`)
- **NEW capability** `battle-runner` — OpenSpec spec defining requirements for programmatic battle execution
- **NEW public API** published to Maven Central — Java/Kotlin library for running battles from code
- Designed as a **Java-friendly API**: `@JvmStatic` on factory methods, `Consumer<Builder>` overloads alongside
  Kotlin DSL lambdas, `@JvmOverloads` for default parameters (see ADR-0024, Decision 15)
- Orchestrates existing Server (embedded or external) and Booter via their existing protocols
- Connects as both Observer and Controller over WebSocket (per ADR-0007)
- Provides typed configuration for battle rules, bot selection, and event handling
- Returns structured battle results

## Impact

- Affected specs: NEW `battle-runner` capability (no existing specs modified)
- Affected code: New `runner/` module; `settings.gradle.kts` updated to include it
- Related: ADR-0024 (Battle Runner API), ADR-0005 (components), ADR-0007 (roles), ADR-0023 (scope)
- Related change: `refactor-bot-api-test-infrastructure` may benefit from this API once available
