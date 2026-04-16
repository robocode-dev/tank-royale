# Bot API Updater Spec

## Purpose

The Bot API Updater ensures that bot root directories always contain up-to-date
bot API library files matching the current GUI version. It bundles all four
platform artifacts inside the GUI fat JAR and provides a startup scan with an
interactive update dialog.

---

## Requirements

### Requirement: Bundle Bot API Artifacts in GUI JAR
The GUI build SHALL bundle all four bot API library artifacts (Java `.jar`,
C# `.nupkg`, Python `.whl`, TypeScript `.tgz`) as classpath resources inside
the GUI fat JAR, using fixed version-free resource names.

#### Scenario: GUI fat JAR contains bot API resources
- **WHEN** the GUI fat JAR is built
- **THEN** the following classpath resources are present:
  `robocode-tankroyale-bot-api-java.jar`,
  `Robocode.TankRoyale.BotApi.nupkg`,
  `robocode-tank-royale-bot-api-python.whl`,
  `robocode-tank-royale-bot-api-typescript.tgz`

---

### Requirement: Startup Scan of Bot Root Directories
On GUI startup, after the main window is shown, the application SHALL scan all
configured bot root directories (both enabled and disabled) for bot API library
files that are outdated or missing.

#### Scenario: All libraries are up to date
- **GIVEN** `checkBotApiUpdates` is `true`
- **AND** every `lib/` and `deps/` subdirectory in every configured bot root
  directory contains a bot API file whose version equals the GUI version
- **WHEN** the GUI starts
- **THEN** no dialog is shown

#### Scenario: An outdated library is found
- **GIVEN** `checkBotApiUpdates` is `true`
- **AND** a bot root directory contains `lib/robocode-tankroyale-bot-api-0.39.0.jar`
  while the GUI version is `0.40.3`
- **WHEN** the GUI starts
- **THEN** the Bot API Library Update dialog is shown listing the affected
  directory, platform "Java", installed version "0.39.0", new version "0.40.3"

#### Scenario: A library file is missing
- **GIVEN** `checkBotApiUpdates` is `true`
- **AND** a bot root directory contains a `lib/` subdirectory with no
  `robocode-tankroyale-bot-api-*.jar` file
- **WHEN** the GUI starts
- **THEN** the Bot API Library Update dialog is shown listing the affected
  directory, platform "Java", installed version "missing"

#### Scenario: A disabled bot directory is included in the scan
- **GIVEN** a bot root directory is marked as disabled in the GUI settings
- **AND** it contains an outdated bot API library
- **WHEN** the GUI starts
- **THEN** that directory is included in the scan and listed in the dialog

#### Scenario: Check is disabled by user preference
- **GIVEN** `checkBotApiUpdates` is `false`
- **WHEN** the GUI starts
- **THEN** no scan is performed and no dialog is shown

---

### Requirement: Update / Restore Dialog
When outdated or missing bot API library files are found, the GUI SHALL show a
dialog that allows the user to update or restore all affected files at once.

#### Scenario: User accepts the update
- **GIVEN** the Bot API Library Update dialog is shown
- **WHEN** the user clicks "Update / Restore All"
- **THEN** each listed library file is replaced (or created) with the version
  bundled in the GUI
- **AND** a success message is shown
- **AND** the dialog closes

#### Scenario: User skips for this session
- **GIVEN** the Bot API Library Update dialog is shown
- **WHEN** the user clicks "Skip"
- **THEN** the dialog closes and the check is not performed again in this session

#### Scenario: User permanently disables the check
- **GIVEN** the Bot API Library Update dialog is shown
- **WHEN** the user clicks "Don't ask again"
- **THEN** `checkBotApiUpdates` is set to `false` in `gui.properties`
- **AND** the dialog closes
- **AND** subsequent GUI startups do not perform the scan

---

### Requirement: TypeScript `package.json` Patch
When a TypeScript bot API tarball is updated, the `package.json` at the bot
root SHALL also be updated to reference the new tarball filename.

#### Scenario: TypeScript library is updated
- **GIVEN** a bot root directory contains a TypeScript bot with
  `package.json` referencing `"file:./deps/robocode-tank-royale-bot-api-0.39.0.tgz"`
- **WHEN** the user accepts the update in the dialog
- **THEN** the old tarball is replaced with
  `deps/robocode-tank-royale-bot-api-0.40.3.tgz`
- **AND** the `package.json` reference is updated to
  `"file:./deps/robocode-tank-royale-bot-api-0.40.3.tgz"`

---

### Requirement: macOS Quarantine Attribute Cleared
On macOS, after writing a bot API library file, the application SHALL attempt
to remove the `com.apple.quarantine` extended attribute so that Gatekeeper does
not block bot startup.

#### Scenario: File written on macOS
- **GIVEN** the OS is macOS
- **WHEN** a bot API library file is written by `BotApiLibraryService`
- **THEN** `xattr -d com.apple.quarantine <file>` is executed
- **AND** failure of this command is silently ignored

---

### Requirement: Opt-Out Setting
The GUI SHALL provide a persistent setting `checkBotApiUpdates` (default:
`true`) that allows users to permanently disable the startup scan.

#### Scenario: Default value
- **GIVEN** a fresh installation with no `gui.properties`
- **WHEN** the GUI starts
- **THEN** the startup scan is performed (opt-out defaults to enabled)
