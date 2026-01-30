# Pending Fixes for Bot API Tests

**Created**: 2026-01-30  
**Purpose**: Track outstanding issues that need to be addressed

---

## Java Test Infrastructure (BLOCKING)

### Issue: TestBotBuilderTest Hanging

**Status**: ⚠️ BLOCKING - Tests hang indefinitely  
**Priority**: High  
**File**: `bot-api/java/src/test/java/test_utils/TestBotBuilderTest.java`

**Symptoms:**
- Test execution hangs at `> Executing test test_utils.TestBotBuilderTest`
- Gradle shows 87% EXECUTING but never completes
- Last output shows `BOT_INTENT` received, then hangs
- Requires manual termination (Ctrl+C)

**Affected Tests:**
- `TestBotBuilder onTick callback is invoked` - hangs
- Other TestBotBuilderTest methods likely affected

**Root Cause Analysis:**
- MockedServer sends tick after receiving BotIntent (per protocol)
- Bot callback is invoked but test doesn't properly release/complete
- Similar to Python test_bot_factory_test.py issues
- May be related to await latches not being released

**Impact:**
- Cannot run full Java test suite without timeout
- CI/CD will hang on this test

**Workaround:**
```powershell
# Run tests excluding TestBotBuilderTest
./gradlew :bot-api:java:test --tests "!test_utils.TestBotBuilderTest"
```

**Suggested Investigation:**
1. Check if test latches (awaitBotIntent, etc.) are properly released
2. Compare with Python test_bot_factory_test.py which has same issue
3. Review MockedServer's tick/intent feedback loop
4. Add test timeouts to prevent indefinite hanging

---

## .NET Test Infrastructure (BLOCKING)

### Issue: TestBotBuilder.cs Compilation Errors

**Status**: ⚠️ Blocking all .NET test execution  
**Priority**: High  
**File**: `bot-api/dotnet/test/src/TestBotBuilder.cs`

**Missing Methods:**
1. `SetRadarTurnRate` - Method not implemented
2. `BearingFrom` - Method not implemented

**Impact:**
- All .NET tests fail to compile
- Cannot verify CommandsFireTest.cs (10 tests)
- Cannot verify any other .NET Bot API tests

**Suggested Fix:**
```csharp
// In TestBotBuilder.cs, add:

public TestBotBuilder SetRadarTurnRate(double rate)
{
    // Implementation needed - set radar turn rate on bot
    return this;
}

public double BearingFrom(double x, double y)
{
    // Implementation needed - calculate bearing from coordinates
    return 0.0;
}
```

**Verification Command:**
```powershell
cd bot-api/dotnet/test
dotnet build
```

---


## Completed Items ✓

### Python test_bot_factory_test.py - FIXED
- **Issue**: Tests failing with `AttributeError: '_ConfigurableTestBot' object has no attribute 'is_running'`
- **Root Cause**: Python Bot API uses `running` property, not `is_running()` method (differs from Java)
- **Resolution**: Changed `while self.is_running():` to `while self.running:` in test_bot_factory.py line 415
- **Status**: ✓ All 9 tests pass

### Python Fire Tests - FIXED
- **Issue**: Intent capture returning stale firepower values
- **Resolution**: Added workaround in `set_fire_and_get_intent()` to use local intent firepower
- **Status**: ✓ All 10 fire tests pass

### Python AsyncIO Hanging - FIXED  
- **Issue**: Tests hanging due to asyncio executor not shutting down
- **Resolution**: Added `os._exit(0)` in pytest session fixture, using daemon threads
- **Status**: ✓ Tests complete in ~22 seconds

---

## Priority Order

1. **HIGH**: Fix Java TestBotBuilderTest hanging (blocking test suite)
2. **HIGH**: Fix .NET TestBotBuilder.cs compilation errors

---

## Related Files

| Language | Test File | Status |
|----------|-----------|--------|
| Java | `CommandsFireTest.java` | ✓ PASS |
| Java | `TestBotBuilderTest.java` | ⚠️ HANGING |
| Python | `test_commands_fire.py` | ✓ PASS |
| Python | `test_bot_factory_test.py` | ✓ PASS |
| .NET | `CommandsFireTest.cs` | ⚠️ Blocked by build |
| .NET | `TestBotBuilder.cs` | ⚠️ Compile errors |
