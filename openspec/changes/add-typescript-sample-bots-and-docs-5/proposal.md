# Change: Add TypeScript Sample Bots, npm Packaging, and Documentation

## Why

Sample bots are essential for demonstrating the TypeScript Bot API, serving as integration tests against the real server,
and providing copy-paste starting points for bot developers. npm packaging enables distribution via the standard
JavaScript ecosystem. Booter integration allows Node.js bots to participate in battles launched from the GUI. This is
the fifth and final proposal (5 of 5) for a complete TypeScript Bot API with 1:1 semantic equivalence to the Java
reference implementation.

## What Changes

- Create `sample-bots/typescript/` with all sample bots matching the Java set: MyFirstBot, Corners, Crazy, Fire,
  RamFire, SpinBot, Target, TrackFire, VelocityBot, Walls, PaintingBot, MyFirstDroid, MyFirstLeader, MyFirstTeam
- Each sample bot includes a TypeScript source file (`.ts`) and a JSON config file (`.json`) with `"platform": "Node.js"`
  and `"programmingLang": "TypeScript"`
- Bot code is synchronous — no async/await in the developer API (ADR-0028: Web Workers + Atomics.wait)
- Finalize npm package configuration for `@robocode.dev/tank-royale-bot-api` (dual ESM/CJS exports, TypeScript
  declarations, peer dependency on `ws` for Node.js)
- Add booter integration for launching TypeScript bots as Node.js processes (script files, platform detection)
- Create README for `bot-api/typescript/` covering installation, usage, and API overview
- Create README for `sample-bots/typescript/` covering how to run the sample bots
- Update ADR-0003 to list TypeScript as the 4th supported language alongside Java, C#, and Python
- Add Gradle build tasks for packaging and distributing TypeScript sample bots (matching Java/Python/C# pattern)

## Impact

- Affected specs: `typescript-bot-api` (new requirements for sample bots, packaging, booter integration)
- Affected code:
  - `sample-bots/typescript/` (new — 14 sample bots)
  - `bot-api/typescript/package.json` (finalized exports and peer dependencies)
  - `bot-api/typescript/README.md` (new)
  - `sample-bots/typescript/assets/ReadMe.md` (new)
  - `sample-bots/build.gradle.kts` (include TypeScript in build)
  - `booter/` (Node.js process launching support)
  - `docs-internal/architecture/adr/0003-cross-platform-bot-api-strategy.md` (add TypeScript)
- No impact on existing Bot APIs (Java, C#, Python)
