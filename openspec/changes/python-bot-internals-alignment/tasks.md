## 1. Flatten BaseBotInternals state

- [ ] 1.1 Copy all fields from `BaseBotInternalData.__init__` into `BaseBotInternals.__init__` with identical names and defaults (`bot_intent`, `_my_id`, `_teammate_ids`, `_game_setup`, `_initial_position`, `_tick_event`, `_tick_start_nano_time`, `_server_handshake`, `_conditions`, `_event_handling_disabled_turn`, `graphics_state`, `is_stopped`, `saved_target_speed`, `saved_turn_rate`, `saved_gun_turn_rate`, `saved_radar_turn_rate`, `was_current_event_interrupted`, `recording_stdout`, `recording_stderr`)
- [ ] 1.2 Move all property definitions from `BaseBotInternalData` (`my_id`, `teammate_ids`, `game_setup`, `initial_position`, `tick_event`, `current_tick_or_throw`, `current_tick_or_null`, `tick_start_nano_time`, `server_handshake`, `conditions`, `is_running`, `event_handling_disabled_turn`) directly onto `BaseBotInternals`, merging with any existing duplicates (`is_running`, `bot_info`)
- [ ] 1.3 Remove the `self.data = BaseBotInternalData(self.bot_info)` line and the `from .base_bot_internal_data import BaseBotInternalData` import from `base_bot_internals.py`
- [ ] 1.4 Replace every `self.data.<field>` access in `base_bot_internals.py` with `self.<field>` (verify none remain)

## 2. Update EventQueue and WebSocketHandler constructors

- [ ] 2.1 Change `EventQueue.__init__` parameter from `base_bot_internal_data: BaseBotInternalData` to `base_bot_internals: BaseBotInternals`; rename internal attribute accordingly; remove `BaseBotInternalData` import
- [ ] 2.2 Replace all `self.base_bot_internal_data.<field>` accesses inside `EventQueue` with `self.base_bot_internals.<field>`
- [ ] 2.3 Change `WebSocketHandler.__init__` parameter from `base_bot_internal_data: BaseBotInternalData` to `base_bot_internals: BaseBotInternals`; rename internal attribute; remove import
- [ ] 2.4 Replace all `self.base_bot_internal_data.<field>` accesses inside `WebSocketHandler` with `self.base_bot_internals.<field>`
- [ ] 2.5 Update the `BaseBotInternals` method that constructs `EventQueue` and `WebSocketHandler` to pass `self` instead of `self.data`

## 3. Update base_bot.py call sites

- [ ] 3.1 Replace every `self._internals.data.<field>` access in `base_bot.py` with `self._internals.<field>` (covers `bot_intent.*`, `is_stopped`, and any others)

## 4. Extract BotInternals into its own module

- [ ] 4.1 Create `internal/bot_internals.py`; copy the full `_BotInternals` class from `bot.py` into it, renaming the class to `BotInternals` (remove leading underscore)
- [ ] 4.2 Update `bot.py`: add `from .internal.bot_internals import BotInternals`; remove the `_BotInternals` class definition; change `_BotInternals(...)` instantiation to `BotInternals(...)`

## 5. Delete BaseBotInternalData

- [ ] 5.1 Delete `internal/base_bot_internal_data.py`
- [ ] 5.2 Verify no remaining imports of `base_bot_internal_data` anywhere in the production source tree

## 6. Fix test-side workarounds

- [ ] 6.1 In `tests/test_shared.py` line 129: change `internals.data.bot_intent` to `internals.bot_intent`
- [ ] 6.2 In `tests/bot_api/abstract_bot_test.py` line 324: change `bot._internals.data.bot_intent.firepower` to `bot._internals.bot_intent.firepower`
- [ ] 6.3 In `tests/test_imports.py`: remove the assertion that imports and checks `base_bot_internal_data` (the module no longer exists)

## 7. Verify

- [ ] 7.1 Run `pytest bot-api/python/` — all previously passing tests must pass with no new failures
- [ ] 7.2 Confirm via grep that no `data.` accesses remain in production source (`src/`) or test files (`tests/`)
