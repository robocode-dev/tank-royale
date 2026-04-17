## Context

Java and C# each have two dedicated `internal` classes:
- `BaseBotInternals` — holds all state (bot intent, tick, IDs, handshake, conditions, flags) and
  the connection lifecycle. No separate data object.
- `BotInternals` — the `Bot`-class helper that tracks remaining movement/turn values, manages the
  bot thread, and implements `IStopResumeListener`. Defined in its own file.

The Python API diverges in two ways:

1. **`_BotInternals` is private and embedded in `bot.py`** — defined as a module-level private
   class `_BotInternals` instead of a separate `internal/bot_internals.py` module. This prevents
   direct instantiation in tests and makes the class invisible to cross-platform test runners.

2. **`BaseBotInternalData` is a standalone data class** — all mutable bot state is delegated to
   `internal/base_bot_internal_data.py` and accessed via `self.data.<field>` everywhere. This
   object has no parallel in Java or C#. It propagates through `EventQueue` and
   `WebSocketHandler` as a constructor argument, creating Python-specific API surface.

These deviations were introduced in earlier Python work before the 1:1 parity requirement was
formalized in ADR-0003/0038. The `converge-bot-api-test-parity` change documented them as deferred.

## Goals / Non-Goals

**Goals:**
- `BotInternals` lives in `internal/bot_internals.py` and is a proper public class, identical in
  role to `BotInternals.java` and `BotInternals.cs`
- `BaseBotInternalData` is deleted; all its fields live directly on `BaseBotInternals`
- `EventQueue` and `WebSocketHandler` take `BaseBotInternals` (not the removed data class)
- All `self.data.<field>` / `bot._internals.data.<field>` accesses are replaced with direct access
- All existing Python tests pass unchanged (except the three test-side workarounds removed)
- `test_imports.py` no longer asserts the existence of the deleted module
- Zero behavior change — only structure changes

**Non-Goals:**
- No changes to Java, C#, or TypeScript
- No new tests beyond removing the import assertion for the deleted module
- No public API surface change (`Bot`, `BaseBot`, event classes remain identical)
- No protocol or WebSocket changes

## Decisions

### 1. Flat state in `BaseBotInternals` (no data object)

Move all fields from `BaseBotInternalData.__init__` directly into `BaseBotInternals.__init__`.
Properties with guards (e.g., `my_id` raising `BotException` when `None`) are re-declared
directly on `BaseBotInternals`. The `data` attribute is removed entirely.

**Why not keep `BaseBotInternalData` as an implementation detail?**
ADR-0003 requires the internal architecture to be symmetric. A separate data holder is invisible
to test runners and forces every Python test that inspects state to use `internals.data.*` — a
Python-only access path that doesn't exist in Java/C# test utilities.

### 2. `BotInternals` as a top-level public class

Extract `_BotInternals` verbatim into `internal/bot_internals.py`, rename to `BotInternals`
(drop the underscore prefix). `bot.py` imports it:
```python
from .internal.bot_internals import BotInternals
```
and instantiates it as before:
```python
self._bot_internals = BotInternals(bot=self, base_bot_internals=self._internals)
```

**Why public (not package-private)?**  
Python has no package-private access modifier. Java uses package-private; C# uses `internal`.
The Python equivalent is a non-underscore name without `__all__` export — public within the
package, not advertised to end users. This matches how the rest of `internal/` is structured.

### 3. `EventQueue` and `WebSocketHandler` take `BaseBotInternals`

Both classes currently accept `BaseBotInternalData` as a constructor parameter. Replace with
`BaseBotInternals`. Since `BaseBotInternals` now holds all state directly, no new methods are
needed — the existing field accesses work unchanged after the rename.

### 4. Sequential field migration in `BaseBotInternals`

Fields migrate one-for-one:

| `BaseBotInternalData` field | `BaseBotInternals` field (after) |
|-----------------------------|----------------------------------|
| `bot_info` | already on `BaseBotInternals` — skip |
| `bot_intent` | `self.bot_intent` |
| `_my_id` / `my_id` property | `self._my_id` / `my_id` property |
| `_teammate_ids` / `teammate_ids` | `self._teammate_ids` / `teammate_ids` |
| `_game_setup` / `game_setup` | `self._game_setup` / `game_setup` |
| `_initial_position` | `self._initial_position` |
| `_tick_event` / `tick_event` | `self._tick_event` / `tick_event` |
| `_tick_start_nano_time` | `self._tick_start_nano_time` |
| `_server_handshake` | `self._server_handshake` |
| `_conditions` / `conditions` | `self._conditions` / `conditions` |
| `_is_running_atomic` / `is_running` property | already on `BaseBotInternals` — unify |
| `_event_handling_disabled_turn` | `self._event_handling_disabled_turn` |
| `graphics_state` | `self.graphics_state` |
| `is_stopped`, `saved_*` | `self.is_stopped`, `self.saved_*` |
| `was_current_event_interrupted` | `self.was_current_event_interrupted` |
| `recording_stdout`, `recording_stderr` | `self.recording_stdout`, `self.recording_stderr` |

**Note:** `is_running` already exists on `BaseBotInternals` as a method delegating to
`self.data.is_running`. After migration it becomes a direct field read — no interface change.

## Risks / Trade-offs

**[Risk] Missed `data.` reference** — A single remaining `self.data.<field>` access becomes an
`AttributeError` at runtime.  
→ Mitigation: Run the full Python test suite (`pytest bot-api/python/`) immediately after the
migration. Any missed reference will produce an immediate test failure.

**[Risk] `EventQueue` / `WebSocketHandler` constructor signature breaks a test double** — Tests
that construct these directly with a `BaseBotInternalData` mock will fail.  
→ Mitigation: Audit test files for direct `EventQueue(...)` or `WebSocketHandler(...)` 
construction. Currently none are found — only `BaseBotInternals` is constructed in tests.

**[Risk] Duplicate field name between `BaseBotInternals` and `BaseBotInternalData`** — `bot_info`
and `is_running` exist on both. After merge, ensure only one definition survives.  
→ Mitigation: `bot_info` stays on `BaseBotInternals` as-is (already there). `is_running` becomes
a direct bool field; the method `is_running()` is kept as a thin wrapper for Java/C# parity.

## Migration Plan

1. Add all `BaseBotInternalData` fields directly to `BaseBotInternals.__init__`, using the same
   names and default values.
2. Update `EventQueue` to accept `BaseBotInternals` instead of `BaseBotInternalData`; update
   the one call site in `BaseBotInternals`.
3. Update `WebSocketHandler` similarly.
4. Replace every `self.data.<field>` in `base_bot_internals.py` with `self.<field>`.
5. Replace every `self._internals.data.<field>` in `base_bot.py` with `self._internals.<field>`.
6. Delete `internal/base_bot_internal_data.py`.
7. Extract `_BotInternals` from `bot.py` into `internal/bot_internals.py` as `BotInternals`;
   update import in `bot.py`.
8. Fix the three test-side workarounds (`test_shared.py`, `abstract_bot_test.py`, `test_imports.py`).
9. Run `pytest bot-api/python/` — all tests must pass.

**Rollback:** The change is a single branch. Git revert restores previous state fully.

## Open Questions

*(None — the scope is fully bounded and all call sites are known.)*
