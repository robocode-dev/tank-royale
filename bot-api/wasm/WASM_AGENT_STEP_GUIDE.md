### Tank Royale Bot API — Kotlin/Wasm Agent Execution Guide

Objective
- This is a step-by-step, agent-friendly companion to WASM_IMPLEMENTATION_PLAN.md.
- Each milestone lists: prerequisites, tasks, files to touch, tests to add, commands to run, acceptance criteria, and pitfalls.
- Follow TEST-MATRIX.md IDs exactly and mirror Java semantics.

Global rules
- Source of truth: Java Bot API. Match names, defaults, error messages, and event semantics.
- Public API stability: do not break public names/behavior. Keep wire protocol identical to schema.
- Minimal changes per task; avoid drive-by refactors.
- Use Node runtime for wasm tests. Do not introduce browser-only APIs.

How to use with LLMs
- Primary handoff file: this guide (C:\Code\tank-royale\bot-api\wasm\WASM_AGENT_STEP_GUIDE.md).
- How to point to a step: In your prompt, reference the exact section and items to execute, e.g.,
  "Complete: Milestone 2 — Core Foundations → Tasks → items (1)–(3). Stop after tests pass."
- Minimal attachments for context (read-only):
  1) C:\Code\tank-royale\bot-api\tests\TEST-MATRIX.md (canonical test IDs and expectations)
  2) C:\Code\tank-royale\bot-api\wasm\WASM_IMPLEMENTATION_PLAN.md (background and dependencies)
- Progress marking (required after each completed step):
  1) Update parity tracker at C:\Code\tank-royale\bot-api\wasm\PARITY-CHECKLIST.md (set Status to Done/Partial and link tests).
  2) Append a line under "LLM Progress" below using this pattern:
     - [x] `DATE` — `MILESTONE` — Tasks `RANGE_OR_LIST` — by `AGENT_OR_TOOL` — tests: `ID_LIST`; result: `Passed|Failed`
     Example: - [x] 2025-11-09 — M2 Core Foundations — Tasks (1)-(3) — by Junie — tests: TR-API-VAL-005, UTL-002; result: Passed
  3) Commit message convention:
     wasm: `MILESTONE_SHORT` — `short description` (`ID_LIST`)
     Include a short Why in the body and mention tests run.

LLM Progress (append lines here)
- [ ] (no steps completed yet)

Commands
- Run tests: `./gradlew :bot-api:wasm:test`
- Run wasm entry (manual): `./gradlew :bot-api:wasm:run`

Artifacts to maintain
- PARITY-CHECKLIST.md: update in Milestone 1 and as you land features.
- Tests under bot-api/wasm/src/wasmJsTest/kotlin with IDs per TEST-MATRIX.md.

---

Milestone 1 — Inventory & Parity Checklist
Prerequisites
- Open: bot-api/tests/TEST-MATRIX.md
- Open Java reference (bot-api/java) for API types.

Tasks
1) Enumerate Java public API: packages dev.robocode.tankroyale.api (value objects, enums, interfaces, events, commands).
2) Populate bot-api/wasm/PARITY-CHECKLIST.md rows for every public type; set Status to TODO initially.
3) Mark any existing Wasm implementations as Partial/Done and link file paths.

Files to touch
- bot-api/wasm/PARITY-CHECKLIST.md

Tests
- None to write in this milestone.

Acceptance
- Checklist covers 100% of Java public surface.
- Commit message: "wasm: parity checklist populated (Milestone 1)."

Pitfalls
- Don’t forget nested types and enums.

---

Milestone 2 — Core Foundations (constants + JSON base)
Prerequisites
- Milestone 1 complete.

Tasks
1) Create constants file (e.g., dev.robocode.tankroyale.api.Constants).
2) Add kotlinx.serialization setup for wasm: Json { ignoreUnknownKeys = true; explicitNulls = false; }
3) Create base DTO annotations and a small sample DTO for round-trip test.

Files to touch
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/Constants.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/proto/Json.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/proto/SampleDto.kt

Tests
- src/wasmJsTest/kotlin/ProtocolRoundTripTest.kt
  - test_TR_API_PRT_002_round_trip_known_fields

Acceptance
- JSON round-trip works; unknown fields ignored on input.
- Commit: "wasm: add JSON foundation and constants (Milestone 2)."

Pitfalls
- Keep field names exactly as schema; configure serializers accordingly.

---

Milestone 3 — VAL: Value Objects and Constants
Prerequisites
- M2 JSON base and constants.

Tasks
1) Implement BotInfo with defaults/validation (per Java behavior).
2) Implement InitialPosition and InitialPositionMapper.
3) Implement Color and Point value objects.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/BotInfo.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/InitialPosition.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/InitialPositionMapper.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/Color.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/Point.kt

Tests (IDs from TEST-MATRIX.md)
- BotInfoTest: TR-API-VAL-001, TR-API-VAL-002
- InitialPositionTest: TR-API-VAL-003, TR-API-VAL-004
- ValueObjectsTest: TR-API-VAL-006, TR-API-VAL-007

Acceptance
- All VAL tests green.
- Commit: "wasm: implement VAL objects (Milestone 3)."

Pitfalls
- Enforce same error messages and bounds as Java.

---

Milestone 4 — UTL: Utilities
Prerequisites
- VAL complete.

Tasks
1) ColorUtil RGB↔HSV with tolerances.
2) JsonUtil with canonical formatting for API DTOs.
3) CountryCode normalization/validation.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/util/ColorUtil.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/util/JsonUtil.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/util/CountryCode.kt

Tests
- ColorUtilTest: TR-API-UTL-001
- JsonUtilTest: TR-API-UTL-002
- CountryCodeTest: TR-API-UTL-003

Acceptance
- All UTL tests green.

---

Milestone 5 — GFX: Graphics API (SVG)
Prerequisites
- Color/Point.

Tasks
1) Define IGraphics interface.
2) Implement SvgGraphics producing canonical SVG.
3) Manage style state and reset rules.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/gfx/IGraphics.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/gfx/SvgGraphics.kt

Tests
- SvgGraphicsTest: TR-API-GFX-001..004

Acceptance
- SVG strings match expected canonical outputs.

---

Milestone 6 — PRT: Protocol DTOs & Mapping
Prerequisites
- JSON base.

Tasks
1) Define DTOs for outgoing intents and incoming messages per schema.
2) Configure serialization; unknown fields ignored on input; maintain stable field names.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/proto/... (DTOs)

Tests
- ProtocolRoundTripTest: TR-API-PRT-001..003

Acceptance
- Round-trip and schema validation tests pass.

---

Milestone 7 — BOT: Lifecycle & Configuration
Prerequisites
- PRT DTOs; Harness (M8) may be started earlier to support tests.

Tasks
1) Implement BaseBot constructor; config precedence: explicit args > ENV.
2) Implement Connect/Disconnect; Start/Stop/Pause/Resume flags.
3) Error handling to safe state.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/BaseBot.kt
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/internal/Config.kt

Tests
- BaseBotConstructorTest: TR-API-BOT-001a..001d
- ConnectionLifecycleTest: TR-API-BOT-002..004

Acceptance
- Lifecycle tests green under Node env.

---

Milestone 8 — Harness: WasmMockedServer (Test Scaffolding)
Prerequisites
- JSON base; some DTOs.

Tasks
1) Implement an in-process harness that feeds handshake, ticks, and captures intents.
2) Provide helper assertions for intents.

Files
- src/wasmJsTest/kotlin/harness/WasmMockedServer.kt
- src/wasmJsTest/kotlin/harness/Fixtures.kt

Tests
- Used by BOT/CMD/EVT/TCK suites.

Acceptance
- Harness supports enqueuing messages and recording outgoing intents per tick.

---

Milestone 9 — CMD: Commands & Intents
Prerequisites
- BaseBot + DTOs + Harness.

Tasks
1) Implement movement, fire, radar/scan, and graphics frame emission.
2) Ensure next-tick intent payloads match spec.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/commands/*.kt

Tests
- CommandIntentTest: TR-API-CMD-001..004

Acceptance
- Command tests green; payloads exactly match expected JSON.

---

Milestone 10 — TCK: Ticks & State
Prerequisites
- BOT + CMD.

Tasks
1) Expose immutable per-tick snapshots.
2) Implement intent apply/reset timing; timeouts behavior.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/state/*.kt

Tests
- TickStateTest: TR-API-TCK-001..003

Acceptance
- All TCK tests green.

---

Milestone 11 — EVT: Events
Prerequisites
- DTOs; dispatch loop.

Tasks
1) Implement event types and dispatch order; interruption semantics.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/events/*.kt

Tests
- EventDispatchTest: TR-API-EVT-001..005

Acceptance
- Event tests green; order matches Java.

---

Milestone 12 — INT: Internals & Dispatch Loop
Prerequisites
- BOT lifecycle.

Tasks
1) Implement mapping/validation helpers.
2) Event dispatch loop with spy/observer hooks for tests.

Files
- src/wasmJsMain/kotlin/dev/robocode/tankroyale/api/internal/*.kt

Tests
- InternalBehaviorTest: TR-API-INT-001..002

Acceptance
- Internal behavior validated via public effects or spies.

---

Milestone 13 — Docs & Versioning
Prerequisites
- Core surfaces implemented.

Tasks
1) Update README to include wasm module and run instructions.
2) Update VERSIONS.MD with wasm milestones.

Files
- README.md
- VERSIONS.MD

Acceptance
- Docs consistent across modules.

---

Appendix — Review checklist per task
- Matches Java reference semantics and names.
- Backward compatibility preserved (API + wire).
- Tests added/updated with matrix IDs; all pass.
- Minimal, focused diff.
- Comments/Javadoc/docstrings accurate across ports.
