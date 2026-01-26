# Change: Refactor Python Bot API from Async to Synchronous

## Why

The Python Bot API currently uses `async/await` throughout its public interface, which **violates the 1:1 semantic
equivalence requirement** with the Java reference implementation (see CONTRIBUTING.md). This causes:

1. **Violates hard requirement** - All official Bot APIs must be 1:1 semantically equivalent to Java for
   maintainability, testing, documentation, and learning purposes
2. **Testing difficulties** - Async tests require `IsolatedAsyncioTestCase`, leading to test hangs and complexity
3. **Maintenance burden** - The async API structure differs significantly from Java/C#, making cross-platform
   maintenance harder
4. **User confusion** - Robocoders familiar with Java/C# expect synchronous blocking methods; Python's async model adds
   unnecessary cognitive overhead for a game bot
5. **Documentation mismatch** - The async signatures don't align with Java/C# documentation patterns

The Java and C# implementations use synchronous blocking methods (via threading primitives like `wait()`/`notify()`)
for the public API, while keeping async I/O internal. Python must follow the same pattern.

**Author's decision**: The Python API is still under development. 1:1 semantic equivalence with Java/C# is a hard
requirement and takes priority over backwards compatibility. Users who want an async approach can build their own
wrapper on top of the official API or create an alternative Bot API in a separate repository.

## What Changes

### **BREAKING** - Public API Methods Become Synchronous

All public `async def` methods become regular `def` methods:

- `BaseBot`: `start()`, `go()`
- `Bot`: `run()`, `forward()`, `back()`, `turn_left()`, `turn_right()`, `turn_gun_left()`, `turn_gun_right()`,
  `turn_radar_left()`, `turn_radar_right()`, `fire()`, `stop()`, `resume()`, `rescan()`, `wait_for()`
- Event handlers: `on_scanned_bot()`, `on_hit_by_bullet()`, `on_hit_bot()`, `on_hit_wall()`, `on_death()`, etc.

### Internal Architecture

- WebSocket I/O remains async, running in a background daemon thread with its own event loop
- Blocking achieved via `threading.Event` and `threading.Condition` (matching Java's `wait()`/`notifyAll()`)
- Bot logic runs in a separate thread (like Java's `startThread()`/`stopThread()` pattern)

### Sample Bots (14 bots)

All Python sample bots rewritten to remove `async/await`:

- `Corners`, `Crazy`, `Fire`, `MyFirstBot`, `MyFirstDroid`, `MyFirstLeader`, `MyFirstTeam`, `PaintingBot`, `RamFire`,
  `SpinBot`, `Target`, `TrackFire`, `VelocityBot`, `Walls`

### Tests

All async tests converted to synchronous `unittest.TestCase`:

- `test_base_bot_constructor.py`, `test_base_bot_precedence.py`, `test_base_bot_type_parsing.py`, `test_bot.py`,
  `test_bot_factory.py`, `test_commands_fire.py`, `test_commands_movement.py`, `test_constants.py`,
  `test_team_message_realistic.py`, `test_team_message_serialization.py`
- `MockedServer` updated for synchronous testing

### Documentation

- All docstrings updated to match Java API documentation (Javadoc)
- `VERSIONS.md` updated with breaking change entry and rationale

## Impact

- **Affected specs**: New `python-bot-api` spec created
- **Affected code**:
    - `bot-api/python/src/robocode_tank_royale/bot_api/` (all public API files)
    - `bot-api/python/src/robocode_tank_royale/bot_api/internal/` (internals refactored)
    - `bot-api/python/tests/` (all test files)
    - `sample-bots/python/` (all 14 sample bots)
    - `VERSIONS.md`
- **Breaking change**: Yes - all existing Python bots must be updated to remove `async/await`
- **Migration effort**: Low - mechanical removal of `async`/`await` keywords
