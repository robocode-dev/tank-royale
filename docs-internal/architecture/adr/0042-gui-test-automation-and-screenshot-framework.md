# ADR-0042: GUI Test Automation and Screenshot Framework

**Status:** Proposed  
**Date:** 2026-04-25

---

## Context

The GUI module (`gui/`) has zero UI automation tests. All 35 documentation screenshots in `docs-build/docs/images/gui/` are produced manually — a developer must run the application, navigate to each dialog or state, and take a screenshot by hand. This creates two problems:

1. **Screenshots go stale.** UI changes (new fields, resized dialogs, reworded labels) are not reflected in documentation until a developer notices and re-shoots manually. There is no automated signal that a screenshot is out of date.

2. **UI regressions go undetected.** There is no way to verify that a refactoring left every dialog looking and behaving correctly. The existing 10 test files cover component logic (ANSI parsing, color resolution, console text) but nothing that opens a dialog, interacts with it, or checks its rendered appearance.

The current test stack is: Kotest 6.1.11 (StringSpec / FunSpec) running on the JUnit 6 platform, with AssertJ 3.27.7 already available for assertion. The GUI framework is Java Swing with MigLayout. Target JVM: Java 21.

**Goals for this decision:**
- Choose a library that can open Swing dialogs programmatically and interact with their components (click buttons, read labels, select list items)
- Use the same library to capture screenshots and save them to `docs-build/docs/images/gui/`
- Keep screenshot regeneration as a developer workflow (not a CI gate), but make GUI component-level tests part of normal CI
- Avoid adding a new test language or external tool to the project

**References:**
- [ADR-0021: Java Swing as GUI Reference Implementation](./0021-java-swing-gui-reference-implementation.md)
- [ADR-0022: Event System for GUI Decoupling](./0022-event-system-gui-decoupling.md)
- [ADR-0037: Functional Core Extraction for Bot API Testability](./0037-functional-core-bot-api-testability.md)
- [ADR-0039: Server Testability](./0039-server-testability.md)
- [ADR-0043: Java Sample Bots as Ephemeral GUI Test Fixtures](./0043-sample-bots-as-gui-test-fixtures.md)

---

## Decision

### 1. AssertJ Swing as the UI driver

Use **AssertJ Swing** (`org.assertj:assertj-swing:3.17.1` — core only) as the Swing automation library. The JUnit integration artifacts (`assertj-swing-junit`, `assertj-swing-junit-jupiter`) are **not** used: they target JUnit 5's extension model and are from an abandoned project with no JUnit 6 support. EDT lifecycle is managed by Kotest hooks instead (see §7).

Add to `gradle/test-libs.versions.toml`:

```toml
[versions]
assertj-swing = "3.17.1"

[libraries]
assertj-swing = { module = "org.assertj:assertj-swing", version.ref = "assertj-swing" }
```

Add to `gui/build.gradle.kts` test dependencies:

```kotlin
testImplementation(testLibs.assertj.swing)
```

AssertJ Swing core provides:
- Component-based lookup by type, name, and text (not screen coordinates)
- `FrameFixture` / `DialogFixture` wrappers for JFrame and JDialog interactions
- Screenshot capture via `ScreenshotTaker`

### 2. New test package for GUI automation

All GUI automation tests live in:

```
gui/src/test/kotlin/dev/robocode/tankroyale/gui/guitest/
```

Sub-packages mirror the main source tree:

```
guitest/
  screenshot/     — screenshot-generating tests (tagged `screenshot`)
  dialog/         — per-dialog functional tests (tagged `gui`)
  fixtures/       — shared setup helpers (GuiTestFixtures, ScreenshotRunner)
```

### 3. Two screenshot modes

Screenshots split into two groups based on whether a live battle is needed:

#### Mode A — Headless component painting (~25 of 35 images)

For dialogs and panels that can be instantiated and displayed without a running game (about-box, all config dialogs, new-battle setup panels, results frame with synthetic data, server-log frame, console frame):

```kotlin
fun captureComponent(component: JComponent, outputFile: File) {
    val img = BufferedImage(component.width, component.height, BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()
    component.paint(g)
    g.dispose()
    ImageIO.write(img, "PNG", outputFile)
}
```

`Component.paint()` renders to an off-screen buffer without requiring a visible display. This works in headless JVM environments.

#### Mode B — Live screen capture (~10 of 35 images)

For battle-state screenshots (battle-view, side-panel, bot-console, recording indicator, replay mode): the test boots sample bots (per ADR-0043), starts a battle via the GUI's event system, waits for the first turn to render, then uses `AssertJ Swing`'s `ScreenshotTaker` to capture the region of the main window.

```kotlin
val screenshotTaker = ScreenshotTaker()
screenshotTaker.saveComponentAsPng(MainFrame, outputFile)
```

Mode B tests require a display (real or virtual). They are excluded from headless CI runs via tag filtering.

### 4. Kotest tag strategy

| Tag | Meaning | CI |
|-----|---------|-----|
| `gui` | Tests that open a dialog and assert behavior | Included (requires display) |
| `screenshot` | Tests that also save a PNG to `docs-build/docs/images/gui/` | Excluded (developer workflow) |
| `screenshot-live` | Screenshot tests that also need a running battle (subset of `screenshot`) | Excluded |

Run screenshot regeneration:

```bash
./gradlew :gui:test -Dkotest.tags="screenshot"
```

Run GUI functional tests only (CI):

```bash
./gradlew :gui:test -Dkotest.tags="gui & !screenshot"
```

Run all tests excluding GUI (existing CI behavior, unchanged):

```bash
./gradlew :gui:test -Dkotest.tags="!gui"
```

Tags are declared as Kotest `Tag` objects in a shared `GuiTags.kt`:

```kotlin
object GuiTags {
    object Gui : Tag()
    object Screenshot : Tag()
    object ScreenshotLive : Tag()
}
```

### 5. Screenshot output path

All screenshots are saved directly to `docs-build/docs/images/gui/` using the same filenames as the existing PNGs. A test run with the `screenshot` tag is a full in-place replacement of the image set.

### 6. CI exclusion of GUI tests (initial phase)

During the initial implementation phase, all `gui`-tagged tests are excluded from CI to avoid requiring a display server on CI agents. When CI is updated to provide a virtual display (xvfb on Linux or the existing display on Windows), the exclusion can be lifted for `gui` tests. The `screenshot` tag remains a developer-only workflow regardless.

### 7. EDT safety

All GUI interactions are performed via `SwingUtilities.invokeAndWait {}` inside Kotest `beforeSpec` / `beforeTest` hooks to ensure they run on the Event Dispatch Thread. The AssertJ Swing JUnit integration (`@GUITest`, `GuiActionRunner`) is not used. Tests that construct dialogs must always do so on the EDT.

---

## Rationale

**Why AssertJ Swing and not `java.awt.Robot`:**  
`Robot` operates at pixel coordinates — it clicks at `(x, y)` on the screen. Any window resize or DPI change breaks the test. AssertJ Swing locates components by type and name, which survives layout changes. It also handles EDT safety automatically.

**Why AssertJ Swing and not Marathon:**  
Marathon is a standalone record-and-playback tool that requires a separate GUI IDE and uses Ruby/JRuby scripting. It cannot be embedded in a Gradle test run. The project uses Kotlin throughout; introducing a Ruby scripting layer for what is fundamentally a test-time concern adds unnecessary complexity.

**Why AssertJ Swing and not TestFX:**  
TestFX is exclusively for JavaFX. The GUI uses Swing (ADR-0021). TestFX cannot interact with Swing components.

**Why AssertJ Swing and not Automaton:**  
Automaton requires Groovy as a runtime dependency and is community-maintained with minimal recent activity. AssertJ Swing integrates with the existing AssertJ assertion style already in use.

**Why AssertJ Swing core only (no JUnit integration artifacts):**  
`assertj-swing-junit-jupiter` targets JUnit 5's extension model (`@GUITest`, `GuiActionRunner`). AssertJ Swing has not released since September 2020 and has no JUnit 6 support. Depending on the JUnit integration artifact would bind the GUI test framework to an unmaintained JUnit-version-specific extension. Using only the core library with Kotest lifecycle hooks avoids this coupling entirely.

**Why two screenshot modes instead of always using `ScreenshotTaker`:**  
`ScreenshotTaker` uses `java.awt.Robot.createScreenCapture()`, which requires a real display and captures what is actually on screen (including other windows that may overlap). `Component.paint()` renders the component in isolation, reproducibly, with no display requirement. For static dialogs this is strictly better.

**Why keep screenshots as a developer workflow (not CI):**  
Screenshot regeneration requires a visible, pixel-accurate display. CI environments vary (HiDPI vs. standard, different font rendering, virtual vs. real display). Pixel-level differences between environments would cause false positives. Screenshots are documentation artifacts produced by developers on a controlled machine, not pass/fail CI checks.

---

## Alternatives Considered

| Option | Verdict | Reason rejected |
|--------|---------|----------------|
| `java.awt.Robot` + custom framework | Rejected | Coordinate-based, brittle, no component abstraction |
| Marathon (Jalian Systems) | Rejected | External tool, Ruby scripting, not embeddable in Gradle |
| TestFX | Rejected | JavaFX only, incompatible with Swing |
| Automaton | Rejected | Requires Groovy, low activity |
| Custom `BufferedImage` only (no AssertJ Swing) | Rejected | Cannot interact with components (click, type, select); only useful for rendering, not testing |

---

## Consequences

### Positive

- Screenshot regeneration becomes a single Gradle command (`./gradlew :gui:test -Dkotest.tags="screenshot"`)
- Dialog functional tests (correct labels, visible fields, button states) can be automated and added to CI incrementally
- Same test code serves as both a regression guard and a documentation artifact producer
- No new scripting language or tool chain required
- Component-based lookups survive layout changes; tests are not brittle by default

### Negative

- **AssertJ Swing is abandoned** (last release September 2020, no JUnit 6 support). Only the core artifact is used, which reduces coupling, but any future incompatibility with newer JVMs must be worked around or the library replaced entirely.
- The JUnit integration features (`@GUITest`, automatic EDT cleanup on test failure) are not available; EDT lifecycle must be managed manually via Kotest hooks — requiring discipline from test authors.
- Battle-state screenshots (Mode B) still require a display — not fully automated in headless CI.
- Initial setup requires care around EDT threading; mistakes produce intermittent failures.

---

## References

- [ADR-0021: Java Swing as GUI Reference Implementation](./0021-java-swing-gui-reference-implementation.md)
- [ADR-0022: Event System for GUI Decoupling](./0022-event-system-gui-decoupling.md)
- [ADR-0037: Functional Core Extraction for Bot API Testability](./0037-functional-core-bot-api-testability.md)
- [ADR-0039: Server Testability](./0039-server-testability.md)
- [ADR-0043: Java Sample Bots as Ephemeral GUI Test Fixtures](./0043-sample-bots-as-gui-test-fixtures.md)
- [AssertJ Swing GitHub](https://github.com/assertj/assertj-swing)
