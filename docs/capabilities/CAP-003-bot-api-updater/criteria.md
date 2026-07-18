---
id: CAP-003-criteria
type: criteria
status: draft
links: [CAP-003]
title: Acceptance criteria for CAP-003 (bot-api-updater)
ac-prefix: BAU
provenance: inferred
---

```gherkin
Feature: bot-api-updater — The Bot API Updater ensures that bot root directories always contain up-to-date

  # Requirement: Bundle Bot API Artifacts in GUI JAR
  # The GUI build SHALL bundle all four bot API library artifacts (Java `.jar`,
  # C# `.nupkg`, Python `.whl`, TypeScript `.tgz`) as classpath resources inside
  # the GUI fat JAR, using fixed version-free resource names.

  @BAU-001
  Scenario: GUI fat JAR contains bot API resources
    When the GUI fat JAR is built
    Then the following classpath resources are present:
    # `robocode-tankroyale-bot-api-java.jar`,
    # `Robocode.TankRoyale.BotApi.nupkg`,
    # `robocode-tank-royale-bot-api-python.whl`,
    # `robocode-tank-royale-bot-api-typescript.tgz`
    # ---

  # Requirement: Startup Scan of Bot Root Directories
  # On GUI startup, after the main window is shown, the application SHALL scan all
  # configured bot root directories (both enabled and disabled) for bot API library
  # files that are outdated or missing.

  @BAU-002
  Scenario: All libraries are up to date
    Given `checkBotApiUpdates` is `true`
    And every `lib/` and `deps/` subdirectory in every configured bot root
    # directory contains a bot API file whose version equals the GUI version
    When the GUI starts
    Then no dialog is shown

  @BAU-003
  Scenario: An outdated library is found
    Given `checkBotApiUpdates` is `true`
    And a bot root directory contains `lib/robocode-tankroyale-bot-api-0.39.0.jar`
    # while the GUI version is `0.40.3`
    When the GUI starts
    Then the Bot API Library Update dialog is shown listing the affected
    # directory, platform "Java", installed version "0.39.0", new version "0.40.3"

  @BAU-004
  Scenario: A library file is missing
    Given `checkBotApiUpdates` is `true`
    And a bot root directory contains a `lib/` subdirectory with no
    # `robocode-tankroyale-bot-api-*.jar` file
    When the GUI starts
    Then the Bot API Library Update dialog is shown listing the affected
    # directory, platform "Java", installed version "missing"

  @BAU-005
  Scenario: A disabled bot directory is included in the scan
    Given a bot root directory is marked as disabled in the GUI settings
    And it contains an outdated bot API library
    When the GUI starts
    Then that directory is included in the scan and listed in the dialog

  @BAU-006
  Scenario: Check is disabled by user preference
    Given `checkBotApiUpdates` is `false`
    When the GUI starts
    Then no scan is performed and no dialog is shown
    # ---

  # Requirement: Update / Restore Dialog
  # When outdated or missing bot API library files are found, the GUI SHALL show a
  # dialog that allows the user to update or restore all affected files at once.

  @BAU-007
  Scenario: User accepts the update
    Given the Bot API Library Update dialog is shown
    When the user clicks "Update / Restore All"
    Then each listed library file is replaced (or created) with the version
    # bundled in the GUI
    And a success message is shown
    And the dialog closes

  @BAU-008
  Scenario: User skips for this session
    Given the Bot API Library Update dialog is shown
    When the user clicks "Skip"
    Then the dialog closes and the check is not performed again in this session

  @BAU-009
  Scenario: User permanently disables the check
    Given the Bot API Library Update dialog is shown
    When the user clicks "Don't ask again"
    Then `checkBotApiUpdates` is set to `false` in `gui.properties`
    And the dialog closes
    And subsequent GUI startups do not perform the scan
    # ---

  # Requirement: TypeScript `package.json` Patch
  # When a TypeScript bot API tarball is updated, the `package.json` at the bot
  # root SHALL also be updated to reference the new tarball filename.

  @BAU-010
  Scenario: TypeScript library is updated
    Given a bot root directory contains a TypeScript bot with
    # `package.json` referencing `"file:./deps/robocode-tank-royale-bot-api-0.39.0.tgz"`
    When the user accepts the update in the dialog
    Then the old tarball is replaced with
    # `deps/robocode-tank-royale-bot-api-0.40.3.tgz`
    And the `package.json` reference is updated to
    # `"file:./deps/robocode-tank-royale-bot-api-0.40.3.tgz"`
    # ---

  # Requirement: macOS Quarantine Attribute Cleared
  # On macOS, after writing a bot API library file, the application SHALL attempt
  # to remove the `com.apple.quarantine` extended attribute so that Gatekeeper does
  # not block bot startup.

  @BAU-011
  Scenario: File written on macOS
    Given the OS is macOS
    When a bot API library file is written by `BotApiLibraryService`
    Then `xattr -d com.apple.quarantine <file>` is executed
    And failure of this command is silently ignored
    # ---

  # Requirement: Opt-Out Setting
  # The GUI SHALL provide a persistent setting `checkBotApiUpdates` (default:
  # `true`) that allows users to permanently disable the startup scan.

  @BAU-012
  Scenario: Default value
    Given a fresh installation with no `gui.properties`
    When the GUI starts
    Then the startup scan is performed (opt-out defaults to enabled)
```
