## Phase 1 — Java-Friendly API (done)

- [x] 1.1 Add `on(owner, Consumer<T>)` and `on(owner, Int, Consumer<T>)` to `Event<T>`
- [x] 1.2 Add `once(owner, Consumer<T>)` and `once(owner, Int, Consumer<T>)` to `Event<T>`
- [x] 1.3 Add `off(owner)` alias to `Event<T>`
- [x] 1.4 Update `BattleRunner.kt` internal code to use `on()` / `off()`
- [x] 1.5 Update `BattleHandle.kt` internal code to use `on()` / `off()`
- [x] 1.6 Update `AsyncBattle.java` example — remove Kotlin imports, use `on()` syntax
- [x] 1.7 Add Java event API tests to `JavaInteropTest.java`

## Phase 2 — Remove Operators + Single API (this extension)

### 2.1 Core — Event.kt

- [x] 2.1.1 Add `@JvmSynthetic on(owner, (T) -> Unit)` and `on(owner, Int, (T) -> Unit)` Kotlin overloads
- [x] 2.1.2 Add `@JvmSynthetic once(owner, (T) -> Unit)` and `once(owner, Int, (T) -> Unit)` Kotlin overloads
- [x] 2.1.3 Remove `plusAssign` operator
- [x] 2.1.4 Remove `minusAssign` operator (keep `off()` as the public unsubscribe method)
- [x] 2.1.5 Update class KDoc to reflect new API only
- [x] 2.1.6 Update `off()` implementation — call `unsubscribe()` directly instead of `minusAssign()`

### 2.2 Delete Subscription.kt

- [x] 2.2.1 Delete `lib/common/src/main/kotlin/dev/robocode/tankroyale/common/event/Subscription.kt`

### 2.3 Update EventDelegate.kt KDoc

- [x] 2.3.1 Remove `On` reference from `EventDelegate.kt` KDoc example

### 2.4 Update lib/common tests

- [x] 2.4.1 Update `lib/common/src/test/.../EventTest.kt` — replace `+= On`/`+= Once`/`-=` with `on()`/`once()`/`off()`

### 2.5 Update recorder module

- [x] 2.5.1 Update `recorder/.../RecordingObserver.kt` (~6 usages of `+= On`, `+= Once`, `-=`)

### 2.6 Update runner module

- [x] 2.6.1 Update `runner/.../internal/ServerManager.kt` (1 usage of `-=`)

### 2.7 Update gui module (~40 files)

- [x] 2.7.1 Update all GUI source files — replace every `+= On(...)` with `.on(...)`, `+= Once(...)` with `.once(...)`, `-=` with `.off()`
- [x] 2.7.2 Remove all `import dev.robocode.tankroyale.common.event.On` imports from GUI files
- [x] 2.7.3 Remove all `import dev.robocode.tankroyale.common.event.Once` imports from GUI files
- [x] 2.7.4 Update `gui/src/test/.../EventTest.kt`

### 2.8 Build + verify

- [x] 2.8.1 `./gradlew :lib:common:build` — `Event.kt` compiles without operators; `Subscription.kt` gone
- [x] 2.8.2 `./gradlew build` — full project build (no `On`/`Once` import errors anywhere)
- [x] 2.8.3 `./gradlew test` — all tests pass (137 tests across lib:common, runner, gui)

## Spec Delta

- [x] Phase 1: Write `specs/battle-runner/spec.md` delta with ADDED requirement
- [x] Phase 2: Update `specs/battle-runner/spec.md` to MODIFIED requirement (single API)
