## 1. Project Setup

- [x] 1.1 Create `bot-api/typescript/` directory structure (`src/`, `test/`)
- [x] 1.2 Create `build.gradle.kts` with Node.js Gradle plugin (download Node.js, wire npm tasks)
- [x] 1.3 Create `package.json` with `@robocode.dev/tank-royale-bot-api` package name
- [x] 1.4 Create `tsconfig.json` (strict mode, ESM + CJS dual output)
- [x] 1.5 Register module in root `settings.gradle.kts`
- [x] 1.6 Verify `./gradlew :bot-api:typescript:build` works

## 2. Constants

- [x] 2.1 Implement `Constants` (BOUNDING_CIRCLE_RADIUS, SCAN_RADIUS, MAX_SPEED, etc.)
- [x] 2.2 Implement `GameType` (CLASSIC, MELEE, ONE_VS_ONE)
- [x] 2.3 Implement `DefaultEventPriority` (all 15 priority levels)
- [x] 2.4 Add tests verifying values match Java exactly

## 3. Value Objects

- [x] 3.1 Implement `InitialPosition` (nullable x/y/direction, fromString parser)
- [x] 3.2 Implement `BotInfo` (builder pattern, validation: name/version/authors required, length limits, country code validation)
- [x] 3.3 Implement `GameSetup` (gameType, arenaWidth/Height, numberOfRounds, gunCoolingRate, timeouts)
- [x] 3.4 Implement `BotState` (all 22 fields: position, direction, speed, energy, colors, etc.)
- [x] 3.5 Implement `BulletState` (bulletId, ownerId, power, position, direction, color, computed speed)
- [x] 3.6 Implement `BotResults` (rank, scores, placements)
- [x] 3.7 Add tests for all value objects (construction, validation, edge cases)

## 4. Graphics

- [x] 4.1 Implement `Color` (factory methods: fromRgba, fromRgb, fromHexColor; toHexColor; 140+ named constants)
- [x] 4.2 Implement `Point` (x, y, equality, toString)
- [x] 4.3 Implement `IGraphics` interface (drawLine, drawRectangle, fillCircle, setStrokeColor, etc.)
- [x] 4.4 Implement `SvgGraphics` (SVG element generation, style state, text escaping, decimal formatting)
- [x] 4.5 Add tests for Color (factory methods, hex conversion, named colors)
- [x] 4.6 Add tests for SvgGraphics (canonical SVG output per drawing operation)

## 5. Utilities

- [x] 5.1 Implement `ColorUtil` (toHex, fromHexColor, fromHex — 3/6 digit support)
- [x] 5.2 Implement `MathUtil` (clamp)
- [x] 5.3 Add tests for utilities

## 6. Marker Interface

- [x] 6.1 Implement `Droid` interface (empty marker)

## 7. Verification

- [x] 7.1 `./gradlew :bot-api:typescript:test` passes
- [x] 7.2 All constant values match Java reference exactly
- [x] 7.3 BotInfo validation rules match Java (required fields, length limits, country codes)
- [x] 7.4 Color named constants match Java (140+ colors)
- [x] 7.5 SvgGraphics output matches Java canonical format
