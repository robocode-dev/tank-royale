## [0.42.1] - 2026-04-24 - GUI bug fix

### 🐞 Bug Fixes

- GUI:
    - Fixed the Tank Color Mode setting ("Bot Colors (default)") reverting to "Bot Colors
      (Debug Only)" after restarting the GUI. The Options dialog was re-saving the color mode
      from the radio button state on OK, which could override the immediately-saved selection
      if the dialog state had been refreshed in between.

## [0.42.0] - 2026-04-22 - First release of the TypeScript Bot API

This is the first official release of the TypeScript Bot API, making TypeScript a first-class
member of the Bot API family alongside Java, .NET, and Python. The groundwork was laid in 0.41.0;
this release ships the TypeScript API as a fully supported platform for writing bots.

### 🐞 Bug Fixes

- Bot API (Java):
    - Fixed JDK reflection warning about Gson mutating `final` fields in `Point` during
      deserialization. Gson now constructs `Point` via its constructor instead of reflective
      field mutation, which will be blocked in a future JDK release.

- Bot API (Java, .NET, Python, TypeScript):
    - #202, #210: Fixed events (`ScannedBotEvent`, `TickEvent`, etc.) firing one turn late.
      The bot API dispatched events **after** `go()` returned, so `run()` loop code could read
      the new tick state before the corresponding events had fired. Events now fire **before**
      `execute()` returns, matching Classic Robocode semantics: events for turn N always fire
      before the bot reads turn N state in `run()`.

- GUI:
    - Fixed `ArrayIndexOutOfBoundsException` in `BasicListUI` caused by `SortedListModel`
      returning inconsistent sizes during concurrent updates.

- Booter:
    - Removed misleading bot-dir prefix from per-line stderr output; fixed `Log` atomicity.

- TypeScript Bot API:
    - Fixed debug graphics not appearing in `PaintingBot`.

- Bot API (Java, .NET, Python, TypeScript):
    - Fixed `setGunTurnRate()`, `setRadarTurnRate()`, `setTurnRate()`, and `setTargetSpeed()`
      values being silently reset to zero after the first turn when using the continuous
      (rate-based) movement API on `Bot`. Bots that set a turn or speed rate in `run()` and
      relied on it persisting across turns now behave correctly for the full round.

- Sample bots:
    - Renamed `VelocityBot` → `VelociBot` on all platforms (Java, .NET, Python, TypeScript),
      matching the original classic Robocode `VelociRobot` name more closely.
    - Fixed `VelociBot` not shooting at scanned bots. An erroneous `setRadarTurnRate(15)` call
      caused the radar to spin at 30°/turn while the gun spun at 15°/turn, so the gun was never
      aimed at the enemy when a scan event fired. Removed the redundant radar rate — the radar
      already follows the gun automatically since the radar turn rate is cumulative on top of
      the gun turn rate.

## [0.41.0] - 2026-04-19 - Bot API library updater and stability improvements

This release lays the groundwork for an upcoming TypeScript Bot API. The TypeScript API is
functional but still under test and lacks documentation, so it is not included in this release.
The cross-platform test infrastructure, semantic alignment across all Bot APIs, and Python internals
refactoring introduced here were all necessary steps to make the TypeScript API a first-class
member of the Bot API family — that work is already done and waiting.

### ✨ Features

- GUI:
    - #207: Added bot API library updater. On startup, the GUI scans all configured bot
      directories for outdated or missing library files (Java `.jar`, .NET `.nupkg`,
      Python `.whl`, TypeScript `.tgz`) and offers to update them in one click.
      A "Don't ask again" option is available for users who manage libraries manually.

- Bot API (Java, .NET, Python, TypeScript):
    - #208: Added missing rules constants to the `Constants` class: `INACTIVITY_ZAP` (0.1),
      `RAM_DAMAGE` (0.6), `STARTING_GUN_HEAT` (3.0), `TEAM_MESSAGE_MAX_SIZE` (32768), and
      `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN` (10).

### 🐞 Bug Fixes

- Bot API (Python):
    - Fixed Python bots failing to connect to the server when the server's handshake included
      a `features` field — the JSON deserializer crashed with `KeyError: 'dict'` and the bot
      never joined the game.
    - Fixed Python bots throwing `BotException: Tick event is not available` at round
      boundaries, which could cause the bot thread to terminate unexpectedly or silently skip
      event handlers (e.g. `on_scanned_bot`) between rounds.

- Bot API (Java, .NET, Python, TypeScript):
    - #202: Fixed radar and gun commands set in `run()` before the first `go()` being silently
      dropped on turn 1. Commands such as `setTurnRadarRight(Double.MAX_VALUE)` in `run()` now
      take effect from turn 1 as expected.

### 🔧 Changes

- Server / GUI:
    - Raised the default **ready timeout** from 1 second to 10 seconds. This prevents bots using
      runtimes that need startup time (JVM, Python, .NET) from being silently excluded from a
      battle when the server starts before they have finished loading. The setting remains
      adjustable in the Setup Rules dialog.

- Bot API (Java, .NET, Python, TypeScript) / Server:
    - Significantly expanded the test bed for both the server and all Bot API implementations,
      adding cross-platform conformance tests that verify all four Bot APIs behave identically for
      the same inputs — making it easier to catch regressions and keep the APIs in sync across
      languages. As part of this work, the Python Bot API internals were refactored to align more
      closely with the Java, .NET, and TypeScript implementations, achieving true 1-to-1 semantic
      parity across all platforms and making the codebase easier to maintain and debug going
      forward.

## [0.40.2] - 2026-04-14 - Bot API stability and intent fixes

### 🐞 Bug Fixes

- Bot API (Java):
    - #207: Fixed Java 26 warning about mutating `final` fields via reflection during
      deserialization of `RoundStartedEvent` and `RoundEndedEvent`. Gson now deserializes
      into the mutable schema classes first, then constructs the immutable bot API event
      objects via their constructors.

- Bot API (Java, .NET, Python, TypeScript):
    - #202: Fixed bots receiving a `SkippedTurnEvent` on turn 1 when the OS scheduler delayed
      the bot thread's first time slice by more than the turn timeout. The bot API now sends 
      default intent immediately after the bot thread wakes up for the first turn, ensuring turn 1
      is never skipped due to scheduling latency.
    - #202: Fixed an edge case where the pre-warmed bot thread could bypass the tick-arrival
      wait at the start of rounds 2+ if the previous round's tick state was still set.

- Bot API (.NET):
    - Fixed console output is becoming corrupted after a bot reconnects to the server in a
      multi-game session.
    - Fixed a rare crash where an interrupted thread flag could leak into unrelated operations
      after a bot disconnects.

- Bot API (Java, .NET, Python):
    - Fixed `rescan()` / `setRescan()` having no effect. The rescan flag was cleared internally
      before the intent was sent to the server, so the server never received it.

## [0.40.1] - 2026-04-12 - First-turn skip fix + breakpoint disconnect fix

### 🐞 Bug Fixes

- Server:
    - #206: Fixed bots disconnecting from the server after ~80 seconds while paused at a
      debugger breakpoint.

- Bot API (Java, .NET, Python):
    - #202: Fixed bots receiving a `SkippedTurnEvent` on turn 1 and missing their first chance
      to act.

## [0.40.0] - 2026-04-11 - Debug Mode, Breakpoints & Debugger Detection

This release makes Tank Royale a first-class environment for bot development and debugging. The three
new capabilities work together as a system: each Bot API now **auto-detects a connected debugger** and
advertises it to the server; the server uses that signal to **auto-enable breakpoint mode** for that
bot, suspending the turn clock whenever the bot hits a breakpoint rather than issuing a
`SkippedTurnEvent`; and a new **debug mode** lets any controller (or the GUI) step through the battle
one completed turn at a time, inspecting the full game state before advancing. Together these remove
the friction of debugging a bot under real game conditions — no manual setup, no missed turns, no
racing the clock.

### ✨ Features

- Server:
    - Added **debug mode** (`EnableDebugMode` / `DisableDebugMode`): turns complete fully before
      pausing; `next-turn` steps one turn, `ResumeGame` exits. `GamePaused` gains a `pauseCause`
      field (`pause`, `debug_step`, or `breakpoint`).
    - #204: Added **breakpoint mode**: server waits for a late bot intent instead of issuing
      `SkippedTurnEvent`; auto-resumes on arrival. Advertised via `features.breakpointMode`.
      Auto-enabled for bots with `debuggerAttached = true`.
    - Added `server.properties` (`debugModeSupported`, `breakpointModeSupported`) with CLI
      overrides (`--[no-]debug-mode`, `--[no-]breakpoint-mode`). `breakpointModeSupported = false`
      silently ignores breakpoint requests (tournament safety).

- GUI:
    - Added a **Debug 🐛** toggle to the control panel — steps one complete turn per **Next Turn** click.
    - #205: Added **Start paused** to the New Battle dialog — enters debug mode from turn 1.
    - #204: Added **Breakpoint Mode** toggle to the Bot Properties panel; auto-enabled and 🐛-labelled
      when `debuggerAttached = true`. Hidden when the server doesn't support breakpoint mode.

- Bot APIs (Java, .NET, Python):
    - #204: Added **debugger detection**: `debuggerAttached: true` is included in the bot handshake
      when a debugger is found (JDWP args / `Debugger.IsAttached` / `sys.gettrace()`). Overridable
      via `ROBOCODE_DEBUG`.

### 🐞 Bug Fixes

- Sample bots (C#):
    - Fixed `PaintingBot.cs` with a broken .json file.

- Bot API (Java, .NET, Python):
    - #202: Fixed race condition in `go()` / `execute()` where the WebSocket thread could deliver a new
      tick between event dispatch and intent sending, causing the bot to skip a turn. This produced early
      `SkippedTurnEvent` at tick 1, prevented the bot from acting at tick 2, and delayed `ScannedBotEvent`
      delivery by one tick. The fix passes the captured turn number from `go()` into `execute()` so both
      use the same consistent tick throughout the call.

## [0.39.0] - 2026-04-06 - Convention-over-Configuration & Scriptless Bots

### ✨ Features

- Booter:
    - Template-based booting for JVM, .NET, and Python — bots no longer need `.sh`/`.bat` scripts.
      The Booter selects a template from `platform`, `programmingLang`, and `base` in the bot's JSON.
    - Bots without a `.json` file are now supported: the Booter heuristically detects the platform
      from files in the directory (`.java`, `.py`, `.cs`, JARs, etc.) and boots accordingly.
    - The bot's parent-directory name is used as the default `base` when not set in JSON.

- GUI:
    - Boot progress dialog handles no-JSON bots via baseline-snapshot tracking — waits for the right
      number of new connections rather than a specific name/version.
    - #201: Added **Tank Color Mode** to the config dialog (persisted in `gui.properties`):
        - **Bot Colors** (default) — bot-defined colors apply freely.
        - **Bot Colors (Once)** — the first color set per component is locked for the entire battle.
        - **Default Colors** — system defaults always used; bot colors ignored.
        - **Bot Colors (Debug Only)** — bot colors visible only when Graphical Debugging is active.

- Bot API (Java, .NET, Python):
    - Runtime validation of required properties (`name`, `version`, `authors`). A `BotException` with
      a descriptive message is thrown on connection if any are missing.

### 🔧 Changes

- Build:
    - #203: Updated `release-docs-template.md` to use the direct JAR link for the Java Bot API on Maven Central.

- Sample Bots:
    - Removed generated `.cmd`/`.sh` scripts from all standard sample bots — they now rely on template-based booting.
    - Added `NuGet.Config` to C# sample bot distributions for standalone source-based builds.

### 🐞 Bug Fixes

- GUI:
    - Fixed double-clicking in the **New Battle** dialog. The boot-progress dialog was `APPLICATION_MODAL`,
      blocking the window and stealing focus between clicks. It is now modeless with `setAutoRequestFocus(false)`,
      so the bot list keeps focus and additional double-clicks accumulate in the same dialog.

## [0.38.3] - 2026-04-05 - TPS Resume Dialog & TimeLeft Fix

### 🐞 Bug Fixes

- Build:
    - #203: Fixed Maven Central artifacts being published with version "unspecified" instead of the correct
      version number. The `create("libs")` call in `settings.gradle.kts` silently lost to Gradle's auto-loaded
      `libs.versions.toml` catalog, so the `tankroyale` version entry was never registered. Fixed by reading
      the `VERSION` file in the root `build.gradle.kts` and propagating via `allprojects` instead of the
      version catalog.

- Bot API (Java, .NET, Python):
    - #202: Fixed `getTimeLeft()` returning negative values when turns were skipped or the bot was busy.
      The timing now uses the arrival time of the latest tick received by the bot to ensure accurate
      reporting of the time remaining for the current turn.

- GUI:
    - Fixed JNA restricted native access warning (`java.lang.System::load`) on Java 16+ when starting the
      booter, server, and recorder subprocesses. The `--enable-native-access=ALL-UNNAMED` JVM flag is now
      passed automatically when running on Java 16 or later.

### 🔧 Changes

- Bot API (Java, .NET, Python, TypeScript) + Schemas:
    - Increased the maximum number of country codes per bot from 5 to 20.

## [0.38.2] - 2026-03-29 – TPS Resume Dialog & Double-Turn Fixes

### 🐞 Bug Fixes

- Server:
    - #199: Fixed double-turn bugs when stepping at TPS=0, rapidly changing TPS, or switching between TPS values.
      The turn-timeout timer is now rescheduled only after the visual delay completes, and nonzero→nonzero TPS
      changes no longer reset the timer.
    - Fixed game freeze on pause/resume and when resuming from TPS=0. Resume now calls `resetTurnTimeout()`
      directly instead of relying on `turnTimeoutTimer.resume()`, which could be a no-op depending on timer state.

### ✨ Features

- GUI:
    - Resume at TPS=0 now shows a confirmation dialog asking the user to resume at the default TPS instead of
      silently resuming with no visual delay.

### 🔧 Changes

- Bot API (Java, .NET, Python) + Schemas:
    - Increased the maximum number of authors per bot from 5 to 20.

### ♻️ Refactoring

- Server:
    - Improved thread-safety, model immutability, error handling, and game start validation.
- Booter:
    - Improved CLI structure and error logging.

## [0.38.1] - 2026-03-23 – GUI Boot Progress Dialog

### ✨ Features

- GUI:
    - Boot progress dialog: when starting a battle, a modal dialog now shows each expected bot identity with a status
      icon (⏳ pending / ✅ connected). For duplicate identities (e.g., 4 droids), the row shows
      `MyFirstDroid v1.0 (2/4 connected)`.
    - Elapsed time label (`Elapsed: 12s / 30s`) updates every 500 ms during the wait.
    - The cancel button aborts the boot, kills booted bot processes, and returns to the bot selection dialog.
    - Timeout error: after the configured timeout, the status area is replaced with a list of pending bots and
      "Retry" / "Cancel" buttons. "Retry" resets the timer and continues waiting.
    - Boot timeout is now configurable in GUI Options (default: 30 seconds).

### 🐞 Bug Fixes

- Python bot API:
    - Fixed `AssertionError: No current event to check interruptibility for` causing bots to stall after ~200–300
      rounds at high TPS (#196). Root causes: threading race between the bot thread and WebSocket thread in
      `dispatch_events` (fixed by joining the bot thread in `stop_thread()`), and a missing `None` guard on
      `current_top_event` after a `ThreadInterruptedException` in the event dispatch loop.

## [0.38.0] - 2026-03-21 – Identity-Based Bot Matching

### ✨ Features

- Runner API:
    - Identity-based bot matching: bots are now matched by `name` + `version` from their `bot.json` rather than by count
      alone. This fixes incorrect matching when teams, stray bots, or duplicate bot instances are present — only bots
      whose identity matches an expected slot are counted.
    - Configurable boot timeout via `botConnectTimeout(java.time.Duration)` on `BattleRunner.Builder`. Default remains
      30 seconds. Replaces the previous hard-coded constant.
    - Boot progress reporting: `BattleHandle.onBootProgress` fires on every `BotListUpdate` and every 500 ms during the
      wait loop, delivering a `BootProgress` snapshot with `expected`, `connected`,
      `pending` identity maps, `elapsedMs`, and `timeoutMs`. Intended for GUI progress dialogs.
    - Team member directory validation: `BooterManager.validateBotDir()` now checks that every directory listed in
      `teamMembers` exists as a sibling directory at battle-start time, throwing
      `BattleException` with the missing member name before any bot process is launched.
    - Added `suppressServerOutput()` builder option to opt out of routing embedded server and booter stdout through JUL.
      By default, all output from the embedded server and booter processes is logged at INFO level with `[SERVER]` and
      `[BOOTER]` prefixes. Call `suppressServerOutput()` on the builder to silence this when you configure your own
      logging.

### 🐞 Bug Fixes

- Runner API:
    - Fixed orphaned stdout reader thread in `ServerManager`: if the embedded server process died unexpectedly and
      `ensureStarted()` was called again, the previous reader thread was overwritten without being joined. The thread is
      now joined (with a 500 ms timeout) before a new one is started.
    - Embedded server and booter stdout pipes are now always drained regardless of the `captureServerOutput` setting,
      preventing OS pipe buffer fill-up when output capture is
      suppressed.

## [0.37.0]- 2026-03-04 – Battle Runner API

### ✨ Features

- Battle Runner API ([documentation](https://robocode-dev.github.io/tank-royale/api/battle-runner)):
    - New `runner` module providing a programmatic API for running battles without the GUI.
    - Supports both embedded server (auto-managed lifecycle) and external server connection modes.
    - Synchronous (`runBattle()`) and asynchronous (`startBattleAsync()`) battle execution.
    - Game type presets (Classic, Melee, 1v1, Custom) with full parameter overrides via `BattleSetup`.
    - Battle recording to `.battle.gz` replay files compatible with the Recorder module.
    - Intent diagnostics for capturing raw bot-intent messages per bot per turn (opt-in).
    - Battle control: pause, resume, single-step, and stop running battles.
    - Real-time events: tick, round start/end, game start/end, abort, pause/resume.
    - Structured `BattleResults` with per-bot rankings and detailed scores.
    - Resource management via `AutoCloseable` for server lifecycle, bot processes, and graceful shutdown.
    - Published to Maven Central as `dev.robocode.tankroyale:robocode-tankroyale-runner`.

### 🐞 Bug Fixes

- GUI:
    - #191: GUI freezes from time to time in 0.36.1

- Bot APIs (C#, Python):
    - #192: Fixed `TimeLeft` / `time_left` returning incorrect (often negative) values on Windows. The start timestamp
      for the turn timeout was being captured on the bot thread at dispatch time, which is after OS scheduling has
      already consumed part of the turn budget. The fix aligns C# and Python with Java: the timestamp is now taken on
      the WebSocket thread when the tick is first received (`_ticksStart` / `tick_start_nano_time`), so all time spent
      scheduling and dispatching correctly counts against `TurnTimeout`.
    - C# only: added `timeBeginPeriod(1)` (Windows Multimedia API) to set 1 ms timer resolution for the bot process,
      matching the behavior of the JVM and CPython runtimes. This ensures `Thread.Sleep` durations are accurate to
      ~1 ms instead of the default ~15.6 ms Windows timer granularity.

## [0.36.1] - 2026-02-24 – WonRoundEvent and Python Console Output

### 🐞 Bug Fixes

- Bot API (.NET):
    - #188: Fixed thread-safety issue in `RecordingTextWriter` where concurrent `Console.WriteLine` calls from event
      handlers (e.g., `OnScannedBot`) could cause race conditions, leading to performance degradation, radar lock loss,
      and eventual event queue overflow ("Maximum event queue size has been reached: 256"). The fix adds proper locking
      around all write and read operations, matching the thread-safety pattern used in the Java implementation.
    - Added thread-safety test to verify `RecordingTextWriter` handles concurrent writes correctly.

- Bot APIs (Java, C#, Python):
    - #190: Fixed `WonRoundEvent` not being triggered when a bot wins a round. The `onWonRound()` handler is now invoked
      even if the server doesn't send a separate `WonRoundEvent`, by checking the rank in `RoundEndedEvent` and
      publishing `WonRoundEvent` when rank equals 1.

- Bot API (Python):
    - Implemented missing stdout/stderr redirection to bot console. Python bots can now use `print()` statements and see
      output in the Bot Console, matching Java and C# functionality. This feature was missing since v0.19.0.

- Bot API (All platforms):
    - Fixed the one-by-one error in the event queue size check. The queue now correctly enforces a maximum of 256 events
      instead of allowing 257 events before showing the error message. Changed the boundary check from
      `<= MAX_QUEUE_SIZE` to `< MAX_QUEUE_SIZE` in Java, .NET, and Python implementations.

## [0.36.0] - 2026-02-16 – Team Indicator in Results

### ✨ Features

- GUI:
    - Results window now displays an additional "team" column with a checkmark (✓) indicator showing whether each
      participant is a team or an individual bot.

- Schema:
    - Added `isTeam` flag to `results-for-observer.schema` to support team classification in results data.

### 🐞 Bug Fixes

- GUI:
    - Fixed the ANSI color parsing bug where text following ANSI RESET escape code was rendered in black instead
      of the default white color in the server console window. The RESET handler now correctly sets the foreground color
      to the default color (brightWhite) and preserves font family/size settings.
    - Fixed replaying recorded battles where bot buttons, bot names, and bot versions were not visualized correctly.

## [0.35.5] - 2026-02-13 – Critical Timing Fixes

### 🐞 Bug Fixes

- Server:
    - #180: Fixed critical reentrancy bug in 0.35.4 where `ResettableTimer` executed synchronously on the calling thread
      when delay was 0 (TPS=-1), causing recursive calls to `onNextTurn()`. This led to inconsistent bot behavior, event
      queue overflow ("max event queue size reached: 256"), timing issues, and potential memory problems during
      high-speed battles. The timer now uses `executor.submit()` instead of direct execution when delay is 0,
      maintaining proper thread separation and allowing task cancellation. This preserves both the memory leak fix from
      0.35.3 and timing precision at unlimited TPS.
    - Fixed the issue where the bots turn timeout was not respected when TPS=-1. Turn now completes immediately when all
      bots respond (or at the `turn timeout` deadline), and TPS controls only the visual delay for observers.
    - Thanks to Jan Durovec for reporting these issues! ❤️

## [0.35.4] - 2026-02-12 – Timing Fix for High TPS (BROKEN - DO NOT USE)

**⚠️ WARNING: This version introduced a critical bug. Use 0.35.5 instead.**

### 🐞 Bug Fixes

- Server:
    - #180: ~~Fixed timing regression introduced in 0.35.3 where `ResettableTimer` caused turn delays at high TPS
      (especially TPS=-1). The timer now executes immediately when delay is 0, instead of queueing tasks, restoring
      the original NanoTimer behavior while preserving the memory leak fix. This resolves issues where bots experienced
      inconsistent results and missed events at unlimited TPS.~~

      **BUG**: This fix caused re-entrancy issues by executing on the calling thread instead of the executor thread.

## [0.35.3] - 2026-02-08 – Memory Leak Fixes

### 🐞 Bug Fixes

- Server:
    - #180: Fixed critical memory leak where `NanoTimer` created a new thread for every turn (37M threads over 21 hours
      at 500 TPS), causing `OutOfMemoryError`. Replaced with `ResettableTimer` that reuses a single thread via
      `ScheduledExecutorService`.
    - #180: Fixed the memory leak from unbounded game history accumulation. Now limits turn history to last 2 turns and
      clears old rounds, reducing memory footprint from gigabytes to megabytes during long games.
    - #180: Fixed memory leak from collections not being cleared after the game end (`participantIds`, `botIntents`,
      `participantMap`, etc.), preventing garbage collection of game objects.

## [0.35.2] - 2026-01-28 – Windows Path Fix

### 🐞 Bug Fixes

- GUI:
    - Fixed `InvalidPathException` on Windows when bot directories contained backslashes. Windows paths like
      `C:\robocode\bots\python` were being corrupted because backslash sequences (e.g., `\r`, `\b`) were interpreted
      as escape characters when saved to the properties file. Bot directory paths are now normalized to use forward
      slashes, which work correctly on all platforms.

## [0.35.1] - 2026-01-27 – Automatic User Data Migration

### 🚀 Improvements

- GUI:
    - Added automatic migration of user data files (`gui.properties`, `server.properties`, `game-setups.properties`,
      and `recordings/` directory) from the old location (current working directory or JAR location) to the new
      platform-specific user data directory. This removes the need for manual file migration when upgrading from
      pre-0.35.0 versions.

## [0.35.0] - 2026-01-26 – **BREAKING**: Python Bot API Converted to Synchronous API & Refactored to Use Properties

### 💥 Breaking Changes (Python Bot API)

The Python Bot API has been refactored to match Java and .NET:

1. **Async → Sync**: Converted from `async`/`await` to synchronous blocking API
2. **Properties**: Replaced getter/setter methods with Pythonic property accessors
3. **Typed Team Messages**: Use `@team_message_type` decorator for automatic serialization

#### Quick Migration

| Before (0.34.x)                           | After (0.35.0)                         |
|-------------------------------------------|----------------------------------------|
| `async def run(self):`                    | `def run(self):`                       |
| `await self.forward(100)`                 | `self.forward(100)`                    |
| `asyncio.run(bot.start())`                | `bot.start()`                          |
| `self.get_energy()`                       | `self.energy`                          |
| `self.is_running()`                       | `self.running`                         |
| `self.set_adjust_gun_for_body_turn(True)` | `self.adjust_gun_for_body_turn = True` |

> **Full migration guide**: See the `MyFirstBot` sample bots
> and [Team Messages](docs-build/docs/articles/team-messages.md) article.

### 🚀 Improvements

- **Python Bot API**: Synchronous API with property accessors, typed team messages with `Color` serialization
- **Bot Events Panel**: Optimized logging, improved formatting with ANSI colors, grouped `bulletStates`
- **Recordings**: Now saved to `recordings` subdirectory in user data directory
- **Build**: Replaced Proguard with R8 for code shrinking

### 🐞 Bug Fixes

- Bot API (Java, C#/.NET, Python):
    - Fixed a bug where the user's `OnDeath` callback would not be called when the bot dies. The internal death handler
      was stopping the bot thread before the event queue could dispatch the `DeathEvent` to user callbacks.
      Now the `OnDeath` handler is subscribed to public event handlers with priority 0 (lower than user's default of 1),
      ensuring user's `OnDeath` callback runs before the thread is stopped.

- GUI:
    - Improved performance of the bot console and server log windows under heavy logging.
    - Added **batched updates** to reduce UI freezes when bots or the server produce a high volume of output.
    - Introduced **bounded retention** to prevent the console from consuming too much memory and becoming slow over
      time.
    - Added a new configuration setting `console-max-characters` (default: 10,000) to control the maximum amount of
      text retained in each console window.
        - **How to configure**:
            - Open the `Config` -> `GUI Options` menu in the GUI.
            - Adjust the `Console max characters` value and click `OK` to save.

## [0.34.2] - 2025-12-25 – Added native installer packages for GUI

Native installer packages for the GUI have been added for Windows, Linux, and macOS. These packages might still be early
days. If you find any issues, please report them on the
[issue tracker](https://github.com/robocode-dev/tank-royale/issues).
These packages still require Java 11 or higher to be installed on your system, but they make it easier to install,
uninstall, and run Robocode Tank Royale.
You can still download the JAR version as usual, which works on all platforms with Java 11 or higher installed.

### 🔧 Changes

- Replaced Picocli for Clikt for the server, booter, and recorder.

### 🐞 Bug Fixes

- #178: Fix .sh scripts to use `java` instead of `javaw`
- Fixed a major bug with the Server, Booter, and Recorder where the application ran when `--info` or `--help` was passed
  as a command line argument.

### 🚀 Improvements

- Bot API for C#/.NET:
    - Updated to the newest dependencies.

- GUI:
    - Added native installer packages for the GUI (Windows MSI, macOS PKG, Linux RPM, and DEB).
        - Note: Java 11+ is required and the runtime must be discoverable via the `JAVA_HOME` environment variable. See
          the installation documentation for platform-specific instructions.
    - Settings are now stored in platform-specific user directories to avoid permission issues:
        - Windows: `%LOCALAPPDATA%\Robocode Tank Royale` or `%APPDATA%\Robocode Tank Royale`
        - macOS: `~/Library/Application Support/Robocode Tank Royale`
        - Linux: `$XDG_CONFIG_HOME/robocode-tank-royale` or `~/.config/robocode-tank-royale`
    - You need to move your `gui.properties`, `server.properties`, `game-setups.properties` and the `recordings`
      directory into the new user data directory for the GUI.
    - Added jlink runtime image creation for Windows installers to avoid "Failed to launch VM" issues on some machines.

## [0.34.1] - 2025-11-14 – Fixes on_death in Python Bot API and GUI improvements

### 🚀 Improvements

- GUI
    - #174: Prevent overwriting UI scale property (`sun.java2d.uiScale`) for high DPI displays if already set (via
      command line). Thanks go to [David Martinez Peña](https://github.com/martinezpenya) 👍
        - Fixed for the `sun.java2d.d3d` and `sun.java2d.opengl` as well.

- Updated the devcontainer so it uses the current and full tech stack necessary to build all of Robocode Tank Royale.

### 🐞 Bug Fixes

- Bot API for Python:
    - The on_death() event handler is now called when the bot dies.

## [0.34.0] - 2025-10-19 – Python Support + GUI Improvements

The Python Bot API, sample bots, and documentation are now complete. 🎉
We also added Spanish, Catalan/Valencian, and Danish translations. New translations are always welcome!
Additionally, we introduced UI improvements such as UI scaling and a volume slider in the options.

### 🚀 Improvements

- Bot API
    - #128: Python Bot API + sample bots.
        - A huge thank-you, [Yang Li](https://github.com/yangli2) for the tremendous effort in making this a reality! ❤️
    - .NET version has been updated to .NET 8 and C# 12.

- GUI
    - #168: Added GUI Options dialog with a UI scale setting (100%, 125%, 150%, 175%, 200%, 250%, 300%).
    - #168: Applied full-UI HiDPI scaling using the JVM property `sun.java2d.uiScale`, based on the saved setting.
        - Thanks to [David Martinez Peña](https://github.com/martinezpenya) for proposing the solution that made this
          possible. 💪
    - Added volume slider in the Sound Options dialog.
    - #169: Added Spanish and Catalan/Valencian translations.
        - Thank you, [David Martinez Peña](https://github.com/martinezpenya), for contributing the first translations
          ever—and for helping test the Python API! ❤️
    - Added Danish translations as well.
    - Added an option to switch between Spanish, English, and Danish in the GUI Options dialog.

## [0.33.1] - 2025-09-08 – Fixes for Recording and Replays

### 🐞 Bug Fixes

- Maven Central:
    - Fixed the names of the artifacts in the Maven Central repository, which were lacking the `robocode-tankroyale`
      prefix.
- GUI:
    - #162: Corrected the order of death indicators (skulls) to accurately reflect in-game events.
    - Auto-Record Bug Fix: Resolved an issue where battles were still being recorded after disabling the auto-record
      feature.

### 🚀 Improvements

- GUI:
    - #165: Enhanced "🔴REC" Indicator: Improved the visual design of the recording indicator for better clarity and
      user experience.

## [0.33.0] - 2025-09-06 – Added Recording and Replays

This version adds support for recording and replaying battles (#55: Game replays). All credits go
to [Jan Durovec](https://github.com/jandurovec) for implementing this cool feature! 🤩

The new recording and replay functionality allows you to:

- Record battles and save them for later replay
- View the replay timeline and seek to specific points in time
- See skull markers on the timeline indicating when bots were killed
- Study the standard output from bots in their console windows
- Inspect bot properties and events at any point during the battle

This feature is particularly useful for:

- Debugging bot behavior
- Analyzing past battles in detail
- Learning from recorded matches

### 🚀 Improvements

- Server and GUI:
    - Add support for enabling/disabling server secrets in GUI and server. Server secrets will be disabled by default.
      In earlier versions, the server secrets were enabled by default, which could be annoying when you want to test
      your bots without having to worry about the server secrets.

- Recorder:
    - #55: A new **recorder** application was added, which is a command-line tool for recording battles.

- GUI:
    - #159: Replay feature added.
    - #160: Added replay timeline and allow seeking.
    - #161: Updated round/turn indicator in the arena panel.

### ⚠️ Breaking Changes

- GUI:
    - The GUI configuration file has been renamed from `config.properties` to `gui.properties`. If you have a local
      `config.properties`, rename it to `gui.properties` to retain your settings.
    - The game type setup file has been renamed from `games.properties` to `game-setups.properties`. Rename your
      existing file to `game-setups.properties` to preserve custom setups.

### 🐞 Bug Fixes

- Server, Booter, Recorder:
    - Got rid of `WARNING: A restricted method in java.lang.System has been called` when running with Java 22 or later.

- Booter:
    - Bots could not be booted on Linux. Thanks goes to [Yang Li](https://github.com/yangli2) for fixing this. ❤️

## [0.32.1] - 2025-07-01 – No need of default constructor

### 🚀 Improvements

- Bot API:
    - The `BaseBot()` constructor (instead of `Bot()` constructor) now automatically searches for the bot config file (
      .json) and falls back on using environment variables if the config file could not be found.
    - The sample bots could not start up due to this issue.

### 🐞 Bug Fixes

- Booter
    - Fixed the escaping issue with double backslash characters.
- Sample Bot:
    - Fixed a bug in TrackFire.cs where the turret misaligned while attempting to lock onto a target, causing it to
      oscillate erratically during target acquisition.

## [0.32.0] - 2025-06-24 – Added Booter Error Log

This version makes it easier to figure out why a bot will not boot from a bot directory.

### 🚀 Improvements

- Booter:
    - The Booter now logs errors to standard error. Each error is recorded with the name of the bot directory in which
      the error occurred.

- GUI:
    - When booting one or more bots that crash with an error, the error is written to a Booter Error Log window. This
      window automatically appears and displays the detected errors for each bot directory that caused an error.

- Bot API:
    - The `Bot()` constructor now automatically searches for the bot config file (.json) and falls back on using
      environment variables if the config file could not be found.
        - Due to this change, the default constructor has been removed from all sample bots.

## [0.31.0] - 2025-06-19 – Improved Graphical Debugging

### ⚠️ Breaking Changes

#### Bot API Updates

If you are using `Color` and the `getGraphics()` method or `Graphics` property, you may need to update your bot(s).

**Changes Implemented:**

- Platform-specific graphics classes and external graphics libraries, such as those for SVG, have been replaced with the
  following classes:
    - `IGraphics`
    - `SvgGraphics`
    - `Color`
    - `Point`

**Reasons for Changes:**

- Simplify the development and maintenance of the debugging graphics feature.
- Avoid using platform-specific features and frameworks that are not compatible across all platforms and operating
  systems.
- Enhance the speed of SVG serialization, reduce RAM usage, and decrease distribution archive sizes.
- Standardize the API for debug painting across all Bot APIs (Java, .NET, Python, etc.).

**Specific Updates:**

- **Java:** A new package `dev.robocode.tankroyale.botapi.graphics` has been introduced.
- **.NET:** A new namespace `Robocode.TankRoyale.BotApi.Graphics` has been introduced.

**Additional Information:**

- Sample bots have been updated to use the new classes.

### 🐞 Bug Fixes

- BotAPI:
    - #143: .NET bot fails to start with no or invalid country code.
    - #144: .NET bots run only on Windows since 0.30.0.
        - SvgNet requires `System.Drawing`, which is not available on Linux and macOS. Hence, the new Robocode specific
          `IGraphics` interface must be used instead.
    - Fixed the generated bot Bash scripts for .NET so `dotnet build` will be called if the `bin` folder is missing.
- GUI:
    - Fixed multiple issues with the "No bot directory root has been configured" error dialog.

## [0.30.2] - 2025-06-02 – GUI auto-scales the Battle View

### 🚀 Improvements

- GUI:
    - The GUI now automatically scales the battle view when it’s first displayed and adjusts it dynamically as the
      window is resized.
      screen only. This is helpful when running on systems with multiple screens.
        - Thanks to [Jan Durovec](https://github.com/jandurovec) for this nice improvement. ❤️
    - Windows now open on the "active" screen where the mouse cursor is located instead of showing up on the primary.
    - Fixed an issue where Robocode could get stuck showing a black window.

### 🐞 Bug Fixes

- Server:
    - #132: Fixed several race conditions and synchronization issues on the server side.
- BotAPI / Sample Bots:
    - Fixed issue where the Fire sample bot did not lock the radar on its target.
- Sample Bots:
    - The `Fire` and `MyFirstBot` sample bot did not turn perpendicular to the bullet direction anymore, and the RamFire
      did not drive directly towards its target.
- Documentation:
    - #136: The turn timeout and ready timeout were incorrectly specified in ~~milliseconds~~ instead of **microseconds
      **.
      1 millisecond = 1/1,000 of a second, where 1 microsecond = 1/1,000,000 of a second

## [0.30.1] - 2025-03-08 – Fixes to sample bots

This version provides bug fixes to the sample bots only.

### 🐞 Bug Fixes

- Sample Bots: Corners and Walls did not turn correctly towards the wall.

## [0.30.0] - 2025-02-07 – Added Graphics Debugging

### 🚀 #116: Graphical Debugging Implemented Using SVG

**Graphical debugging**, known from the original game, has been implemented using SVG with the [JSVG] library. This
allows you to paint graphics objects on the battlefield while the game is running, making it easier to visualize
elements such as estimated target positions. This feature is available for both the Java API and .NET API.

#### Java API

- Use the new `getGraphics()` method to return a `java.awt.Graphics2D` object for painting objects.
- Thanks to [Tobias Zimmermann](https://github.com/Cu3PO42) for bringing this feature to the Bot Java API, for
  suggesting the use of SVG and [JSVG], and also adding `transform-box` to JSVG to mirror texts. ❤️
- Thanks also to [Jannis Weis](https://github.com/weisJ) for providing [JSVG] and helping fix an issue with using the
  correct libraries for JSVG. Make sure to [buy him a coffee](https://buymeacoffee.com/weisj). 😊

#### .NET API

- Get a `SvnNet.SvgGraphics` context for painting by using the `Graphics` getter.
- The .NET API uses the [SvgNet](https://github.com/managed-commons/SvgNet) library. The `SvgGraphics` is context used
  similarly to
  [System.Drawing.Graphics](https://learn.microsoft.com/en-us/dotnet/api/system.drawing.graphics?view=windowsdesktop-6.0).

> ⚠️ **Graphical Debugging must be enabled before anything is rendered to the battlefield.**

#### Enabling Graphical Debugging

Graphical debugging can be enabled from the Properties pane of the bot console by toggling the
`Toggle Graphical Debugging` button. Graphical debugging _is not_ enabled per default, and must be applied to individual
bots.

#### Sample Bots

A new sample bot, PaintingBot, has been introduced to showcase the use of debug painting in both Java and C#.

#### Schema changes

- A new `debugGraphics` (string) field has been added to the `bot-intent` schema. This field allows you to provide SVG
  content as a string, which will be rendered if debugging graphics is enabled. Note that not all SVG functionality is
  supported by [JSVG](https://github.com/weisJ/jsvg) used for rendering SVG onm the battlefield.

- A new `isDebuggingEnabled` (boolean) field has been added to the `bot-state` schema. This field specifies whether the
  debugging graphics feature is enabled for the bot. The `debugGraphics` field will only be used if `isDebuggingEnabled`
  is set to `true`.

### 🐞 Bug Fixes

- Server:
    - Log messages printed out `{}` instead of content.
- GUI:
    - Fixed issue with the switch button in the Server Options for switching between using a local and remote server.

## [0.29.0] - 2024-01-19 – All about colors

This version causes a breaking change for the Color class. The Color class was removed and replaced with the Color class
used with the used platform (Java or C#).

### 🚀 Improvements

- Server:
    - Replaced SLF4J's SimpleLogger with own implementation based on the SLF4J API to provide ANSI colors on log levels.
- Bot API:
    - Replaced using Robocode's Color class with `java.awt.Color` for Java and `System.Drawing.Color` for C#.
- Sample Bots:
    - Updated to use the Color class from the used platform (Java or C#).

## [0.28.1] - 2025-01-01 – Fixed Survival Scores

### 🐞 Bug Fixes

- Server:
    - #122: Fixed the scoring issue with Survival Score and Last Survivor Bonus, which became too large after multiple
      rounds.
    - The server now only listens to the wildcard IP address (0.0.0.0), meaning it will listen on all available network
      interfaces for the specified port (IPv4 and IPv6) on the system.

## [0.28.0] - 2024-12-21 – Inherited Socket Support

### 🚀 Improvements

- Server:
    - **#115: Inherited Socket Support**: You can now start the server with an inherited socket from another process by
      setting `--port=inherit` or `-p=inherit`.
    - **Socket Activation**: This enables the use of [Socket Activation], such as with `systemd` or `xinetd` on Linux.
    - **Lazy Loading**: With socket activation, the server will only start when a client accesses the port for the first
      time.
    - **Setup Guide**: A detailed guide for setting up socket activation for the Robocode server is
      provided [here](https://github.com/robocode-dev/tank-royale/blob/main/server/docs/systemd-socket-activation.md).
    - **Acknowledgements**: Special thanks to [Tobias Zimmermann](https://github.com/Cu3PO42) for bringing this feature
      to Robocode! ❤️
    - **Java WebSocket**: Gratitude to [Marcel Prestel](https://github.com/marci4) for providing and maintaining the
      excellent [Java WebSocket](https://tootallnate.github.io/Java-WebSocket/) library, making WebSockets accessible
      for small stand-alone Java applications without the need for a traditional server. 🏆

### 🐞 Bug Fixes

- Bot API:
    - The internal event handling for state updates in the Bot API is now separated from bot event queue handling.
      Internal events are processed immediately, while bot events are eventually processed through the event queue,
      if at all.

## [0.27.0] - 2024-12-02 – Lots of bug fixes

This version fixes various issues, some found when running legacy bots with the [Robocode API Bridge].

### 🚀 Improvements

- Bot API for .NET:
    - Improved performance of event handling by replacing `ImmutableList` and `ImmutableHashSet` with `List` and
      `HashSet` protected with `lock` blocks.
- Bot API for Java:
    - Improved performance of event handling by replacing `CopyOnWriteArrayList` with
      `synchronizedList(new ArrayList<>())`.

### 🐞 Bug Fixes

- Server:
    - #117: Server process consumed 100% of a CPU core even when the game is paused (due to nano timing).
        - Thank you, [Tobias Zimmermann](https://github.com/Cu3PO42), for spotting and fixing this nasty issue! ❤️
- Bot API:
    - [#1](https://github.com/robocode-dev/robocode-api-bridge/issues/1): Wrapped bot throws an exception. Fixes in both
      the Bot API and [Robocode API Bridge](https://github.com/robocode-dev/robocode-api-bridge) caused by the Tank
      Royale Bot APIs.
    - #119: Battle was not starting on Windows 11 and 10 after installing Docker Linux subsystem.
    - Bridged bots run multiple threads after round 2 due to old thread(s) hanging that could not be stopped.
    - Bridged bots could not be restarted as they got a connection error with the WebSocket when restarting.
    - .NET bots sometimes exited when running at max speed without any exceptions occurring.

## [0.26.1] - 2024-11-03 – Various bug fixes

### 🚀 Improvements

- Server:
    - When no bullets or functioning bots remain, the round ends immediately as a draw to speed things up.
- Bot API and schema:
    - The `enemyCount` field was moved from `TickEvent` for the bot (`tick-event-for-bot`) into `BotState`. This change
      provides an observer with the enemy count for each bot, which is useful for the UI to display the current enemy
      count for a specific bot. This also benefits the properties in the Bot Console.

## [0.26.0] - 2024-10-20 – Support for remote servers

### 🚀 Improvements

- GUI:
    - Replaced the `Server` -> `Select Server` with `Config` -> `Server Config` which makes it possible choose between
      using a local or remote server. In addition, it is possible to add, edit, and remove remote servers, and it is
      possible to specify a controller and a bot secret to allow the GUI and bots to access the remote server.
    - If a booted bot process exits, it is now removed from the active New Battle window.
- Booter:
    - When a pid of a bot process cannot be found, the Booter writes out a `load {pid}` to indicate that a process could
      not be found with the provided pid.

### 🐞 Bug Fixes

- GUI:
    - Control Panel was shown when server was stopped or rebooted.

## [0.25.0] - 2024-10-11 – Fix for Ranks and Event Queue improvements

### 🐞 Bug Fixes

- Server:
    - #108: The ranks were wrong as they did not reflect the total score and placements
- Bot API:
    - An interruptible event was not interrupted when `setInterruptible(true)` was called. The event queue contained
      more bot events than it should, due to this bug.
    - The event queue and dispatcher was improved so it is closer to the implementation for the original Robocode.

## [0.24.4] - 2024-09-19 – Support for IPv6 endpoints

### 🚀 Improvements

- GUI:
    - Improved the support for IPv6 endpoints.

### 🐞 Bug Fixes

- GUI:
    - #96: When starting a battle when a remote server was selected, no window was opened when starting a battle. Now an
      error message will show up plus the dialog for selecting a (local) server.
- Server
    - #85: Fixed edge case when crossing the speed of 0, when target speed is 0.

## [0.24.3] - 2024-08-26 – bug fixes

### 🐞 Bug Fixes

- Server
    - #101: The `ServerHandshake` now includes the `GameSetup` containing information about the current game setup.
    - #104: `ServerHandshake.version` was returning a string with the name of the server + version instead of just the
      version.

## [0.24.2] - 2024-08-22 – bug fixes

### 🚀 Improvements

- Server:
    - ~~#101: The `ServerHandshake` now includes the `GameSetup` containing information about the current game
      setup.~~

### 🐞 Bug Fixes

- Server
    - #102: Double events were sent to observers and controllers from the server.

## [0.24.1] - 2024-07-13 – Support for local IP addresses

### 🚀 Improvements

- Server:
    - #96: It is now possible to connect to the local server with local IP addresses like the wildcard IP address
      0.0.0.0, or a local IP address like e.g. 10.0.0.106 or 192.168.56.1.
- GUI:
    - Improved validation of URL when attempting to add new server URL.

## [0.24.0] - 2024-06-14 – Minor bug fixes and code improvements

### 🚀 Improvements

- Code:
    - Lots of code smells have been fixed in this version, and lots of code has been refactored to make it easier to
      understand and maintain.

### 🐞 Bug Fixes

- Bot API:
    - Sometimes the RoundEnded event was not received by a bot.
    - Java API only: `BotInfo.setGameTypes()` is now taken a Set instead of a List as input parameter.
    - Some public methods on abstract classes were changed to protected methods.
- Server:
    - Scores in the results table are now sorted, so bots with the highest total score is put in the top of the results.
- Java archive (jar) files:
    - The jar files containing javadoc documentation and source files was not given the correct name when they were
      built and published to the repositories.

## [0.23.2] - 2024-05-21 – Bug fixed booting + enabling/disabling bot directories

### 🐞 Bug Fixes

- Booter:
    - #89: Major bug fix: Booting and unbooting multiple bots did not work anymore.

### 🚀 Improvements

- GUI:
    - Bot directories can now be enabled and disabled on the Bot Root Directories Config dialog. In addition, multiple
      directories can be selected with the file dialog, and multiple files can be removed from the Bot Root Directories
      list.

## [0.23.1] - 2024-05-18 – Fixing missing stdout and stderr messages

### 🐞 Bug Fixes

- Bot API:
    - #87: Fixed shaking when bots are standing still with the recent fix in server.rules.math::calcNewBotSpeed (#85)
    - Fixed issue with the Java API, where some messages sent to stdout and stderr were not written out to the bot
      console.

## [0.23.0] - 2024-04-29 – Fixing movement bugs in bot APIs

### 🐞 Bug Fixes

- Bot API:
    - #80: Fixed a major bug causing the body, gun, and radar to move in a wrong angle, which could be seen with
      the Walls sample bot.
    - #85: Fixed a major bug in server.rules.math::calcNewBotSpeed. Thanks goes to Mark (mnbrandl) for fixing this! 😊
- Server:
    - #83: Bots receives skipped-turn-event events while dead.

### 🚀 Improvements

- Bot API:
    - Extended the GameStartedEvent with the InitialPosition property containing the starting position and shared
      direction of the body, gun, and radar. This helped on fixing #80
    - Renamed the "Angle" property of InitialPosition to "Direction".

## [0.22.2] - 2024-03-29 – Fixing issues with Bot Console

### 🚀 Improvements

- GUI:
    - Console windows: Added support for 8-bit and 24-bit ANSI colors.

### 🐞 Bug Fixes

- Bot API:
    - Entire lines output to `System.out` and `System.err` were sometimes missing in the bot console.
- GUI:
    - Bot Console: Turn numbers was sometimes written in the middle of the output, and newlines could occur in the wrong
      places.

## [0.22.1] - 2024-01-07 – GUI improvements for Linux

### 🐞 Bug Fixes

- GUI:
    - #81: Fixed tooltips they are consistent on the entire UI, and works on NixOS (Linux) as well.
    - Fixed the TPS slider, which was not displayed correctly - especially with the height.
    - Added border to the Bot Root Directories Config dialog.

### 🚀 Improvements

- GUI:
    - Improved the Bot Console and Server Console to use anti-aliased text.

## [0.22.0] - 2023-12-03 – Improved ANSI support

### 🚀 Improvements

- Bot API:
    - When running a bot from the command line, `stdout` and `stderr` will also be printed out in the bot console.

- GUI:
    - The Bot Console and Server Log have been totally rewritten to improve performance (is not used HTML rendering
      anymore)
    - The logo in the server log in now shown with ANSI colors per default (might not work on all systems?)
    - The bot console now writes out the turn numbers from the previous turn, when output from the bot is written out.

### 🐞 Bug Fixes

- GUI:
    - #75: The GUI could get stuck and become unresponsive when displaying the busy pointer on Ubuntu.
    - #77: Unreadable Server Log on NixOS.
    - The scores presented after the battle has ended was not accumulated for all rounds, and hence incorrect.
    - The Properties tab of the Bot Console did not show any values when initialized, e.g. when game is paused.
    - Some stdout text from the bot were sometimes missing in the Bot Console. Solved by using the EDT.

## [0.21.0] - 2023-10-02 – Improved scoring

### 🚀 Improvements

- Bot API:
    - Added `stop(boolean overwrite)` and `setStop(boolean overwrite)` to overwrite the saved movement from a previous
      call to `setStop()` with current movement values. These methods provide backwards compatibility with the original
      Robocode with the [Robocode API Bridge] (in progress).
    - Due to the removal of Java's `Thread.kill()` and .NET's `Thread.Abort()`, the main thread of the bot is no longer
      being killed by those methods is those threads could not be interrupted. Instead, the bot process it shut down
      with an error message:
      `The thread of the bot could not be interrupted causing the bot to hang. So the bot was stopped by force.`

### 🐞 Bug Fixes

- Scoring:
    - Fixed various scoring issues when comparing to original Robocode.
- Bot API:
    - Fixed race condition when adding custom events.
- Bot Console:
    - Events are only printed out while the bot is alive or has just died.
    - The DeathEvent was missing.

## [0.20.1] - 2023-09-15 – Added Events to bot console

### 🚀 Improvements

- Bot Console (in GUI):
    - Added an [Events tab] on the Bot Console that continuously dumps the bot events as they occur.
    - Bot property column values on bot consoles are now written in bold and with a monospace font.

## [0.20.0] - 2023-08-23 – Added support for teams and droids

### 🚀 Improvements

- Team support:
    - Added support for teams, where a team is a group of bots that work together to battle against other teams (or
      bots). Each team member can communicate with the other teammates, which is crucial to share information and
      coordinate movement and attacks.
- Droid interface:
    - A **Droid** interface was added that turns a bot into a droid bot, which has no radar but has 120 initial energy
      points instead of the normal 100 starting points. The droid is intended for team bots.
- Sample Bots:
    - **MyFirstTeam** has been added to demonstrate how to set up a team.
    - **MyFirstLeader** is a team bot that has been added to demonstrate a (leader) bot that is responsible for scanning
      enemy bots and sending the coordinates of the current target, to fire at, to the teammates.
    - **MyFirstDroid** is a team bot that is a droid, and hence comes with no radar, and relies entirely on the
      MyFirstLeader to send data about which target coordinate to fire at.
- Bot API:
    - Added the `TeamMessageEvent` which is received when another team member has sent a message.
    - Added these new methods to support teams:
        - `getTeammateIds()` to get the ids of all teammates in a team.
        - `isTeammate(botId)` to check if a bot with a specific id is a teammate or opponent.
        - `broadcastTeamMessage(message)` broadcasts a message to all team members.
        - `sendMessage(botId, message)` sends a message to a specific team member.
        - `onMessageReceived(TeamMessageEvent)` is an event handler to take action when a team message is received.
    - Added the `Droid` interface (no scanner, but 120 initial energy points).
- Booter:
    - Updated to support booting teams besides bots.
    - Changed the name of the `run` command into `boot`.
    - Changed command option `-T` into `-g` for filtering game rules (`-t` is now used for the team-only option).
    - Added `--botsOnly` and corresponding `-b` option flag for filtering on bots only (excluding teams).
    - Added `--teamsOnly` and corresponding `-t` option flag for filtering on teams only (excluding bots).
- GUI:
    - Added `Directory Filter` dropdown to filter on bots and teams.
    - The results window now aggregates scores for teams as well as bots and got a wider "Name" column. Some column
      names were changed, and tooltips were added to fields containing values/scores.
- Schema:
    - Renamed `bot-results-for-bot` and `bot-results-for-observer` into `results-for-bot` and `results-for-observer` as
      participants now include teams and not just bots.

### 🐞 Bug Fixes

- GUI:
    - The GUI could get stuck and become unresponsive when displaying the busy pointer.

## [0.19.3] - 2023-08-04 – Maintenance

### 🚀 Improvements

- Server and Booter:
    - The command-line options and commands are now case-insensitive.
    - Changed `--botSecrets` and `--controllerSecrets` into `--bot-secrets` and `--controller-secrets`.
- GUI:
    - The standard output and standard error messages are now being cached within the GUI for the current round for the
      individual bots, so the history of the round is available when opening the bot consoles.
    - Changed the display name of the bot on the bot buttons and bot console title.

### 🐞 Bug Fixes

- Server:
    - Allow bots to connect to non-localhost addresses (PR [#69]).
- Bot API:
    - Fixed broken unit test for .NET.
- GUI:
    - Fixed issue, where the battle graphics was not displayed when starting the battle.
    - Fixed calculation of 1st, 2nd, and 3rd places.

## [0.19.2] - 2023-04-05 – Added bot properties tab

### 🚀 Improvements

- GUI:
    - A new Properties tab was added to the bot console, which shows the values of all bot properties for the current
      turn. This is useful for viewing the exact values and for debugging a bot.
    - When displaying an arena bigger than the window showing the battle, the window is now zooming into the battle
      arena automatically so that the visual field fits into the window.

### 🐞 Bug Fixes

- GUI:
    - When changing the game type for a battle, the arena did not change visually to accommodate to a new size of the
      battle arena.
- Server:
    - #66: The server is not accepting the full range of ports.

## [0.19.1] - 2023-02-26 – Maintenance

### 🚀 Improvements

- GUI:
    - Console windows for bots and the server now supports 3-bit and 4-bit [ANSI colors][ANSI escape code] (foreground
      and background) and simple text formatting.
    - Added `bot died` info to the bot console.
    - Direct3D and OpenGL acceleration is now disabled under Windows. You can add those yourself when you start up
      the GUI using the `java` command. Read more about it
      [here](https://docs.oracle.com/javase/7/docs/technotes/guides/2d/flags.html).

### 🐞 Bug Fixes

- Server:
    - #60: The round-ended event was not sent, in the round when the game ended.
- GUI:
    - Joined Bots list was not updated (removing bots) when the game is aborted.
    - The properties `sun.java2d.d3d`, `sun.java2d.noddraw`, and `sun.java2d.opengl` is now omitted under macOS.
    - Fixed label colors for fields showing country codes (with flags).
    - Sometimes flags were "empty" when clicking on a joined bot.
- Bot API for .NET:
    - Issue where the `Game Types` field was overwritten by `Country Codes` (could be seen on the UI).

## [0.19.0] - 2023-01-28 – Added bot console windows

### 🚀 Improvements

- GUI:
    - A side panel has been added to the right side of the battle view with buttons for each participant bot. When
      clicking one of these buttons, a bot console window is opened that prints out the standard output (stdout) and
      standard error (stderr) from the bot.
    - Added 'OK', 'Clear', and 'Copy to clipboard' buttons to the server log window.
    - Renamed buttons named "Dismiss", used for closing dialogs, to "OK".
- Bot API:
    - When a bot is booted, its standard output (stdout) and standard error (stderr) streams are redirected to the bot
      intent sent to the server, which forwards the data to the observers, making it possible to show stdout and stderr
      in the bot console windows.
    - #46: The `RoundEndedEvent` now contains a `results` field containing the accumulated results by the end of a
      round.
- Schema:
    - #46: The `round-ended-event` has been split up into `round-ended-event-for-bot` that provides only results for a
      bot,
      and `round-ended-event-for-observer` that provides the results for all bots for observers.

### 🐞 Bug Fixes

- Bot API:
    - `setAdjustRadarForGunTurn()` was made compatible with orig. Robocode.
    - Fixed issues with moving and turning, which could override the set target speed and turn rates, when these had
      been set explicitly on the API.

## [0.18.0] - 2022-12-18 – Improvements to the Bot APIs

### 🐞 Bug Fixes

- Bot API:
    - #52: The Bot API used comma-separated strings for `authors`, `countryCodes`, and `gameTypes` when parsing
      the `BotInfo` schema even though the [BotInfo/BotHandshake] uses string arrays. This was fixed to keep Bot APIs
      consistent with the schemas.
    - #53: Major bug where each bot received the bullet states of _all_ bullets on the battle arena, including bullets
      fired by other bots, and not just the bullets fired by the bot itself.
    - ~~Fixed issues with moving and turning, which could override the set target speed and turn rates, when these had
      been set explicitly on the API.~~
    - Reading the turn rates and target speed for the current turn did not reflect if the turn rate and/or target speed
      had been set for the current turn.
- Bot API for .NET:
    - Setting the max speed and max turn rates for the body, gun, and radar did not work.
- GUI:
    - Fixed rendering issues on Windows (and Linux?) when using accelerated hardware, by setting these Java properties
    - as defaults:
        - `sun.java2d.d3d=false` (turn off use of Direct3D)
        - `sun.java2d.noddraw=true` (no use of Direct Draw)
        - `sun.java2d.opengl=true` (turn on OpenGL)

### 🚀 Improvements

- Bot API:
    - `addCondition()` and `removeCondition()` now returns a boolean value instead of void.
    - Condition can take an optional lambda expression as an input parameter as an alternative to overriding the test()
      method.
    - Added more test units, which did show bugs within the Bot API for both Java and .NET.
    - The event priority order was reversed to match the order used with the original Robocode.

## [0.17.4] - 2022-10-16 – Maintenance

### 🚀 Improvements

- Server:
    - Added `--tps` option to the server for setting the initial Turns Per Second (TPS).
    - Fire assistant is now only activated, when both the radar and gun are pointing in the same direction for two turns
      in a row (like original Robocode).
- GUI:
    - Switched the order of the Game Type dropdown and Setup Rules button.
    - The positioning of the battle view is now controlled by dragging the mouse, instead of clicking.
    - Now sends the current tps to the server when changing game type.
    - Added `Help` menu item under the Help menu, which opens browser to the documentation of the GUI.
- Release
    - All Java artifacts are now sent to Maven Central, i.e. Bot API, GUI, Server, and Booter.
    - New assets are now added to releases: Server and Booter
    - Added build scripts to automated creating (draft) releases using GitHub REST API.
    - Improved layout for release notes.
    - Optimized ProGuard rules for compressing the Booter and Server jar archives even more, making the jar archive for
      the GUI smaller as well.

### 🐞 Bug Fixes

- Bot API and GUI: Fixed various concurrency issues.

## [0.17.3] - 2022-10-02 – Maintenance

#### ⚡ Changes

- #43: Possibility to identify bot from Process to Tick:
    - Went back to boot process `pid` instead of using the `boot id`.

### 🚀 Improvements

- Sample bots: Improved the Fire sample bot by removing unnecessary code.

### 🐞 Bug Fixes

- Server:
    - The fire assistant was not always activated.
    - #45: Server is kidnapping CPU cycles
        - Game is now aborted, when the last bot participating in a battle is leaving (disconnecting).
        - Timer used for turn timeout was not stopped, when a battle had ended/aborted.
        - Freeing heap memory used for storing game state when game is ended/aborted.

## [0.17.2] - 2022-09-20 – Added sounds

### 🚀 Improvements

- GUI/Sounds: [#4: Fire and Explosion Sounds:
    - Added feature to play sounds to the GUI.
    - Added Sound Options to configure sounds.
    - Note that sound files are distributed independently besides the GUI from the [sounds releases].
- GUI: Moved the ~~Dismiss~~OK button on the Bot Root Directories configuration down under the Add and Remove buttons,
  and adjusted the size of the dialog.

### 🐞 Bug Fixes

- GUI: When the Bot Directories on the Select Bots for Battle window is updated, all selections are now being cleared.

## [0.17.1] - 2022-09-15 – Identifying a bot from Boot-up to Ticks

### 🚀 Improvements

- #43: Possibility to identify bot from Process to Tick:
    - Added `sessionId` and `bootId` to these schemas: `bot-handshake`, `bot-state-with-id`, and `participants`

### 🐞 Bug Fixes

- Bot API:
    - The `onWonRound(WonRoundEvent)` was not triggered as soon as the round has ended.
    - The blocking turn methods for turning the body, gun, and radar invoked `go()` twice internally making the turning
      turn in "step" and take the double amount of time.
    - Some events already handled were not removed from the event queue.

## [0.17.0] - 2022-09-06 – Introduction of process id

The _boot id_ concept introduced in release 0.16.0 has been rolled back and the _pid_ is used with the Booter again.
However, a new concept is introduced to replace the _boot id_, which is the _process id_.

When a client (bot, controller, observer) is connecting to a server, the server will generate a unique session id and
send to the client via the `server-handshake`. The session id is used to uniquely identify the running instance of a
client, and the client _must_ send back the session id when sending its handshake to the server, e.g. a `bot-handshake`.

The _session id_ is replacing the _boot id_ as the boot id is only available with bots being booted, and which might
only be unique locally, but across multiple systems. With the _session id_, all clients have a single and unique id.

### 🚀 Improvements

- GUI:
    - Improved sorting of the bot directories list to be case-insensitive.

### 🐞 Bug Fixes

- Server:
    - The `server-handshake` was missing the name and version.

## [0.16.0] - 2022-08-31 – Introduction of boot id

### 🐞 Bug Fixes

- Bot API:
    - Fixed `waitFor(Condition)` so it executes before checking the condition the first time.
- Server
    - Fixed major bug where the firepower was not limited at the server side.
    - Adjusted gun to fire at gun direction for a new turn.
- GUI:
    - Issue with reading huge bot list from the booter when reading from stdin.

## [0.15.0] - 2022-08-17 – Added fire assistance

### 🚀 Improvements

Fire assistance:

- Added fire assistance known from the original Robocode game. Fire assistance is useful for bots with limited
  aiming capabilities as it will help the bot by firing directly at a scanned bot when the gun is fired, which is a
  very simple aiming strategy.
- When fire assistance is enabled the gun will fire towards the center of the scanned bot when all these conditions
  are met:
    - The gun is fired (`setFire` and `fire`)
    - The radar is scanning a bot _when_ firing the gun (`onScannedBot`, `setRescan`, `rescan`)
    - The gun and radar are pointing in the exact the same direction. You can call `setAdjustRadarForGunTurn(false)` to
      align the gun and radar and make sure not to turn the radar beside the gun.
- When calling `setAdjustRadarForGunTurn(true)` then fire assistance will automatically be disabled, and when calling
  `setAdjustRadarForGunTurn(false)` then fire assistance will automatically be disabled.
- Added new `setFireAssist(enable)` to enable and disable the fire assistance explicitly.

GUI:

- Updated various tooltip texts for the dialog used for starting battles.

Bot info:

- The `gamesTypes` field is no longer required with the JSON config file of a bot. When it omitted, the bot will be able
  to participate in _all_ game types. When defined, the bot will only be able to participate in battles with the game
  type(s) defined within this field.
- The `gameTypes` field has been removed from the sample bots, as all sample bots can participate in any game type.
  At least for now.

### 🐞 Bug Fixes

- GUI & Booter:
    - Fixed major bug as the GUI and booter did not filter out bots based on their selected game types.
    - Fixed issue with parsing JSON bot info file for the optional fields `description`, `countryCodes`, `homepage`,
      causing an issue with loading a bot, when these fields were omitted.
    - Fixed issue with parsing json content within the JSON bot info due to text encoding.
- Bot API:
    - Corrected the description of the `onScannedBot()` event handler.
- Server:
    - TPS: When TPS was set to 0 and the battle was restarted, the battle ran in max TPS.

## [0.14.3] - 2022-08-07 – Fixed setting adjustment turns

### 🐞 Bug Fixes

- GUI:
    - NullPointerException when running robocode-tankroyale-gui-0.14.2.jar ([#38])
- Server:
    - `AdjustRadarForBodyTurn` had no effect on the server side.
    - `updateBotTurnRatesAndDirections` did not work properly with adjustments for body and gun turn when
      using `setAdjustGunForBodyTurn`, `setAdjustRadarForGunTurn`, and `setAdjustRadarForBodyTurn`.
- Sample bots
    - Updated Corners to start at random corner when a new battle is started.

## [0.14.2] - 2022-07-29 – Added Velocity Bot

### 🚀 Improvements

- Bot API:
    - The run() method is now forced to stop/abort (in case the run method enters and infinite loop).
- Server:
    - Bots are not "sliding" along walls anymore when hitting those.
- Sample bots
    - Added _VelocityBot_ that demonstrates use of turn rates.

### 🐞 Bug Fixes

- Bot API:
    - Setting the turn rates with the `Bot` class did not work properly (bot, gun, and radar did not turn at all).
- Server:
    - Fixed calculation of 1st, 2nd, and 3rd places with the end results.
    - Fixed issue with restarting a game that has ended.
    - Removal of NullPointerExceptions occurring with max TPS.
- GUI:
    - TPS:
        - Loop could occur when typing in the TPS, where TPS would continuously change to different values.
        - Setting the TPS to max could be reset to default TPS when restarting the battle or GUI.
    - GUI client was registered multiple times with the server.

## [0.14.1] - 2022-07-14 – Added BotInfo builder

### 🚀 Improvements

- Bot API:
    - BotInfo:
        - A builder has been provided for creating `BotInfo` instances.
        - Size constraints have been added for the individual `BotInfo` fields/methods.
        - Various bug fixes for BotInfo.
    - GameTypes:
        - Added missing game type for "classic" game + updated documentation.

## [0.14.0] - 2022-07-03 – Adjustments for bot events

#### Changes

- Bot API:
    - Adjusted the bot events to make it easier to make a bridge between legacy Robocode bots and Tank Royale
      later ([#12]).
    - Introduced `BotDeathEvent` and `HitByBulletEvent`.
    - Made priority values for`DefaultEventPriority` public, and changed event priority order to match the original
      Robocode game.
    - Bot event priorities can now be read and changed.
- GUI:
    - The Pause/Resume and Stop button are now disabled when the battle is stopped.
- Schema:
    - Replaced `$type` with `type` to make it possible to use more code generators ([#31]).

## [0.13.4] - 2022-06-06 – GUI improvements + bug fix

### 🐞 Bug Fixes

- Bot API for Java:
    - Major bug fix with `getTurnRemaining()` ([#28]), which returned the wrong value. This could be seen with the
      sample
      bot, Crazy, which did not turn correctly.

#### Changes

- GUI:
    - **Del Key**: It is now possible to use the Del key to remove a selected bot item on the Booted Bots (to unboot),
      Joined Bots, and Selected Bot lists when selecting bots for a new battle.
    - **Unboot All**: An `← Unboot All` button has been added right below the `← Unboot` button.
    - **TPS**:
        - The last used TPS (Turns Per Second) is now restored when starting up the GUI again.
        - Added a "Default TPS" button to reset the TPS to the default setting (30 TPS).
    - **Tool tips**: Added tool tip texts on the Control panel.

## [0.13.3] - 2022-06-01 – Stabilization of Bot APIs

### 🐞 Bug Fixs

- Bot API:
    - Fix for running bots under macOS/bash when bot directory contains whitespaces ([#20]).
    - New fix for issue [#17] (Blocking bot functions do not
      stop bot after finishing)
    - Fix for `setForward(POSITIVE_INFINITE)` which moved the bot slowly forward with no
      acceleration ([#26]).
    - Fixed issue where the event queue was not disabled if `run()` method threw an exception, causing the event queue
      to reach its max limit of unprocessed 256 events.
    - Fixed issue with events being handled 1 turn later than they
      happened ([#8])
- Bot API for Java:
    - Fixed: `IllegalArgumentException: -Infinity is not a valid double value as per JSON specification`
- Server
    - Fixed various issues with invoking SkippedTurns ([#8])
- GUI:
    - When stopping a battle and starting a new one, the Pause/Resume and Stop buttons were disabled.

## [0.13.2] - 2022-05-19 – Fixing issue #23

### 🐞 Bug Fixes

- Bot API for .NET:
    - Setting Bot.TargetSpeed did not set the distance to travel.
    - Fixed issue with turning the body, gun, and radar as first and only thing ([#22], [#23]).
    - Fixed issue with event queue reaching max after bot has terminated execution ([#23]).
- Bot APIs:
    - Added missing documentation about using positive and negative infinity values when moving and turning.

## [0.13.1] - 2022-05-18 – Fixing event queue + country codes

### 🐞 Bug Fixes

- Bot APIs:
    - Fixed issue with rescanning and interrupting current event handler. Redesigned the event queue.
    - Fixed issue with dangling threads not been stopped (interrupted) correctly.
- Bot API for .NET:
    - Fixed issue with country codes (on macOS) which also caused boot up problems for .NET based bots running under
      macOS ([#20]).
    - Fixed issue with bots stopping doing actions when battle is restarted. E.g. the Corners sample bot was affected by
      this issue.

#### Changes

- Bot API:
    - Order of priorities was changed, so higher priority values gives higher priority.
- Sample bots:
    - Optimized TrackFire after fixing rescanning issue.

## [0.13.0] - 2022-05-03 – Bot API for .NET 6

#### Changes

- Bot API:
    - Upgraded the .NET version of the bot API for .NET 6.0 as .NET 5.0 is soon EOL.
    - The id was removed from `BotResults`, and `GameEndedEvent` will only return a single `BotResult` containing
      the battle results for the bot (instead of a list containing results from all bots).
    - Added setting and getting 'adjusting radar for body turn'.
    - The `scan()` and `setScan()` methods were renamed to `rescan()` and `setRescan()`.
    - Added `setInterruptible(boolean interruptible)` / `Interruptable = [bool]` to allow restarting an event handler
      while it is processing an earlier event.
- GUI:
    - A tooltip text has been added to the 'Start button' that shows the minimum or maximum number of participants
      required for starting the battle.
- Protocol:
    - Removal of the `speed` field of the `bullet-state` as the speed can be calculated as: `20 - 3 x power`.
      Hence, there is no need to transfer this information over the network.
    - Moved id from `bot-results-for-bot` to `bot-results-for-observer`.
    - The `scan` field on `bot-intent` was renamed to `rescan`.

### 🐞 Bug Fixes

- Bot API:
    - Blocking bot functions do not stop bot after finishing ([#17]).
    - Fixed issue where event queue would overflow with unhandled events when a bot's `run()` method has ended.
- GUI:
    - The 'Start game' button is now disabled when the minimum or maximum number of participants is not met.

## [0.12.0] - 2022-04-17 – Single stepping battle

#### Changes

- GUI:
    - Added 'Next turn' button beside the pause button in with the control panel to useful for single-stepping a
      battle, e.g. when debugging your bot.
    - Improvements to the battle dialog for selecting game type and bots for a new battle.
        - Added button for setting up rules.
        - Added tooltip texts.
- Protocol:
    - Removal of rgb-value type from the protocol of Bot APIs.
- Bot API:
    - The `Color.fromRgb()` has been replaced by `Color.fromString()`.

### 🐞 Bug Fixes

- Bot API:
    - Make sure the bot terminates (system exit) upon a connection error to prevent dangling bot processes, which
      is usually occurring when terminating the UI, which closes the bot connections.
- GUI:
    - When dragging the battle arena, the graphics were not updated while dragging.

## [0.11.2] - 2022-04-12 – Fix restart issue

### 🐞 Bug Fixes

- Battle does not restart ([#10]).
- Bot API did not stop thread when GameAbortedEvent occurred (when restarting).
- When restarting, a two or more games could be started on the server.

## [0.11.1] - 2022-04-07 – Patch for the GUI

### 🐞 Bug Fixes

- ConcurrentModificationException could occur for `BotSelectionPanel.updateJoinedBots()`. Could not create a new battle.
- Fixed issue with starting a new battle from the menu with Battle → 'Start Battle' a second time.

#### Changes

- "Restart server" has been renamed into "Reboot Server", and the server and battle is fully stopped, and bots are
  disconnected. Confirmation dialog for rebooting now differ between the reboot is a user action or due to a changed
  server setting.
- Control panel (Pause/Resume, Stop, Restart, TPS) is now always shown when first battle has been started. Various
  improvements were done to the control panel.
- A busy cursor is now (automatically) shown when an operation takes some time to perform.

## [0.11.0] - 2022-04-02 – Initial position feature

### 🐞 Bug Fixes

- Fixes for building Java artifacts to deploy to Maven artifactory.
- Bot API:
    - Replaced the environment variable BOT_URL (deprecated) with BOT_HOMEPAGE.
- Bot API for .NET:
    - Added missing public Bot(BotInfo, Uri, string serverSecret) constructor.

#### Changes

- Implemented initial position feature:
    - Add an `initialPosition` field in the JSON file for your bot with a value like `50,50,90` to request a starting
      coordinate at (50,50) with the bot heading toward 90°.
    - A `--enable-initial-position` (or the short version `-I`) must be set to enable the initial positions feature on
      the server. If initial position is not enabled, the bot will start at a random position.
    - Initial positions can be set from the menu with Config → Debug Options → Enable initial start position.
    - The Target.json file of the Target sample has been updated with an `initialPosition` to demonstrate this feature.
- Updated the SpinBot sample bot for C# to demonstrate the use of `BotInfo.FromConfiguration(IConfiguration)`.
- The config file named `misc.properties` has been renamed into `config.properties` and the `server-url` setting was
  moved to the `server.properties` file.
- When the server settings are changed on the GUI, the user will be asked if the server should be rebooted to let the
  changes take effect.

## [0.10.0] - 2022-03-24 – Bot API improvements

### 🐞 Bug Fixes

- Fixed MyFirstBot.java (sample bot) not running.
- Various bug fixes were found in the Bot APIs, and missing get/set methods were added.
- Selected bots were not removed from the New Battle dialog when bots are disconnecting.
- Booter did not transfer environment variables to bot processes from GUI.

#### Changes

- Moved documentation from robocode.dev to [GitHub Pages].
- Changed the default server port from 80 to 7654 to avoid the use of `sudo` before `java` command (ports above 1023
  does not need `sudo`)
- Bot APIs: Moved constants to a Constants class.
- Bot APIs: Introduced a Color class for representing colors.
- Bot API for .NET: Some Set/Get methods were refactored into properties for e.g. speed, turn rates, and colors.
- Bot API for .NET: Lots of optimizations were made.

### 🚀 Improvements

- Bot handshake has been extended to include server secret with the bot-handshake and the BaseBot class has been
  extended for setting this as input parameter in the constructor.
- Server secret has been split into *controller/observer secrets* and *bot secrets*
    - It is now possible to supply multiple server secrets
- Client is disconnected when sending a wrong secret to the server.
    - Bot API now writes out status-code and the reason for disconnecting when it is due to a wrong secret.

## [0.9.12] - 2022-01-23

- First alpha version was released! 🚀😀

[installation guide]: https://robocode-dev.github.io/tank-royale/articles/installation.html "Installing and running Robocode"

[GUI]: https://robocode-dev.github.io/tank-royale/articles/gui.html "The Graphical User Interface (GUI)"

[BotInfo/BotHandshake]: https://github.com/robocode-dev/tank-royale/blob/master/schema/schemas/bot-handshake.yaml

[BotInfo.FromConfiguration(IConfiguration)]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html#Robocode_TankRoyale_BotApi_BotInfo_FromConfiguration_Microsoft_Extensions_Configuration_IConfiguration_

[sounds releases]: https://github.com/robocode-dev/sounds/releases

[GitHub Pages]: https://robocode-dev.github.io/tank-royale/

[ANSI escape code]: https://en.wikipedia.org/w/index.php?title=ANSI_escape_code

[Events tab]: https://robocode-dev.github.io/tank-royale/articles/gui.html#viewing-the-bot-events "Events tab in Bot Console"

[Robocode API Bridge]: https://github.com/robocode-dev/robocode-api-bridge "Robocode API bridge for Tank Royale"

[Socket Activation]: https://insanity.industries/post/socket-activation-all-the-things/ "Socket Activation"

[JSVG]: https://github.com/weisJ/jsvg "JSVG library on GitHub"
