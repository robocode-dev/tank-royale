# Change: Add TypeScript Bot API Foundation

## Why

Tank Royale needs a TypeScript Bot API for web platform support (ADR-0027). This is the first of five proposals that
together deliver a complete TypeScript Bot API with 1:1 semantic equivalence to the Java reference implementation.

This proposal establishes the project structure, build integration, and implements all foundation-layer classes: value
objects, constants, utilities, and graphics. These are prerequisites for the protocol, event, and lifecycle layers that
follow in subsequent proposals.

## What Changes

- Create `bot-api/typescript/` module with Gradle build integration (Node.js Gradle plugin)
- Implement value objects: BotInfo, GameSetup, BotState, BulletState, BotResults, InitialPosition
- Implement constants: Constants, GameType, DefaultEventPriority
- Implement graphics: Color, Point, IGraphics, SvgGraphics
- Implement utilities: ColorUtil, MathUtil
- Implement marker interface: Droid
- Add unit tests for all classes matching Java semantics
- Register module in `settings.gradle.kts`

## Impact

- Affected specs: `typescript-bot-api` (new capability)
- Affected code: `bot-api/typescript/` (new module), `settings.gradle.kts`
- No impact on existing Bot APIs (Java, C#, Python)
