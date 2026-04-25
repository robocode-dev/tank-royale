# ADR-0043: Java Sample Bots as Ephemeral GUI Test Fixtures

**Status:** Proposed  
**Date:** 2026-04-25

---

## Context

The GUI test and screenshot framework (ADR-0042) requires bots to test battle-related GUI states: the arena view, the side panel, bot consoles, the results frame, the recording indicator, and replay. These tests must boot real bots, connect them to the server, and run at least one battle round.

Three options exist for supplying these bots:

1. **Dedicated minimal test bots** — Small bots written specifically for GUI testing. Must be maintained alongside the test infrastructure, versioned, and kept compatible with the current server protocol.

2. **Live network bots** — Bots already running elsewhere on the network, joined via remote server address. Introduces external dependency; not reproducible in isolation.

3. **Java sample bots** — The existing 14 Java sample bots (`sample-bots/java/`) built as part of the normal project build. Already maintained, already protocol-compliant, represent realistic and diverse bot behavior.

The GUI tests also need a way to configure a bot root directory that the booter can discover and launch from — without modifying the user's real bot directory configuration.

**References:**
- [ADR-0026: Identity-Based Bot Matching in Battle Runner](./0026-identity-based-bot-matching.md)
- [ADR-0030: Template-Based Booting and Base Convention](./0030-convention-over-configuration-bot-entry-points.md)
- [ADR-0031: Optional Bot Config and Runtime Validation](./0031-optional-bot-config-and-runtime-validation.md)
- [ADR-0042: GUI Test Automation and Screenshot Framework](./0042-gui-test-automation-and-screenshot-framework.md)

---

## Decision

### 1. Use Java sample bots as GUI test fixtures

GUI integration tests and battle-state screenshot tests use the **Java sample bots** as their bot supply. No dedicated test bots are created.

The bots are selected from those built by the `:sample-bots:java` Gradle subproject. For most tests, two or three simple bots (e.g., `Corners`, `Crazy`, `SpinBot`) are sufficient to run a battle.

### 2. Stage bots to a JVM temp directory before tests

A `GuiTestFixtures` Kotlin object (in `gui/src/test/kotlin/.../guitest/fixtures/`) provides:

```kotlin
object GuiTestFixtures {

    /** Stage sample bot directories to a temporary folder and return its path. */
    fun stageBots(vararg botNames: String = defaultBots): Path

    /** Remove the staged directory. */
    fun cleanupBots(stagedDir: Path)

    private val defaultBots = arrayOf("Corners", "Crazy", "SpinBot")
}
```

The staged directory is created under `System.getProperty("java.io.tmpdir")/tank-royale-guitest-bots-<uuid>/`. Using a UUID suffix avoids collisions when multiple test runs overlap (e.g., parallel Gradle workers).

Each bot is staged as a self-contained directory:

```
tank-royale-guitest-bots-<uuid>/
  Corners/
    Corners.json
    Corners.jar        ← copied from sample-bots/java/build/archive/Corners/
    Corners.cmd
    Corners.sh
  Crazy/
    ...
```

### 3. Lifecycle: BeforeAll / AfterAll with shutdown hook fallback

Test suites that need bots use JUnit 5 `@BeforeAll` and `@AfterAll` (or Kotest `beforeSpec` / `afterSpec`):

```kotlin
class BattleScreenshotTest : FunSpec({
    lateinit var botDir: Path

    beforeSpec {
        botDir = GuiTestFixtures.stageBots()
        GuiTestFixtures.injectBotDirectory(botDir)
    }

    afterSpec {
        GuiTestFixtures.cleanupBots(botDir)
    }

    // ... tests
})
```

A JVM shutdown hook is registered when `stageBots()` is called as a fallback to delete the temp directory if the process is killed before `afterSpec` runs. This matches the pattern used in `ProcessManager.kt`:

```kotlin
Runtime.getRuntime().addShutdownHook(Thread {
    stagedDir.toFile().deleteRecursively()
})
```

### 4. Inject bot directory without modifying user settings

`GuiTestFixtures.injectBotDirectory(path)` sets the bot root directories in `BotRootDirectoriesSettings` directly (via the settings model) before each test suite, and restores the original value in `afterSpec`. This avoids writing to the user's settings file on disk.

The booter's bot discovery reads `BotRootDirectoriesSettings.rootDirectories` at boot time, so injecting before the test starts a battle is sufficient.

### 5. Gradle task dependency

A test task in `gui/build.gradle.kts` that runs battle-related GUI tests declares a dependency on the sample bot build:

```kotlin
tasks.named("test") {
    dependsOn(":sample-bots:java:build")
}
```

This ensures the sample bot archives are current before any GUI test runs. The build output path used for staging is resolved from the Gradle project structure:

```kotlin
val sampleBotArchiveDir: Path =
    rootProject.projectDir.resolve("sample-bots/java/build/archive").toPath()
```

This path is injected into `GuiTestFixtures` via a system property set in the test task configuration:

```kotlin
tasks.named<Test>("test") {
    dependsOn(":sample-bots:java:build")
    systemProperty(
        "tankroyale.test.sampleBotsArchiveDir",
        rootProject.layout.projectDirectory
            .dir("sample-bots/java/build/archive").asFile.absolutePath
    )
}
```

---

## Rationale

**Why Java sample bots (not dedicated test bots):**  
Dedicated test bots would need to be written, maintained, and kept protocol-compatible as the server evolves. The sample bots are already maintained for exactly this level of compatibility — they ship with every release and are tested against the current server. Reusing them avoids a parallel maintenance burden.

**Why stage to a temp directory (not use the archive directory directly):**  
The archive directory (`sample-bots/java/build/archive/`) is inside the project tree and may be cleaned by `./gradlew clean`. Using a temp directory outside the project tree provides isolation and prevents test artifacts from interfering with the build. The UUID suffix prevents cross-run collisions.

**Why inject into settings (not use a config dialog):**  
Opening the bot directory config dialog and navigating it programmatically is itself a GUI test. The fixture layer should be reliable infrastructure, not a test subject. Direct settings injection is simpler, deterministic, and does not depend on the dialog layout remaining unchanged.

**Why a shutdown hook (not rely solely on `@AfterAll`):**  
If the test process is interrupted (Ctrl+C, OOM, process kill), `@AfterAll` does not run. Temp directories accumulate. The shutdown hook provides a best-effort cleanup guarantee. The same pattern is already established in `ProcessManager.kt` for bot processes.

---

## Alternatives Considered

| Option | Verdict | Reason rejected |
|--------|---------|----------------|
| Dedicated minimal test bots | Rejected | Maintenance overhead; must track protocol changes independently |
| Live network bots | Rejected | External dependency; not reproducible in isolation; breaks offline development |
| Use archive directory directly | Rejected | Subject to `./gradlew clean`; may cause cross-run interference |
| Configure bot directory via dialog automation | Rejected | Couples fixture reliability to dialog implementation; adds fragility |

---

## Consequences

### Positive

- No new bot code to write or maintain
- Sample bots are already protocol-compliant and regularly exercised
- The UUID-suffixed temp directory is safe for parallel test runs
- Shutdown hook ensures temp directories are cleaned up even on abnormal exit
- Gradle `dependsOn` guarantees archive is fresh before tests run

### Negative

- GUI battle tests require `:sample-bots:java:build` to have run; cold builds are slower
- `sampleBotsArchiveDir` must be passed as a system property — adds one configuration point in `gui/build.gradle.kts`
- If the sample bots are significantly changed (e.g., a bot is removed), the fixture default list must be updated

---

## References

- [ADR-0026: Identity-Based Bot Matching in Battle Runner](./0026-identity-based-bot-matching.md)
- [ADR-0030: Template-Based Booting and Base Convention](./0030-convention-over-configuration-bot-entry-points.md)
- [ADR-0031: Optional Bot Config and Runtime Validation](./0031-optional-bot-config-and-runtime-validation.md)
- [ADR-0042: GUI Test Automation and Screenshot Framework](./0042-gui-test-automation-and-screenshot-framework.md)
- [ProcessManager.kt — shutdown hook pattern](../../../booter/src/main/kotlin/dev/robocode/tankroyale/booter/process/ProcessManager.kt)
