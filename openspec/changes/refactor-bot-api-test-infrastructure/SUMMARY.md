# Visual Summary: Test Infrastructure Refactoring

## The Problem (Current State)

```
┌─────────────────────────────────────────────────────────────────────┐
│  Test Method                                                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. server.setEnergy(5.0)            ◄─── Manual state management  │
│  2. server.sendTick()                ◄─── Manual message sending   │
│  3. Thread.sleep(100)                ◄─── Hope bot processed it    │
│  4. boolean fired = bot.setFire(1.0) ◄─── State might be stale!    │
│  5. bot.go()                         ◄─── Manual coordination      │
│  6. awaitBotIntent()                 ◄─── Manual synchronization   │
│  7. assert fired == false            ◄─── Finally... the test!     │
│                                                                     │
│  Result: 7 steps, 3 race conditions, unclear failures              │
└─────────────────────────────────────────────────────────────────────┘
```

### Issues:

- ❌ **7-step process** for a simple assertion
- ❌ **Race conditions** between steps 2-4
- ❌ **No guarantee** bot sees configured state
- ❌ **Timeout ambiguity**: Bot stuck or just slow?
- ❌ **Fragile**: One sleep value change breaks everything

## The Solution (New State)

```
┌─────────────────────────────────────────────────────────────────────┐
│  Test Method                                                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  server.setBotStateAndAwaitTick(energy: 5.0, gunHeat: 10.0)       │
│  ↓ (internally: set state + send tick + wait for bot)             │
│                                                                     │
│  var (fired, intent) = executeCommand(() => bot.setFire(1.0))     │
│  ↓ (internally: execute + go() + capture intent)                  │
│                                                                     │
│  assert !fired                       ◄─── The actual test!         │
│  assert intent.firepower == null                                   │
│                                                                     │
│  Result: 2 steps, 0 race conditions, clear semantics               │
└─────────────────────────────────────────────────────────────────────┘
```

### Benefits:

- ✅ **2-step process** from test perspective
- ✅ **Guaranteed state** before command execution
- ✅ **Clear semantics**: command result + intent in one call
- ✅ **Deterministic**: No sleeps, no races
- ✅ **Maintainable**: Change internals without breaking tests

## Architecture Comparison

### Current Architecture: Message Capture

```
┌──────────┐   manual   ┌──────────────┐   async   ┌────────┐
│   Test   │ ─────────→ │ MockedServer │ ←───────→ │  Bot   │
│   Code   │   steps    │  (Protocol)  │   msgs    │Instance│
└──────────┘            └──────────────┘           └────────┘
     │                         │                       │
     │ setEnergy(5)            │                       │
     │ sendTick() ────────────→│                       │
     │ sleep(100)              │                       │
     │                         │─────tick─────────────→│
     │ setFire(1.0) ──────────────────────────────────→│
     │                         │                       │
     │ go() ──────────────────────────────────────────→│
     │                         │←─────intent───────────│
     │ awaitIntent()           │                       │
     │←────────────────────────│                       │
     │ assert                  │                       │
```

**Problem**: Test must orchestrate every step. Coordination is manual and error-prone.

### New Architecture: State-Driven

```
┌──────────┐    sync    ┌──────────────┐    sync   ┌────────┐
│   Test   │ ─────────→ │   Enhanced   │ ←───────→ │  Bot   │
│   Code   │    API     │ MockedServer │   coord   │Instance│
└──────────┘            └──────────────┘           └────────┘
     │                         │                       │
     │ setBotStateAndAwaitTick(energy:5, gunHeat:10)  │
     │                         │                       │
     │                         │ (internal: set + tick + wait)
     │                         │─────tick─────────────→│
     │                         │←─────ready────────────│
     │                         │                       │
     │ executeCommand(() => setFire(1.0))             │
     │                         │                       │
     │                         │ (internal: execute + go + wait)
     │                         │←─────intent───────────│
     │                         │                       │
     │←───(result, intent)─────│                       │
     │ assert result           │                       │
     │ assert intent           │                       │
```

**Solution**: Test uses high-level APIs. Coordination is automatic and guaranteed.

## Test Code Comparison

### Fire Command Test: Before

```java
@Test
void test_fire_cooldown() {
    // Setup server
    MockedServer server = new MockedServer();
    server.start();
    
    // Start bot manually
    BaseBot bot = new TestBot(botInfo, server.getServerUrl());
    CompletableFuture.runAsync(bot::start);
    
    // Wait for handshake (hope it works)
    assertTrue(server.awaitBotHandshake(1000));
    server.sendGameStarted();
    assertTrue(server.awaitGameStarted(1000));
    
    // Wait for first tick
    assertTrue(server.awaitTick(1000));
    
    // Set state (not atomic)
    server.setEnergy(100.0);
    server.setGunHeat(10.0);
    
    // Send tick (manual)
    server.sendTick();
    Thread.sleep(100); // Hope bot processed it
    
    // Execute command
    boolean fired = bot.setFire(1.0);
    
    // Trigger intent submission
    CompletableFuture.runAsync(bot::go);
    Thread.sleep(100); // Hope go() started
    
    // Wait for intent
    assertTrue(server.awaitBotIntent(1000));
    
    // Finally, assert!
    assertFalse(fired);
    assertNull(server.getBotIntent().getFirepower());
    
    // Cleanup
    server.stop();
}
```

**Lines of code**: ~35  
**Coordination points**: 7  
**Potential race conditions**: 3  
**Readability**: Poor - what's being tested?

### Fire Command Test: After

```java
@Test
@DisplayName("TR-API-CMD-002 Fire cooldown prevents firing")
void test_TR_API_CMD_002_fire_cooldown() {
    // Arrange
    BaseBot bot = startBot();
    server.setBotStateAndAwaitTick(
        /* energy */ 100.0,
        /* gunHeat */ 10.0,
        /* other fields */ null, null, null, null
    );
    
    // Act
    var result = executeCommand(() -> bot.setFire(1.0));
    
    // Assert
    assertFalse(result.result, "setFire should return false when gun is cooling");
    assertNull(result.intent.getFirepower(), "Firepower should not be set in intent");
}
```

**Lines of code**: ~15  
**Coordination points**: 0 (handled internally)  
**Potential race conditions**: 0  
**Readability**: Excellent - clear Arrange/Act/Assert

## Key Improvements

### 1. State Management

**Before:**

```java
server.setEnergy(5.0);
server.sendTick();
Thread.sleep(100);
// Hope bot sees energy=5.0
```

**After:**

```java
server.setBotStateAndAwaitTick(energy: 5.0, gunHeat: 0.0);
// GUARANTEED bot sees energy=5.0
```

### 2. Command Execution

**Before:**

```java
boolean fired = bot.setFire(1.0);
bot.go();
awaitBotIntent();
BotIntent intent = server.getBotIntent();
// 4 separate steps
```

**After:**

```java
var (fired, intent) = executeCommand(() => bot.setFire(1.0));
// 1 step, returns both result and intent
```

### 3. Blocking Commands

**Before:**

```java
final boolean[] result = new boolean[1];
Thread thread = new Thread(() -> {
    bot.fire(1.0);
});
thread.start();
Thread.sleep(100);
awaitBotIntent();
// Complex manual threading
```

**After:**

```java
BotIntent intent = executeBlocking(() -> bot.fire(1.0));
// One line, handles threading internally
```

## Implementation Roadmap

```
Phase 1: Enhanced MockedServer
  │
  ├─→ Java:   awaitBotReady(), setBotStateAndAwaitTick()
  ├─→ .NET:   AwaitBotReady(), SetBotStateAndAwaitTick()
  └─→ Python: await_bot_ready(), set_bot_state_and_await_tick()
  
Phase 2: Command Execution Utilities
  │
  ├─→ Java:   AbstractBotTest + executeCommand()
  ├─→ .NET:   Enhance AbstractBotTest + ExecuteCommand()
  └─→ Python: AbstractBotTest + execute_command()
  
Phase 3: Implement Fire Tests (TR-API-CMD-002)
  │
  ├─→ Java:   CommandsFireTest.java (4 tests)
  ├─→ .NET:   CommandsFireTest.cs (4 tests)
  └─→ Python: test_commands_fire.py (4 tests)
  
Phase 4: Refactor Radar Tests (TR-API-CMD-003)
  │
  ├─→ Java:   Refactor CommandsRadarTest
  ├─→ .NET:   Refactor CommandsRadarTest
  └─→ Python: Implement test_commands_radar.py
  
Phase 5: Refactor All Existing Tests (incl. CMD-001, CMD-004)
  │
  ├─→ Audit all MockedServer-based tests
  ├─→ Apply new patterns to CMD-001 (movement) tests
  ├─→ Apply new patterns to CMD-004 (graphics) tests if applicable
  └─→ Remove old coordination code
  
Phase 6: Mock/Stub Test Bot Factory
  │
  ├─→ Java:   TestBotBuilder + configurable behaviors
  ├─→ .NET:   TestBotBuilder + configurable behaviors
  └─→ Python: test_bot_factory + configurable behaviors
  
Phase 7: Documentation
  │
  ├─→ Create TESTING-GUIDE.md
  ├─→ Update TEST-MATRIX.md
  └─→ Add inline documentation
  
Phase 8-9: Validation & Review
  │
  ├─→ Run all tests 20x for stability
  ├─→ Measure performance
  ├─→ Verify cross-language parity
  └─→ Code review & finalization
```

## Success Metrics

| Metric                     | Before    | After     | Improvement          |
|----------------------------|-----------|-----------|----------------------|
| Lines per test             | ~35       | ~15       | **57% reduction**    |
| Coordination points        | 7         | 0         | **100% elimination** |
| Race conditions            | 3         | 0         | **100% elimination** |
| Thread.sleep() calls       | 2-3       | 0         | **100% elimination** |
| Test readability           | Poor      | Excellent | **Qualitative**      |
| Test stability             | Flaky     | Stable    | **Qualitative**      |
| Time to write test         | 30-60 min | 10-15 min | **60% reduction**    |
| Mock bot setup boilerplate | High      | Minimal   | **Builder pattern**  |

## Impact Summary

### What Changes

- ✅ Test infrastructure (MockedServer, AbstractBotTest)
- ✅ Test implementations (new and refactored)
- ✅ TR-API-CMD-001 tests (movement) - refactored
- ✅ TR-API-CMD-002 tests (fire) - new
- ✅ TR-API-CMD-003 tests (radar) - refactored
- ✅ TR-API-CMD-004 tests (graphics) - refactored where applicable
- ✅ Mock/stub test bot factory
- ✅ Documentation (TESTING-GUIDE.md)
- ✅ Bad practice methods **removed** from MockedServer (`setEnergy()`, `setGunHeat()`, `setSpeed()`, `sendTick()`)

### What Doesn't Change

- ❌ Bot API public interfaces (zero changes)
- ❌ Bot behavior (zero changes)
- ❌ Wire protocol (zero changes)
- ❌ User-visible functionality (zero changes)

### Breaking Changes (Test Code Only)

- ⚠️ MockedServer methods `setEnergy()`, `setGunHeat()`, `setSpeed()`, `sendTick()` are **deleted**
- ⚠️ Any test using these methods must be migrated first
- ⚠️ After migration, these methods cannot be used (compile/runtime error)

**This is purely internal test infrastructure improvement.**

## Timeline

```
Week 1-3:  Enhanced MockedServer (all languages)
Week 4:    Command execution utilities (all languages)
Week 5-6:  Fire command tests (TR-API-CMD-002)
Week 7:    Radar test refactor (TR-API-CMD-003)
Week 8-10: Refactor all existing tests (incl. CMD-001, CMD-004)
Week 11:   Mock/stub test bot factory
Week 12:   Documentation
Week 13:   Validation, review, finalization

Total: 13 weeks (7-11 weeks estimated effort)
```

## Next Steps

1. **Review this specification** with maintainer
2. **Start with Java** (reference implementation)
3. **Validate patterns work** before porting
4. **Port to .NET and Python** with language-appropriate adjustments
5. **Test continuously** throughout implementation
6. **Document patterns** as they emerge
7. **Keep old code working** until migration complete

---

**Questions?** See:

- [Proposal](./proposal.md) - Full context and motivation
- [Design](./design.md) - Technical details and API specs
- [Tasks](./tasks.md) - Detailed task breakdown
- [MockedServer API](./specs/mocked-server-api.md) - API reference
- [AbstractBotTest API](./specs/abstract-bot-test-api.md) - Usage patterns

