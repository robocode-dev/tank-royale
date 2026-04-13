# Change: Breakpoint Mode and Bot API Debugger Detection

## Why

When a bot developer sets a breakpoint inside an event handler (e.g. `onScannedBot()`),
the bot thread freezes. The server's turn timeout fires, and the bot receives a
`SkippedTurnEvent` — the game records a missed turn that wouldn't have happened in
production. The developer's debugging session has directly altered the game behaviour.

Two complementary ADRs address this:

- **ADR-0034 (Breakpoint Mode)**: A controller can mark a specific bot as being in
  breakpoint mode. When that bot misses the turn timeout, the server pauses and waits
  for its intent instead of issuing a `SkippedTurnEvent`. The server auto-resumes when
  the intent arrives. Disabling breakpoint mode while the server is paused causes an
  immediate skip+resume (recovery mechanism for crashed bots).

- **ADR-0035 (Bot API Debugger Detection)**: Each Bot API auto-detects whether a debugger
  is attached at startup and includes `debuggerAttached: true` in the `bot-handshake`.
  The server forwards this to controllers via `bot-list-update`. Controllers can show a
  visual indicator and optionally auto-enable breakpoint mode for that bot.

These two ADRs were designed together and are implemented as one change because ADR-0035's
detection feeds directly into ADR-0034's UI flow (detect → indicate → offer breakpoint mode).

## What Changes

### Protocol / Schema
- **`bot-policy-update.schema.yaml`** — `breakpointEnabled` field already present (schema-only work done)
- **`game-paused-event-for-observer`** — `pauseCause: "breakpoint"` value already present
- **`server-handshake.schema.yaml`** — add `features.breakpointMode: boolean` to advertise capability
- **`bot-handshake.schema.yaml`** — add `debuggerAttached: boolean` (optional, informational)

### Server
- **`ParticipantRegistry.kt`** — add `breakpointEnabledMap` (per-bot flag store)
- **`GameLifecycleManager.kt`** — add state to track which bots the server is waiting for
  during a breakpoint pause
- **`GameServer.kt`** — turn timeout handler: if bot with breakpoint enabled missed timeout →
  pause with `pauseCause: "breakpoint"` instead of sending `SkippedTurnEvent`; intent handler:
  auto-resume when awaited bot responds; policy handler: if breakpoint disabled while paused
  for that bot → issue `SkippedTurnEvent` and resume
- **`ClientWebSocketsHandler.kt`** — advertise `features.breakpointMode = true` in server handshake

### Bot APIs (ADR-0035)
- **Java** `BotHandshakeFactory.java` — detect JDWP agent via `ManagementFactory.getRuntimeMXBean()`;
  override with `ROBOCODE_DEBUG` environment variable; log hint if detected
- **C#** `BotHandshakeFactory.cs` — `System.Diagnostics.Debugger.IsAttached`; override with
  `ROBOCODE_DEBUG` environment variable
- **Python** `bot_handshake_factory.py` — `sys.gettrace()` + check `debugpy`/`pydevd` in
  `sys.modules`; override with `ROBOCODE_DEBUG` environment variable
- **TypeScript** — deferred until the TypeScript Bot API (ADR-0027-29) is implemented

### Client Library
- **`lib/client/model/Messages.kt`** `BotPolicyUpdate` — add `breakpointEnabled: Boolean? = null`
- **`lib/client/model/BotInfo.kt`** — add `debuggerAttached: Boolean? = null`

### GUI (Controller)
- **`BotPropertiesPanel.kt`** — add breakpoint toggle (alongside existing debug graphics toggle);
  shown only when `serverFeatures.breakpointMode == true`; show debugger indicator icon when
  `bot.debuggerAttached == true`
- Battle view — when `pauseCause == "breakpoint"`, display
  "Paused — waiting for *BotName* (breakpoint)" status message
- Optionally auto-enable breakpoint mode when `debuggerAttached == true`

### Resources
- Add `breakpoint_mode` / `debugger_attached` string + hint keys in all 4 locales
  (en, da, es, ca) in `Strings.properties` and `Hints.properties`

## Impact

- **Affected schemas**: `server-handshake.schema.yaml`, `bot-handshake.schema.yaml`
  (two others already updated)
- **Affected server**: `ParticipantRegistry`, `GameLifecycleManager`, `GameServer`,
  `ClientWebSocketsHandler`
- **Affected Bot APIs**: Java, C#, Python (TypeScript deferred)
- **Affected client lib**: `Messages.kt`, `BotInfo.kt`
- **Affected GUI**: `BotPropertiesPanel.kt`, battle view status display
- **Breaking changes**: None — all new fields are optional; old controllers/bots work unchanged
- **Cross-platform note**: `debuggerAttached` detection is platform-specific by design
  (ADR-0035) but all three active Bot APIs implement it; TypeScript deferred to its own change
- **Related ADRs**: ADR-0034, ADR-0035, ADR-0033 (debug mode, already implemented)
- **Related issue**: [#204](https://github.com/robocode-dev/tank-royale/issues/204)
