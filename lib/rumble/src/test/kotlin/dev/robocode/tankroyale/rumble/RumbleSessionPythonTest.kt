package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.rumble.protocol.Job
import dev.robocode.tankroyale.rumble.protocol.JobBot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RumbleSessionPythonTest {

    private fun entries(folder: Path): List<Path> = Files.list(folder).use { it.iterator().asSequence().toList() }

    /** A wheel with the metadata the real one has: its version, and build tools beside what it imports when a bot runs. */
    private fun wheel(version: String = "1.4.0", requiresPython: String? = ">=3.10") = PythonBotApiWheel {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry("robocode_tank_royale-$version.dist-info/METADATA"))
            zip.write(
                (
                    "Metadata-Version: 2.4\nName: robocode-tank-royale\nVersion: $version\n" + (requiresPython?.let { "Requires-Python: $it\n" } ?: "") +
                        "Requires-Dist: pip>=24\nRequires-Dist: PyYAML>=6\n" +
                        "Requires-Dist: setuptools>=77\nRequires-Dist: pycountry>=24\nRequires-Dist: websockets>=15\nRequires-Dist: mypy\n\nThe Bot API.\n"
                    ).toByteArray(),
            )
            zip.closeEntry()
        }
        ByteArrayInputStream(bytes.toByteArray())
    }

    /** Plays Python and pip: answers the version check and makes the files pip would install into the target. */
    private class FakePython(var version: String? = "3 14", val executable: String = "C:\\Python314\\python.exe", var pipFails: Boolean = false) : ToolRunner {
        val commands = mutableListOf<List<String>>()
        val wheelsSeen = mutableListOf<String>()

        override fun run(command: List<String>, timeout: Duration): ToolOutput? {
            commands += command
            if (command.getOrNull(1) == "-c") return version?.let { ToolOutput(0, "$it $executable\n") }
            if (pipFails) return ToolOutput(1, "ERROR: Could not find a version that satisfies the requirement websockets>=15")
            val target = Path.of(command[command.indexOf("--target") + 1])
            if ("--no-deps" in command) {
                if (Files.isRegularFile(Path.of(command.last()))) wheelsSeen += Path.of(command.last()).fileName.toString()
                Files.createDirectories(target.resolve("robocode_tank_royale/bot_api"))
                Files.writeString(target.resolve("robocode_tank_royale/bot_api/__init__.py"), "# the Bot API\n")
            } else {
                Files.createDirectories(target.resolve("websockets"))
                Files.writeString(target.resolve("websockets/__init__.py"), "# websockets\n")
            }
            return ToolOutput(0, "Successfully installed")
        }
    }

    private fun python(root: Path, runner: ToolRunner, needed: String? = "3.14", botApi: PythonBotApiWheel? = wheel()) =
        SessionPython(runner, root, botApi, needed, windows = true)

    private fun apiFolder(root: Path): Path =
        entries(root.resolve(SessionPython.SESSIONS_FOLDER)).single { Files.isDirectory(it) }.let { session -> entries(session).single { it.fileName.toString().startsWith("api-") } }

    // RCL-131

    @Test
    @Tag("RCL-131")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL131_UnitPositive_theBotApiIsInstalledFreshForTheSessionWithTheSupportedPythonAndRemovedAtTheEnd(@TempDir root: Path) {
        val runner = FakePython()
        val session = python(root, runner)

        val ready = assertInstanceOf(SessionPythonSetup.Ready::class.java, session.setUp())

        assertEquals("3.14", ready.version)
        assertEquals("C:\\Python314\\python.exe", ready.python)
        assertEquals(listOf("python", "-c"), runner.commands[0].take(2), "the Python the launchers call, found on the path")
        val (first, second) = runner.commands.drop(1)
        assertEquals(listOf("C:\\Python314\\python.exe", "-m", "pip", "install", "--target"), first.take(5))
        assertTrue("--no-deps" in first, "the program's own Bot API, without its build tools")
        assertTrue("--no-cache-dir" in first && "--no-cache-dir" in second, "pip's own cache, which a bot could have changed, is not used")
        assertEquals(listOf("robocode_tank_royale-1.4.0-py3-none-any.whl"), runner.wheelsSeen, "the wheel's file name comes from its own metadata")
        assertEquals(listOf("pycountry>=24", "websockets>=15"), second.takeLast(2), "only what it imports when a bot runs, with the wheel's own bounds")
        val sessionFolders = entries(root.resolve(SessionPython.SESSIONS_FOLDER))
        assertEquals(1, sessionFolders.count { Files.isDirectory(it) }, "one folder of the session's own; the wheel's folder is gone")
        assertEquals(listOf("api-"), entries(sessionFolders.single { Files.isDirectory(it) }).map { it.fileName.toString().take(4) })

        session.close()
        assertEquals(emptyList<Path>(), entries(root.resolve(SessionPython.SESSIONS_FOLDER)), "the session's folder and lock are removed at the end")
    }

    @Test
    @Tag("RCL-131")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL131_UnitNegative_aMissingTooOldOrFailingPythonOrNoBotApiLeavesPythonOutWithAReason(@TempDir root: Path) {
        assertEquals(SessionPythonSetup.Unavailable(SessionPythonProblem.MISSING, "3.14"), python(root.resolve("a"), FakePython(version = null)).setUp())
        assertEquals(SessionPythonSetup.Unavailable(SessionPythonProblem.TOO_OLD, "3.14", "3.12"), python(root.resolve("b"), FakePython(version = "3 12")).setUp())
        assertEquals(SessionPythonSetup.Unavailable(SessionPythonProblem.NO_BOT_API), python(root.resolve("c"), FakePython(), botApi = null).setUp())
        val unreadable = PythonBotApiWheel { ByteArrayInputStream(ByteArray(0)) }
        assertEquals(SessionPythonProblem.NO_BOT_API, (python(root.resolve("c2"), FakePython(), botApi = unreadable).setUp() as SessionPythonSetup.Unavailable).problem)
        val failing = python(root.resolve("d"), FakePython(pipFails = true))
        val failed = assertInstanceOf(SessionPythonSetup.Unavailable::class.java, failing.setUp())
        assertEquals(SessionPythonProblem.INSTALL_FAILED, failed.problem)
        assertTrue(failed.details.contains("websockets>=15"), failed.details)
        failing.close()
        assertEquals(emptyList<Path>(), entries(root.resolve("d").resolve(SessionPython.SESSIONS_FOLDER)), "a failed install leaves nothing behind")
        // Without the Rumble's list of versions the wheel's own lowest Python is required, and without that any Python is used.
        assertEquals(SessionPythonSetup.Unavailable(SessionPythonProblem.TOO_OLD, "3.10", "3.9"), python(root.resolve("e"), FakePython(version = "3 9"), needed = null).setUp())
        assertInstanceOf(SessionPythonSetup.Ready::class.java, python(root.resolve("f"), FakePython(version = "3 9"), needed = null, botApi = wheel(requiresPython = null)).setUp())
        // A wheel that requires nothing at run time gets one pip command, since pip refuses one with nothing to install.
        assertEquals(1, SessionPython.installCommands("python", root, root.resolve("w.whl"), emptyList()).size)
    }

    @Test
    @Tag("RCL-131")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL131_UnitNegative_theDigestIsKeptOnlyInMemoryAndLeftoversOfEndedSessionsAreRemovedButNotLiveOnes(@TempDir root: Path) {
        val live = python(root, FakePython()).apply { setUp() }
        val liveFolder = apiFolder(root)
        val digest = SessionPython.digestOf(liveFolder)
        // A lock file holds nothing, and Windows does not let anyone read a locked one.
        val written = Files.walk(root).use { paths -> paths.iterator().asSequence().filter { Files.isRegularFile(it) && !it.toString().endsWith(".lock") }.toList() }
        assertTrue(written.none { Files.readString(it).contains(digest) }, "no file holds the digest")

        // A session that was ended without closing, as by a third Ctrl+C, left its folder and an unheld lock behind.
        val sessions = root.resolve(SessionPython.SESSIONS_FOLDER)
        val leftover = Files.createDirectories(sessions.resolve("ended/api-1"))
        Files.writeString(leftover.resolve("x.py"), "x")
        Files.createFile(sessions.resolve("ended.lock"))
        // A folder without a lock file may belong to a session that is just starting, so it is left alone.
        val unknown = Files.createDirectories(sessions.resolve("unknown/api-1"))

        val next = python(root, FakePython()).apply { setUp() }

        assertFalse(Files.exists(sessions.resolve("ended")), "the ended session's folder is removed")
        assertTrue(Files.exists(sessions.resolve("ended.lock")), "another session's lock file is never removed")
        assertTrue(Files.exists(unknown), "a folder whose lock file is not there is left alone")
        assertTrue(Files.exists(liveFolder), "a session that still runs keeps its folder")
        next.close()
        live.close()
    }

    @Test
    @Tag("RCL-131")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL131_UnitNegative_aFolderThatCannotBeRemovedKeepsItsLockFileSoALaterSessionRemovesIt(@TempDir root: Path) {
        org.junit.jupiter.api.Assumptions.assumeTrue(System.getProperty("os.name").startsWith("Windows"), "Windows refuses to remove a read-only file")
        val session = python(root, FakePython()).apply { setUp() }
        // A file that cannot be removed now, as one an indexer or a scanner holds.
        val held = apiFolder(root).resolve("robocode_tank_royale/bot_api/__init__.py")
        Files.setAttribute(held, "dos:readonly", true)
        val sessions = root.resolve(SessionPython.SESSIONS_FOLDER)

        session.close()

        val left = entries(sessions)
        assertEquals(1, left.count { Files.isDirectory(it) }, "the folder that could not be removed is still there")
        assertEquals(1, left.count { it.fileName.toString().endsWith(".lock") }, "its lock file stays with it")
        Files.setAttribute(held, "dos:readonly", false)
        python(root, FakePython()).apply { setUp() }.close()
        assertEquals(emptyList<Path>(), entries(sessions).filter { Files.isDirectory(it) }, "a later session removes it")
    }

    @Test
    @Tag("RCL-131")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL131_UnitPositive_requirementsForAnExtraAreNotRuntimeRequirementsAndNamesCompareAsPythonCompares() {
        val metadata = "Name: robocode-tank-royale\nVersion: 1.4.0\nRequires-Dist: WebSockets>=15\nRequires-Dist: py_country>=24; extra == \"docs\"\n" +
            "Requires-Dist: pycountry>=24 ; python_version >= \"3.10\"\n\n"
        val wheel = PythonBotApiWheel {
            val bytes = ByteArrayOutputStream()
            ZipOutputStream(bytes).use { zip ->
                zip.putNextEntry(ZipEntry("robocode_tank_royale-1.4.0.dist-info/METADATA"))
                zip.write(metadata.toByteArray())
                zip.closeEntry()
            }
            ByteArrayInputStream(bytes.toByteArray())
        }

        assertEquals(listOf("WebSockets>=15", "pycountry>=24"), wheel.runtimeRequirements)
        assertEquals("py-country", PythonBotApiWheel.requirementName("Py.Country__x>=1").removeSuffix("-x"))
    }

    @Test
    @Tag("RCL-131")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL131_UnitPositive_theRealWheelNamesItsVersionAndItsRuntimeRequirements() {
        val dist = Path.of(System.getProperty("user.dir")).resolve("../../bot-api/python/dist").normalize()
        val wheels = if (Files.isDirectory(dist)) entries(dist).filter { it.fileName.toString().endsWith("-py3-none-any.whl") } else emptyList()
        org.junit.jupiter.api.Assumptions.assumeTrue(wheels.isNotEmpty(), "The Python Bot API wheel is built by :bot-api:python:build-dist")
        val real = PythonBotApiWheel { Files.newInputStream(wheels.maxBy { Files.getLastModifiedTime(it) }) }

        assertTrue(real.fileName.startsWith("robocode_tank_royale-${real.version}-"))
        assertEquals(SessionPython.RUNTIME_IMPORTS, real.runtimeRequirements.map { PythonBotApiWheel.requirementName(it) }.toSet(), "the wheel requires every runtime import")
    }

    // RCL-132

    @Test
    @Tag("RCL-132")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL132_UnitPositive_aBattleWithAPythonBotGetsItsOwnCheckedCopyThroughTheWorkersEnvironment(@TempDir root: Path) {
        val session = python(root.resolve("state"), FakePython()).apply { setUp() }
        val battle = Files.createDirectories(root.resolve("battle"))

        val variables = session.forBattle(battle)

        val copy = battle.resolve(SessionPython.COPY_FOLDER)
        assertEquals(mapOf("PYTHONPATH" to copy.toString(), "PYTHONNOUSERSITE" to "1", "PYTHONDONTWRITEBYTECODE" to "1", "RUMBLE_PYTHON" to "C:\\Python314\\python.exe"), variables)
        assertTrue(Files.isRegularFile(copy.resolve("robocode_tank_royale/bot_api/__init__.py")))
        assertFalse(variables.values.any { it.contains("state") }, "no folder of the Rumble's state is named to a bot")

        // The variables reach the worker on top of its usual environment.
        val java = File(System.getProperty("java.home"), "bin/java").absolutePath
        val job = Job(jobId = "job-1", behaviorVersion = 2, gameType = "1v1", numberOfRounds = 1, bots = listOf(JobBot("A", "1.0", "sha256:aa", "/a"), JobBot("B", "1.0", "sha256:bb", "/b")))
        var battleFolder: Path? = null
        val outcome = WorkerHost(WorkerLimits(totalTime = Duration.ofSeconds(60))).run(
            WorkerCommand { listOf(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.FakeWorkerKt", "env") }, job,
        ) { folders -> battleFolder = folders.battle; session.forBattle(folders.battle) }
        val report = assertInstanceOf(WorkerOutcome.Failed::class.java, outcome).message
        assertTrue(report.contains("PYTHONNOUSERSITE=1") && report.contains("PYTHONPATH="), report)
        assertTrue(battleFolder!!.parent != null && !report.contains("TEMP=" + battleFolder.toString()), "the battle's own folder is not the bots' temporary folder")
        session.close()
    }

    @Test
    @Tag("RCL-132")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL132_UnitNegative_aBattleWhoseCopyCannotBeMadeDoesNotStartWithItsReason(@TempDir dir: Path) {
        val host = WorkerHost(WorkerLimits(totalTime = Duration.ofSeconds(30)))
        val job = Job(jobId = "j", behaviorVersion = 2, gameType = "1v1", numberOfRounds = 1, bots = emptyList())
        val command = WorkerCommand { listOf("definitely-not-a-program-rumble") }

        val io = host.run(command, job) { throw IOException("The Python Bot API changed and could not be installed again: no network") }
        val unexpected = host.run(command, job) { throw java.io.UncheckedIOException(IOException("a file is locked")) }

        assertEquals(WorkerOutcome.CouldNotStart("The Python Bot API changed and could not be installed again: no network"), io, "the reason is kept")
        assertInstanceOf(WorkerOutcome.CouldNotStart::class.java, unexpected, "no failure of preparing a battle escapes the session")

        // The session's folder changed and installing it again fails: the battle gets the reason.
        val runner = FakePython()
        val session = python(dir.resolve("state"), runner).apply { setUp() }
        Files.writeString(apiFolder(dir.resolve("state")).resolve("robocode_tank_royale/bot_api/__init__.py"), "# changed\n")
        runner.pipFails = true
        val failed = assertThrows(IOException::class.java) { session.forBattle(Files.createDirectories(dir.resolve("b"))) }
        assertTrue(failed.message!!.startsWith("The Python Bot API changed and could not be installed again"), failed.message)
        session.close()
    }

    // RCL-133

    @Test
    @Tag("RCL-133")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL133_UnitNegative_aChangeToABattlesCopyOrToTheSessionsFolderNeverReachesAnotherBattle(@TempDir root: Path) {
        val runner = FakePython()
        val session = python(root.resolve("state"), runner).apply { setUp() }
        val first = Files.createDirectories(root.resolve("first"))
        val second = Files.createDirectories(root.resolve("second"))
        val third = Files.createDirectories(root.resolve("third"))

        session.forBattle(first)
        // A bot of the first battle changes the Bot API it was given.
        Files.writeString(first.resolve("${SessionPython.COPY_FOLDER}/robocode_tank_royale/bot_api/__init__.py"), "# changed by a bot\n")
        session.forBattle(second)
        assertEquals("# the Bot API\n", Files.readString(second.resolve("${SessionPython.COPY_FOLDER}/robocode_tank_royale/bot_api/__init__.py")))

        // Something changes the session's own folder: it is installed again before the next battle gets a copy.
        val sessionFolder = apiFolder(root.resolve("state"))
        Files.writeString(sessionFolder.resolve("robocode_tank_royale/bot_api/__init__.py"), "# changed\n")
        val installsBefore = runner.commands.size
        session.forBattle(third)
        assertEquals(installsBefore + 2, runner.commands.size, "installed again")
        assertEquals("# the Bot API\n", Files.readString(third.resolve("${SessionPython.COPY_FOLDER}/robocode_tank_royale/bot_api/__init__.py")))
        assertFalse(Files.exists(sessionFolder), "the changed folder is gone")
        assertNotEquals(sessionFolder, apiFolder(root.resolve("state")))

        // Two sessions never share a folder.
        val other = python(root.resolve("state"), FakePython()).apply { setUp() }
        assertEquals(2, entries(root.resolve("state").resolve(SessionPython.SESSIONS_FOLDER)).count { Files.isDirectory(it) })
        other.close()
        session.close()
        assertThrows(IOException::class.java) { session.forBattle(Files.createDirectories(root.resolve("after"))) }
    }

    @Test
    @Tag("RCL-133")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL133_UnitPositive_theRuntimeImportsAreExactlyWhatThePythonBotApiImportsFromOutside() {
        val source = Path.of(System.getProperty("user.dir")).resolve("../../bot-api/python/src/robocode_tank_royale").normalize()
        val pattern = Regex("""^\s*(?:import\s+([A-Za-z_][A-Za-z0-9_]*)\s*(?:$|[.,#]|\s+as\s)|from\s+([A-Za-z_][A-Za-z0-9_]*)[A-Za-z0-9_.]*\s+import\s)""")
        val imported = Files.walk(source).use { paths ->
            paths.iterator().asSequence().filter { it.toString().endsWith(".py") }.toList().flatMap { file ->
                Files.readAllLines(file).mapNotNull { line -> pattern.find(line)?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() } }
            }.toSet()
        }
        val outside = imported.filter { it != "robocode_tank_royale" && it !in standardLibrary && it != "__future__" }.toSet()

        assertEquals(SessionPython.RUNTIME_IMPORTS, outside)
    }

    private val standardLibrary = setOf(
        "typing", "dataclasses", "abc", "math", "json", "threading", "sys", "re", "os", "traceback", "time", "inspect", "enum",
        "asyncio", "weakref", "urllib", "types", "locale", "io", "importlib", "heapq", "collections", "functools", "itertools",
        "random", "logging", "queue", "signal", "socket", "struct", "uuid", "copy", "contextlib", "pathlib", "warnings",
    )

    @Test
    @Tag("RCL-133")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL133_UnitPositive_theDigestIsReadFileByFileAndSeesEveryChange(@TempDir root: Path) {
        val folder = Files.createDirectories(root.resolve("f"))
        Files.write(folder.resolve("big.bin"), ByteArray(3 * 1024 * 1024) { (it % 251).toByte() })
        Files.writeString(Files.createDirectories(folder.resolve("a")).resolve("b.py"), "x")
        val before = SessionPython.digestOf(folder)

        Files.writeString(folder.resolve("a/b.py"), "y")
        val changed = SessionPython.digestOf(folder)
        Files.writeString(folder.resolve("a/b.py"), "x")
        Files.move(folder.resolve("a/b.py"), folder.resolve("a/c.py"))
        val renamed = SessionPython.digestOf(folder)

        assertNotEquals(before, changed)
        assertNotEquals(before, renamed, "a file's name counts too")
        FileChannel.open(folder.resolve("a/c.py"), StandardOpenOption.READ).use { }
    }
}
