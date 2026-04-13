# Change: Add TypeScript Sample Bot Runtime Startup via deps Folder

## Why

TypeScript sample bots cannot run today. The launch scripts call `node BotName.js`, but no compiled
`.js` files exist and there is no mechanism to resolve the `@robocode.dev/tank-royale-bot-api`
import at runtime. The distribution model must be changed to run bots directly from `.ts` source
using `tsx` — the same way Python bots run directly from `.py` source.

## What Changes

- Add a `deps/` folder to the TypeScript sample bot archive (mirrors Python's `deps/` pattern exactly)
- Add `install-dependencies.cmd` and `install-dependencies.sh` scripts to `sample-bots/typescript/assets/`
- Add `npmPack` Gradle task to `bot-api/typescript/build.gradle.kts` to produce a distributable tarball
- Add `prepareDepsTask` Gradle task to `sample-bots/typescript/build.gradle.kts` to assemble the `deps/` folder
- Update generated startup scripts to call `tsx BotName.ts` via the local `node_modules/.bin/tsx`
- Update `sample-bots/typescript/assets/ReadMe.md` to reflect the new startup model

## Impact

- Affected specs: `typescript-bot-api`
- Affected code:
  - `bot-api/typescript/build.gradle.kts` — new `npmPack` task
  - `sample-bots/typescript/build.gradle.kts` — new `prepareDepsTask`, updated script generators
  - `sample-bots/typescript/assets/` — two new installer scripts, updated ReadMe.md
