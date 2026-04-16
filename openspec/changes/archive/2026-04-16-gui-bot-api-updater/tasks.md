# Tasks: GUI Bot API Library Updater

## Phase 1 — Build: Bundle Bot API Artifacts

- [ ] Update `gui/build.gradle.kts`
  - Add `copyBotApiJavaJar` task (depends on `:bot-api:java:jar`; copies and
    renames to `robocode-tankroyale-bot-api-java.jar` in
    `./build/classes/kotlin/main`)
  - Add `copyBotApiDotnetNupkg` task (depends on `:bot-api:dotnet:build`; copies
    and renames to `Robocode.TankRoyale.BotApi.nupkg`)
  - Add `copyBotApiPythonWhl` task (depends on `:bot-api:python:build-dist`;
    copies and renames to `robocode-tank-royale-bot-api-python.whl`)
  - Add `copyBotApiTypescriptTgz` task (depends on `:bot-api:typescript:npmPack`;
    copies and renames to `robocode-tank-royale-bot-api-typescript.tgz`)
  - Extend `copyJars` to depend on all four new tasks
- [ ] Verify the four resources are reachable via `getResourceAsStream()` in a
  smoke-test or unit test

## Phase 2 — Model: Platform Metadata

- [ ] Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/botapi/BotApiPlatform.kt`
  - Enum with values `JAVA`, `DOTNET`, `PYTHON`, `TYPESCRIPT`
  - Each entry carries: `subDir`, `fileGlob`, `resourceName`, `newFileName`
    (newFileName includes `Version.version`)
  - Add `companion object` helper `extractVersionFromFilename(filename: String): String?`

- [ ] Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/botapi/BotApiLibEntry.kt`
  - Data class: `botRootDir: Path`, `platform: BotApiPlatform`,
    `installedVersion: String?` (null = missing)

## Phase 3 — Scanner

- [ ] Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/botapi/BotApiScanner.kt`
  - `object BotApiScanner { fun scan(): List<BotApiLibEntry> }`
  - Returns empty list immediately if `ConfigSettings.checkBotApiUpdates == false`
  - Iterates all entries in `ConfigSettings.botDirectories` (enabled + disabled)
  - For each root × each `BotApiPlatform`:
    - Skip if `subDir` does not exist under the root
    - Glob for `fileGlob`; if no match → emit entry with `installedVersion = null`
    - If match found, extract version from filename
    - Emit entry only if version ≠ `Version.version`

## Phase 4 — Library Service

- [ ] Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/botapi/BotApiLibraryService.kt`
  - `object BotApiLibraryService { fun place(botRootDir: Path, platform: BotApiPlatform) }`
  - Extract classpath resource (`platform.resourceName`) to a temp file
  - Delete existing file(s) matching `platform.fileGlob` in `subDir`
  - Copy temp file to `subDir/platform.newFileName`
  - If `platform == TYPESCRIPT`: patch `package.json` at bot root (regex replace
    the `file:./deps/robocode-tank-royale-bot-api-X.Y.Z.tgz` reference)
  - If macOS: run `xattr -d com.apple.quarantine <newFile>`; ignore failure

## Phase 5 — UI

- [ ] Create `gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/botapi/BotApiUpdateDialog.kt`
  - Modal `JDialog` with:
    - Message label
    - `JTable` (columns: Bot Directory | Platform | Installed Version | New Version)
    - "Update / Restore All" button → calls `BotApiLibraryService.place()` for
      each entry; shows brief success message; disposes dialog
    - "Skip" button → disposes dialog
    - "Don't ask again" button → sets `ConfigSettings.checkBotApiUpdates = false`;
      disposes dialog
  - "Installed Version" cell shows `Strings["bot.api.update.dialog.missing"]` when
    `installedVersion == null`

## Phase 6 — Settings & Strings

- [ ] Update `gui/src/main/kotlin/dev/robocode/tankroyale/gui/settings/ConfigSettings.kt`
  - Add `private const val CHECK_BOT_API_UPDATES = "check-bot-api-updates"`
  - Add `var checkBotApiUpdates: Boolean` property (default: `true`)

- [ ] Update `gui/src/main/resources/dev/robocode/tankroyale/gui/Strings.properties`
  and the `_da`, `_es`, `_ca` variants with all `bot.api.update.*` keys
  (see design.md for the full key list; non-English variants may use English
  text initially)

## Phase 7 — Wire Startup Check

- [ ] Update `gui/src/main/kotlin/dev/robocode/tankroyale/gui/GuiApp.kt`
  - After `MainFrame.isVisible = true`, inside `SwingUtilities.invokeLater { … }`:

    ```kotlin
    val entries = BotApiScanner.scan()
    if (entries.isNotEmpty()) {
        BotApiUpdateDialog(entries).isVisible = true
    }
    ```

## Phase 8 — Tests & Verification

- [ ] Add unit tests for `BotApiPlatform`
  - `extractVersionFromFilename` returns correct version for each platform's filename
  - Returns `null` for non-matching filename
- [ ] Add unit tests for `BotApiScanner`
  - Returns empty list when `checkBotApiUpdates = false`
  - Returns entry for outdated file (mocked bot dir)
  - Returns entry with `installedVersion = null` when file is absent but subDir exists
  - Skips subdirectory if `subDir` does not exist at all
  - Includes disabled bot directories in the scan
- [ ] Add unit tests for `BotApiLibraryService`
  - File is written to correct path
  - Old versioned file is deleted after placement
  - TypeScript `package.json` reference is patched correctly
  - TypeScript: missing `package.json` does not throw (log warning only)
- [ ] Run `.\gradlew :gui:smokeTest` and confirm four new resources are loadable
- [ ] Verify `PropertiesConsistencyTest` still passes (new `check-bot-api-updates`
  key must be covered)
