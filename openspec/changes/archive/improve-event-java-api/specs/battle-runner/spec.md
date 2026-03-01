## MODIFIED Requirements

### Requirement: Event Subscription from Java

The `Event<T>` class SHALL provide `on()`, `once()`, and `off()` methods as the **sole** mechanism
for subscribing to and unsubscribing from events, accepting either `java.util.function.Consumer<T>`
(for Java callers) or a Kotlin `(T) -> Unit` lambda (for Kotlin callers, via `@JvmSynthetic`
overloads). The `+=` / `-=` operator API and the `On<T>`, `Once<T>`, `Subscription<T>` wrapper
classes SHALL NOT exist.

#### Scenario: Subscribe from Java without Kotlin artifacts

- **WHEN** a Java caller registers an event handler using `event.on(owner, handler)`
- **THEN** the handler SHALL be invoked for every event fired on that `Event<T>`
- **AND** the caller SHALL NOT need to import or use `On<T>`, `plusAssign`, or `Unit.INSTANCE`

#### Scenario: Subscribe from Kotlin with lambda syntax

- **WHEN** a Kotlin caller registers a handler using `event.on(owner) { e -> handle(e) }`
- **THEN** the handler SHALL be invoked for every event fired on that `Event<T>`
- **AND** the caller SHALL NOT need to import or use `On<T>` or `plusAssign`

#### Scenario: One-shot subscription from Java

- **WHEN** a Java caller registers a handler using `event.once(owner, handler)`
- **THEN** the handler SHALL be invoked exactly once and then automatically unsubscribed

#### Scenario: One-shot subscription from Kotlin

- **WHEN** a Kotlin caller registers a handler using `event.once(owner) { e -> handle(e) }`
- **THEN** the handler SHALL be invoked exactly once and then automatically unsubscribed

#### Scenario: Unsubscribe from any language

- **WHEN** a caller invokes `event.off(owner)`
- **THEN** the handler associated with that owner SHALL be removed and no longer invoked

#### Scenario: Subscribe with priority

- **WHEN** a caller registers a handler using `event.on(owner, priority, handler)`
- **THEN** the handler SHALL be invoked in priority order relative to other handlers
