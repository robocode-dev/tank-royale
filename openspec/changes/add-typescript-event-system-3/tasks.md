## 1. Base Event Types

- [x] 1.1 Implement `IEvent` marker interface
- [x] 1.2 Implement `BotEvent` abstract class (turnNumber: number, isCritical(): boolean defaulting to false)
- [x] 1.3 Implement `ConnectionEvent` abstract class (serverUri: string), implements IEvent

## 2. Concrete Bot Event Classes

- [x] 2.1 Implement `TickEvent` (roundNumber, botState, bulletStates, events)
- [x] 2.2 Implement `ScannedBotEvent` (scannedByBotId, scannedBotId, energy, x, y, direction, speed)
- [x] 2.3 Implement `HitBotEvent` (victimId, energy, x, y, isRammed)
- [x] 2.4 Implement `HitByBulletEvent` (bullet: BulletState, damage, energy)
- [x] 2.5 Implement `HitWallEvent` (turnNumber only)
- [x] 2.6 Implement `BulletFiredEvent` (bullet: BulletState)
- [x] 2.7 Implement `BulletHitBotEvent` (victimId, bullet: BulletState, damage, energy)
- [x] 2.8 Implement `BulletHitBulletEvent` (bullet: BulletState, hitBullet: BulletState)
- [x] 2.9 Implement `BulletHitWallEvent` (bullet: BulletState)
- [x] 2.10 Implement `BotDeathEvent` (victimId)
- [x] 2.11 Implement `DeathEvent` (turnNumber only, isCritical = true)
- [x] 2.12 Implement `WonRoundEvent` (turnNumber only, isCritical = true)
- [x] 2.13 Implement `SkippedTurnEvent` (turnNumber only, isCritical = true)
- [x] 2.14 Implement `CustomEvent` (condition: Condition)
- [x] 2.15 Implement `TeamMessageEvent` (message: unknown, senderId; null message throws)

## 3. Connection Events

- [x] 3.1 Implement `ConnectedEvent` (extends ConnectionEvent)
- [x] 3.2 Implement `DisconnectedEvent` (remote, statusCode?, reason?)
- [x] 3.3 Implement `ConnectionErrorEvent` (error: Error)

## 4. Lifecycle Events (IEvent, not BotEvent)

- [x] 4.1 Implement `GameStartedEvent` (myId, initialPosition, gameSetup)
- [x] 4.2 Implement `GameEndedEvent` (numberOfRounds, results: BotResults)
- [x] 4.3 Implement `RoundStartedEvent` (roundNumber)
- [x] 4.4 Implement `RoundEndedEvent` (roundNumber, turnNumber, results: BotResults)

## 5. Condition and CustomEvent Support

- [x] 5.1 Implement `Condition` class (name?: string, test: () => boolean via callable or override)
- [x] 5.2 Implement `NextTurnCondition` (extends Condition, tests turnNumber > creationTurnNumber)

## 6. Event Priority System

- [x] 6.1 Implement `EventPriorities` registry (Map from event type string to priority number)
- [x] 6.2 Initialize all 15 default priorities matching Java: WON_ROUND=150, SKIPPED_TURN=140, TICK=130, CUSTOM=120, TEAM_MESSAGE=110, BOT_DEATH=100, BULLET_HIT_WALL=90, BULLET_HIT_BULLET=80, BULLET_HIT_BOT=70, BULLET_FIRED=60, HIT_BY_BULLET=50, HIT_WALL=40, HIT_BOT=30, SCANNED_BOT=20, DEATH=10
- [x] 6.3 Implement `setPriority(eventType, priority)` and `getPriority(eventType)` methods
- [x] 6.4 Implement `EventInterruption` (set/check interruptible flag per event type)

## 7. Event Handler Infrastructure

- [x] 7.1 Implement `EventHandler<T>` (subscribe with optional priority, unsubscribe, publish, subscriber ordering)
- [x] 7.2 Implement `BotEventHandlers` (one EventHandler per event type, fireEvent dispatch by event type)
- [x] 7.3 Wire IBaseBot callback methods (onTick, onScannedBot, onHitBot, etc.) as default subscribers

## 8. EventQueue

- [x] 8.1 Implement event storage with synchronized list, MAX_QUEUE_SIZE=256
- [x] 8.2 Implement `addEvent()` with size guard
- [x] 8.3 Implement `addEventsFromTick()` (add TickEvent + all nested events)
- [x] 8.4 Implement `removeOldEvents()` (remove events older than MAX_EVENT_AGE=2 turns, preserve critical)
- [x] 8.5 Implement `sortEvents()` (critical first, then older turn first, then higher priority first)
- [x] 8.6 Implement `dispatchEvents(turnNumber)` with priority-based dispatch loop
- [x] 8.7 Implement event interruption: higher-priority events interrupt lower-priority handlers when interruptible flag is set
- [x] 8.8 Implement `addCustomEvents()` (evaluate all Conditions, create CustomEvent for those that test true)
- [x] 8.9 Implement `clear()` (clear events and conditions, reset priority)
- [x] 8.10 Implement `setCurrentEventInterruptible(boolean)`

## 9. Tests

- [x] 9.1 Test all event classes construct with correct fields and getters
- [x] 9.2 Test critical events (DeathEvent, WonRoundEvent, SkippedTurnEvent) return isCritical=true
- [x] 9.3 Test non-critical events return isCritical=false
- [x] 9.4 Test EventPriorities returns correct default values for all 15 event types
- [x] 9.5 Test EventPriorities.setPriority overrides default
- [x] 9.6 Test EventQueue dispatches events in priority order (higher priority first)
- [x] 9.7 Test EventQueue removes old non-critical events but preserves critical events
- [x] 9.8 Test EventQueue max size guard (256)
- [x] 9.9 Test event interruption: interruptible handler is interrupted by higher-priority event
- [x] 9.10 Test Condition with callable and with override
- [x] 9.11 Test CustomEvent fires when Condition.test() returns true
- [x] 9.12 Test NextTurnCondition triggers when turn advances
- [x] 9.13 Test EventHandler subscribe/publish ordering by priority
- [x] 9.14 Test TeamMessageEvent throws on null message
- [x] 9.15 Test DisconnectedEvent optional fields (statusCode, reason)
