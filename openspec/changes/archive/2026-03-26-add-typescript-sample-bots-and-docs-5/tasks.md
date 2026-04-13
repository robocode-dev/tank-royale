## 1. Sample Bot Structure

- [x] 1.1 Create `sample-bots/typescript/` directory with per-bot subdirectories
- [x] 1.2 Define JSON config format: `"platform": "Node.js"`, `"programmingLang": "TypeScript"` (matching Java/Python pattern)
- [x] 1.3 Create `sample-bots/typescript/assets/ReadMe.md` explaining requirements (Node.js 18+) and how to run bots
- [x] 1.4 Create `sample-bots/typescript/build.gradle.kts` for packaging and distribution (matching Java/Python/C# pattern)

## 2. Sample Bots — Basic

- [x] 2.1 MyFirstBot — seesaw movement with gun spin; `onScannedBot` fires, `onHitByBullet` turns perpendicular
- [x] 2.2 Target — stationary bot using custom events (`addCustomEvent`, `onCustomEvent`) to move when energy drops
- [x] 2.3 VelocityBot — demonstrates turn rates (`setGunTurnRate`, `setTurnRate`, `setTargetSpeed`, `go()`)

## 3. Sample Bots — Movement Strategies

- [x] 3.1 Corners — moves to a corner, swings gun back and forth; switches corners on death if doing poorly
- [x] 3.2 Crazy — zigzag movement using `setForward`/`setBack` with `waitFor(TurnCompleteCondition)`; reverses on wall hit
- [x] 3.3 SpinBot — continuous circular movement (`setTurnRight(10000)`, `setMaxSpeed(5)`, `forward(10000)`); fires max power
- [x] 3.4 Walls — navigates arena perimeter with gun pointed inward; uses `peek`/`rescan` pattern
- [x] 3.5 RamFire — seeks and rams opponents; adjusts fire power based on enemy energy

## 4. Sample Bots — Targeting

- [x] 4.1 Fire — sits still, rotates gun slowly; adjusts fire power by distance; moves when hit by bullet
- [x] 4.2 TrackFire — sits still, tracks nearest bot with `gunBearingTo`; fires when gun aligned; victory dance on win

## 5. Sample Bots — Graphics

- [x] 5.1 PaintingBot — demonstrates `IGraphics` API; draws fading circles at scanned bot positions using `onTick`

## 6. Sample Bots — Teams (Droid and Leader)

- [x] 6.1 MyFirstDroid — droid bot (implements `Droid` interface); follows team leader orders via `onTeamMessage`
- [x] 6.2 MyFirstLeader — team leader; broadcasts enemy positions and robot colors to teammates
- [x] 6.3 MyFirstTeam — team JSON config referencing MyFirstLeader + MyFirstDroid members

## 7. npm Package Finalization

- [x] 7.1 **[HUMAN]** Reserve `@robocode.dev/tank-royale-bot-api` package name on npm (requires npm org admin)
- [x] 7.2 Finalize `package.json` exports: ESM (`./dist/esm/index.js`) and CJS (`./dist/cjs/index.js`)
- [x] 7.3 Include TypeScript declarations (`.d.ts`) in package
- [x] 7.4 Declare `ws` as optional peer dependency for Node.js runtime
- [x] 7.5 Set package metadata: name `@robocode.dev/tank-royale-bot-api`, license, repository, keywords
- [x] 7.6 Configure `files` field to include only `dist/`, `README.md`, and `LICENSE`

## 8. Booter Integration

- [x] 8.1 Add Node.js platform detection in booter's `ProcessManager` (recognize `"platform": "Node.js"`)
- [x] 8.2 Generate launch scripts for TypeScript bots (`.cmd` and `.sh` using `node` / `npx ts-node` or `tsx`)
- [x] 8.3 Set up `lib/` directory with bot API package for standalone bot execution (matching Java's `../lib/*` pattern)
- [x] 8.4 Verify booter can start, stop, and restart Node.js bot processes

## 9. Documentation

- [x] 9.1 Create `bot-api/typescript/README.md` — installation (`npm install`), quick start, API overview, runtime targets
- [x] 9.2 Document synchronous API model (no async/await in bot code per ADR-0028)
- [x] 9.3 Document browser vs Node.js runtime differences (ADR-0029)
- [x] 9.4 Document environment variable configuration (matching Java/Python/C# env var names per ADR-0013)

## 10. Verification

- [x] 10.1 All sample bots compile with `tsc` (no type errors)
- [x] 10.2 `./gradlew :sample-bots:typescript:build` succeeds
- [x] 10.3 Each sample bot connects to server, joins battle, and participates (manual or automated test)
- [x] 10.4 MyFirstBot demonstrates basic movement and firing
- [x] 10.5 Corners demonstrates cross-round state persistence (corner switching on death)
- [x] 10.6 Crazy demonstrates `waitFor` with custom `Condition`
- [x] 10.7 Target demonstrates `addCustomEvent` / `onCustomEvent`
- [x] 10.8 PaintingBot demonstrates graphics API (`getGraphics()`, `fillCircle`)
- [x] 10.9 MyFirstTeam demonstrates team messaging (`broadcastTeamMessage`, `onTeamMessage`)
- [x] 10.10 npm package installs correctly via `npm install @robocode.dev/tank-royale-bot-api`
- [x] 10.11 Booter launches TypeScript bots from GUI
- [x] 10.12 Sample bot behavior matches Java equivalents (same strategies, same event handling patterns)

## 11. ADR Updates

- [x] 11.1 Update ADR-0003 supported languages list: add TypeScript alongside Java, C#, Python
- [x] 11.2 Update ADR-0003 symmetry section to reference `bot-api/typescript`





