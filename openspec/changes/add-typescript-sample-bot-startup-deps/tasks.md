## 1. Bot API — npm Pack Task

- [x] 1.1 Add `npmPack` Gradle task to `bot-api/typescript/build.gradle.kts` that runs `npm pack` after `npmBuild`, producing `robocode.dev-tank-royale-bot-api-X.Y.Z.tgz` in the project directory

## 2. Installer Scripts (Assets)

- [x] 2.1 Create `sample-bots/typescript/assets/install-dependencies.cmd` with lock/marker pattern, Node.js detection, and `npm install --prefer-offline`
- [x] 2.2 Create `sample-bots/typescript/assets/install-dependencies.sh` with the same logic for Unix/macOS

## 3. Sample Bots Build — Deps Preparation

- [x] 3.1 Add `prepareDepsTask` to `sample-bots/typescript/build.gradle.kts` that depends on `:bot-api:typescript:npmPack`
- [x] 3.2 Task copies `install-dependencies.cmd` and `install-dependencies.sh` from assets to `archive/deps/`
- [x] 3.3 Task copies the bot API tarball from `bot-api/typescript/` to `archive/deps/`
- [x] 3.4 Task generates `archive/package.json` (at archive root, not in deps/) with the correct tarball reference (`file:./deps/robocode.dev-tank-royale-bot-api-X.Y.Z.tgz`) plus `tsx` as a dependency

## 4. Update Startup Scripts

- [x] 4.1 Update `createBatchScript(botName)` in `sample-bots/typescript/build.gradle.kts` to call `install-dependencies.cmd` then run `..\node_modules\.bin\tsx BotName.ts`
- [x] 4.2 Update `createShellScript(botName)` in `sample-bots/typescript/build.gradle.kts` to call `install-dependencies.sh` then run `../node_modules/.bin/tsx BotName.ts`
- [x] 4.3 Wire `prepareDepsTask` into the `build` task

## 5. Documentation

- [x] 5.1 Update `sample-bots/typescript/assets/ReadMe.md` — remove reference to pre-compiled JS; state Node.js is the only runtime prerequisite; describe the first-run dep install behavior
