# Bot API Test Infrastructure Refactoring

**Status**: Specification Phase  
**Date**: 2025-12-31  
**Affects**: Bot API test infrastructure across Java, .NET, and Python

## Quick Links

- [Proposal](./proposal.md) - Why we need this change
- [Design](./design.md) - Technical design and architecture
- [Tasks](./tasks.md) - Detailed implementation breakdown

## Overview

This change refactors the Bot API test infrastructure to make tests **simple, stable, and maintainable**. The current
MockedServer-based testing approach has proven extremely challenging, even for advanced AI coding assistants,
particularly for:

- **TR-API-CMD-001**: Movement command tests
- **TR-API-CMD-002**: Fire command tests
- **TR-API-CMD-003**: Radar/Scan command tests
- **TR-API-CMD-004**: Graphics frame emission

## The Problem

The current test infrastructure is a **message-capture framework** optimized for protocol validation, not a *
*state-driven testing framework** optimized for behavior testing. This creates:

1. **Multi-step coordination** for simple assertions (set state → send tick → sleep → execute command → wait → assert)
2. **Race conditions** between state updates and command execution
3. **Fragile tests** that require manual thread spawning and sleep-based timing
4. **Unclear failures** when tests timeout or hang

## The Solution

### 1. Enhanced State Control

Add **synchronous state management** to MockedServer:

```java
// OLD: Multi-step, racy
server.setEnergy(5.0);
server.

sendTick();
Thread.

sleep(100); // Hope it processed!
bot.

setFire(1.0);

// NEW: Atomic, guaranteed
server.

setBotStateAndAwaitTick(energy:5.0, gunHeat:0.0);

boolean fired = bot.setFire(1.0); // Guaranteed to see energy=5.0
```

### 2. Command Execution Utilities

Add **helper methods** that handle async orchestration:

```java
// OLD: Manual coordination
server.resetBotIntentLatch();

boolean fired = bot.setFire(1.0);
bot.

go();

awaitBotIntent();

BotIntent intent = server.getBotIntent();

assertTrue(fired);

// NEW: One-step execution
var(fired, intent) =

executeCommand(() ->bot.

setFire(1.0));

assertTrue(fired);

assertEquals(1.0,intent.getFirepower());
```

### 3. Consistent Patterns

Apply the same patterns across **all three languages** (Java, .NET, Python) to ensure:

- Tests are easy to read and write
- Behavior is predictable and deterministic
- Maintenance is straightforward

## Impact

### Files Changed

**New Files**:

- Java: `AbstractBotTest.java`, `CommandsFireTest.java`
- .NET: `CommandsFireTest.cs`
- Python: `abstract_bot_test.py`, `test_commands_fire.py`, `test_commands_radar.py`
- Documentation: `TESTING-GUIDE.md`

**Enhanced Files**:

- All three `MockedServer` implementations
- .NET `AbstractBotTest.cs`
- Existing command test files (refactored)

**Updated Files**:

- `TEST-MATRIX.md` (mark CMD-002, CMD-003 complete)

### Non-Goals

- ❌ No changes to Bot API public interfaces
- ❌ No changes to bot behavior or wire protocol
- ❌ No new Bot API features

This is **purely test infrastructure improvement**.

## Timeline

**Estimated Duration**: 6-10 weeks (29-48 days)

**Phases**:

1. Enhanced MockedServer (3 weeks)
2. Command execution utilities (1 week)
3. Fire command tests (1.5 weeks)
4. Radar test refactor (1 week)
5. Full test refactor (2-3 weeks)
6. Documentation (1 week)
7. Validation (1 week)
8. Review (0.5 weeks)

## Success Criteria

- ✅ All TR-API-CMD-001 movement command tests passing
- ✅ All TR-API-CMD-002 fire command tests passing
- ✅ All TR-API-CMD-003 radar/scan tests passing
- ✅ All existing tests refactored and stable
- ✅ No flaky tests
- ✅ Test execution time reasonable (<2x baseline)
- ✅ TESTING-GUIDE.md complete

## Getting Started

### For Implementers

1. Read the [Proposal](./proposal.md) to understand the context
2. Review the [Design](./design.md) for technical details
3. Follow the [Tasks](./tasks.md) in order
4. Start with Java (reference implementation)
5. Port to .NET and Python with adjustments for language idioms

### For Reviewers

1. Check that tests follow the patterns in [Design](./design.md)
2. Verify tests pass consistently (run 20 times)
3. Ensure tests are readable and maintainable
4. Confirm behavior is identical across languages

### For Users

No action needed - this is an internal infrastructure change. Bot API remains unchanged.

## Open Questions

See [Design](./design.md#open-questions) for discussion of:

- State injection timing
- Python threading model
- Test isolation strategy
- Timeout configuration

## Follow-Up Work

After completion:

1. Apply patterns to TR-API-CMD-001 (movement commands)
2. Consider test bot builder pattern for complex scenarios
3. Explore timeline-based test replays for advanced cases
4. Extract common patterns into cross-language test library

## Related Documents

- [TEST-MATRIX.md](../../tests/TEST-MATRIX.md) - Cross-language test parity matrix
- [TESTING-GUIDE.md](../../tests/TESTING-GUIDE.md) - To be created by this change

## History

- **2025-12-31**: Initial specification created
- **[Future]**: Implementation begins
- **[Future]**: Change completed and archived

