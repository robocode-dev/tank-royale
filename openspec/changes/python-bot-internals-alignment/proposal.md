## Why

The Python Bot API violates ADR-0003 (symmetric APIs across all platforms) in its internal
architecture: the `BotInternals` class is embedded as a private `_BotInternals` class inside
`bot.py` instead of living in its own `internal/bot_internals.py` module, and a Python-only
`BaseBotInternalData` state holder exists that has no equivalent in Java, C#, or TypeScript.
These deviations make it impossible to test the Python API using the same shared runner
infrastructure required by ADR-0038 (cross-platform test parity), as explicitly noted as deferred
in the `converge-bot-api-test-parity` change.

## What Changes

- **New file `internal/bot_internals.py`** — Extract the `_BotInternals` private class from
  `bot.py` into a dedicated module and make it a proper public class `BotInternals`, matching
  `internal/BotInternals.java` and `internal/BotInternals.cs` in structure and naming.
- **Remove `BaseBotInternalData`** — Eliminate `internal/base_bot_internal_data.py`. All state
  fields it holds (bot intent, tick, IDs, handshake, conditions, flags) are merged directly into
  `BaseBotInternals`, matching Java and C# where `BaseBotInternals` holds all state directly.
- **Update `bot.py`** — Remove the `_BotInternals` class definition; import `BotInternals` from
  `internal.bot_internals` instead.
- **Update `base_bot_internals.py`** — Remove the `data: BaseBotInternalData` field; inline all
  state fields that were delegated to it.
- **Update all internal call sites** — Replace every `self.data.<field>` / `internals.data.<field>`
  access with a direct field access (e.g., `self.bot_intent`, `internals.bot_intent`).
- **Update all production `data.` call sites** — Replace every `self._internals.data.<field>`
  access in `base_bot.py` (adjust flags, rescan, firepower, is_stopped, etc.) with direct field
  access on `BaseBotInternals`. Likewise in `event_queue.py` and `websocket_handler.py`, which
  currently receive `BaseBotInternalData` as a constructor argument and must be updated to
  reference `BaseBotInternals` directly.
- **Audit and remove Python-specific test workarounds** — After the refactor, review all test
  files for special-casing introduced because of the structural discrepancy:
  - `test_shared.py` line 129: `internals.data.bot_intent` → `internals.bot_intent`
  - `abstract_bot_test.py` line 324: `bot._internals.data.bot_intent.firepower` →
    `bot._internals.bot_intent.firepower` (using the now-public `BotInternals`)
  - `test_imports.py`: test that explicitly imports and checks `base_bot_internal_data` — delete
    the module and remove that test assertion once the module no longer exists
- **All existing Python tests must continue to pass** — This is a pure structural refactor with
  zero behavior change.

## Capabilities

### New Capabilities

- `python-bot-api-internal-alignment`: Structural alignment of Python `internal/` module layout
  with Java and C# — introduces `BotInternals` as a standalone module, removes the
  `BaseBotInternalData` indirection layer, and eliminates all test workarounds that existed due
  to the structural discrepancy.

### Modified Capabilities

*(None — no spec-level behavioral requirements change; this is an internal implementation refactor.)*

## Impact

- `bot-api/python/src/robocode_tank_royale/bot_api/internal/` — new `bot_internals.py`, deleted
  `base_bot_internal_data.py`, modified `base_bot_internals.py`, `event_queue.py`,
  `websocket_handler.py`
- `bot-api/python/src/robocode_tank_royale/bot_api/base_bot.py` — `data.` → direct field access
- `bot-api/python/src/robocode_tank_royale/bot_api/bot.py` — import change, class removed
- `bot-api/python/tests/test_shared.py` — `internals.data.*` → `internals.*`
- `bot-api/python/tests/bot_api/abstract_bot_test.py` — `data.bot_intent` → `bot_intent`
- `bot-api/python/tests/test_imports.py` — remove assertion for deleted module
- No public API changes — `Bot` and `BaseBot` external interface is unchanged
- No protocol or WebSocket changes
- Java, C#, TypeScript: unaffected
