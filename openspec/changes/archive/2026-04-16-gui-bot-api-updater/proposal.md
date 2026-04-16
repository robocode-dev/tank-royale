## Why

When users upgrade the Robocode Tank Royale GUI, the bot API library files
inside their local bot directories are not updated. These files live in
`lib/` (Java, C#) or `deps/` (Python, TypeScript) and carry a version number
in the filename. After a GUI upgrade the old files remain, creating a
version mismatch between the GUI's embedded server and the running bots.

A concrete symptom was reported in
[issue #207](https://github.com/robocode-dev/tank-royale/issues/207): Java bots
running under Java 26 logged reflection warnings because a stale
`robocode-tankroyale-bot-api-0.40.0.jar` was in the `lib/` directory while the
GUI was at a newer version.

A secondary problem: bot developers who manually set up a bot directory may be
missing the library file entirely, leading to silent runtime failures.

## What Changes

- The GUI fat JAR bundles all four bot API artifacts (Java `.jar`, C# `.nupkg`,
  Python `.whl`, TypeScript `.tgz`) as classpath resources alongside the
  existing booter / server / recorder JARs.
- On startup (after the main window is shown), the GUI scans all configured bot
  root directories (both enabled and disabled) for:
  - Bot API library files whose version ≠ current GUI version, **and**
  - Bot API library files that are missing from an otherwise-valid `lib/` or
    `deps/` directory.
- If any affected directories are found, a dialog lists them (directory,
  platform, installed version or "missing", GUI version).
- The user can click **Update / Restore** to fix all at once, **Skip** to
  dismiss for this session, or turn off the check permanently in settings.
- For TypeScript, replacing the tarball also patches the `package.json`
  dependency reference at the bot root to point to the new filename.
- A new `BotApiLibraryService` encapsulates the extract-and-place logic and
  is designed for reuse by a future "New Bot Creator" feature.

**Non-goals (explicitly out of scope):**
- Downloading artifacts from the internet (Maven Central / NuGet / PyPI / npm).
- Per-bot or per-directory opt-out (whole check can only be disabled globally).
- Any changes to the server, protocol, or Bot API source.
- "New Bot Creator" wizard (separate ADR and OpenSpec change).

## Capabilities

### New Capabilities
- `bot-api-library-updater`: Startup check that detects outdated or missing bot
  API library files across all configured bot root directories and offers a
  one-click update / restore path.

### Modified Capabilities
- `gui-startup`: After `MainFrame` is shown, trigger the bot API library check
  (unless `checkBotApiUpdates = false` in settings).

## Impact

- `gui/build.gradle.kts`: Add four copy tasks that bundle the bot API artifacts
  as classpath resources.
- `gui`: New package `dev.robocode.tankroyale.gui.botapi` with
  `BotApiPlatform`, `BotApiLibInfo`, `BotApiScanner`, `BotApiLibraryService`.
- `gui`: New UI class `BotApiUpdateDialog` (startup dialog).
- `gui`: `GuiApp.kt` — wire startup check after main window is shown.
- `gui`: `ConfigSettings.kt` — add `checkBotApiUpdates: Boolean` (default: `true`).
- `gui`: `Strings.properties` (+ da/es/ca) — i18n strings for dialog.

## References

- [Issue #207 — Java 26 final-field reflection warning](https://github.com/robocode-dev/tank-royale/issues/207)
- [ADR-0041: Bot API Library Version Management in the GUI](/docs-internal/architecture/adr/0041-bot-api-library-version-management.md)
- [gui/build.gradle.kts](/gui/build.gradle.kts) — existing resource-copy pattern
