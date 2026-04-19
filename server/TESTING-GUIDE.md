# Server Testing Guide

This guide describes the testing patterns and infrastructure for the Tank Royale Server.

## Test Registry (TR-SRV-xxx)

All server tests are mapped to acceptance criteria in `server/TEST-REGISTRY.md`. 
Every new test suite should be tagged with its corresponding TR-SRV ID using Kotest tags.

```kotlin
class MyNewTest : FunSpec({
    tags(Tag("TR-SRV-CAT-001"))
    // ...
})
```

## Testing Tiers

### Tier 1: Pure Physics & Logic (Unit Tests)

These tests target individual components like `CollisionDetector`, `GunEngine`, and `TurnProcessor`.
They are **pure**—they have no I/O, threading, or WebSocket dependencies.

- **Pattern:** Use real instances of the components. Provide known inputs (positions, speeds, intents) and verify the mutated state or returned outcomes.
- **Example:** `TurnProcessorTest.kt`, `CollisionDetectorTest.kt`.

### Tier 2: System Integration Tests

These tests verify the interaction between components through `ModelUpdater` or `GameServer`.

- **Pattern:** 
    - Use `ModelUpdater` directly for physics/scoring flows across multiple turns/rounds.
    - Use `GameServer` with mocked `ConnectionHandler` and `MessageBroadcaster` for lifecycle and connection handling.
- **Example:** `GameLifecycleTest.kt`, `GameScoringTest.kt`.

## Best Practices

1. **Positive & Negative Cases:** Every TR-SRV ID must cover both happy-path (positive) and edge/rejection (negative) scenarios.
2. **Deterministic Timing:** Tests involving time (like `TurnTimingTest`) should verify that turn intervals are exactly respected regardless of processing time.
3. **No Reflection:** Avoid using reflection to access private fields. If a class is hard to test, refactor it to use constructor injection.
4. **Mocking:** Use `mockk` for external dependencies (WebSocket, I/O) but prefer real objects for the functional core.

## Running Tests

Run all tests:
```bash
./gradlew :server:test
```

Run only the new testable architecture (ignoring legacy if any remain):
```bash
./gradlew :server:test -Dkotest.tags="!Legacy"
```
