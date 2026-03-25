# Change: Add TypeScript Bot API Event System

## Why

The event system is the core dispatch mechanism that drives all bot behavior in Tank Royale. Bots receive game state
changes (collisions, scans, bullets, deaths) as prioritized events and react to them through handler callbacks. Without
the event system, the TypeScript Bot API cannot respond to anything that happens during a battle. This is proposal 3 of
5, building on the foundation (proposal 1) and protocol (proposal 2) layers.

## What Changes

- Implement base event types: `IEvent` interface, `BotEvent` abstract class (turnNumber, isCritical), `ConnectionEvent`
  abstract class (serverUri)
- Implement all 17 concrete bot event classes with fields matching the Java reference:
  `TickEvent`, `ScannedBotEvent`, `HitBotEvent`, `HitByBulletEvent`, `HitWallEvent`, `BulletFiredEvent`,
  `BulletHitBotEvent`, `BulletHitBulletEvent`, `BulletHitWallEvent`, `BotDeathEvent`, `DeathEvent`, `WonRoundEvent`,
  `SkippedTurnEvent`, `CustomEvent`, `TeamMessageEvent`, `ConnectedEvent`, `DisconnectedEvent`,
  `ConnectionErrorEvent`
- Implement lifecycle events (not BotEvent subclasses): `GameStartedEvent`, `GameEndedEvent`, `RoundStartedEvent`,
  `RoundEndedEvent`
- Implement `Condition` class (name, test function) and `NextTurnCondition` prebuilt condition
- Implement `EventQueue` with priority-based dispatch, event aging (MAX_EVENT_AGE=2), critical event preservation,
  max queue size (256), custom event evaluation, and event interruption semantics
- Implement `EventPriorities` registry (get/set priority per event type) with all 15 default priorities matching Java
- Implement `EventInterruption` for tracking interruptible event types
- Implement `EventHandler<T>` (subscribe/unsubscribe with priority, publish) for handler registration
- Implement `BotEventHandlers` that maps event types to handlers and wires IBaseBot callback methods
- Add unit tests for all event classes, EventQueue dispatch ordering, interruption, and Condition/CustomEvent

## Impact

- Affected specs: `typescript-bot-api` (extends capability)
- Affected code: `bot-api/typescript/` (extends module)
- No impact on existing Bot APIs (Java, C#, Python)
