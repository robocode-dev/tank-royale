## Context

Tank Royale's architecture (ADR-0005) defines independently deployable components (Server, GUI, Booter, Recorder) that
communicate via WebSocket (ADR-0009) with role-based separation (ADR-0007: Bot, Observer, Controller). The GUI currently
serves as the only high-level orchestrator — starting Server and Booter, connecting as Observer+Controller, and managing
battle lifecycle.

The Battle Runner API extracts this orchestration capability into a standalone library, making it available to any JVM
program.

**Stakeholders:** Bot developers, tournament organizers, educators, Tank Royale maintainers (integration testing).

## Goals / Non-Goals

**Goals:**

- Provide a type-safe Java/Kotlin API for running battles programmatically
- Support both embedded (in-process) and external (pre-started) server modes
- Deliver battle events via the existing Event<T> system (ADR-0022)
- Publish to Maven Central as a public library
- Enable integration/service testing of the Tank Royale platform itself
- Serve as the foundation for 3rd-party tournament and competition systems (analogous to the Control API / Robocode
  Engine in classic Robocode) — Tank Royale itself will not ship tournament tooling (ADR-0023), but the Battle Runner
  is the explicit enabler for those who want to build it

**Non-Goals:**

- Built-in tournament/competition management — Tank Royale itself will not ship tournament tooling (ADR-0023), but the
  Battle Runner is explicitly designed to be the foundation upon which 3rd-party tournament systems can be built (this
  mirrors the role of the Control API / Robocode Engine in classic Robocode)
- Cross-platform ports (Python, .NET) — future work, following ADR-0003 pattern
- Alternative server implementations — this orchestrates the existing server
- GUI or visualization — this is headless-only
- Replacing the GUI's battle management — GUI continues to work independently

## Decisions

### Decision 0: Extract Shared Rules to lib/common

**What:** Move game rule defaults and game type preset definitions from their current scattered locations into
`lib/common`, the existing shared library.

**Why:** Rule definitions currently live in three places:
- `server/rules/setup.kt` — default constants (arena size, rounds, cooling rate, etc.)
- `lib/client/model/GameSetup.kt` — client-side data class with same fields
- `gui/settings/GamesSettings.kt` — preset definitions (classic, melee, 1v1, custom)

Without extraction, the Battle Runner would become a fourth consumer duplicating preset knowledge. Moving to
`lib/common` gives a single source of truth that server, GUI, `lib/client`, and Battle Runner all share.

**Scope:** Constants and preset definitions only. The server's `GameSetup` (with Kotlin `Duration` types) and the
client's `GameSetup` (with `Int` microseconds) remain in their respective modules — they have different serialization
needs. The shared layer provides the *values*, not the transport types.

### Decision 1: Orchestration Pattern

**What:** Battle Runner composes existing Server + Booter rather than embedding game logic.

**Why:** Avoids code duplication, ensures battles behave identically to GUI-run battles, and respects the
network-first architecture (ADR-0009). The Server remains the single source of truth for physics and game state.

**Alternatives considered:**
- *Direct physics embedding* — Would duplicate server logic, create behavioral divergence
- *Server plugin/extension* — Adds complexity to server; would need a new extension mechanism

### Decision 2: Embedded Server and Booter Artifacts

**What:** In embedded mode, the Battle Runner bundles the Server and Booter JAR artifacts inside its own JAR as
classpath resources. At runtime, they are extracted to temp files and launched via `java -jar`.

**Why:** This is the same proven approach used by the GUI module (`gui/build.gradle.kts` copies shrunken JARs into
`build/classes/kotlin/main/`, the fat JAR includes them, `ResourceUtil` extracts them at runtime). Benefits:
- **Version consistency** — Server, Booter, and Runner are always from the same release; no version mismatch risk
- **Zero-config** — Users add a single Maven dependency; no need to locate or download separate JARs
- **Proven pattern** — Reuses the existing `ResourceUtil` extraction and `ProcessBuilder` launch logic from the GUI
- In external mode, no embedded artifacts are needed since the user provides a server URL

**Alternatives considered:**
- *Expect user to provide JAR paths* — Rejected: error-prone, version mismatches, poor developer experience
- *Download JARs at runtime from Maven Central* — Rejected: requires network access, fragile, slow startup

### Decision 3: Dual WebSocket Role

**What:** A single Battle Runner instance connects to the Server as BOTH Observer and Controller using separate
WebSocket connections.

**Why:** Observer provides full game state and events; Controller provides battle management commands. This matches
how the GUI already operates and is fully supported by ADR-0007.

### Decision 4: Synchronous-First API

**What:** The primary API is `runBattle()` which blocks until the battle completes and returns results. Async
variants are also provided for real-time event streaming.

**Why:** The most common use case is "run a battle, get results" (testing, benchmarking). Blocking is simpler to
reason about. Async is available for advanced use cases (live monitoring, early termination).

### Decision 5: Builder Pattern Configuration

**What:** Use Kotlin DSL builders for configuring `BattleRunner` and `BattleSetup`.

**Why:** Provides a fluent, discoverable API. Kotlin DSL builders are idiomatic and compile to clean Java-compatible
code. Matches patterns already used in the codebase.

### Decision 6: Game Type Presets

**What:** Battle configuration starts from a game type preset (`classic`, `melee`, `1v1`, `custom`), reusing the
existing definitions in `dev.robocode.tankroyale.server.rules.setup`. Individual parameters can be overridden.

**Why:** Most users just want "run a classic battle" without specifying every rule parameter. Presets match what the
GUI already provides and ensure consistency. `custom` allows full control when needed. Unset parameters fall back to
server defaults, so the simplest API call is `runBattle(GameType.CLASSIC)`.

**Alternatives considered:**
- *Flat parameter API only* — Forces users to know all parameters; easy to get wrong
- *Separate config file* — Adds file management; less discoverable than code

### Decision 7: Max-Speed by Default (No TPS Control)

**What:** Battles run at TPS = -1 (unlimited speed) by default. No API is provided for changing TPS at runtime.

**Why:** Without a GUI rendering frames, there is no reason to throttle. Users wanting visual observation should use
the GUI. This keeps the API focused on its core purpose: running battles and getting results as fast as possible.

### Decision 8: Intent Diagnostics via WebSocket Proxy

**What:** When enabled, the Battle Runner starts a lightweight WebSocket proxy between bots and server. Bots connect
to the proxy (via `SERVER_URL` env var); the proxy forwards all messages transparently to the real server while
capturing `bot-intent` messages per bot per turn. Intents are stored in memory for the current battle.

**Why:** The observer protocol deliberately does NOT include raw bot intents, and extending it for this purpose is a
no-go — it would add diagnostics data to a protocol used by all observers. Instead, the proxy approach:
- Requires no server or protocol changes
- Works for ALL Bot APIs (Java, Python, .NET, custom) since it operates at the wire protocol level
- Is language-agnostic — any bot that speaks WebSocket is captured
- Is opt-in — disabled by default to avoid latency from the extra hop

**Design constraint — reusable library:** The WS proxy must be implemented as a standalone library component (e.g. in
`lib/common` or a dedicated `lib/intent-diagnostics`) rather than embedded directly in the Battle Runner. This enables
future reuse by the GUI to provide a real-time "Intents" tab in the bot console (see Future Enhancements section).

**Alternatives considered:**
- *Extend observer protocol* — Rejected: pollutes the observer protocol with diagnostics data for all consumers
- *Server-side intent logging* — Rejected: requires server changes and coupling to diagnostics concerns
- *Bot-side logging* — Rejected: only works for official Bot APIs; custom implementations wouldn't benefit

### Decision 9: Optional Battle Recording

**What:** The Battle Runner supports optional recording of battles using the same GZIP ND-JSON format as the existing
Recorder module (ADR-0017). The core file writer (`GameRecorder` class — ~36 lines, GZIP + ND-JSON, no external
dependencies) is extracted from the `recorder` module into `lib/common` so both the Recorder and Battle Runner can
reuse it.

**Why:** The Battle Runner already has an observer connection receiving all events — it only needs the file writer,
not the Recorder's own WebSocket client or CLI infrastructure. Extracting `GameRecorder` to `lib/common` avoids
pulling in unnecessary dependencies (Clikt CLI, duplicate WebSocket client) and keeps the Battle Runner artifact lean.

**What stays in the `recorder` module:** `RecordingObserver` (WebSocket lifecycle, event filtering, manual
start/stop controls) and `RecorderCli` (Clikt CLI). These are specific to the standalone recorder use case.

**Alternatives considered:**
- *Depend on `recorder` module directly* — Rejected: pulls in CLI code (Clikt), its own WebSocket client, and
  `RecordingObserver` — all unnecessary since the Battle Runner already has its own observer connection
- *Copy `GameRecorder` into Battle Runner* — Rejected: code duplication; both modules would diverge over time

## Risks / Trade-offs

- **Process management complexity** — Booter spawns OS processes; cleanup on JVM crash requires shutdown hooks
  → Mitigation: Register JVM shutdown hooks; provide `close()` / `AutoCloseable` for deterministic cleanup
- **Port conflicts** — Embedded server needs an available port
  → Mitigation: Support dynamic port allocation (port 0) and configurable ports
- **API stability** — Public Maven Central artifact commits to backward compatibility
  → Mitigation: Start with a minimal, focused API surface; expand based on user feedback

## Resolved Questions

1. **Multiple sequential battles on same server instance?** — **Yes, this is the default.** The Battle Runner reuses the
   same embedded server across multiple battles (e.g. 1000 battles of 10 rounds each). No server restart between battles.

2. **"Dry run" mode for configuration validation?** — **No separate dry-run mode needed.** Configuration validation is
   handled at construction time using value classes (e.g. `ArenaSize`, `RoundCount`, `GameType`) that enforce invariants
   before they can be passed to the Battle Runner. Invalid configuration fails fast at build time, not at battle start.

3. **Intent diagnostics: raw JSON or typed objects?** — **Deserialized typed objects only.** This is a Java/Kotlin API —
   end users should not deal with JSON. The intent data model should reuse or extend existing types from `lib/common`
   (e.g. matching the server's `BotIntent` structure) to avoid duplication.

## Future Enhancements (Out of Scope)

- **GUI Bot Console — Intent Tab:** The Intent Diagnostics WS proxy should be designed as a reusable library component
  (not coupled to the Battle Runner API) so the GUI can integrate it independently. This would enable an "Intents" tab
  in the GUI's bot console, displaying the raw `bot-intent` messages sent by each bot in real time — alongside the
  existing stdout/stderr event tabs. This gives developers real-time visibility into what a bot is requesting (target
  speed, turn rates, fire power, etc.) versus what the server computed. The proxy component's API should be designed with
  this reuse in mind from the start.
