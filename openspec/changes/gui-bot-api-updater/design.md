## Context

The GUI fat JAR already bundles the booter, server, and recorder JARs as
classpath resources (copied and renamed by Gradle during the build). This
change extends that pattern to include the four bot API artifacts and adds
a startup scan-and-update flow.

**ADR reference:** [ADR-0041](/docs-internal/architecture/adr/0041-bot-api-library-version-management.md)

## Goals / Non-Goals

**Goals:**
- Bundle all four bot API artifacts as classpath resources in the GUI fat JAR.
- On startup, scan all configured bot root directories for outdated or missing
  bot API library files.
- Present a dialog and let the user update/restore in one click.
- Encapsulate extract-and-place logic in `BotApiLibraryService` for reuse by
  future features.
- On macOS, clear the `com.apple.quarantine` xattr after placing each file so
  Gatekeeper does not block bot startup.

**Non-Goals:**
- Network downloads of any kind.
- Per-bot or per-directory opt-out granularity.
- "New Bot Creator" scaffolding (separate ADR).
- Any changes to the server, protocol, or Bot API source.

## New Kotlin Classes

All new classes live under `gui/src/main/kotlin/dev/robocode/tankroyale/gui/`.

### `botapi/BotApiPlatform.kt`

Enum that identifies each supported platform and carries the metadata needed
to locate and replace its library file:

```kotlin
enum class BotApiPlatform(
    val subDir: String,          // "lib" or "deps"
    val fileGlob: String,        // glob to match existing file in subDir
    val resourceName: String,    // fixed classpath resource name (no version)
    val newFileName: String,     // filename to write (includes Version.version)
) {
    JAVA(
        subDir = "lib",
        fileGlob = "robocode-tankroyale-bot-api-*.jar",
        resourceName = "robocode-tankroyale-bot-api-java.jar",
        newFileName = "robocode-tankroyale-bot-api-${Version.version}.jar",
    ),
    DOTNET(
        subDir = "lib",
        fileGlob = "Robocode.TankRoyale.BotApi.*.nupkg",
        resourceName = "Robocode.TankRoyale.BotApi.nupkg",
        newFileName = "Robocode.TankRoyale.BotApi.${Version.version}.nupkg",
    ),
    PYTHON(
        subDir = "deps",
        fileGlob = "robocode_tank_royale-*-py3-none-any.whl",
        resourceName = "robocode-tank-royale-bot-api-python.whl",
        newFileName = "robocode_tank_royale-${Version.version}-py3-none-any.whl",
    ),
    TYPESCRIPT(
        subDir = "deps",
        fileGlob = "robocode-tank-royale-bot-api-*.tgz",
        resourceName = "robocode-tank-royale-bot-api-typescript.tgz",
        newFileName = "robocode-tank-royale-bot-api-${Version.version}.tgz",
    ),
}
```

**Version extraction from filename:** The installed version is parsed from the
existing filename by stripping the known prefix and suffix, e.g.
`robocode-tankroyale-bot-api-0.40.0.jar` → `0.40.0`.

### `botapi/BotApiLibEntry.kt`

Data class representing one scanner finding:

```kotlin
data class BotApiLibEntry(
    val botRootDir: Path,
    val platform: BotApiPlatform,
    val installedVersion: String?,  // null = file absent
)
```

### `botapi/BotApiScanner.kt`

Reads `ConfigSettings.botDirectories` (all entries, enabled and disabled),
and for each root directory scans every platform:

1. Check whether `subDir` exists under the root.
2. If it exists, glob for `fileGlob`. If no match → `installedVersion = null`.
3. If a match is found, extract the version from the filename.
4. Emit a `BotApiLibEntry` if `installedVersion != Version.version` (including
   the `null` / missing case).

Returns `List<BotApiLibEntry>`. An empty list means everything is up to date.

### `botapi/BotApiLibraryService.kt`

Reusable service that places a bot API library file on disk:

```kotlin
object BotApiLibraryService {
    fun place(botRootDir: Path, platform: BotApiPlatform) { … }
}
```

Steps performed by `place()`:

1. Extract the classpath resource (`platform.resourceName`) to a temp file via
   `getResourceAsStream`.
2. Delete the old file matching `platform.fileGlob` in `subDir` (if present).
3. Copy the temp file to `subDir/platform.newFileName`.
4. **macOS only:** Run `xattr -d com.apple.quarantine <newFile>` to clear the
   quarantine attribute. Failure is silently ignored (attribute may not be set).

### `ui/botapi/BotApiUpdateDialog.kt`

Modal dialog shown on startup when `BotApiScanner` finds issues:

- Title: _"Bot API Library Update"_
- Message row: _"The following bot API library files are outdated or missing:"_
- A `JTable` with columns: **Bot Directory | Platform | Installed Version | New Version**
  - "Installed Version" shows `"missing"` when `installedVersion == null`.
- Buttons: **"Update / Restore All"** · **"Skip"** · **"Don't ask again"**
  - "Update / Restore All" → calls `BotApiLibraryService.place()` for each
    entry, then shows a brief success message.
  - "Skip" → dismisses for this session.
  - "Don't ask again" → sets `ConfigSettings.checkBotApiUpdates = false`, then
    dismisses permanently.

## Modified Files

### `gui/build.gradle.kts`

Four new `Copy` tasks mirror the existing pattern:

```kotlin
val copyBotApiJavaJar by registering(Copy::class) {
    dependsOn(":bot-api:java:jar")
    duplicatesStrategy = DuplicatesStrategy.FAIL
    from(project(":bot-api:java").file("./build/libs"))
    into(file("./build/classes/kotlin/main"))
    include("robocode-tankroyale-bot-api-*.jar")
    exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
    rename(".*", "robocode-tankroyale-bot-api-java.jar")
}
// … DOTNET, PYTHON, TYPESCRIPT follow same pattern
```

The existing `copyJars` task gets `dependsOn(…)` extended to include all four.

### `GuiApp.kt`

After `MainFrame.isVisible = true`:

```kotlin
val entries = BotApiScanner.scan()
if (entries.isNotEmpty()) {
    BotApiUpdateDialog(entries).isVisible = true
}
```

Wrapped in an `invokeLater` block so it runs on the EDT after the main frame
is fully painted.

### `ConfigSettings.kt`

```kotlin
private const val CHECK_BOT_API_UPDATES = "check-bot-api-updates"

var checkBotApiUpdates: Boolean
    get() = load(CHECK_BOT_API_UPDATES, "true").toBoolean()
    set(value) { save(CHECK_BOT_API_UPDATES, value.toString()) }
```

`BotApiScanner.scan()` returns an empty list immediately when
`checkBotApiUpdates == false`.

### `Strings.properties` (+ da/es/ca)

New keys:

```
bot.api.update.dialog.title = Bot API Library Update
bot.api.update.dialog.message = The following bot API library files are outdated or missing:
bot.api.update.dialog.column.bot_dir = Bot Directory
bot.api.update.dialog.column.platform = Platform
bot.api.update.dialog.column.installed_version = Installed Version
bot.api.update.dialog.column.new_version = New Version
bot.api.update.dialog.missing = missing
bot.api.update.dialog.update_all = Update / Restore All
bot.api.update.dialog.skip = Skip
bot.api.update.dialog.dont_ask = Don't ask again
bot.api.update.success = Bot API libraries updated successfully.
```

## Platform Behaviour — Native Installers

The native installers (Windows MSI, Linux DEB/RPM, macOS DMG/PKG) are produced
by `jpackage` which bundles the fat JAR alongside a private JRE. The JAR is not
extracted; classpath resource loading (`getResourceAsStream`) works identically
inside a native installer as it does when running `java -jar` directly.

The target files (user bot directories) are entirely outside the install
directory, so no elevated permissions are needed.

The only platform-specific code path is the macOS quarantine clear described
above.

## Risks / Trade-offs

- **[Low] R8 pass on binary resources**: R8 runs in `--classfile` mode, which
  should pass non-bytecode resources (JAR, nupkg, whl, tgz) through unchanged.
  This must be verified in the build smoke-test.
- **[Low] TypeScript `package.json` fragility**: regex replace on a known key.
  Fails gracefully (logs a warning, does not abort the update) if the key is
  not found.
- **[Negligible] macOS quarantine xattr**: `xattr` call failure is silently
  ignored. The worst outcome is a Gatekeeper prompt for the user — a known,
  recoverable state.
- **[Low] Build time**: four additional bot-api sub-project builds are required
  before the GUI JAR can be assembled. GUI-only CI jobs may need adjustment.
