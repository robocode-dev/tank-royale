### Tank Royale Bot API — WebAssembly (Kotlin/Wasm) Implementation Plan

This plan defines a dependency-ordered roadmap to implement the WebAssembly (Wasm) Bot API using Kotlin Multiplatform (wasmJs), mirroring the Java Bot API semantics and public surface. It is intended to be executed incrementally and to be consumable by LLM agents for small, focused tasks.

Guiding principles
- Java is the reference implementation for semantics, naming, defaults, timing, and error messages.
- Keep changes minimal and focused. Do not break protocol or public API stability.
- Maintain cross-language parity and test naming per tests/TEST-MATRIX.md.
- Prefer immutability, explicit nullability, and clear error messages.

Build/runtime context
- Module: bot-api/wasm (kotlin { wasmJs { nodejs() } })
- Test runtime: Node.js (via kotlin-wasm-js test tasks)
- Wire format: JSON per /schema, identical to Java behavior

High-level milestones (dependency-ordered)
1) Module bootstrap and conventions (DONE)
   - Build works with wasmJs + Node (see wasm/build.gradle.kts).
   - Establish package naming to mirror Java logically (kotlin idioms allowed).
2) Inventory & gap analysis (PRIORITY)
   - List existing Kotlin/Wasm classes and tests already present in bot-api/wasm.
   - Cross-check full Java API surface to build a parity checklist.
3) Core foundations
   - Shared constants, math helpers, geometry primitives used widely.
   - JSON serialization infrastructure and schema DTO base classes.
4) Value objects & constants (VAL)
   - BotInfo, InitialPosition, Color, Point, constants (battlefield, min/max values).
5) Utilities (UTL)
   - ColorUtil (RGB↔HSV), JsonUtil, CountryCode.
6) Graphics API (GFX)
   - IGraphics contract and SvgGraphics implementation with state handling.
7) Protocol DTOs and mappers (PRT)
   - All incoming/outgoing message DTOs, schema compatibility, unknown-field handling.
8) Lifecycle & configuration (BOT)
   - BaseBot configuration source precedence and ENV in Node/Wasm.
   - Connect/Disconnect, Start/Stop/Pause/Resume semantics.
9) Commands & intents (CMD)
   - Movement, Fire, Radar/Scan, Graphics frame emission timing.
10) Ticks & state (TCK)
   - Immutable per-tick snapshots, intent apply/reset, timeouts.
11) Events (EVT)
   - Event model and dispatch order per Java semantics.
12) Internals (INT)
   - Mapping/validation helpers and event dispatch loop
13) Documentation & versioning
   - Update README, VERSIONS.MD; parity checklist maintained.

Testing strategy
- Mirror TEST-MATRIX.md IDs exactly; organize tests by subject/scenario.
- Use Node-hosted mock server/harness analogous to Java/.NET to feed handshake, ticks, and capture intents.
- Round-trip JSON tests for DTOs; schema validation against /schema examples where feasible.

Detailed task breakdown (with dependencies and deliverables)

0. Conventions and structure (depends on 1)
- Packages
  - api value objects and commands: `dev.robocode.tankroyale.api` (align with Java naming; adjust if repo uses different base)
  - graphics: `dev.robocode.tankroyale.api.gfx`
  - proto DTOs: `dev.robocode.tankroyale.api.proto`
  - internal: `dev.robocode.tankroyale.api.internal`
- Directory layout
  - src/wasmJsMain/kotlin
  - src/wasmJsTest/kotlin
- Coding
  - data classes for value objects; prefer val; validate in init blocks.
  - Use kotlin.Result or sealed types internally; public API mirrors Java exceptions/returns.

1. Inventory & parity checklist (start here)
- Output: docs file bot-api/wasm/PARITY-CHECKLIST.md listing all public types/methods and test IDs.
- Steps
  - Enumerate Java public API (value objects, enums, interfaces, classes, commands, events, DTOs).
  - Mark which ones already exist in wasm; link or note file names.
  - Record gaps to drive subsequent tasks.
- Acceptance
  - Checklist covers 100% of Java public API surface.

2. Core foundations (base for most layers)
- Implement math/geometry utility if used across modules (angles, normalization).
- Implement common constants file.
- Implement Json serialization base (Kotlinx.serialization for wasm). Configure encoders to match Java formatting (field names, null handling, unknown fields). Enable ignoreUnknown on input but preserve pass-through where needed.
- Tests
  - Basic JSON round-trip for a simple DTO stub (use a tiny example).

3. VAL — Value objects and constants
- BotInfo
  - Fields, defaults, validation (TR-API-VAL-001/002)
  - Test: required fields, invalid values, getter parity
- InitialPosition + InitialPositionMapper
  - Defaults and mapping round-trip (VAL-003/004)
- Constants integrity (VAL-005)
- Graphics Color and Point (VAL-006/007), including fromHex/toHex, clamping, equality/hash.

4. UTL — Utilities
- ColorUtil conversions (UTL-001): numerical tolerance tests
- JsonUtil (UTL-002): canonical formatting and schema compliance for API DTOs
- CountryCode (UTL-003): ISO normalization and errors

5. GFX — Graphics API
- IGraphics contract: primitives (moveTo, lineTo, curve, rect, circle, text, style setters)
- SvgGraphics implementation producing SVG fragments and handling style state and resets (GFX-001..004)
- Tests compare canonical SVG strings per action sequence

6. PRT — Protocol & schema DTOs and mapping
- Define all outgoing intent and incoming event/setup/message DTOs (mirror Java names and fields)
- Serializer configuration: stable field order if required by tests; ignore unknown fields on input; ensure unknowns preserved where pass-through applies
- Tests
  - PRT-001/002/003 per TEST-MATRIX: validate against /schema samples; round-trip; unknown fields behavior

7. BOT — Lifecycle and configuration
- BaseBot constructor and configuration precedence (TR-API-BOT-001a..001d)
  - Wasm/Node: explicit args > ENV (no Java System properties)
  - ENV name mapping identical to Java keys
  - Validation and deterministic error messages
- Connect/Disconnect (BOT-002)
- Start/Stop/Pause/Resume primitives and flags (BOT-003)
- Error handling to safe state (BOT-004)
- Tests use MockedServer harness to simulate server

8. CMD — Commands
- Movement command parameters and bounds (CMD-001)
- Fire (CMD-002): power bounds and cooldown if applicable
- Radar/Scan (CMD-003)
- Graphics frame emission/reset timing (CMD-004)
- Tests assert exact next-tick intent payloads

9. TCK — Ticks and state
- Immutable snapshots exposed to consumers (TCK-001)
- Intent apply/reset timing (TCK-002)
- Timeouts behavior (TCK-003)

10. EVT — Events
- GameStartedEvent, RoundStartedEvent, ScannedBotEvent, SkippedTurnEvent, etc.
- Event dispatch order and interruption semantics (EVT-001..005)
- Tests via harness with controlled inputs

11. INT — Internals
- Mapping/validation helpers tested via public effects or reflection-like techniques available in Kotlin/wasm tests
- Event dispatch loop tested with spy/observer hooks

12. Documentation & release hygiene
- Update README sections describing the new wasm API and how to run tests: `./gradlew :bot-api:wasm:test`
- Update VERSIONS.MD with wasm milestone entries
- Keep cross-language docs synchronized

Node/Wasm environment specifics
- ENV access in Node: use js("process.env") via Kotlin/JS interop or a small shim to read env vars during tests.
- Timers/scheduling: avoid real timers for tests; drive ticks from harness.
- Networking: Prefer a test harness using in-process message queues/fixtures instead of sockets; production path can use WebSocket bindings if/when required (ensure abstraction so tests do not depend on network).

MockedServer & harness plan (shared test scaffolding)
- Implement WasmMockedServer with the following capabilities:
  - serve handshake/setup payloads from fixtures
  - queue incoming events per tick
  - capture outgoing intents per tick
  - simulate disconnects and protocol errors
- Provide helper assertions to compare captured intents to canonical JSON
- Reuse JSON fixtures from /schema when possible

Test organization (wasmJsTest)
- Follow naming by subject: BotInfoTest, InitialPositionTest, SvgGraphicsTest, BaseBotConstructorTest, CommandIntentTest, EventDispatchTest, TickStateTest, ProtocolRoundTripTest, JsonUtilTest, ColorUtilTest, CountryCodeTest
- Method names: test_TR_API_<AREA>_<NNN>_<short_description>() with @DisplayName annotations if supported; otherwise include ID in method name and doc comment.

Acceptance criteria per area (summary)
- VAL: All 7 checklist items green
- UTL: 3 items green
- GFX: 4 items green with SVG canonical strings
- BOT: 5 items (001a..001d plus others) green under Node env
- CMD: 4 items green
- EVT: 5 items green
- TCK: 3 items green
- PRT: 3 items green
- INT: 2 items green

Cross-language parity checklist (to maintain in PARITY-CHECKLIST.md)
- For each public class/interface/enum, record:
  - Java FQN, Wasm/Kotlin FQN, status (Done/Partial/TODO), test IDs covering it
  - Notes on semantic equivalence and any idiomatic differences

Task ticket template for LLM agents
- Title: [Area] Short task name (e.g., VAL: Implement Color value object)
- Inputs
  - Java reference files/classes
  - This plan + TEST-MATRIX.md
  - Any existing wasm files to modify
- Outputs
  - Kotlin/Wasm implementation files
  - Unit tests with TEST-MATRIX IDs
  - Update to PARITY-CHECKLIST.md
- Steps
  1) Review Java reference behavior and docs
  2) Implement Kotlin data class/logic mirroring behavior
  3) Add/extend tests named per matrix; include edge cases and error messages
  4) Run: `./gradlew :bot-api:wasm:test`
  5) Ensure no public API or protocol semantics are altered
- Acceptance
  - Tests pass and match semantics; code follows conventions; no protocol breakage

Milestone sequencing with dependencies
1. Inventory & parity checklist (enables accurate scope)
2. Core JSON + constants foundation (unblocks most others)
3. VAL + UTL (low risk, many downstream deps)
4. GFX (depends on Color/Point)
5. Protocol DTOs (depends on JSON foundation)
6. MockedServer harness (unblocks BOT/CMD/EVT/TCK tests)
7. BOT lifecycle & config (depends on harness + DTOs)
8. CMD intents (depends on DTOs + BaseBot)
9. TCK semantics (depends on BOT + CMD)
10. EVT suite (depends on DTOs + dispatch implementation)
11. INT internals (depends on dispatch)
12. Docs & versioning

Run commands
- Build tests: `./gradlew :bot-api:wasm:test`
- Run wasm Node entrypoint (if needed for manual runs): `./gradlew :bot-api:wasm:run`

Notes and risks
- Schema compatibility is critical: prefer additive changes if any divergence is found; do not change field meanings.
- ENV access in Node must be carefully abstracted to allow deterministic tests.
- Avoid browser-only APIs; target Node-only for now (per build config).

Definition of Done for the Wasm API
- All TEST-MATRIX items applicable to Wasm are implemented with passing tests.
- PARITY-CHECKLIST shows 100% Done for public surface matching Java.
- Protocol round-trips and schema validations succeed.
- README and VERSIONS.MD updated with Wasm details and status.



---

Companion documents for agents
- WASM_AGENT_STEP_GUIDE.md: step-by-step instructions per milestone (prereqs, files, tests, commands, acceptance, pitfalls).
- PARITY-CHECKLIST.md: table to track Java↔Wasm API parity; start with Milestone 1 and keep it updated.
