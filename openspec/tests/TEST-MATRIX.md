# Cross-Language Test Parity Matrix

Each row lists a test concern and the equivalent test class in each language.
A ✅ means the test exists and passes; ❌ means not yet implemented.

## Phase 1 — MockedServer Enhancement

| Concern | Java | .NET | Python |
|---|---|---|---|
| `awaitBotReady` succeeds | ✅ `MockedServerEnhancementTest#awaitBotReady_ShouldSucceed` | ✅ `MockedServerEnhancementTest.AwaitBotReady_ShouldSucceed` | ✅ `MockedServerEnhancementTest.test_await_bot_ready_should_succeed` |
| `setBotStateAndAwaitTick` updates state | ✅ `MockedServerEnhancementTest#setBotStateAndAwaitTick_ShouldUpdateState` | ✅ `MockedServerEnhancementTest.SetBotStateAndAwaitTick_ShouldUpdateState` | ✅ `MockedServerEnhancementTest.test_set_bot_state_and_await_tick_should_update_state` |

### Language-Specific Quirks

| Quirk | Language | Detail |
|---|---|---|
| `ManualResetEvent` requires explicit `Reset()` before re-waiting | .NET | `SetBotStateAndAwaitTick` calls `_tickEvent.Reset()` before releasing the intent gate |
| `threading.Event` requires explicit `clear()` before re-waiting | Python | Same pattern — `_tick_event.clear()` in `set_bot_state_and_await_tick` |
| `CountDownLatch` is not resettable — re-assigned after each use | Java | New instance assigned at `MockedServer.java:361` after each BOT_INTENT; fields are `volatile` for cross-thread visibility |
| `awaitTick` in .NET uses `ManualResetEventSlim` (auto-reset not used) | .NET | Second call without explicit `Reset()` returns immediately — always reset before calling `AwaitTick` |
| Python `await_bot_ready` default timeout is 2 000 ms vs 1 000 ms in Java/.NET | Python | Longer default accounts for asyncio event loop startup overhead |
