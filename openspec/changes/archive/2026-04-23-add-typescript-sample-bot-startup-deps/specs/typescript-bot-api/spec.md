## ADDED Requirements

### Requirement: Sample Bot Runtime Distribution

The TypeScript sample bot archive SHALL include a `deps/` folder containing all runtime
prerequisites so that bots can be started directly from `.ts` source without any pre-compilation
step. The `deps/` folder SHALL contain:

- `install-dependencies.cmd` and `install-dependencies.sh` — platform-specific installers
- `robocode.dev-tank-royale-bot-api-X.Y.Z.tgz` — the bot API npm tarball (produced by `npm pack`)
- `package.json` — specifying `@robocode.dev/tank-royale-bot-api` (file reference to local tarball)
  and `tsx` as runtime dependencies

#### Scenario: Deps folder is present after Gradle build

- **WHEN** `./gradlew :sample-bots:typescript:build` completes
- **THEN** `build/archive/deps/` exists and contains `install-dependencies.cmd`,
  `install-dependencies.sh`, `package.json`, and the bot API `.tgz` tarball

#### Scenario: install-dependencies script is idempotent

- **WHEN** `install-dependencies.cmd` (or `.sh`) is run more than once from the same `deps/` folder
- **THEN** only the first run performs `npm install`; subsequent runs exit immediately because `.deps_installed` marker exists

#### Scenario: install-dependencies handles concurrent launch

- **WHEN** two bots are started simultaneously from the same archive
- **THEN** only one `npm install` runs; the other waits on the `.deps_lock` mutex directory

#### Scenario: install-dependencies fails clearly when Node.js is absent

- **WHEN** `node` is not on the PATH
- **THEN** the script prints an actionable error message and exits with a non-zero code

### Requirement: Sample Bot Launch via tsx

TypeScript sample bot launch scripts SHALL execute the bot directly from its `.ts` source file
using the locally installed `tsx` binary from `deps/node_modules/.bin/tsx`. No pre-compilation
to JavaScript SHALL be required.

#### Scenario: Windows bot launch

- **WHEN** `MyFirstBot.cmd` is executed on Windows
- **THEN** `install-dependencies.cmd` is called first
- **AND** the bot is started with `..\deps\node_modules\.bin\tsx MyFirstBot.ts`

#### Scenario: Unix bot launch

- **WHEN** `MyFirstBot.sh` is executed on Linux or macOS
- **THEN** `install-dependencies.sh` is called first
- **AND** the bot is started with `../deps/node_modules/.bin/tsx MyFirstBot.ts`

#### Scenario: Bot connects to Robocode server after launch

- **WHEN** a TypeScript sample bot is started via its launch script
- **THEN** it connects to the Robocode server using the `@robocode.dev/tank-royale-bot-api`
  installed in `deps/node_modules/`
