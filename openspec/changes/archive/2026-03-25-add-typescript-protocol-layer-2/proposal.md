# Change: Add TypeScript Bot API Protocol Layer

## Why

The TypeScript Bot API (ADR-0027) needs a protocol/communication layer to connect to the Tank Royale server via
WebSocket. This is the second of five proposals, building on the foundation layer (proposal 1: value objects, constants,
graphics, utilities). The protocol layer enables server communication, message parsing, and event mapping -- prerequisites
for the event system, lifecycle management, and public API layers that follow in subsequent proposals.

## What Changes

- Implement protocol DTOs as TypeScript interfaces matching the server JSON schema: `BotHandshake`, `ServerHandshake`,
  `BotIntent`, `BotReady`, `TickEventForBot`, `GameStartedEventForBot`, `GameEndedEventForBot`, `RoundStartedEvent`,
  `RoundEndedEventForBot`, `SkippedTurnEvent`, `GameAbortedEvent`, and all in-tick event types (`BotDeathEvent`,
  `BotHitBotEvent`, `BotHitWallEvent`, `BulletFiredEvent`, `BulletHitBotEvent`, `BulletHitBulletEvent`,
  `BulletHitWallEvent`, `ScannedBotEvent`, `WonRoundEvent`, `TeamMessageEvent`)
- Implement `Message.Type` enum with all message type string values matching the server protocol
- Implement `RuntimeAdapter` interface with `NodeRuntimeAdapter` and `BrowserRuntimeAdapter` (per ADR-0029) abstracting
  WebSocket creation, environment variable access, and process exit
- Implement `EnvVars` utility matching Java's environment variable names (`SERVER_URL`, `SERVER_SECRET`, `BOT_NAME`,
  `BOT_VERSION`, `BOT_AUTHORS`, etc.) using `RuntimeAdapter.getEnvVar()`
- Implement `BotHandshakeFactory` to construct bot handshake messages from `BotInfo`
- Implement `WebSocketHandler` for server connection lifecycle (connect, disconnect, message routing by type)
- Implement mappers with 1:1 correspondence to Java: `EventMapper`, `GameSetupMapper`, `BotStateMapper`,
  `BulletStateMapper`, `ResultsMapper`, `InitialPositionMapper`
- Implement JSON serialization utilities for protocol messages (type-discriminated event deserialization)
- Add unit tests for all protocol components

## Impact

- Affected specs: `typescript-bot-api` (extends capability from proposal 1)
- Affected code: `bot-api/typescript/` (extends existing module)
- No impact on existing Bot APIs (Java, C#, Python)
