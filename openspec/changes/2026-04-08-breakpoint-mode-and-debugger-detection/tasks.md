# Implementation Tasks

## 1. Schema

- [x] 1.1 Add `features.breakpointMode: boolean` to `schema/schemas/server-handshake.schema.yaml`
       (alongside the existing `features.debugMode` field added for ADR-0033)
- [x] 1.2 Add `debuggerAttached: boolean` (optional) to `schema/schemas/bot-handshake.schema.yaml`

Note: `breakpointEnabled` in `bot-policy-update.schema.yaml` and `pauseCause: "breakpoint"` in
`game-paused-event-for-observer.schema.yaml` are **already present** — no changes needed.

---

## 2. Server — Breakpoint Mode

- [x] 2.1 **`ParticipantRegistry.kt`** — Add `_breakpointEnabledMap: ConcurrentHashMap<BotId, Boolean>`
       alongside the existing `_debugGraphicsEnableMap`. Add `setBreakpointEnabled(botId, enabled)` and
       a public read-only accessor `breakpointEnabledMap`.

- [x] 2.2 **`GameLifecycleManager.kt`** — Add state to track bots the server is currently waiting for
       in a breakpoint pause (e.g. `breakpointPausedForBots: MutableSet<BotId>`). This set is populated
       when the server pauses for a breakpoint and cleared on resume or skip.

- [x] 2.3 **`GameServer.handleBotPolicyUpdate()`** — Extend to handle `breakpointEnabled`:
       - Store via `participantRegistry.setBreakpointEnabled(botId, value)`.
       - If `breakpointEnabled = false` and the server is currently in a breakpoint pause waiting for
         that bot: issue `SkippedTurnEvent` for that bot, remove it from the waiting set, and if the
         set is now empty, process the turn and resume (sending `game-resumed-event`).

- [x] 2.4 **`GameServer` turn timeout handler (`onNextTurn()`)** — After the turn timeout expires,
       check for alive bots that did NOT send an intent. For each such bot:
       - If `breakpointEnabled == true`: do NOT send `SkippedTurnEvent`. Add to `breakpointPausedForBots`.
       - If `breakpointEnabled == false`: send `SkippedTurnEvent` as normal.
       If `breakpointPausedForBots` is non-empty after this check: call `lifecycleManager.pauseGame()`,
       broadcast `game-paused-event` with `pauseCause: "breakpoint"`, and return without processing
       the turn. The turn is processed only when all awaited bots have responded.

- [x] 2.5 **`GameServer.handleBotIntent()`** — If the game is in a breakpoint pause and the responding
       bot is in `breakpointPausedForBots`: remove it from the set. When the set is empty (all awaited
       bots have responded): process the turn, then either re-pause (if debug mode is also active) or
       auto-resume by broadcasting `game-resumed-event`.

- [x] 2.6 **`ClientWebSocketsHandler`** — Set `features.breakpointMode = true` in the
       `ServerHandshake` message (alongside the existing `features.debugMode = true`).

---

## 3. Server — Debugger Detection Passthrough

- [x] 3.1 The `debuggerAttached` field added to `bot-handshake.schema.yaml` (task 1.2) will be
       auto-generated into `BotHandshake.java` via `jsonschema2pojo`. Since `bot-info` extends
       `bot-handshake` in the schema, the field is automatically forwarded to controllers in
       `bot-list-update`. **No additional server logic is needed** — store and forward only.

---

## 4. Bot APIs — Debugger Detection

- [x] 4.1 **Java** `bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/BotHandshakeFactory.java`
       — Detect JDWP agent:
       ```java
       boolean isDebuggerAttached() {
           if ("true".equalsIgnoreCase(System.getenv("ROBOCODE_DEBUG"))) return true;
           if ("false".equalsIgnoreCase(System.getenv("ROBOCODE_DEBUG"))) return false;
           return ManagementFactory.getRuntimeMXBean()
               .getInputArguments().stream()
               .anyMatch(arg -> arg.contains("jdwp"));
       }
       ```
       Set `handshake.setDebuggerAttached(isDebuggerAttached())`.
       Log `"Debugger detected. Consider enabling breakpoint mode for this bot in the controller."` if true.

- [x] 4.2 **C#** `bot-api/dotnet/api/src/internal/BotHandshakeFactory.cs`
       — Detect managed debugger:
       ```csharp
       static bool IsDebuggerAttached() {
           var env = Environment.GetEnvironmentVariable("ROBOCODE_DEBUG");
           if ("true".Equals(env, StringComparison.OrdinalIgnoreCase)) return true;
           if ("false".Equals(env, StringComparison.OrdinalIgnoreCase)) return false;
           return System.Diagnostics.Debugger.IsAttached;
       }
       ```
       Set `DebuggerAttached = IsDebuggerAttached()` in the handshake.
       Log the hint if true.

- [x] 4.3 **Python** `bot-api/python/src/robocode_tank_royale/bot_api/internal/bot_handshake_factory.py`
       — Detect trace-function-based debugger:
       ```python
       def is_debugger_attached() -> bool:
           env = os.environ.get("ROBOCODE_DEBUG", "").lower()
           if env == "true": return True
           if env == "false": return False
           if sys.gettrace() is not None: return True
           return "debugpy" in sys.modules or "pydevd" in sys.modules
       ```
       Set `debugger_attached=is_debugger_attached()` in the handshake dataclass.
       Log the hint if true.

- [ ] 4.4 **TypeScript** — Deferred. When the TypeScript Bot API (ADR-0027-29) is implemented,
       add detection using `inspector.url() !== undefined` (Node.js) plus `ROBOCODE_DEBUG` env var.

---

## 5. Client Library Updates

- [x] 5.1 **`lib/client/src/main/kotlin/dev/robocode/tankroyale/client/model/Messages.kt`**
       `BotPolicyUpdate` — add `val breakpointEnabled: Boolean? = null`
       (alongside existing `debuggingEnabled`).

- [x] 5.2 **`lib/client/src/main/kotlin/dev/robocode/tankroyale/client/model/BotInfo.kt`**
       — add `val debuggerAttached: Boolean? = null`.

---

## 6. GUI — Breakpoint Mode Controls

- [x] 6.1 **`gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/console/BotPropertiesPanel.kt`**
       — Add a breakpoint toggle switch alongside the existing debug graphics toggle.
       Guard it: only enabled when `serverFeatures?.breakpointMode == true`.
       On toggle: fire `ClientEvents.onBotPolicyChanged(BotPolicyUpdate(bot.id, debuggingEnabled = <existing>, breakpointEnabled = <newValue>))`.
       Note: `debuggingEnabled` was made optional in ADR-0034 — both fields can be sent independently.

- [x] 6.2 **Battle view status display** — When a `GamePausedEvent` arrives with
       `pauseCause == "breakpoint"`, display a message such as
       "Paused — waiting for *BotName* (breakpoint)" to inform the developer.
       Locate where `pauseCause` is currently used for debug_step and extend similarly.

---

## 7. GUI — Debugger Detected Indicator

- [x] 7.1 **`BotPropertiesPanel.kt`** — Show a visual indicator (e.g. bug icon or "🐛" label)
       when `bot.debuggerAttached == true`. This gives the developer an immediate cue that
       the bot was launched in debug mode.

- [x] 7.2 **Auto-enable breakpoint mode** — When `debuggerAttached == true` and
       `serverFeatures?.breakpointMode == true`, auto-enable breakpoint mode for that bot
       (send `BotPolicyUpdate` with `breakpointEnabled = true`) or show a prompt to offer it.
       Keep it simple: silent auto-enable is preferred over a modal prompt.

---

## 8. Resources & Changelog

- [x] 8.1 **`gui/src/main/resources/Strings.properties`** (and `_da`, `_es`, `_ca`) —
       Add keys: `breakpoint_mode` (label for the toggle) and any other needed UI strings.

- [x] 8.2 **`gui/src/main/resources/Hints.properties`** (and `_da`, `_es`, `_ca`) —
       Add `bot_properties.breakpoint_mode` tooltip hint explaining what breakpoint mode does.

- [x] 8.3 **`CHANGELOG.md`** — Add entries under `[0.39.1]` for:
       - ADR-0034: Breakpoint mode (server + GUI)
       - ADR-0035: Bot API debugger detection (Java, C#, Python)

---

Notes:

- `debuggingEnabled` in `BotPolicyUpdate` was made optional in ADR-0034. The server's
  `handleBotPolicyUpdate()` and the GUI's `BotPropertiesPanel` should handle `null` values
  for both fields (treat `null` as "no change to this policy").
- The server must handle multiple bots in breakpoint mode simultaneously: if bots A and B
  both have breakpoint mode enabled and both miss the timeout, the server waits for both
  before processing the turn.
- `debuggerAttached` is informational only — the server stores and forwards it but does not
  change any game behaviour based on it. Behaviour changes (breakpoint mode) are always
  controller-initiated via `bot-policy-update`.
