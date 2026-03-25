## 1. Protocol DTOs

- [x] 1.1 Implement `MessageType` enum with all type string values (`BotHandshake`, `ServerHandshake`, `BotReady`, `BotIntent`, `TickEventForBot`, `GameStartedEventForBot`, `GameEndedEventForBot`, `RoundStartedEvent`, `RoundEndedEventForBot`, `SkippedTurnEvent`, `GameAbortedEvent`, `TeamMessageEvent`, `WonRoundEvent`, etc.)
- [x] 1.2 Implement `ServerHandshake` interface (sessionId, name, variant, version, gameTypes, gameSetup)
- [x] 1.3 Implement `BotHandshake` interface (sessionId, name, version, authors, description, homepage, countryCodes, gameTypes, platform, programmingLang, initialPosition, teamId, teamName, teamVersion, isDroid, secret)
- [x] 1.4 Implement `BotReady` interface (type only, extends Message)
- [x] 1.5 Implement `BotIntent` interface (turnRate, gunTurnRate, radarTurnRate, targetSpeed, firepower, adjustGunForBodyTurn, adjustRadarForBodyTurn, adjustRadarForGunTurn, rescan, fireAssist, bodyColor, turretColor, radarColor, bulletColor, scanColor, tracksColor, gunColor, stdOut, stdErr, teamMessages, debugGraphics)
- [x] 1.6 Implement `TickEventForBot` interface (turnNumber, roundNumber, botState, bulletStates, events)
- [x] 1.7 Implement `GameStartedEventForBot` interface (myId, startX, startY, startDirection, teammateIds, gameSetup)
- [x] 1.8 Implement `GameEndedEventForBot` interface (numberOfRounds, results)
- [x] 1.9 Implement `RoundStartedEvent` interface (roundNumber)
- [x] 1.10 Implement `RoundEndedEventForBot` interface (roundNumber, turnNumber, results)
- [x] 1.11 Implement in-tick event interfaces: `BotDeathEvent` (victimId), `BotHitBotEvent` (victimId, energy, x, y, rammed), `BotHitWallEvent` (turnNumber), `BulletFiredEvent` (bullet), `BulletHitBotEvent` (victimId, bullet, damage, energy), `BulletHitBulletEvent` (bullet, hitBullet), `BulletHitWallEvent` (bullet), `ScannedBotEvent` (scannedByBotId, scannedBotId, energy, x, y, direction, speed), `WonRoundEvent` (turnNumber), `TeamMessageEvent` (message, messageType, senderId)
- [x] 1.12 Implement `SkippedTurnEvent` interface (turnNumber)
- [x] 1.13 Implement schema `BotState` interface (isDroid, energy, x, y, direction, gunDirection, radarDirection, radarSweep, speed, turnRate, gunTurnRate, radarTurnRate, gunHeat, enemyCount, bodyColor, turretColor, radarColor, bulletColor, scanColor, tracksColor, gunColor, isDebuggingEnabled)
- [x] 1.14 Implement schema `BulletState` interface (bulletId, ownerId, power, x, y, direction, color)
- [x] 1.15 Implement schema `GameSetup` interface (gameType, arenaWidth, arenaHeight, numberOfRounds, gunCoolingRate, maxInactivityTurns, turnTimeout, readyTimeout)
- [x] 1.16 Implement schema `ResultsForBot` interface (rank, survival, lastSurvivorBonus, bulletDamage, bulletKillBonus, ramDamage, ramKillBonus, totalScore, firstPlaces, secondPlaces, thirdPlaces)
- [x] 1.17 Implement schema `InitialPosition` interface (x, y, direction)
- [x] 1.18 Implement schema `TeamMessage` interface (message, messageType, receiverId)

## 2. RuntimeAdapter

- [x] 2.1 Define `WebSocketLike` interface (onopen, onclose, onerror, onmessage, send, close, readyState)
- [x] 2.2 Define `RuntimeAdapter` interface (createWebSocket, getEnvVar, exit)
- [x] 2.3 Implement `NodeRuntimeAdapter` using `ws` library for WebSocket, `process.env` for env vars, `process.exit()` for exit
- [x] 2.4 Implement `BrowserRuntimeAdapter` using native `WebSocket`, no-op env vars (returns undefined), no-op exit
- [x] 2.5 Implement `detectRuntime()` auto-detection function (`typeof process !== 'undefined'` for Node.js)
- [x] 2.6 Add tests for runtime detection and adapter behavior

## 3. EnvVars

- [x] 3.1 Implement `EnvVars` utility class using `RuntimeAdapter.getEnvVar()` for all environment variable names matching Java: `SERVER_URL`, `SERVER_SECRET`, `BOT_NAME`, `BOT_VERSION`, `BOT_AUTHORS`, `BOT_DESCRIPTION`, `BOT_HOMEPAGE`, `BOT_COUNTRY_CODES`, `BOT_GAME_TYPES`, `BOT_PLATFORM`, `BOT_PROG_LANG`, `BOT_INITIAL_POS`, `TEAM_ID`, `TEAM_NAME`, `TEAM_VERSION`, `BOT_BOOTED`
- [x] 3.2 Implement `getBotInfo()` that constructs a `BotInfo` from env vars (throws BotException if name/version/authors missing)
- [x] 3.3 Implement comma-separated list parsing for `BOT_AUTHORS`, `BOT_COUNTRY_CODES`, `BOT_GAME_TYPES` (matching Java's `propertyAsList`)
- [x] 3.4 Implement `getTeamId()` with null/integer parsing matching Java behavior
- [x] 3.5 Add tests for all env var getters including missing/blank/comma-separated values

## 4. WebSocket Handler

- [x] 4.1 Implement `WebSocketHandler` class that accepts `RuntimeAdapter`, server URL, server secret, `BotInfo`, and event callback hooks
- [x] 4.2 Implement `connect()` using `RuntimeAdapter.createWebSocket()` with onopen/onclose/onerror/onmessage handlers
- [x] 4.3 Implement message routing by `type` field: `ServerHandshake`, `TickEventForBot`, `RoundStartedEvent`, `RoundEndedEventForBot`, `GameStartedEventForBot`, `GameEndedEventForBot`, `SkippedTurnEvent`, `GameAbortedEvent`
- [x] 4.4 Implement `handleServerHandshake()`: store handshake, reply with `BotHandshake` via `BotHandshakeFactory`
- [x] 4.5 Implement `handleGameStarted()`: extract myId, teammateIds, gameSetup, initialPosition; send `BotReady`
- [x] 4.6 Implement `handleTick()`: deserialize `TickEventForBot`, map events via `EventMapper`, fire internal callbacks
- [x] 4.7 Implement `handleRoundStarted()`, `handleRoundEnded()`, `handleGameEnded()`, `handleGameAborted()`, `handleSkippedTurn()` matching Java WebSocketHandler behavior
- [x] 4.8 Implement `sendBotIntent()` for sending `BotIntent` JSON to server
- [x] 4.9 Implement `disconnect()` to close WebSocket connection
- [x] 4.10 Add tests for message routing and handler callbacks (mock WebSocket)

## 5. BotHandshakeFactory

- [x] 5.1 Implement `BotHandshakeFactory.create()` matching Java: populate from BotInfo fields, isDroid flag, server secret, session ID, team env vars via EnvVars
- [x] 5.2 Add tests for handshake construction with full and minimal BotInfo

## 6. Mappers

- [x] 6.1 Implement `EventMapper.map(tickEventForBot, myBotId)` returning mapped `TickEvent` with all sub-events
- [x] 6.2 Implement individual event mapping: `BotDeathEvent` to `DeathEvent` (when victimId == myBotId) or `BotDeathEvent`, `BotHitBotEvent` to `HitBotEvent`, `BotHitWallEvent` to `HitWallEvent`, `BulletFiredEvent` to `BulletFiredEvent`, `BulletHitBotEvent` to `HitByBulletEvent` (when victimId == myBotId) or `BulletHitBotEvent`, `BulletHitBulletEvent`, `BulletHitWallEvent`, `ScannedBotEvent`, `SkippedTurnEvent`, `WonRoundEvent`, `TeamMessageEvent`
- [x] 6.3 Implement `GameSetupMapper.map()` converting schema GameSetup to API GameSetup
- [x] 6.4 Implement `BotStateMapper.map()` converting schema BotState to API BotState (with color hex-to-Color conversion)
- [x] 6.5 Implement `BulletStateMapper.map()` for single and collection mapping (with color hex-to-Color conversion)
- [x] 6.6 Implement `ResultsMapper.map()` converting schema ResultsForBot to API BotResults
- [x] 6.7 Implement `InitialPositionMapper.map()` converting API InitialPosition to schema InitialPosition (for handshake)
- [x] 6.8 Add tests for all mappers verifying field-level correctness

## 7. JSON Utilities

- [x] 7.1 Implement type-discriminated event deserialization (parse `type` field in event array to instantiate correct event subtype, matching Java's `RuntimeTypeAdapterFactory` behavior)
- [x] 7.2 Implement `toJson()` and `fromJson()` utility functions for protocol messages
- [x] 7.3 Implement Color serialization/deserialization (hex string format matching Java's `ColorTypeAdapter`)
- [x] 7.4 Add tests for JSON round-trip serialization of all protocol message types
- [x] 7.5 Add tests for type-discriminated event deserialization (BotDeathEvent, ScannedBotEvent, WonRoundEvent, etc.)

## 8. Verification

- [x] 8.1 All unit tests pass via `./gradlew :bot-api:typescript:test`
- [x] 8.2 Protocol DTO field names match server JSON schema exactly (camelCase wire format)
- [x] 8.3 MessageType enum values match Java's `Message.Type` string values exactly
- [x] 8.4 EnvVars environment variable names match Java exactly
- [x] 8.5 EventMapper correctly distinguishes self-events (DeathEvent, HitByBulletEvent) from opponent events using myBotId
- [x] 8.6 BotStateMapper applies ColorUtil.fromHexColor for all 7 color fields
- [x] 8.7 WebSocketHandler message routing handles all 8 server message types
