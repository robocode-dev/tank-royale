package dev.robocode.tankroyale.botproject

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class BotProjectStagingTest {

    private fun unix(bytes: ByteArray): ByteArray =
        String(bytes, Charsets.ISO_8859_1).replace("\r\n", "\n").toByteArray(Charsets.ISO_8859_1)

    private fun staged(result: StagingResult): StagedSubmission = assertInstanceOf(StagingResult.Staged::class.java, result).submission

    private fun filesBelow(folder: Path): List<String> =
        Files.walk(folder).use { paths ->
            paths.filter { Files.isRegularFile(it) }.map { folder.relativize(it).joinToString("/") }.sorted().toList()
        }

    /** A valid project in [language] with a lot of files around it that are not part of the bot. */
    private fun messyProject(parent: Path, language: String): Path {
        val project = Fixtures.bot(parent.resolve("projects"), language)
        listOf(
            "bin/Debug/MyBot.dll", "obj/Generated.cs", ".git/config", ".idea/workspace.xml", ".vscode/settings.json",
            "node_modules/pkg/index.d.ts", "node_modules/pkg/index.ts", "__pycache__/x.pyc", "notes.txt", "docs/guide.md",
            "src/data/table.txt", "build.log",
        ).forEach { Fixtures.write(project.resolve(it), "junk") }
        return project
    }

    private fun notIncluded(submission: StagedSubmission, path: String) = submission.notIncluded.firstOrNull { it.path == path }

    // RBC-020

    @Test
    @Tag("RBC-020")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC020_UnitPositive_onlyConfigurationLaunchersSourceAndDeclarationFilesAreCopiedAndTheRestIsListedByFolder(@TempDir parent: Path) {
        val java = staged(BotStaging.stage(messyProject(parent.resolve("j"), "java"), parent.resolve("stage-j")))
        assertEquals(listOf("MyBot.cmd", "MyBot.json", "MyBot.sh", "src/MyBot.java"), java.files.map { it.path })
        assertEquals(listOf("MyBot.cmd", "MyBot.json", "MyBot.sh", "src/MyBot.java"), filesBelow(java.botFolder))
        assertEquals(1, notIncluded(java, "bin/")!!.fileCount)
        assertEquals(1, notIncluded(java, "obj/")!!.fileCount)
        assertEquals(1, notIncluded(java, "notes.txt")!!.fileCount)
        assertEquals(1, notIncluded(java, "docs/")!!.fileCount)
        assertEquals(1, notIncluded(java, "src/data/")!!.fileCount)
        assertTrue(java.notIncluded.all { it.reason.isNotEmpty() && it.fileCount > 0 })
        listOf(".git/", ".idea/", ".vscode/", "node_modules/", "__pycache__/").forEach { assertTrue(notIncluded(java, it) != null, it) }

        val ts = staged(BotStaging.stage(messyProject(parent.resolve("t"), "typescript"), parent.resolve("stage-t")))
        assertEquals(
            listOf("MyBot.cmd", "MyBot.json", "MyBot.sh", "package-lock.json", "package.json", "src/MyBot.ts"),
            ts.files.map { it.path },
        )

        val python = Fixtures.bot(parent.resolve("p"), "python").also { Fixtures.write(it.resolve("requirements.txt"), "robocode-tank-royale==1.3.1\n") }
        assertTrue(staged(BotStaging.stage(python, parent.resolve("stage-p"))).files.any { it.path == "requirements.txt" })

        val csharp = Fixtures.bot(parent.resolve("c"), "csharp").also {
            Fixtures.write(it.resolve("MyBot.csproj"), "<Project/>")
            Fixtures.write(it.resolve("LICENSE"), "MIT")
        }
        val cs = staged(BotStaging.stage(csharp, parent.resolve("stage-c")))
        assertTrue(cs.files.any { it.path == "MyBot.csproj" } && cs.files.any { it.path == "LICENSE" })
    }

    @Test
    @Tag("RBC-020")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC020_UnitNegative_sourceLookingFilesInBuildFoldersAndSymbolicLinksAreNotTaken(@TempDir parent: Path) {
        val project = messyProject(parent.resolve("c"), "csharp")
        val outside = parent.resolve("outside.cs").also { Files.writeString(it, "secret") }
        val linked = try {
            Files.createSymbolicLink(project.resolve("src").resolve("Linked.cs"), outside)
            true
        } catch (e: Exception) {
            false
        }

        val result = staged(BotStaging.stage(project, parent.resolve("stage")))

        assertFalse(result.files.any { it.path.startsWith("obj/") || it.path.startsWith("node_modules/") })
        assertEquals(listOf("MyBot.cmd", "MyBot.json", "MyBot.sh", "src/MyBot.cs"), result.files.map { it.path })
        assertFalse(filesBelow(result.stagingFolder).any { it.endsWith("outside.cs") || it.endsWith("Linked.cs") })
        if (linked) {
            assertEquals("symbolic link or special file", notIncluded(result, "src/Linked.cs")!!.reason)
        } else {
            assumeTrue(false, "This system does not allow the test to create a symbolic link")
        }
    }

    // RBC-021

    @Test
    @Tag("RBC-021")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC021_UnitPositive_theCopyIsLaidOutLikeTheBotRepositoryAndTheProjectIsUntouched(@TempDir parent: Path) {
        val project = messyProject(parent, "python")
        val before = TreeHash.ofDirectory(project)

        val result = staged(BotStaging.stage(project, parent.resolve("stage")))

        assertEquals(parent.resolve("stage").resolve("bots").resolve("python").resolve("MyBot"), result.botFolder)
        assertTrue(Files.exists(parent.resolve("stage").resolve(BotStaging.MARKER)))
        assertEquals(before, TreeHash.ofDirectory(project))

        val team = staged(BotStaging.stage(Fixtures.team(parent.resolve("teams")), parent.resolve("stage-team"), teamLanguage = "python"))
        assertEquals(true, team.isTeam)
        assertEquals(listOf("MyTeam.json"), filesBelow(team.botFolder))
        assertEquals("python", team.language)
    }

    @Test
    @Tag("RBC-021")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC021_UnitNegative_unsafeStagingFoldersAreRefusedBeforeAnythingIsWritten(@TempDir parent: Path) {
        val project = Fixtures.bot(parent.resolve("work"), "java")
        val before = TreeHash.ofDirectory(project)

        val inside = BotStaging.stage(project, project.resolve("stage"))
        assertTrue((inside as StagingResult.Refused).message.contains("inside the project"))
        assertFalse(Files.exists(project.resolve("stage")))

        val containing = BotStaging.stage(project, parent.resolve("work"))
        assertTrue((containing as StagingResult.Refused).message.contains("contain the project"))

        val same = BotStaging.stage(project, project)
        assertInstanceOf(StagingResult.Refused::class.java, same)

        val foreign = parent.resolve("foreign").also { Fixtures.write(it.resolve("keep.txt"), "my own file") }
        assertTrue((BotStaging.stage(project, foreign) as StagingResult.Refused).message.contains("not empty"))
        assertEquals("my own file", Files.readString(foreign.resolve("keep.txt")))

        val file = parent.resolve("a-file").also { Files.writeString(it, "x") }
        assertInstanceOf(StagingResult.Refused::class.java, BotStaging.stage(project, file))

        assertEquals(before, TreeHash.ofDirectory(project))
    }

    @Test
    @Tag("RBC-021")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC021_UnitNegative_aSymbolicLinkCannotHideTheProjectFolderAsTheStagingFolder(@TempDir parent: Path) {
        val project = Fixtures.bot(parent.resolve("work"), "java")
        val before = TreeHash.ofDirectory(project)
        val link = parent.resolve("link-to-project")
        try {
            Files.createSymbolicLink(link, project)
        } catch (e: Exception) {
            assumeTrue(false, "This system does not allow the test to create a symbolic link")
        }

        val result = BotStaging.stage(project, link.resolve("stage"))

        assertInstanceOf(StagingResult.Refused::class.java, result)
        assertEquals(before, TreeHash.ofDirectory(project))
    }

    @Test
    @Tag("RBC-021")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC021_UnitPositive_aFolderMadeByEarlierStagingIsEmptiedAndReused(@TempDir parent: Path) {
        val project = Fixtures.bot(parent.resolve("work"), "java")
        val stage = parent.resolve("stage")
        staged(BotStaging.stage(project, stage))
        Fixtures.write(stage.resolve("stale.txt"), "left over")
        Fixtures.write(stage.resolve("bots/java/OldBot/OldBot.json"), "{}")

        staged(BotStaging.stage(project, stage))

        assertEquals(listOf(BotStaging.MARKER, "bots/java/MyBot/MyBot.cmd", "bots/java/MyBot/MyBot.json", "bots/java/MyBot/MyBot.sh", "bots/java/MyBot/src/MyBot.java"), filesBelow(stage))
    }

    // RBC-022

    @Test
    @Tag("RBC-022")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC022_UnitPositive_windowsLineEndingsBecomeUnixOnlyInTheCopyAndTheLauncherIsExecutable(@TempDir parent: Path) {
        val project = Fixtures.bot(parent.resolve("work"), "java")
        val crlf = mapOf(
            "MyBot.json" to Files.readString(project.resolve("MyBot.json")).replace("\n", "\r\n"),
            "MyBot.sh" to "#!/bin/sh\r\necho start\r\n",
            "MyBot.cmd" to "@echo off\r\necho start\r\n",
            "src/MyBot.java" to "import dev.robocode.tankroyale.botapi.Bot;\r\nclass MyBot {\r\n}\r\n",
        )
        crlf.forEach { (path, text) -> Files.write(project.resolve(path), text.toByteArray()) }
        val originals = crlf.mapValues { Files.readAllBytes(project.resolve(it.key)) }

        val result = staged(BotStaging.stage(project, parent.resolve("stage")))

        crlf.forEach { (path, text) ->
            val copy = Files.readAllBytes(result.botFolder.resolve(path))
            assertFalse(copy.contains('\r'.code.toByte()), path)
            assertArrayEquals(text.replace("\r\n", "\n").toByteArray(), copy, path)
            assertArrayEquals(originals.getValue(path), Files.readAllBytes(project.resolve(path)), "The original $path must be unchanged")
        }
        assertTrue(result.files.all { it.lineEndingsChanged })
        assertEquals(listOf("MyBot.sh"), result.files.filter { it.executable }.map { it.path })
        if ("posix" in result.botFolder.fileSystem.supportedFileAttributeViews()) {
            assertTrue(Files.isExecutable(result.botFolder.resolve("MyBot.sh")))
        }
    }

    @Test
    @Tag("RBC-022")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC022_UnitNegative_filesWithUnixLineEndingsAreNotReportedAsChangedAndOtherCharactersAreKept(@TempDir parent: Path) {
        val project = Fixtures.bot(parent.resolve("work"), "java")
        Files.write(project.resolve("MyBot.cmd"), "@echo off".toByteArray() + byteArrayOf(10))
        Files.write(project.resolve("src/MyBot.java"), "import dev.robocode.tankroyale.botapi.Bot;\r\nclass MyBot { String s = \"a\rb\"; }\r\n".toByteArray())

        val result = staged(BotStaging.stage(project, parent.resolve("stage")))

        assertEquals(listOf("src/MyBot.java"), result.files.filter { it.lineEndingsChanged }.map { it.path })
        assertEquals(
            "import dev.robocode.tankroyale.botapi.Bot;\nclass MyBot { String s = \"a\rb\"; }\n",
            Files.readString(result.botFolder.resolve("src/MyBot.java")),
        )
        assertFalse(result.files.first { it.path == "MyBot.json" }.lineEndingsChanged)
    }

    // RBC-023

    @Test
    @Tag("RBC-023")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC023_UnitPositive_theDescriptionIsCompleteAndStagingTwiceGivesTheSameFilesAndHash(@TempDir parent: Path) {
        val project = messyProject(parent, "java")

        val first = staged(BotStaging.stage(project, parent.resolve("one")))
        val second = staged(BotStaging.stage(project, parent.resolve("two")))

        assertEquals("MyBot", first.name)
        assertEquals("1.0.0", first.version)
        assertEquals("java", first.language)
        assertEquals("MIT", first.license)
        assertEquals(listOf("A. Author"), first.authors)
        assertEquals(false, first.isTeam)
        assertEquals(first.files.map { it.path }, second.files.map { it.path })
        assertEquals(first.files.map { it.size }, second.files.map { it.size })
        assertEquals(first.treeHash, second.treeHash)
        assertEquals(TreeHash.ofDirectory(first.botFolder), first.treeHash)
        // The same hash computed independently from the original files with Unix line endings, as the bot repository holds them.
        val expected = first.files.associate {
            it.path to unix(Files.readAllBytes(project.resolve(it.path)))
        }
        assertEquals(TreeHash.of(expected), first.treeHash)
        assertTrue(first.treeHash.startsWith("sha256:"))
    }

    @Test
    @Tag("RBC-023")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC023_UnitNegative_aProjectWhoseCopyFailsTheChecksGivesProblemsAndNoUsableCopy(@TempDir parent: Path) {
        val badLicense = Fixtures.bot(parent.resolve("a"), "java").also {
            Files.writeString(it.resolve("MyBot.json"), Files.readString(it.resolve("MyBot.json")).replace("\"MIT\"", "\"Proprietary\""))
        }
        val rejected = BotStaging.stage(badLicense, parent.resolve("stage-a"))
        assertTrue((rejected as StagingResult.Rejected).findings.any { it.code == "LICENSE_NOT_ALLOWED" })
        assertEquals(listOf(BotStaging.MARKER), filesBelow(parent.resolve("stage-a")))

        val noConfig = parent.resolve("b").resolve("Empty").also { Fixtures.write(it.resolve("readme.txt"), "x") }
        assertEquals("MISSING_CONFIG", (BotStaging.stage(noConfig, parent.resolve("stage-b")) as StagingResult.Rejected).findings.single().code)

        val unknown = Fixtures.bot(parent.resolve("c"), "java").also {
            Files.writeString(it.resolve("MyBot.json"), Files.readString(it.resolve("MyBot.json")).replace("\"JVM\"", "\"Cobol\""))
        }
        assertEquals("PLATFORM_UNKNOWN", (BotStaging.stage(unknown, parent.resolve("stage-c")) as StagingResult.Rejected).findings.single().code)

        val noApi = Fixtures.bot(parent.resolve("d"), "java").also { Fixtures.write(it.resolve("src/MyBot.java"), "class MyBot {}") }
        assertTrue((BotStaging.stage(noApi, parent.resolve("stage-d")) as StagingResult.Rejected).findings.any { it.code == "NO_API_REFERENCE" })
    }
}
