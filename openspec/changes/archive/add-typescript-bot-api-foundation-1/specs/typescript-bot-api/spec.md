## ADDED Requirements

### Requirement: TypeScript Bot API Module

The project SHALL include a `bot-api/typescript` module that provides a TypeScript Bot API with 1:1 semantic equivalence
to the Java Bot API reference implementation.

#### Scenario: Module builds via Gradle

- **WHEN** a developer runs `./gradlew :bot-api:typescript:build`
- **THEN** Node.js is downloaded automatically via the Gradle plugin
- **AND** TypeScript source is compiled successfully
- **AND** the output includes both ESM and CJS formats

#### Scenario: Module tests pass

- **WHEN** a developer runs `./gradlew :bot-api:typescript:test`
- **THEN** all unit tests pass

---

### Requirement: Game Constants

The TypeScript Bot API SHALL provide `Constants`, `GameType`, and `DefaultEventPriority` with values identical to the
Java reference implementation.

#### Scenario: Constants match Java values

- **WHEN** a developer accesses `Constants.MAX_SPEED`
- **THEN** the value is `8`
- **AND** all other constant values match the Java Bot API exactly

#### Scenario: Event priorities match Java

- **WHEN** a developer accesses `DefaultEventPriority.WON_ROUND`
- **THEN** the value is `150`
- **AND** all 15 priority levels match the Java Bot API exactly

---

### Requirement: Value Objects

The TypeScript Bot API SHALL provide `BotInfo`, `GameSetup`, `BotState`, `BulletState`, `BotResults`, and
`InitialPosition` with identical fields, defaults, and validation to the Java reference implementation.

#### Scenario: BotInfo validation

- **WHEN** a developer creates a BotInfo without a name
- **THEN** an error is thrown
- **AND** the same validation rules apply as in the Java Bot API (required fields, length limits, country code format)

#### Scenario: BotInfo builder pattern

- **WHEN** a developer uses `BotInfo.builder().name("MyBot").version("1.0").authors(["Dev"]).build()`
- **THEN** a valid BotInfo instance is created with the specified values

#### Scenario: BulletState computed speed

- **WHEN** a BulletState has power `3.0`
- **THEN** `getSpeed()` returns `11.0` (computed as `20 - 3 * power`)

---

### Requirement: Graphics API

The TypeScript Bot API SHALL provide `Color`, `Point`, `IGraphics`, and `SvgGraphics` with identical behavior to the
Java reference implementation.

#### Scenario: Color from hex

- **WHEN** a developer calls `Color.fromHexColor("#FF0000")`
- **THEN** a Color with R=255, G=0, B=0, A=255 is returned

#### Scenario: Named colors

- **WHEN** a developer accesses `Color.RED`
- **THEN** the color matches the Java Bot API's `Color.RED` exactly
- **AND** all 140+ named colors are available

#### Scenario: SvgGraphics output

- **WHEN** a developer calls `graphics.drawCircle(100, 200, 50)` followed by `graphics.toSvg()`
- **THEN** the SVG output matches the canonical format produced by the Java SvgGraphics

---

### Requirement: Utilities

The TypeScript Bot API SHALL provide `ColorUtil` and `MathUtil` with identical behavior to the Java reference
implementation.

#### Scenario: ColorUtil hex parsing

- **WHEN** a developer calls `ColorUtil.fromHex("F00")`
- **THEN** a Color with R=255, G=0, B=0 is returned (3-digit expanded to 6-digit)

#### Scenario: MathUtil clamp

- **WHEN** a developer calls `MathUtil.clamp(15, 0, 10)`
- **THEN** the result is `10`
