# Change: Refactor Python Bot API to Use Pythonic Properties

## Why

The Python Bot API currently uses Java-style getter methods (e.g., `get_energy()`, `get_x()`, `is_disabled()`) instead
of Python's idiomatic property-based accessors. This makes the API feel unnatural to Python developers and inconsistent
with Python conventions (PEP 8).

While the Python API must maintain 1:1 semantic equivalence with the Java reference implementation, it should use
language-appropriate idioms. Python properties provide the same functionality as Java getters/setters while offering a
more natural syntax.

## What Changes

**BREAKING**: All getter methods converted to read-only properties. All `is_*()` boolean methods (except those with
parameters) converted to properties. Paired `is_adjust_*()`/`set_adjust_*()` methods converted to read-write properties.

### IBaseBot Equivalent Changes

| Java Method                   | Old Python                         | New Python                     |
|-------------------------------|------------------------------------|--------------------------------|
| `getMyId()`                   | `get_my_id()`                      | `my_id`                        |
| `getVariant()`                | `get_variant()`                    | `variant`                      |
| `getVersion()`                | `get_version()`                    | `version`                      |
| `getGameType()`               | `get_game_type()`                  | `game_type`                    |
| `getArenaWidth()`             | `get_arena_width()`                | `arena_width`                  |
| `getArenaHeight()`            | `get_arena_height()`               | `arena_height`                 |
| `getNumberOfRounds()`         | `get_number_of_rounds()`           | `number_of_rounds`             |
| `getGunCoolingRate()`         | `get_gun_cooling_rate()`           | `gun_cooling_rate`             |
| `getMaxInactivityTurns()`     | `get_max_inactivity_turns()`       | `max_inactivity_turns`         |
| `getTurnTimeout()`            | `get_turn_timeout()`               | `turn_timeout`                 |
| `getTimeLeft()`               | `get_time_left()`                  | `time_left`                    |
| `getRoundNumber()`            | `get_round_number()`               | `round_number`                 |
| `getTurnNumber()`             | `get_turn_number()`                | `turn_number`                  |
| `getEnemyCount()`             | `get_enemy_count()`                | `enemy_count`                  |
| `getEnergy()`                 | `get_energy()`                     | `energy`                       |
| `getX()`                      | `get_x()`                          | `x`                            |
| `getY()`                      | `get_y()`                          | `y`                            |
| `getDirection()`              | `get_direction()`                  | `direction`                    |
| `getGunDirection()`           | `get_gun_direction()`              | `gun_direction`                |
| `getRadarDirection()`         | `get_radar_direction()`            | `radar_direction`              |
| `getSpeed()`                  | `get_speed()`                      | `speed`                        |
| `getGunHeat()`                | `get_gun_heat()`                   | `gun_heat`                     |
| `getBulletStates()`           | `get_bullet_states()`              | `bullet_states`                |
| `getEvents()`                 | `get_events()`                     | `events`                       |
| `getFirepower()`              | `get_firepower()`                  | `firepower`                    |
| `getTeammateIds()`            | `get_teammate_ids()`               | `teammate_ids`                 |
| `getGraphics()`               | `get_graphics()`                   | `graphics`                     |
| `isDisabled()`                | `is_disabled()`                    | `disabled`                     |
| `isStopped()`                 | `is_stopped()`                     | `stopped`                      |
| `isDebuggingEnabled()`        | `is_debugging_enabled()`           | `debugging_enabled`            |
| `isAdjustGunForBodyTurn()`    | `is_adjust_gun_for_body_turn()`    | `adjust_gun_for_body_turn`     |
| `setAdjustGunForBodyTurn()`   | `set_adjust_gun_for_body_turn()`   | `adjust_gun_for_body_turn =`   |
| `isAdjustRadarForBodyTurn()`  | `is_adjust_radar_for_body_turn()`  | `adjust_radar_for_body_turn`   |
| `setAdjustRadarForBodyTurn()` | `set_adjust_radar_for_body_turn()` | `adjust_radar_for_body_turn =` |
| `isAdjustRadarForGunTurn()`   | `is_adjust_radar_for_gun_turn()`   | `adjust_radar_for_gun_turn`    |
| `setAdjustRadarForGunTurn()`  | `set_adjust_radar_for_gun_turn()`  | `adjust_radar_for_gun_turn =`  |

### IBot Equivalent Changes

| Java Method   | Old Python     | New Python |
|---------------|----------------|------------|
| `isRunning()` | `is_running()` | `running`  |

### Methods That Remain Unchanged

- `is_teammate(bot_id)` — takes a parameter, must remain a method
- `get_event_priority(event_class)` / `set_event_priority()` — parameterized methods
- `set_fire()`, `set_rescan()`, `set_fire_assist()`, `set_interruptible()` — action methods
- `calc_*()`, `*_to()`, `normalize_*()` — calculation/utility methods
- All event handlers (`on_*`)
- All movement/turn action methods (`forward()`, `turn_left()`, etc.)

## Impact

- **Affected specs**: python-bot-api
- **Affected code**:
    - `bot-api/python/src/robocode_tank_royale/bot_api/base_bot_abc.py`
    - `bot-api/python/src/robocode_tank_royale/bot_api/base_bot.py`
    - `bot-api/python/src/robocode_tank_royale/bot_api/bot_abc.py`
    - `bot-api/python/src/robocode_tank_royale/bot_api/bot.py`
    - All sample bots in `sample-bots/python/`
    - Python tests in `bot-api/python/tests/`
    - `VERSIONS.md`
- **Breaking change**: Yes — users must update their bot code
- **Migration effort**: Low — simple find-and-replace

## Migration Guide

Before:

```python
energy = self.get_energy()
if self.is_disabled():
    return
x, y = self.get_x(), self.get_y()
enemy_count = self.get_enemy_count()
```

After:

```python
energy = self.energy
if self.disabled:
    return
x, y = self.x, self.y
enemy_count = self.enemy_count
```
