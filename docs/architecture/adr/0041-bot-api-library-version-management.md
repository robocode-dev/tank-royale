# ADR-0041: Bot API Library Version Management in the GUI

**Status:** Proposed  
**Date:** 2026-04-16

---

## Context

Bot developers install bots (e.g. the official sample bots) into local bot
directories configured in the GUI. Each supported language uses a shared bot
API library file that lives in a fixed subdirectory relative to the bot root:

| Platform   | Subdirectory | Artifact                                          |
|------------|--------------|---------------------------------------------------|
| Java       | `lib/`       | `robocode-tankroyale-bot-api-{version}.jar`       |
| C#         | `lib/`       | `Robocode.TankRoyale.BotApi.{version}.nupkg`      |
| Python     | `deps/`      | `robocode_tank_royale-{version}-py3-none-any.whl` |
| TypeScript | `deps/`      | `robocode-tank-royale-bot-api-{version}.tgz`      |

The sample bot archives are published as independent GitHub Release assets and
are downloaded separately from the GUI. When the user updates the GUI to a new
version, the bot API library files inside the local bot directories are **not
updated automatically**. This creates a version mismatch between the GUI (and
its embedded server) and the running bots.

A concrete symptom was reported in
[issue #207](https://github.com/robocode-dev/tank-royale/issues/207): Java bots
running under Java 26 logged reflection warnings because a stale
`robocode-tankroyale-bot-api-0.40.0.jar` was in the `lib/` directory while the
GUI was at a newer version. The stale JAR still used GSON reflection to mutate
`final` fields, a pattern that Java 26 warns about and future JVMs will block.

The GUI already bundles the server, booter, and recorder JARs as classpath
resources (copied during the Gradle build and embedded in the fat JAR). A
community member independently built a standalone Java tool to detect and
replace outdated bot API JARs in Java bot directories, confirming the demand
for this capability.

Three related problems share a common root cause — the GUI has no built-in
knowledge of the correct bot API library for each platform:

1. **Version mismatch** — existing library files are outdated after a GUI upgrade.
2. **Missing or corrupt library** — a bot directory's `lib/` or `deps/` folder
   exists but the expected library file is absent or damaged.
3. **Future: new bot project setup** — when a bot developer creates a brand-new
   bot project, they need the correct library placed for them. This third use
   case is explicitly **out of scope here** but informs the design; see the
   "Future work" section below.

**Problem (this ADR):** How should the GUI detect and remediate bot API library
files that are outdated or missing across all four supported platforms, without
requiring an internet connection?

---

## Decision

Extend the GUI to detect and update outdated bot API library files for all four
platforms (Java, C#, Python, TypeScript) by:

1. **Bundling the bot API artifacts** inside the GUI fat JAR as classpath
   resources — following the established pattern for the booter, server, and
   recorder JARs. The Gradle build will copy and rename each artifact to a
   fixed, version-free resource name.

2. **Scanning on startup**: after the main window is shown, the GUI scans all
   configured bot root directories (both enabled and disabled) for bot API
   library files. A file is flagged if its version does not match
   `Version.version` **or if it is absent** from an otherwise-valid bot
   directory (`lib/` or `deps/` subdirectory exists but the expected artifact
   is missing).

3. **User-controlled update/restore**: a dialog lists the affected directories
   (directory, platform, installed version or "missing", GUI version). The user
   can update/restore all at once, skip for this session, or permanently disable
   the check via a `checkBotApiUpdates` setting in `gui.properties`.

4. **Reusable library-placement service**: the internal `BotApiLibraryService`
   that extracts a bundled artifact and writes it to a target directory is
   designed as a standalone, reusable service. Future GUI features (e.g. "New
   Bot Creator") can call this service directly without duplicating the
   extraction logic.

---

## Rationale

### Bundled vs. downloaded from the internet

| Approach | Pros | Cons |
|----------|------|------|
| **Bundle in GUI JAR** | No network dependency; version is always in sync with GUI; follows existing pattern for booter/server/recorder | Slightly larger GUI JAR (~650 KB added) |
| **Download from Maven Central / NuGet / PyPI / npm** | Always fetches latest; no JAR bloat | Requires internet; version mismatch risk if user has firewall or offline; more complex error handling |
| **Download from GitHub Releases** | Single source of truth | Same network drawbacks as above; GitHub API rate limits apply |

The bundled approach is the clear winner for this project's context: it is a
spare-time, single-maintainer open-source project where simplicity and offline
reliability outweigh the marginal JAR size increase.

### Startup check vs. on-demand menu item

A startup check with a user prompt is preferred over a hidden menu item because:
- Most users will not discover a menu item.
- The check is fast (file-system scan only; no network).
- The prompt is only shown when outdated files are actually found, so it is not
  intrusive for users whose bot dirs are already up to date.

### Scanning enabled AND disabled bot directories

Disabled directories are included in the scan because:
- A bot directory may be temporarily disabled (e.g. while configuring a new
  game type) but still contains bots the user intends to run.
- Updating only enabled directories would leave a confusing partial state.

### TypeScript `package.json` patch

The TypeScript sample bot archive places a `package.json` at the bot root
directory that hardcodes the tarball filename including its version number:

```json
"@robocode.dev/tank-royale-bot-api": "file:./deps/robocode-tank-royale-bot-api-X.Y.Z.tgz"
```

When replacing the tarball, the updater must also patch this reference in
`package.json` to point to the new filename. This is the only file outside the
`deps/` subdirectory that requires modification.

### Reusable library-placement service

The library file extraction and placement logic is encapsulated in a single
`BotApiLibraryService` class. The startup scanner and update dialog call into
this service. This is intentional: a future "New Bot Creator" feature will need
to place the correct library file when scaffolding a new bot project, and it
can reuse the same service without duplicating the resource-extraction and
file-copy code.

### Repair of missing library files

If a bot developer deletes the library file (intentionally or accidentally), or
if a directory is set up manually without the library, the scanner will detect
the absence and include it in the prompt alongside outdated files. The
restoration path is identical to the update path — extract from the bundled
resource and write to the expected location.

### Version-mismatch direction

The check flags files whose version differs from `Version.version` in either
direction (older **or** newer). A bot developer may have manually installed a
pre-release API, and downgrading it to match the GUI's stable version is the
correct behaviour for compatibility. The dialog makes the direction explicit.

---

## Alternatives Considered

### Do nothing / document only

Leave the problem to the bot developer, with documentation advising them to
check the `lib/` or `deps/` folder after each GUI upgrade. Rejected: the user
experience is poor, the failure mode is subtle (incorrect behaviour or cryptic
warnings), and the fix is straightforward to automate.

### Embed only the Java JAR (most common case)

The Java bot API JAR is the most commonly affected file, but C#, Python, and
TypeScript bot developers face the same problem. Limiting the scope to Java
would be an arbitrary inconsistency.

### Automatic silent update without prompt

Silently replacing files on startup without user consent would be surprising and
could break bot developers who intentionally pinned an older API version. A
prompted update is the minimum respectful interaction.

---

## Consequences

**Positive:**
- Bot developers no longer need to manually hunt for and replace stale bot API
  library files after a GUI upgrade.
- Missing library files are automatically detected and can be restored with one
  click.
- The Java 26 reflection warning (issue #207) is resolved for users who accept
  the update.
- The feature works offline — no network dependency.
- Consistent with the existing resource-bundling pattern in the GUI build.
- `BotApiLibraryService` is reusable by future GUI features (e.g. "New Bot
  Creator") without any duplication.

**Negative:**
- The GUI fat JAR grows by approximately 650 KB (sum of all four bot API
  artifacts after R8 pass). This is acceptable for a desktop application.
- The Gradle build for the GUI now depends on four additional build tasks
  (`:bot-api:java:jar`, `:bot-api:dotnet:build`, `:bot-api:python:build-dist`,
  `:bot-api:typescript:npmPack`), increasing build time for GUI-only changes.
- TypeScript `package.json` patching is the most fragile part; a hand-edited
  `package.json` with unexpected formatting could be improperly updated. This
  risk is mitigated by using a simple regex replacement on the known dependency
  key rather than full JSON parse-and-rewrite.
- On **macOS**, newly written files may receive a `com.apple.quarantine`
  extended attribute set by the OS. Gatekeeper will then prompt the user before
  the file can be executed. After writing each artifact, `BotApiLibraryService`
  must clear the quarantine attribute via `xattr -d com.apple.quarantine
  <file>`. This is the same approach used by other tools that write executable
  artifacts on macOS and adds negligible overhead.

---

## Future Work

**New Bot Creator (separate ADR)**

The same `BotApiLibraryService` will be reused when a "New Bot Creator" feature
is added to the GUI. That feature will (in a future ADR):

- Let the user choose a bot name and a target language
- Create a named directory under a chosen bot root
- Write template files: `.cmd` / `.sh` startup scripts, a source file from a
  language-appropriate template, a `.json` metadata file, and a `README.md`
  with getting-started instructions
- Call `BotApiLibraryService` to place the correct `lib/` or `deps/` library
  without any code duplication

That feature is deliberately **not** included here to keep this change
focused and reviewable.

---

## References

- [Issue #207 — Java 26 final field reflection warning](https://github.com/robocode-dev/tank-royale/issues/207)
- [gui/build.gradle.kts](/gui/build.gradle.kts) — existing resource-copy pattern
- [lib/common/Version.kt](/lib/common/src/main/kotlin/dev/robocode/tankroyale/common/util/Version.kt)
- [gui/settings/ConfigSettings.kt](/gui/src/main/kotlin/dev/robocode/tankroyale/gui/settings/ConfigSettings.kt)
- [sample-bots/java/build.gradle.kts](/sample-bots/java/build.gradle.kts) — `lib/` structure
- [sample-bots/typescript/build.gradle.kts](/sample-bots/typescript/build.gradle.kts) — `deps/` + `package.json` structure
