# Index: Bot API Test Infrastructure Refactoring

**Change ID**: 2025-12-31-refactor-bot-api-test-infrastructure  
**Status**: 📋 Specification Phase  
**Created**: 2025-12-31  
**Estimated Duration**: 6-10 weeks

## 📚 Documentation Structure

### Core Documents

1. **[README.md](./README.md)** - Start here!
    - Quick overview of the change
    - Links to all other documents
    - Getting started guide

2. **[SUMMARY.md](./SUMMARY.md)** - Visual overview
    - Problem/solution comparison with diagrams
    - Before/after code examples
    - Architecture comparison
    - Success metrics

3. **[proposal.md](./proposal.md)** - The "Why"
    - Detailed problem analysis
    - What changes and why
    - Impact assessment
    - Success criteria
    - Timeline estimate

4. **[design.md](./design.md)** - The "How"
    - Technical architecture
    - Core design patterns
    - API specifications
    - Implementation strategy
    - Migration approach

5. **[tasks.md](./tasks.md)** - The "What"
    - Detailed task breakdown by phase
    - Time estimates per task
    - Dependencies and critical path
    - Acceptance criteria

### API Specifications

6. **[specs/mocked-server-api.md](./specs/mocked-server-api.md)**
    - MockedServer API reference
    - Existing API (preserved)
    - New API (Phase 1)
    - Usage examples
    - Implementation notes

7. **[specs/abstract-bot-test-api.md](./specs/abstract-bot-test-api.md)**
    - AbstractBotTest base class API
    - Command execution utilities
    - Usage patterns
    - Best practices
    - Common pitfalls

## 🎯 Quick Reference

### For Implementers

**Start here:**

1. Read [README.md](./README.md) for context
2. Read [proposal.md](./proposal.md) to understand the "why"
3. Read [design.md](./design.md) for technical approach
4. Follow [tasks.md](./tasks.md) phase by phase
5. Reference [specs/](./specs/) for detailed API specs

**Implementation order:**

- Phase 1: Enhanced MockedServer → Start with Java
- Phase 2: Command utilities → Port to .NET, Python
- Phase 3: Fire tests → Verify patterns work
- Phase 4: Radar refactor → Apply to existing code
- Phase 5: Full refactor → Migrate all tests
- Phase 6-8: Documentation, validation, review

### For Reviewers

**Checklist:**

- [ ] Tests follow patterns in [design.md](./design.md)
- [ ] Tests use AbstractBotTest utilities (no manual coordination)
- [ ] Tests use setBotStateAndAwaitTick (no manual state setup)
- [ ] Tests are readable (clear Arrange/Act/Assert)
- [ ] Tests pass consistently (run 20 times)
- [ ] Behavior identical across Java/.NET/Python
- [ ] No flaky tests (no sleeps, no race conditions)
- [ ] Documentation updated (TEST-MATRIX.md, inline comments)

### For Maintainers

**Decision points:**

1. **Approve specification** - Review all docs, ensure alignment
2. **Approve Phase 1 implementation** - Validate patterns work
3. **Approve cross-language consistency** - Verify parity
4. **Approve full migration** - All tests refactored
5. **Approve documentation** - TESTING-GUIDE.md complete

## 📊 Test Coverage

### TR-API-CMD-002: Fire Command (New)

| Test              | Java | .NET | Python | Description                                   |
|-------------------|------|------|--------|-----------------------------------------------|
| fire_power_bounds | ☐    | ☐    | ☐      | Firepower clamping (0.1-3.0)                  |
| fire_cooldown     | ☐    | ☐    | ☐      | setFire returns false when gunHeat > 0        |
| fire_energy_limit | ☐    | ☐    | ☐      | setFire returns false when energy < firepower |
| fire_nan_throws   | ☐    | ☐    | ☐      | setFire(NaN) throws exception                 |

### TR-API-CMD-003: Radar/Scan Commands (Refactor)

| Test              | Java | .NET | Python | Status                                          |
|-------------------|------|------|--------|-------------------------------------------------|
| rescan_intent     | ✅    | ✅    | ☐      | Needs refactor (Java/.NET) / implement (Python) |
| blocking_rescan   | ✅    | ✅    | ☐      | Needs refactor (Java/.NET) / implement (Python) |
| adjust_radar_body | ✅    | ✅    | ☐      | Needs refactor (Java/.NET) / implement (Python) |
| adjust_radar_gun  | ✅    | ✅    | ☐      | Needs refactor (Java/.NET) / implement (Python) |

### Other Tests (Refactor)

| Area                     | Java | .NET | Python | Description              |
|--------------------------|------|------|--------|--------------------------|
| Movement commands        | ☐    | ☐    | ☐      | TR-API-CMD-001 refactor  |
| Lifecycle tests          | ☐    | ☐    | ☐      | Start/stop/connect tests |
| Other MockedServer tests | ☐    | ☐    | ☐      | Various behavior tests   |

## 🗺️ Project Timeline

```
┌─────────────────────────────────────────────────────────────────┐
│ Phase 1: Enhanced MockedServer (3 weeks)                       │
├─────────────────────────────────────────────────────────────────┤
│ Week 1: Java implementation + tests                            │
│ Week 2: .NET implementation + tests                            │
│ Week 3: Python implementation + tests                          │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│ Phase 2: Command Execution Utilities (1 week)                  │
├─────────────────────────────────────────────────────────────────┤
│ Week 4: AbstractBotTest in all languages                       │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│ Phase 3: Fire Command Tests (1.5 weeks)                        │
├─────────────────────────────────────────────────────────────────┤
│ Week 5-6: Implement TR-API-CMD-002 in all languages           │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│ Phase 4: Radar Test Refactor (1 week)                          │
├─────────────────────────────────────────────────────────────────┤
│ Week 7: Refactor TR-API-CMD-003 in all languages              │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│ Phase 5: Full Test Refactor (2-3 weeks)                        │
├─────────────────────────────────────────────────────────────────┤
│ Week 8-10: Migrate all existing MockedServer-based tests      │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│ Phase 6: Documentation (1 week)                                │
├─────────────────────────────────────────────────────────────────┤
│ Week 11: TESTING-GUIDE.md, update TEST-MATRIX.md              │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│ Phase 7-8: Validation & Review (1 week)                        │
├─────────────────────────────────────────────────────────────────┤
│ Week 12: Stability testing, performance check, final review    │
└─────────────────────────────────────────────────────────────────┘

Total: 12 weeks (with buffer)
Core effort: 6-10 weeks
```

## 🎓 Learning Resources

### Understanding the Problem

- Read [SUMMARY.md](./SUMMARY.md#the-problem-current-state) - Visual comparison
- Review current test code in `bot-api/dotnet/test/src/CommandsRadarTest.cs`
- See manual coordination: threads, sleeps, latches

### Understanding the Solution

- Read [SUMMARY.md](./SUMMARY.md#the-solution-new-state) - Visual comparison
- Read [design.md](./design.md#core-design-patterns) - Three core patterns
- See examples in [specs/abstract-bot-test-api.md](./specs/abstract-bot-test-api.md#usage-patterns)

### API Reference

- [MockedServer API](./specs/mocked-server-api.md) - State management
- [AbstractBotTest API](./specs/abstract-bot-test-api.md) - Command execution

### Best Practices

- [Abstract Bot Test Best Practices](./specs/abstract-bot-test-api.md#best-practices)
- [Common Pitfalls](./specs/abstract-bot-test-api.md#common-pitfalls)

## 🔗 Related Documents

### In This Change

- All documents in this directory (see above)

### External References

- [TEST-MATRIX.md](../../tests/TEST-MATRIX.md) - Cross-language test parity
- [TESTING-GUIDE.md](../../tests/TESTING-GUIDE.md) - To be created by this change
- Existing test files:
    - Java: `bot-api/java/src/test/java/test_utils/MockedServer.java`
    - .NET: `bot-api/dotnet/test/src/test_utils/MockedServer.cs`
    - .NET: `bot-api/dotnet/test/src/AbstractBotTest.cs`
    - .NET: `bot-api/dotnet/test/src/CommandsRadarTest.cs`
    - Python: `bot-api/python/tests/test_utils/mocked_server.py`

## 📝 Notes

### Design Decisions

1. **Atomic state setup**: `setBotStateAndAwaitTick()` guarantees bot sees new state
2. **Command wrappers**: `executeCommand()` handles all coordination automatically
3. **Blocking command support**: `executeBlocking()` for commands that call go() internally
4. **Language parity**: Same semantics, idiomatic syntax per language

### Out of Scope

- ❌ Changes to Bot API public interfaces
- ❌ Changes to bot behavior
- ❌ Changes to wire protocol
- ❌ Performance optimization (if tests remain reasonable)
- ❌ New Bot API features

### Follow-Up Work

After this change completes:

1. Apply patterns to TR-API-CMD-001 (movement)
2. Consider test bot builder pattern
3. Explore timeline-based test replays
4. Extract shared test utilities

## 🚀 Getting Started

**Ready to begin? Start here:**

1. **Understand the problem**: Read [SUMMARY.md](./SUMMARY.md)
2. **Read the proposal**: [proposal.md](./proposal.md)
3. **Study the design**: [design.md](./design.md)
4. **Pick up a task**: [tasks.md](./tasks.md)
5. **Reference the APIs**: [specs/](./specs/)

**Questions?** Reach out to the maintainer: [@flemming-n-larsen](https://github.com/flemming-n-larsen)

---

**Change History:**

- 2025-12-31: Initial specification created
- [Future]: Implementation begins
- [Future]: Change completed and archived

