package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.Base64
import java.util.concurrent.Executor
import java.util.stream.Collectors

/** The platform checks and the Python setup of the Download bots screen, with fake tools and real folders. Nothing here starts a real program or contacts GitHub. */
class RumbleDownloadPlatformTest {

    private val versions = SupportedVersions(java = 25, dotnet = 10, python = listOf(3, 14), node = 24)

    /** Answers commands from a table; a command not in it is a tool that is not found. Remembers what was asked. */
    private class Tools(val answers: MutableMap<String, ToolOutput?> = mutableMapOf(), val onRun: (List<String>) -> Unit = {}) : ToolRunner {
        val asked = mutableListOf<List<String>>()
        override fun run(command: List<String>, timeout: Duration): ToolOutput? {
            asked += command
            onRun(command)
            return answers[command.joinToString(" ")]
        }
    }

    private fun ok(text: String) = ToolOutput(0, text)

    private fun allReady(folder: Path, windows: Boolean = true) = Tools(
        mutableMapOf(
            "java -version" to ok("openjdk version \"25.0.1\" 2025-10-21 LTS"),
            "dotnet --list-sdks" to ok("8.0.427 [C:\\Program Files\\dotnet\\sdk]\n10.0.100 [C:\\Program Files\\dotnet\\sdk]"),
            "node --version" to ok("v24.0.0"),
            (if (windows) "npm.cmd" else "npm") + " --version" to ok("11.21.0"),
            PythonEnvironment.python(folder, windows).toString() + " -c import robocode_tank_royale, sys; print('Python %d.%d.%d' % sys.version_info[:3])" to ok("Python 3.14.2"),
        ),
    )

    /** An environment as a finished setup leaves it, or, with [ready] false, as one stopped half-way. */
    private fun environment(folder: Path, windows: Boolean = true, ready: Boolean = true): Path {
        val python = PythonEnvironment.python(folder, windows)
        Files.createDirectories(python.parent)
        if (ready) Files.write(PythonEnvironment.home(folder).resolve(dev.robocode.tankroyale.common.RumblePythonEnvironment.READY), ByteArray(0))
        return Files.write(python, ByteArray(0))
    }

    private fun tree(root: Path): Set<String> =
        if (!Files.exists(root)) emptySet() else Files.walk(root).use { it.collect(Collectors.toList()) }.map { root.relativize(it).toString().replace('\\', '/') }.toSet()

    // RCL-083

    @Test
    @Tag("RCL-083")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL083_UnitPositive_eachPlatformPassesWithAtLeastTheVersionTheRumblesListNames(@TempDir dir: Path) {
        environment(dir)

        val checks = PlatformChecker(allReady(dir), windows = true).check(BotPlatform.entries, versions, dir)

        assertEquals(BotPlatform.entries.toSet(), checks.filterValues { it.ready }.keys)
        assertEquals("25" to "25.0", checks.getValue(BotPlatform.JVM).let { it.needed to it.found })
        assertEquals("10", checks.getValue(BotPlatform.CSHARP).found, "the newest SDK counts")
        assertEquals("3.14" to "3.14", checks.getValue(BotPlatform.PYTHON).let { it.needed to it.found })
        assertTrue(PlatformChecker.atLeast(listOf(26), "25") && PlatformChecker.atLeast(listOf(3, 15), "3.14") && !PlatformChecker.atLeast(listOf(3, 13, 9), "3.14"))
    }

    @Test
    @Tag("RCL-083")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL083_UnitPositive_theVersionsAreReadFromTheRumbleClientsLatestRelease() {
        val file = "java=25\ndotnet=10\npython=3.14\nnode=24\nnodeInstaller=24.21.0\n"
        val api = object : GitHubApi {
            val paths = mutableListOf<String>()
            override fun get(path: String, token: String?): ApiResponse {
                paths += path
                assertEquals(null, token, "the list is public, so no sign-in is sent")
                return when {
                    path.endsWith("/releases/latest") -> ApiResponse(200, """{"tag_name":"v0.3.0"}""")
                    path.contains("/contents/") -> ApiResponse(200, """{"content":"${Base64.getMimeEncoder().encodeToString(file.toByteArray())}"}""")
                    else -> ApiResponse(404, "{}")
                }
            }
            override fun postJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
            override fun patchJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
        }

        assertEquals(versions, SupportedVersions.parse(GitHubRuntimeListSource(api).text()))
        assertEquals(
            listOf("/repos/robocode-dev/rumble-client/releases/latest", "/repos/robocode-dev/rumble-client/contents/src/main/resources/runtime-versions.properties?ref=v0.3.0"),
            api.paths,
        )
    }

    @Test
    @Tag("RCL-083")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL083_UnitNegative_aMissingOlderUnreadableOrSilentToolFailsOnlyItsOwnPlatform(@TempDir dir: Path) {
        val tools = allReady(dir).apply {
            answers["java -version"] = ok("openjdk version \"17.0.12\" 2024-07-16")
            answers["dotnet --list-sdks"] = null
            answers["node --version"] = ok("not a version")
            answers["python --version"] = ok("Python 3.13.15")
        }

        val checks = PlatformChecker(tools, windows = true).check(BotPlatform.entries, versions, dir)

        assertEquals(PlatformReason.TOO_OLD to "17.0", checks.getValue(BotPlatform.JVM).let { it.reason to it.found })
        assertEquals(PlatformReason.MISSING, checks.getValue(BotPlatform.CSHARP).reason, "not found or silent")
        assertEquals(PlatformReason.UNREADABLE, checks.getValue(BotPlatform.TYPESCRIPT).reason)
        assertEquals(PlatformReason.TOO_OLD, checks.getValue(BotPlatform.PYTHON).reason, "no environment, and the Python on the path is older")

        tools.answers["node --version"] = ok("v24.1.0")
        tools.answers.remove("npm.cmd --version")
        assertEquals(PlatformReason.NPM_MISSING, PlatformChecker(tools, windows = true).check(listOf(BotPlatform.TYPESCRIPT), versions, dir).getValue(BotPlatform.TYPESCRIPT).reason)
        tools.answers["dotnet --list-sdks"] = ToolOutput(0, "")
        assertEquals(PlatformReason.MISSING, PlatformChecker(tools, windows = true).check(listOf(BotPlatform.CSHARP), versions, dir).getValue(BotPlatform.CSHARP).reason, "dotnet without an SDK")
    }

    @Test
    @Tag("RCL-083")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL083_UnitNegative_pythonPassesOnlyWithTheFoldersOwnWorkingEnvironment(@TempDir dir: Path) {
        val tools = Tools(
            mutableMapOf(
                "python3 --version" to ok("Python 3.14.0"),
                "python3 -c import venv, ensurepip" to ok(""),
            ),
        )
        val checker = PlatformChecker(tools, windows = false)

        assertEquals(PlatformReason.NO_ENVIRONMENT, checker.check(listOf(BotPlatform.PYTHON), versions, dir).getValue(BotPlatform.PYTHON).reason, "no environment yet")

        environment(dir, windows = false)
        assertEquals(PlatformReason.NO_ENVIRONMENT, checker.check(listOf(BotPlatform.PYTHON), versions, dir).getValue(BotPlatform.PYTHON).reason,
            "an environment that no longer starts, after an upgrade of Python or a move of the folder, counts as missing")

        val own = PythonEnvironment.python(dir, false).toString() + " -c import robocode_tank_royale, sys; print('Python %d.%d.%d' % sys.version_info[:3])"
        tools.answers[own] = ok("Python 3.14.0")
        assertEquals(PlatformReason.READY, checker.check(listOf(BotPlatform.PYTHON), versions, dir).getValue(BotPlatform.PYTHON).reason)
        Files.delete(PythonEnvironment.home(dir).resolve(dev.robocode.tankroyale.common.RumblePythonEnvironment.READY))
        assertEquals(PlatformReason.NO_ENVIRONMENT, checker.check(listOf(BotPlatform.PYTHON), versions, dir).getValue(BotPlatform.PYTHON).reason,
            "an environment whose setup was stopped half-way never counts, even when its Python answers")

        tools.answers.remove("python3 -c import venv, ensurepip")
        assertEquals(PlatformReason.NO_VENV, checker.check(listOf(BotPlatform.PYTHON), versions, dir).getValue(BotPlatform.PYTHON).reason, "as on Debian without python3-venv")

        tools.answers.remove("python3 --version")
        assertEquals(PlatformReason.MISSING, checker.check(listOf(BotPlatform.PYTHON), versions, dir).getValue(BotPlatform.PYTHON).reason)
    }

    @Test
    @Tag("RCL-083")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL083_UnitNegative_checkingInstallsNothingAndAnUnreadableListOnlyAsksWhetherEachToolIsThere(@TempDir dir: Path) {
        val tools = allReady(dir).apply { answers["java -version"] = ok("openjdk version \"11.0.2\"") }
        val before = tree(dir)

        val checks = PlatformChecker(tools, windows = true).check(BotPlatform.entries, null, dir)

        assertTrue(checks.getValue(BotPlatform.JVM).ready, "without the list, Java 11 is only known to be there")
        assertEquals(null, checks.getValue(BotPlatform.JVM).needed)
        assertEquals(before, tree(dir), "nothing is created or changed")
        assertTrue(tools.asked.none { it.contains("pip") || it.contains("venv") && it.contains("-m") }, "nothing is installed")
        assertThrows(IOException::class.java) { SupportedVersions.parse("java=25\ndotnet=10\nnode=24\n") }
        assertThrows(IOException::class.java) { SupportedVersions.parse("java=x\ndotnet=10\npython=3.14\nnode=24\n") }
        assertThrows(IOException::class.java) { SupportedVersions.parse("java=\\uZZZZ\n") }
        val garbled = object : GitHubApi {
            override fun get(path: String, token: String?) =
                if (path.endsWith("/releases/latest")) ApiResponse(200, """{"tag_name":"v1"}""") else ApiResponse(200, """{"content":"%%% not base64 %%%"}""")
            override fun postJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
            override fun patchJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
        }
        assertThrows(IOException::class.java) { GitHubRuntimeListSource(garbled).text() }
    }

    // RCL-084

    private class Port(var read: DownloadRead, val setupResult: PythonSetupResult = PythonSetupResult.Done) : DownloadPort {
        var reads = 0
        var setups = 0
        override fun read(folder: Path): DownloadRead = read.also { reads++ }
        override fun download(catalog: Catalog, bots: List<CatalogBot>, replaceChanged: Set<String>, folder: Path) = DownloadEnded.Done(emptyList())
        override fun pythonSetupCommands(folder: Path) = listOf(listOf("python", "-m", "venv", "x"))
        override fun setUpPython(folder: Path): PythonSetupResult = setupResult.also { setups++ }
    }

    private val direct = Executor { it.run() }

    private fun bot(name: String, platform: String) =
        CatalogBot(name, "1.0.0", platform, "bots/x/$name", "sha256:0", "someone", listOf("A"), "2026-10-01", CatalogBot.ACTIVE)

    private fun listed(dir: Path, checks: Map<BotPlatform, PlatformCheck>, versionsKnown: Boolean = true): DownloadRead.Listed {
        val catalog = Catalog(1, "now", "a".repeat(40), listOf(bot("Walls", "JVM"), bot("Orbit", "Python"), bot("Odd", "Rust")))
        return DownloadRead.Listed(catalog, BotDownloads.rows(catalog, dir), checks, versionsKnown)
    }

    private fun check(platform: BotPlatform, reason: PlatformReason) = PlatformCheck(platform, reason, "1", null)

    @Test
    @Tag("RCL-084")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL084_UnitPositive_onlyPlatformsThatCanRunAreTickedAndThePersonMayTickTheOthers(@TempDir dir: Path) {
        val port = Port(listed(dir, mapOf(BotPlatform.JVM to check(BotPlatform.JVM, PlatformReason.READY), BotPlatform.PYTHON to check(BotPlatform.PYTHON, PlatformReason.NO_ENVIRONMENT))))
        val controller = DownloadController(port, dir, direct, direct) {}
        controller.start()

        val choosing = controller.state as DownloadState.Choosing
        assertEquals(setOf("JVM", "Rust"), choosing.platforms, "a platform the rules do not know is not hidden")
        assertEquals(listOf("Odd", "Walls"), choosing.toDownload.map { it.name }.sorted())
        assertEquals(PlatformReason.NO_ENVIRONMENT, choosing.checkOf("Python")?.reason)

        controller.setPlatform("Python", true)
        assertEquals(listOf("Odd", "Orbit", "Walls"), (controller.state as DownloadState.Choosing).toDownload.map { it.name }.sorted(), "Show anyway ticks it")
        assertEquals(listOf(listOf("python", "-m", "venv", "x")), controller.pythonSetupCommands())
    }

    @Test
    @Tag("RCL-084")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL084_UnitNegative_onlyTheFixedDownloadPagesCanBeOpened() {
        BotPlatform.entries.forEach { assertTrue(PlatformDownloads.isFixedPage(PlatformDownloads.page(it)), it.name) }
        assertTrue(BotPlatform.entries.all { PlatformDownloads.page(it).startsWith("https://") })
        assertFalse(PlatformDownloads.isFixedPage("https://example.com/"))
        assertFalse(PlatformDownloads.isFixedPage(PlatformDownloads.page(BotPlatform.JVM) + "x"))
        assertEquals(BotPlatform.PYTHON, BotPlatform.of("Python"))
        assertEquals(null, BotPlatform.of("Rust"))
    }

    // RCL-085

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL085_UnitPositive_setupMakesTheFoldersOwnEnvironmentAndInstallsTheProgramsBotApiWithItsOwnPip(@TempDir dir: Path) {
        val folder = dir.resolve("downloads")
        val botApi = dir.resolve("robocode_tank_royale-1.4.0-py3-none-any.whl")
        val tools = Tools(onRun = { command -> if ("venv" in command) environment(folder, windows = false, ready = false) })
        val setup = PythonSetup(tools, windows = false)
        setup.commands(folder, botApi).forEach { tools.answers[it.joinToString(" ")] = ok("done") }

        val result = setup.run(folder, botApi)

        assertEquals(PythonSetupResult.Done, result)
        assertTrue(dev.robocode.tankroyale.common.RumblePythonEnvironment.isReady(folder, false), "marked as finished, as its very last step")
        assertEquals(setup.commands(folder, botApi), tools.asked, "exactly the commands that were shown, in order")
        assertEquals(listOf("python3", "-m", "venv", folder.resolve(".rumble-python").toString()), tools.asked[0])
        assertEquals(listOf(PythonEnvironment.python(folder, false).toString(), "-m", "pip", "install", botApi.toString()), tools.asked[1])
        assertEquals(setOf("", ".rumble-python"), tree(folder).map { it.substringBefore('/') }.toSet(), "nothing outside the environment's folder")
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL085_UnitNegative_aFailedStepKeepsItsDetailsAndLeavesNoHalfMadeEnvironment(@TempDir dir: Path) {
        val botApi = dir.resolve("api.whl")
        listOf(PythonSetupStep.MAKE_ENVIRONMENT, PythonSetupStep.INSTALL_BOT_API).forEach { failing ->
            val folder = dir.resolve(failing.name)
            // An old environment that no longer starts is there, with a file of its own.
            environment(folder, windows = true)
            Files.writeString(PythonEnvironment.home(folder).resolve("old.txt"), "old")
            Files.writeString(folder.resolve("Walls.txt"), "a bot's file")
            val tools = Tools(onRun = { command -> if ("venv" in command) environment(folder, windows = true, ready = false) })
            val setup = PythonSetup(tools, windows = true)
            val (make, install) = setup.commands(folder, botApi)
            tools.answers[make.joinToString(" ")] = if (failing == PythonSetupStep.MAKE_ENVIRONMENT) ToolOutput(1, "line\nError: no ensurepip") else ok("")
            tools.answers[install.joinToString(" ")] = ToolOutput(1, "ERROR: Could not find a version that satisfies the requirement websockets")

            val result = setup.run(folder, botApi)

            val failed = assertInstanceOf(PythonSetupResult.Failed::class.java, result)
            assertEquals(failing, failed.step)
            assertTrue(failed.details.contains(if (failing == PythonSetupStep.MAKE_ENVIRONMENT) "ensurepip" else "websockets"))
            assertFalse(Files.exists(PythonEnvironment.home(folder)), "no half-made environment, and the old one was replaced")
            assertEquals("a bot's file", Files.readString(folder.resolve("Walls.txt")), "nothing else is touched")
        }
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL085_UnitPositive_theListIsReadAgainAfterTheSetupAndShowsItsResultOnce(@TempDir dir: Path) {
        val failed = PythonSetupResult.Failed(PythonSetupStep.INSTALL_BOT_API, "offline")
        val port = Port(listed(dir, mapOf(BotPlatform.PYTHON to check(BotPlatform.PYTHON, PlatformReason.NO_ENVIRONMENT))), failed)
        val seen = mutableListOf<String>()
        val controller = DownloadController(port, dir, direct, direct) { seen += it::class.simpleName!! }
        controller.start()

        controller.setUpPython()
        assertEquals(failed, (controller.state as DownloadState.Choosing).setup)
        assertEquals(listOf("Reading", "Choosing", "SettingUpPython", "Reading", "Choosing"), seen)
        assertEquals(2 to 1, port.reads to port.setups)

        controller.start()
        assertEquals(null, (controller.state as DownloadState.Choosing).setup, "shown once")
        Files.createDirectories(PythonEnvironment.home(dir))
        assertTrue(BotDownloads.rows(Catalog(1, "now", "a".repeat(40), emptyList()), dir).isEmpty(), "the environment is never a bot on the screen")
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL085_UnitNegative_setupIsOfferedOnlyFromTheListAndOnceAtATime(@TempDir dir: Path) {
        val port = Port(listed(dir, emptyMap()))
        val held = ArrayDeque<Runnable>()
        val controller = DownloadController(port, dir, { held.addLast(it) }, direct) {}

        controller.setUpPython()
        assertEquals(0, port.setups, "not before the list is there")
        controller.start()
        while (held.isNotEmpty()) held.removeFirst().run()
        controller.setUpPython()
        controller.setUpPython()
        controller.start()
        while (held.isNotEmpty()) held.removeFirst().run()

        assertEquals(1, port.setups, "a second press or a fresh look while it runs starts nothing new")
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL085_UnitNegative_anOldEnvironmentThatCannotBeRemovedIsToldAndNothingIsRun(@TempDir dir: Path) {
        environment(dir, windows = true)
        val tools = Tools()
        val stuck = PythonSetup(tools, windows = true) { throw java.nio.file.AccessDeniedException("python.exe") }

        val result = stuck.run(dir, dir.resolve("api.whl"))

        assertEquals(PythonSetupResult.Failed(PythonSetupStep.REMOVE_OLD_ENVIRONMENT, "python.exe", leftBehind = true), result)
        assertEquals(emptyList<List<String>>(), tools.asked, "nothing is made over a half-removed environment")

        val tools2 = Tools(onRun = { command -> if ("venv" in command) environment(dir, windows = true, ready = false) })
        var calls = 0
        val failsSecondTime = PythonSetup(tools2, windows = true) { home ->
            if (++calls > 1) throw java.io.UncheckedIOException(IOException("locked"))
            Files.walk(home).use { p -> p.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) } }
        }
        val (make, install) = failsSecondTime.commands(dir, dir.resolve("api.whl"))
        tools2.answers[make.joinToString(" ")] = ok("")
        tools2.answers[install.joinToString(" ")] = ToolOutput(1, "ERROR: no network")

        val second = failsSecondTime.run(dir, dir.resolve("api.whl"))

        assertEquals(PythonSetupResult.Failed(PythonSetupStep.INSTALL_BOT_API, "ERROR: no network", leftBehind = true), second,
            "the failing step and its own output are kept, and what is left behind is told")
        assertFalse(dev.robocode.tankroyale.common.RumblePythonEnvironment.isReady(dir, true), "and what is left never counts")
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL085_UnitPositive_thePersonsChoicesOnTheListAreKeptAcrossTheSetup(@TempDir dir: Path) {
        val port = Port(listed(dir, mapOf(BotPlatform.JVM to check(BotPlatform.JVM, PlatformReason.READY), BotPlatform.PYTHON to check(BotPlatform.PYTHON, PlatformReason.NO_ENVIRONMENT))))
        val controller = DownloadController(port, dir, direct, direct) {}
        controller.start()
        controller.setPlatform("Rust", false)
        controller.setAll(false)
        controller.setChosen("Walls", true)
        port.read = listed(dir, mapOf(BotPlatform.JVM to check(BotPlatform.JVM, PlatformReason.READY), BotPlatform.PYTHON to check(BotPlatform.PYTHON, PlatformReason.READY)))

        controller.setUpPython()

        val after = controller.state as DownloadState.Choosing
        assertEquals(PythonSetupResult.Done, after.setup)
        assertEquals(setOf("JVM", "Python"), after.platforms, "Python is ticked now, and Rust stays unticked as the person left it")
        assertEquals(false to setOf("Walls"), after.all to after.chosen)
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL085_UnitNegative_aSetupResultIsNeverShownOnALaterListAfterAFailedRead(@TempDir dir: Path) {
        val port = Port(listed(dir, emptyMap()))
        val controller = DownloadController(port, dir, direct, direct) {}
        controller.start()
        val good = port.read
        port.read = DownloadRead.Failed(DownloadFailure.CATALOG_UNREADABLE)

        controller.setUpPython()
        assertInstanceOf(DownloadState.Failed::class.java, controller.state)
        port.read = good
        controller.start()

        assertEquals(null, (controller.state as DownloadState.Choosing).setup)
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL085_UnitNegative_theBotApiFileLivesInAPrivateFolderOfItsOwnAndTheListIsReadOnce(@TempDir dir: Path) {
        val reads = java.util.concurrent.atomic.AtomicInteger()
        val list = RuntimeListSource { reads.incrementAndGet(); "java=25\ndotnet=10\npython=3.14\nnode=24\n" }
        val catalog = object : CatalogSource {
            override fun catalogText(commit: String) = "{\"schemaVersion\":1,\"generatedAt\":\"x\",\"commit\":\"$commit\",\"bots\":[]}"
            override fun archive(commit: String): java.io.InputStream = throw IOException("not used")
        }
        val head = object : GitHubApi {
            override fun get(path: String, token: String?) = ApiResponse(200, "{\"sha\":\"" + "a".repeat(40) + "\"}")
            override fun postJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
            override fun patchJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
        }
        fun port() = LibraryDownloadPort(RumbleEnvironments.TEST, head, catalog, list, Tools(), "robocode_tank_royale-1.4.0-py3-none-any.whl")
        val one = port()

        one.read(dir)
        one.read(dir.resolve("other"))
        assertEquals(1, reads.get(), "a fresh look on the same screen asks GitHub for the list only once")

        val first = one.pythonSetupCommands(dir).last().last()
        val second = port().pythonSetupCommands(dir).last().last()
        try {
            assertEquals(first, one.pythonSetupCommands(dir.resolve("other")).last().last(), "the same file for the whole screen")
            assertTrue(first != second && Path.of(first).parent.fileName.toString().startsWith("robocode-rumble-python-"), "a new private folder for each screen")
            assertTrue(Files.isDirectory(Path.of(first).parent))
        } finally {
            // The private folders are made in the computer's temp folder, and a test run does not reach the program's own
            // removal at exit, so the test removes what it made.
            listOf(first, second).forEach { Files.deleteIfExists(Path.of(it).parent) }
        }
    }
}
