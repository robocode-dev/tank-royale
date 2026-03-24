## 1. Base Event Types

- [ ] 1.1 Implement `IEvent` marker interface
- [ ] 1.2 Implement `BotEvent` abstract class (turnNumber: number, isCritical(): boolean defaulting to false)
- [ ] 1.3 Implement `ConnectionEvent` abstract class (serverUri: string), implements IEvent

## 2. Concrete Bot Event Classes

- [ ] 2.1 Implement `TickEvent` (roundNumber, botState, bulletStates, events)
- [ ] 2.2 Implement `ScannedBotEvent` (scannedByBotId, scannedBotId, energy, x, y, direction, speed)
- [ ] 2.3 Implement `HitBotEvent` (victimId, energy, x, y, isRammed)
- [ ] 2.4 Implement `HitByBulletEvent` (bullet: BulletState, damage, energy)
- [ ] 2.5 Implement `HitWallEvent` (turnNumber only)
- [ ] 2.6 Implement `BulletFiredEvent` (bullet: BulletState)
- [ ] 2.7 Implement `BulletHitBotEvent` (victimId, bullet: BulletState, damage, energy)
- [ ] 2.8 Implement `BulletHitBulletEvent` (bullet: BulletState, hitBullet: BulletState)
- [ ] 2.9 Implement `BulletHitWallEvent` (bullet: BulletState)
- [ ] 2.10 Implement `BotDeathEvent` (victimId)
- [ ] 2.11 Implement `DeathEvent` (turnNumber only, isCritical = true)
- [ ] 2.12 Implement `WonRoundEvent` (turnNumber only, isCritical = true)
- [ ] 2.13 Implement `SkippedTurnEvent` (turnNumber only, isCritical = true)
- [ ] 2.14 Implement `CustomEvent` (condition: Condition)
- [ ] 2.15 Implement `TeamMessageEvent` (message: unknown, senderId; null message throws)

## 3. Connection Events

- [ ] 3.1 Implement `ConnectedEvent` (extends ConnectionEvent)
- [ ] 3.2 Implement `DisconnectedEvent` (remote, statusCode?, reason?)
- [ ] 3.3 Implement `ConnectionErrorEvent` (error: Error)

## 4. Lifecycle Events (IEvent, not BotEvent)

- [ ] 4.1 Implement `GameStartedEvent` (myId, initialPosition, gameSetup)
- [ ] 4.2 Implement `GameEndedEvent` (numberOfRounds, results: BotResults)
- [ ] 4.3 Implement `RoundStartedEvent` (roundNumber)
- [ ] 4.4 Implement `RoundEndedEvent` (roundNumber, turnNumber, results: BotResults)

## 5. Condition and CustomEvent Support

- [ ] 5.1 Implement `Condition` class (name?: string, test: () => boolean via callable or override)
- [ ] 5.2 Implement `NextTurnCondition` (extends Condition, tests turnNumber > creationTurnNumber)

## 6. Event Priority System

- [ ] 6.1 Implement `EventPriorities` registry (Map from event type string to priority number)
- [ ] 6.2 Initialize all 15 default priorities matching Java: WON_ROUND=150, SKIPPED_TURN=140, TICK=130, CUSTOM=120, TEAM_MESSAGE=110, BOT_DEATH=100, BULLET_HIT_WALL=90, BULLET_HIT_BULLET=80, BULLET_HIT_BOT=70, BULLET_FIRED=60, HIT_BY_BULLET=50, HIT_WALL=40, HIT_BOT=30, SCANNED_BOT=20, DEATH=10
- [ ] 6.3 Implement `setPriority(eventType, priority)` and `getPriority(eventType)` methods
- [ ] 6.4 Implement `EventInterruption` (set/check interruptible flag per event type)

## 7. Event Handler Infrastructure

- [ ] 7.1 Implement `EventHandler<T>` (subscribe with optional priority, unsubscribe, publish, subscriber ordering)
- [ ] 7.2 Implement `BotEventHandlers` (one EventHandler per event type, fireEvent dispatch by event type)
- [ ] 7.3 Wire IBaseBot callback methods (onTick, onScannedBot, onHitBot, etc.) as default subscribers

## 8. EventQueue

- [ ] 8.1 Implement event storage with synchronized list, MAX_QUEUE_SIZE=256
- [ ] 8.2 Implement `addEvent()` with size guard
- [ ] 8.3 Implement `addEventsFromTick()` (add TickEvent + all nested events)
- [ ] 8.4 Implement `removeOldEvents()` (remove events older than MAX_EVENT_AGE=2 turns, preserve critical)
- [ ] 8.5 Implement `sortEvents()` (critical first, then older turn first, then higher priority first)
- [ ] 8.6 Implement `dispatchEvents(turnNumber)` with priority-based dispatch loop
- [ ] 8.7 Implement event interruption: higher-priority events interrupt lower-priority handlers when interruptible flag is set
- [ ] 8.8 Implement `addCustomEvents()` (evaluate all Conditions, create CustomEvent for those that test true)
- [ ] 8.9 Implement `clear()` (clear events and conditions, reset priority)
- [ ] 8.10 Implement `setCurrentEventInterruptible(boolean)`

## 9. Tests

- [ ] 9.1 Test all event classes construct with correct fields and getters
- [ ] 9.2 Test critical events (DeathEvent, WonRoundEvent, SkippedTurnEvent) return isCritical=true
- [ ] 9.3 Test non-critical events return isCritical=false
- [ ] 9.4 Test EventPriorities returns correct default values for all 15 event types
- [ ] 9.5 Test EventPriorities.setPriority overrides default
- [ ] 9.6 Test EventQueue dispatches events in priority order (higher priority first)
- [ ] 9.7 Test EventQueue removes old non-critical events but preserves critical events
- [ ] 9.8 Test EventQueue max size guard (256)
- [ ] 9.9 Test event interruption: interruptible handler is interrupted by higher-priority event
- [ ] 9.10 Test Condition with callable and with override
- [ ] 9.11 Test CustomEvent fires when Condition.test() returns true
- [ ] 9.12 Test NextTurnCondition triggers when turn advances
- [ ] 9.13 Test EventHandler subscribe/publish ordering by priority
- [ ] 9.14 Test TeamMessageEvent throws on null message
- [ ] 9.15 Test DisconnectedEvent optional fields (statusCode, reason)
