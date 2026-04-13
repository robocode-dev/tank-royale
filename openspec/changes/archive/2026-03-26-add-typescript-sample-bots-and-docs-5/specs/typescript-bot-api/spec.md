## ADDED Requirements

### Requirement: TypeScript Sample Bots

The project SHALL include TypeScript sample bots in `sample-bots/typescript/` that match the Java sample bots in
behavior, strategy, and API usage patterns. Bot code SHALL be synchronous (no async/await) per ADR-0028.

#### Scenario: MyFirstBot runs and demonstrates basic API

- **WHEN** MyFirstBot is started against the server
- **THEN** it moves forward 100, spins gun 360, moves back 100 in a loop
- **AND** it fires power 1 when scanning an enemy
- **AND** it turns perpendicular to incoming bullets when hit

#### Scenario: Corners demonstrates cross-round state and conditional strategy

- **WHEN** Corners is started against the server
- **THEN** it moves to a random corner and swings its gun back and forth
- **AND** if it dies with 75% or more enemies still alive, it switches to a different corner in the next round
- **AND** it uses `stop()` and `resume()` to pause movement while firing at scanned bots

#### Scenario: Crazy demonstrates waitFor with custom Condition

- **WHEN** Crazy is started against the server
- **THEN** it uses `setForward`, `setTurnLeft`, `setTurnRight` with `waitFor(TurnCompleteCondition)`
- **AND** it reverses direction when hitting a wall or being rammed

#### Scenario: Fire demonstrates distance-based fire power

- **WHEN** Fire is started against the server
- **THEN** it sits still and rotates its gun slowly
- **AND** it fires power 3 when an enemy is closer than 50 units with energy above 50, power 1 otherwise
- **AND** it moves perpendicular to bullet direction when hit

#### Scenario: RamFire demonstrates ramming strategy

- **WHEN** RamFire is started against the server
- **THEN** it turns toward scanned bots and rams them
- **AND** it adjusts fire power based on enemy energy to avoid killing them (preferring ram bonus)

#### Scenario: SpinBot demonstrates continuous circular movement

- **WHEN** SpinBot is started against the server
- **THEN** it moves in circles using `setTurnRight(10000)` with `setMaxSpeed(5)` and `forward(10000)`
- **AND** it fires power 3 whenever it scans an enemy

#### Scenario: Target demonstrates custom events

- **WHEN** Target is started against the server
- **THEN** it registers a custom event via `addCustomEvent` that triggers when energy drops below a threshold
- **AND** it moves when the custom event fires via `onCustomEvent`

#### Scenario: TrackFire demonstrates gun tracking

- **WHEN** TrackFire is started against the server
- **THEN** it uses `gunBearingTo` to track scanned bots and fires when the gun is aligned within 3 degrees
- **AND** it performs a victory dance (turn 36000 degrees) when it wins the round

#### Scenario: VelocityBot demonstrates turn rate control

- **WHEN** VelocityBot is started against the server
- **THEN** it uses `setGunTurnRate`, `setTurnRate`, `setTargetSpeed`, and explicit `go()` calls
- **AND** it reverses target speed when hitting a wall

#### Scenario: Walls demonstrates perimeter navigation

- **WHEN** Walls is started against the server
- **THEN** it navigates along the arena walls with the gun pointed inward
- **AND** it uses `rescan()` to check for enemies before turning corners

#### Scenario: PaintingBot demonstrates graphics API

- **WHEN** PaintingBot is started against the server with graphical debugging enabled
- **THEN** it calls `getGraphics()` in `onTick` to draw circles at scanned bot positions
- **AND** the circles fade over time using decreasing alpha values via `Color.fromRgba`

#### Scenario: MyFirstDroid demonstrates Droid interface and team messaging

- **WHEN** MyFirstDroid is started as part of MyFirstTeam
- **THEN** it implements the `Droid` interface (no radar, extra energy)
- **AND** it responds to team messages: `Point` messages cause it to turn and fire, `RobotColors` messages set its colors

#### Scenario: MyFirstLeader demonstrates team leadership

- **WHEN** MyFirstLeader is started as part of MyFirstTeam
- **THEN** it broadcasts `RobotColors` to teammates at round start via `broadcastTeamMessage`
- **AND** it broadcasts scanned enemy positions as `Point` objects to teammates (excluding teammates via `isTeammate`)

---

### Requirement: TypeScript Sample Bot Configuration

Each TypeScript sample bot SHALL have a JSON configuration file with `"platform": "Node.js"` and
`"programmingLang": "TypeScript"`, following the same schema as Java and Python sample bots.

#### Scenario: Bot JSON config matches schema

- **WHEN** a TypeScript sample bot's JSON file is read
- **THEN** it contains `name`, `version`, `authors`, `description`, `homepage`, `countryCodes`, `platform`, and
  `programmingLang` fields
- **AND** `platform` is `"Node.js"`
- **AND** `programmingLang` is `"TypeScript"`

#### Scenario: Bot names match Java equivalents

- **WHEN** comparing TypeScript and Java sample bot JSON files
- **THEN** the `name`, `version`, `authors`, `description`, and `countryCodes` fields are identical
- **AND** only `platform` and `programmingLang` differ

---

### Requirement: npm Package Distribution

The TypeScript Bot API SHALL be distributed as an npm package (`@robocode.dev/tank-royale-bot-api`) with dual ESM/CJS
exports and TypeScript declarations.

#### Scenario: ESM import works

- **WHEN** a developer writes `import { Bot } from '@robocode.dev/tank-royale-bot-api'`
- **THEN** the ESM entry point resolves correctly
- **AND** all public API classes are available

#### Scenario: CJS require works

- **WHEN** a developer writes `const { Bot } = require('@robocode.dev/tank-royale-bot-api')`
- **THEN** the CJS entry point resolves correctly
- **AND** all public API classes are available

#### Scenario: TypeScript declarations included

- **WHEN** a developer uses the package in a TypeScript project
- **THEN** full type information is available via the included `.d.ts` files
- **AND** IDE autocompletion works for all public API members

#### Scenario: ws peer dependency for Node.js

- **WHEN** a developer installs the package for Node.js usage
- **THEN** `ws` is listed as an optional peer dependency
- **AND** the package warns if `ws` is not installed when running in Node.js

---

### Requirement: Booter Integration for Node.js Bots

The booter SHALL support launching TypeScript bots as Node.js processes, enabling them to participate in battles started
from the GUI.

#### Scenario: Booter recognizes Node.js platform

- **WHEN** the booter reads a bot JSON file with `"platform": "Node.js"`
- **THEN** it identifies the bot as a Node.js bot
- **AND** it uses the appropriate launch script to start the bot process

#### Scenario: Booter starts and stops Node.js bot processes

- **WHEN** the booter starts a TypeScript bot
- **THEN** a Node.js process is created with the correct environment variables (server URL, bot name, etc.)
- **AND** the bot connects to the server via WebSocket
- **AND** the booter can stop the process cleanly

#### Scenario: Launch scripts generated for TypeScript bots

- **WHEN** TypeScript sample bots are packaged
- **THEN** `.cmd` (Windows) and `.sh` (Unix) launch scripts are generated
- **AND** the scripts invoke Node.js with the correct entry point and library path

---

### Requirement: TypeScript Bot API Documentation

The TypeScript Bot API module SHALL include a README documenting installation, usage, and the synchronous API model.

#### Scenario: README covers quick start

- **WHEN** a developer reads `bot-api/typescript/README.md`
- **THEN** it explains how to install the package via `npm install @robocode.dev/tank-royale-bot-api`
- **AND** it includes a minimal bot example showing the synchronous `run()` loop pattern

#### Scenario: README documents synchronous model

- **WHEN** a developer reads the README
- **THEN** it explains that bot code is synchronous (no async/await) per ADR-0028
- **AND** it describes the Web Workers + Atomics.wait threading model at a high level

#### Scenario: README documents runtime targets

- **WHEN** a developer reads the README
- **THEN** it explains that the API works in both Node.js and browsers (ADR-0029)
- **AND** it describes the runtime adapter auto-detection and manual override

---

### Requirement: ADR-0003 Updated for TypeScript

ADR-0003 (Cross-Platform Bot API Strategy) SHALL list TypeScript as a supported language alongside Java, C#, and Python.

#### Scenario: ADR-0003 includes TypeScript

- **WHEN** a developer reads ADR-0003
- **THEN** the supported languages section lists Java, C#, Python, and TypeScript
- **AND** the symmetry requirements apply to all four languages including TypeScript
