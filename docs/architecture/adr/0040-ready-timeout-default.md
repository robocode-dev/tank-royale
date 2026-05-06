# ADR-0040: Raise Default readyTimeout from 1 Second to 10 Seconds

**Status:** Accepted  
**Date:** 2026-04-15

---

## Context

The `readyTimeoutMicros` setting controls the time window a bot has to send a `BotReady` message
after receiving `GameStartedEventForBot`. If a bot misses the window the server starts the battle
without it — silently, with no error logged to the user.

The previous default was **1,000,000 µs (1 second)**.

This window was sufficient for pre-compiled, always-running bots but proved too tight in practice
for runtimes that need to start up or compile before they can respond:

| Runtime | Startup characteristics |
|---------|------------------------|
| **JVM source-file mode** (`java MyBot.java`) | JVM must compile source before executing |
| **Python** | interpreter start + module import |
| **.NET** | runtime init + assembly load |

In all three cases the bot successfully connects to the server (WebSocket handshake), joins the
lobby, and receives `GameStartedEventForBot` within the 1-second window — but then needs
additional time to finish loading before it can respond with `BotReady`. The server interprets
the silence as a timeout and starts the game without the bot.

This was surfaced during investigation of [issue #202](https://github.com/robocode-dev/tank-royale/issues/202):
end-to-end tests using JVM source-file mode bots failed because the bot was absent from battle
results even though it connected successfully.

---

## Decision

Raise `DEFAULT_READY_TIMEOUT_MICROS` to **10,000,000 µs (10 seconds)**.

All four game-type presets (CUSTOM, CLASSIC, MELEE, ONE_VS_ONE) inherit this value via
`DEFAULT_READY_TIMEOUT_MICROS` in `RuleDefaults.kt` instead of hardcoding `1_000_000`.

---

## Rationale

- **10 seconds** covers even slow JVM cold-start + compilation on modest hardware with headroom.
- It does not noticeably affect game-start UX: the server starts immediately once all bots send
  `BotReady`; the timeout only fires if a bot fails to respond at all.
- The setting is user-configurable via the Setup Rules dialog and saved per game type, so
  tournament operators can lower it for environments where fast startup is guaranteed.

---

## Alternatives Considered

| Value | Rationale for rejection |
|-------|------------------------|
| **3 s** | Marginal for JVM source-file mode on CI runners; fails on slow machines |
| **5 s** | Reasonable but still fails intermittently for heavy .NET assemblies |
| **30 s** | Overly conservative; would delay battle start by 30 s when a bot crashes before sending ready |

---

## Consequences

**Positive:**
- JVM, Python, and .NET source-file mode bots no longer silently fail to join battles.
- End-to-end Battle Runner tests are reliable without per-test timeout overrides.

**Negative:**
- If a bot crashes before sending `BotReady`, the server now waits up to 10 s before starting
  instead of 1 s. This is acceptable: a 10-second delay is easily noticed; a 1-second delay
  was barely perceptible anyway.
- Existing saved `game-setups.properties` files that stored `readyTimeout = 1000000` will
  retain the old value until the user clicks "Reset to Default" in the Setup Rules dialog.
  This is the standard migration path for user-override settings.
