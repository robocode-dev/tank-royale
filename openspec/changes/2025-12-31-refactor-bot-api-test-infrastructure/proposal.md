# Change: Refactor Bot API Test Infrastructure for Simplicity and Stability

## Why

The current Bot API test infrastructure has proven extremely difficult to work with, even for the most powerful AI
coding assistants. Specifically:

1. **TR-API-CMD-002 (Fire command)** and **TR-API-CMD-003 (Radar/Scan commands)** tests are challenging to implement
   correctly across Java, .NET, and Python.
2. **MockedServer is a message-capture framework, not a state-driven testing framework**: Tests must manually
   orchestrate ticks, wait for state propagation, and coordinate async operations with sleeps and latches.
3. **Stateful Bot API vs. Stateless Test Harness**: Methods like `setFire()` return `false` based on bot state (
   `getEnergy()`, `getGunHeat()`), but MockedServer doesn't expose or control this state cleanly.
4. **Blocking methods with hidden concurrency**: Methods like `fire()` and `rescan()` internally call `bot.go()`,
   creating fragile race conditions that require manual task spawning and sleep-based coordination.
5. **Indirect intent capture**: Tests await `BotIntent` messages after the fact; if the bot never calls `go()`, tests
   hang or timeout without clarity.
6. **No facades for state setup**: There's no simple "set bot energy to 5 and ensure it's visible" API—tests must call
   `server.setEnergy(5.0)`, manually send a tick, await bot processing, then invoke the command.

These issues make tests fragile, hard to understand, and difficult to maintain across three languages.

## What Changes

### Phase 1: Enhanced MockedServer State Control (All Languages)

Add **synchronous state management** APIs to MockedServer that guarantee the bot sees the configured state:

**Java** (`test_utils/MockedServer.java`):

- `awaitBotReady()`: Block until bot has processed handshake and is ready for commands
- `setBotStateAndAwaitTick(energy, gunHeat, speed, direction, ...)`: Atomically update state fields, send tick, and wait
  for bot to process it
- `resetBotIntentEvent()`: Renamed from `resetBotIntentLatch()` for clarity

**C#/.NET** (`test_utils/MockedServer.cs`):

- `AwaitBotReady()`: Block until bot has processed handshake
- `SetBotStateAndAwaitTick(energy, gunHeat, speed, direction, ...)`: Atomically update and sync state
- `ResetBotIntentEvent()`: Already exists; ensure naming consistency

**Python** (`test_utils/mocked_server.py`):

- `await_bot_ready()`: Block until bot has processed handshake
- `set_bot_state_and_await_tick(energy, gun_heat, speed, direction, ...)`: Atomically update and sync state
- `reset_bot_intent_event()`: Already exists; ensure naming consistency

**Note**: Bad practice methods (`setEnergy`, `setGunHeat`, `setSpeed`, `sendTick`) will be **removed** after all tests
are migrated in Phase 5, not just deprecated.

### Phase 2: Synchronous Command Execution Utilities

Add test helper methods to base test classes that handle the async orchestration:

**Java** (`test_utils/AbstractBotTest.java` - to be created):

```java
protected <T> CommandResult<T> executeCommand(Supplier<T> command) {
    T result = command.get();
    bot.go();
    awaitBotIntent();
    return new CommandResult<>(result, server.getBotIntent());
}

protected BotIntent executeAndCaptureIntent(Runnable command) {
    command.run();
    bot.go();
    awaitBotIntent();
    return server.getBotIntent();
}
```

**C#/.NET** (`AbstractBotTest.cs` - enhance existing):

```csharp
protected (T result, BotIntent intent) ExecuteCommand<T>(Func<T> command) {
    var result = command();
    GoAsync(bot);
    AwaitBotIntent();
    return (result, Server.BotIntent);
}

protected BotIntent ExecuteAndCaptureIntent(Action command) {
    command();
    GoAsync(bot);
    AwaitBotIntent();
    return Server.BotIntent;
}
```

**Python** (`test_utils/abstract_bot_test.py` - to be created):

```python
def execute_command(self, command_fn):
    """Execute a command and return (result, intent) tuple."""
    result = command_fn()
    self.go_async(self.bot)
    self.await_bot_intent()
    return result, self.server.bot_intent

def execute_and_capture_intent(self, command_fn):
    """Execute a command and return the captured intent."""
    command_fn()
    self.go_async(self.bot)
    self.await_bot_intent()
    return self.server.bot_intent
```

### Phase 3: Implement TR-API-CMD-002 Fire Command Tests

Create new test files following TEST-MATRIX.md conventions:

**Java** (`CommandsFireTest.java`):

```java

@DisplayName("TR-API-CMD-002 Fire command")
@Tag("CMD")
class CommandsFireTest extends AbstractBotTest {

    @Test
    @DisplayName("TR-API-CMD-002 Fire power bounds")
    void test_TR_API_CMD_002_fire_power_bounds() {
        // Test firepower clamping 0.1-3.0 in intent
    }

    @Test
    @DisplayName("TR-API-CMD-002 Fire cooldown prevents firing")
    void test_TR_API_CMD_002_fire_cooldown() {
        // Test setFire() returns false when gunHeat > 0
    }

    @Test
    @DisplayName("TR-API-CMD-002 Fire energy limit prevents firing")
    void test_TR_API_CMD_002_fire_energy_limit() {
        // Test setFire() returns false when energy < firepower
    }

    @Test
    @DisplayName("TR-API-CMD-002 Fire with NaN throws exception")
    void test_TR_API_CMD_002_fire_nan_throws() {
        // Test setFire(NaN) throws IllegalArgumentException
    }
}
```

**C#/.NET** (`CommandsFireTest.cs`):

```csharp
[TestFixture]
[Description("TR-API-CMD-002 Fire command")]
public class CommandsFireTest : AbstractBotTest {
    
    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-002")]
    [Description("TR-API-CMD-002 Fire power bounds")]
    public void Test_TR_API_CMD_002_Fire_Power_Bounds() { }
    
    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-002")]
    [Description("TR-API-CMD-002 Fire cooldown prevents firing")]
    public void Test_TR_API_CMD_002_Fire_Cooldown() { }
    
    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-002")]
    [Description("TR-API-CMD-002 Fire energy limit prevents firing")]
    public void Test_TR_API_CMD_002_Fire_Energy_Limit() { }
    
    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-002")]
    [Description("TR-API-CMD-002 Fire with NaN throws exception")]
    public void Test_TR_API_CMD_002_Fire_Nan_Throws() { }
}
```

**Python** (`test_commands_fire.py`):

```python
def test_TR_API_CMD_002_fire_power_bounds():
    """TR-API-CMD-002 Fire power bounds"""
    pass

def test_TR_API_CMD_002_fire_cooldown():
    """TR-API-CMD-002 Fire cooldown prevents firing"""
    pass

def test_TR_API_CMD_002_fire_energy_limit():
    """TR-API-CMD-002 Fire energy limit prevents firing"""
    pass

def test_TR_API_CMD_002_fire_nan_throws():
    """TR-API-CMD-002 Fire with NaN throws exception"""
    pass
```

### Phase 4: Refactor TR-API-CMD-003 Radar/Scan Tests

Simplify existing radar tests in `CommandsRadarTest` (Java/.NET) to use the new utilities:

- Remove manual `Task.Run()` / `Thread.Sleep()` patterns
- Use `executeCommand()` / `ExecuteCommand()` helpers
- Eliminate race conditions and improve readability

### Phase 5: Refactor All Existing MockedServer-based Tests

Apply the same patterns to existing tests:

- `CommandsMovementTest` (Java/.NET) - TR-API-CMD-001 (already exists, needs refactoring)
- TR-API-CMD-004 Graphics frame emission tests
- Other command tests
- Lifecycle tests

**Goal**: Every test using MockedServer should be simple, readable, and follow the same pattern.

### Phase 6: Mock/Stub Test Bot Factory

Add helper utilities to create configurable mock/stub test bots:

- Test bot factory/builder pattern for common test scenarios
- Pre-configured bot behaviors (passive, aggressive, scanning, etc.)
- Ability to override specific bot callbacks for targeted testing

## Impact

### Affected Code

**Java**:

- `bot-api/java/src/test/java/test_utils/MockedServer.java` (enhanced)
- `bot-api/java/src/test/java/test_utils/AbstractBotTest.java` (new)
- `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/CommandsFireTest.java` (new)
- `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/CommandsRadarTest.java` (refactored)
- `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/CommandsMovementTest.java` (refactored if exists)

**.NET**:

- `bot-api/dotnet/test/src/test_utils/MockedServer.cs` (enhanced)
- `bot-api/dotnet/test/src/AbstractBotTest.cs` (enhanced)
- `bot-api/dotnet/test/src/CommandsFireTest.cs` (new)
- `bot-api/dotnet/test/src/CommandsRadarTest.cs` (refactored)
- `bot-api/dotnet/test/src/CommandsMovementTest.cs` (refactored)

**Python**:

- `bot-api/python/tests/test_utils/mocked_server.py` (enhanced)
- `bot-api/python/tests/test_utils/abstract_bot_test.py` (new)
- `bot-api/python/tests/bot_api/commands/test_commands_fire.py` (new)
- `bot-api/python/tests/bot_api/commands/test_commands_radar.py` (new)
- `bot-api/python/tests/bot_api/commands/test_commands_movement.py` (refactored if exists)

**Documentation**:

- `bot-api/tests/TEST-MATRIX.md` (update CMD-002 and CMD-003 status)
- `bot-api/tests/TESTING-GUIDE.md` (new - document testing patterns and utilities)

### Affected Specs

No user-facing behavior changes. This is purely test infrastructure improvement.

## Success Criteria

1. ✅ All TR-API-CMD-001 movement command tests refactored to use new utilities and passing across Java, .NET, Python
2. ✅ All TR-API-CMD-002 fire command tests implemented and passing across Java, .NET, Python
3. ✅ All TR-API-CMD-003 radar/scan tests refactored and passing across Java, .NET, Python
4. ✅ TR-API-CMD-004 graphics frame emission tests refactored to use new utilities (where applicable)
5. ✅ All existing MockedServer-based tests refactored to use new utilities
6. ✅ Mock/stub test bot factory available for easy test bot creation
7. ✅ Tests are simple, readable, and follow consistent patterns across languages
8. ✅ No flaky tests due to race conditions or timing issues
9. ✅ Test execution time remains reasonable (no significant regression)
10. ✅ TESTING-GUIDE.md documents the new patterns with examples

## Non-Goals

- This change does NOT modify Bot API public interfaces
- This change does NOT affect bot behavior or wire protocol
- This change does NOT add new Bot API features
- Performance optimization of test execution (acceptable if tests remain reasonable)

**Note**: Bad practice MockedServer methods WILL be removed (not just deprecated) to prevent contributors from falling
into flaky test traps.

## Risks and Mitigations

**Risk**: Breaking existing tests during refactoring
**Mitigation**: Refactor incrementally; keep old patterns working until all tests migrated

**Risk**: Language-specific quirks make unified patterns difficult
**Mitigation**: Allow idiomatic differences where necessary; document them

**Risk**: Python async/threading model differs significantly
**Mitigation**: Adapt patterns to Python idioms while maintaining semantic equivalence

## Timeline Estimate

- Phase 1 (Enhanced MockedServer): 2-3 days per language = 6-9 days
- Phase 2 (Command execution utilities): 1-2 days per language = 3-6 days
- Phase 3 (Fire command tests): 2-3 days per language = 6-9 days
- Phase 4 (Refactor radar tests): 1-2 days per language = 3-6 days
- Phase 5 (Refactor all tests, including CMD-001 and CMD-004): 3-5 days per language = 9-15 days
- Phase 6 (Mock/stub test bot factory): 1-2 days per language = 3-6 days
- Documentation: 2-3 days

**Total**: 32-54 days (approximately 7-11 weeks)

## Follow-up Considerations

1. **Recording bot pattern**: For complex scenarios, consider timeline-based test replays
2. **Shared test utilities**: Extract common patterns into a cross-language test library (where practical)
3. **Event injection tests**: Consider adding utilities for injecting events (TR-API-EVT-* tests)

