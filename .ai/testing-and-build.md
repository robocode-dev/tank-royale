# Testing and Build Procedures

<!-- METADATA: ~160 lines, ~1400 tokens -->
<!-- KEYWORDS: test, build, Gradle, gradlew, compile, validation, sample bot, backward compatibility, asyncio, python, hanging, sequence diagram, protocol -->

## Protocol Sequence Diagrams (CRITICAL)

**Always consult `schema/schemas/README.md` before writing tests for Bot API or MockedServer.**

This file contains authoritative Mermaid sequence diagrams showing the exact message flow between:
- Bot ↔ Server
- Observer ↔ Server  
- Controller ↔ Server

**Key sequences to understand:**

| Sequence | When to Reference |
|----------|-------------------|
| `bot-joining` | Tests involving bot handshake, connection |
| `starting-game` | Tests involving game start, bot-ready |
| `running-next-turn` | Tests involving tick events, bot-intent, turn flow |
| `game-ending` | Tests involving game end, results |

**For MockedServer test utilities:**
- The MockedServer MUST follow the same message ordering as the real server
- `await_bot_ready()` must chain: handshake → game-started → tick (per diagram)
- `set_bot_state_and_await_tick()` must send tick and await intent response
- Intent is sent BEFORE waiting for next turn (per `running-next-turn` diagram)

**Example from diagram - Running next turn:**
```
Server → Bot: tick-event-for-bot
Bot → Server: bot-intent
Server → Server: Turn timeout
```

This means: tick is sent first, bot responds with intent, then turn timeout occurs.

## Build Requirements

Always run `./gradlew clean build` after ANY of these changes:

- Code changes (Java, Kotlin, Python, C#)
- Configuration changes (build files, Gradle scripts)
- Build system changes (dependencies, plugins)

**Exception - skip build for:**

- Pure text/markdown documentation
- README updates (unless they affect build)
- Comment-only changes

**Build command:**

```bash
./gradlew clean build
```

## Testing Standards

Always add regression tests for bug fixes.

Always add tests covering new behavior for new features.

Always run tests on ALL platforms (Java, Python, .NET) for Bot API changes.

Always validate JSON schema compliance for protocol changes.

**Test execution:**

Always ensure all tests pass before completing task.

Remember the build system runs tests automatically.

If tests fail, fix or explain why failure is expected.

## Protocol Change Validation

Always follow this sequence when modifying JSON/WebSocket protocol:

1. Update JSON schema in `/schema`
2. Update documentation
3. Verify backward compatibility
4. Add JSON examples demonstrating new behavior
5. Test with existing bots to ensure no breakage

## Sample Bot Validation

Always test with sample bots when making:

- Timing changes (turn order, event sequencing)
- State management changes
- User-visible Bot API changes
- Event handling modifications

**How to validate:**

Always run at least one sample bot from each language.

Always verify expected behavior matches.

Always check console output for errors.

Always ensure cross-platform consistency.

## Module-Specific Builds

**Building individual modules:**

```bash
# Build specific module
./gradlew :bot-api:java:build
./gradlew :server:build
./gradlew :gui:build
```

**When to use:**

- Faster iteration during focused work
- Isolating build issues
- Testing single-platform changes

## Python Asyncio Testing (CRITICAL)

**Problem:** Python tests using asyncio (websockets, event loops in threads) will hang
after completion due to asyncio's default ThreadPoolExecutor never shutting down.

**Root causes of hanging:**
- `loop.run_in_executor(None, ...)` creates a hidden ThreadPoolExecutor
- `asyncio.run_coroutine_threadsafe()` uses the same executor
- websockets library's `Server._close()` waits indefinitely for accept coroutines
- Python's threading shutdown waits for non-daemon threads

**Solution implemented in this project:**

1. Use `os._exit(0)` in a pytest session-scoped fixture (`conftest.py`):
   ```python
   @pytest.fixture(scope="session", autouse=True)
   def force_exit_after_tests():
       yield
       os._exit(0)
   ```

2. Use daemon threads for asyncio event loops in test infrastructure:
   ```python
   self._thread = threading.Thread(target=self._run_loop, daemon=True)
   ```

3. Keep `stop()` methods simple - just stop the loop, don't wait for cleanup:
   ```python
   def stop(self):
       if self._loop and self._loop.is_running():
           self._loop.call_soon_threadsafe(self._loop.stop)
   ```

**What NOT to do:**
- Don't use `run_in_executor(None, ...)` - use polling with `asyncio.sleep(0)` instead
- Don't wait for `server.wait_closed()` in cleanup - it may hang
- Don't use `pytest-asyncio` unless tests are actual `async def` functions
- Don't try complex asyncio cleanup sequences - they often deadlock

**Testing:**
```bash
# Verify tests don't hang
./gradlew :bot-api:python:test

# Or directly with pytest
python -m pytest tests/ --timeout=15
```

