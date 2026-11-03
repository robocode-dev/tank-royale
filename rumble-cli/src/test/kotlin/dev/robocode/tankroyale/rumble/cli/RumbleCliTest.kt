package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.RegistrationRecord
import dev.robocode.tankroyale.rumble.RegistrationStore
import dev.robocode.tankroyale.rumble.RumbleEnvironments
import dev.robocode.tankroyale.rumble.SaveResult
import dev.robocode.tankroyale.rumble.SignInIdentity
import dev.robocode.tankroyale.rumble.SignInStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant

class RumbleCliTest {

    private val fakeToken = "ghr_FAKEPREFERENCETOKEN0123456789"

    private class Run(val exit: Int, val out: String, val err: String) {
        val json: JsonObject get() = Json.parseToJsonElement(out.trim()).jsonObject
    }

    private fun run(defaultState: Path, vararg args: String): Run {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val code = RumbleCli.run(arrayOf(*args), Io(PrintStream(out, true, Charsets.UTF_8), PrintStream(err, true, Charsets.UTF_8), { defaultState }))
        return Run(code, out.toString(Charsets.UTF_8), err.toString(Charsets.UTF_8))
    }

    private fun oldClient(dir: Path, bots: String = "https://github.com/robocode-dev/rumble-bots", data: String = "https://github.com/robocode-dev/rumble-data", withFiles: Boolean = true): Path {
        val root = Files.createDirectories(dir.resolve("old"))
        val config = root.resolve("rumble-client.json")
        Files.writeString(
            config,
            """{ "schemaVersion": 1, "botsRepo": "$bots", "dataRepo": "$data", "clientId": "alice-1a2b3c4d", "myBots": ["Walls"], "gameTypes": ["1v1"], "battlesPerSession": 10, "mode": "ranked", "workDirectory": ".rumble-client" }""",
        )
        val work = Files.createDirectories(root.resolve(".rumble-client"))
        if (withFiles) {
            Files.createDirectories(work.resolve("journal"))
            Files.writeString(work.resolve("journal/records.jsonl"), "{\"battleId\":\"b1\"}\n")
            Files.createDirectories(work.resolve("evidence"))
            Files.write(work.resolve("evidence/replay.bin"), ByteArray(300) { it.toByte() })
        }
        return config
    }

    private fun snapshot(dir: Path): Map<String, String> =
        if (!Files.exists(dir)) emptyMap()
        else dir.toFile().walkTopDown().filter { it != dir.toFile() }.associate {
            it.relativeTo(dir.toFile()).path.replace('\\', '/') to
                (if (it.isDirectory) "dir" else MessageDigest.getInstance("SHA-256").digest(it.readBytes()).joinToString("") { b -> "%02x".format(b) } + "@" + it.lastModified())
        }

    private fun saveSignIn(state: Path, environment: String, identity: SignInIdentity? = null) {
        val env = RumbleEnvironments.all.first { it.name == environment }
        val store = if (identity == null) SignInStore(env, state) else SignInStore(env, state, identity)
        assertEquals(SaveResult.Saved, store.save(fakeToken, null))
    }

    // RCI-014

    @Test
    @Tag("RCI-014")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI014_UnitPositive_everyCommandAnswersInTextOrInOneJsonDocumentWithAVersion(@TempDir dir: Path) {
        val state = dir.resolve("state")
        val config = oldClient(dir)
        val commands = listOf(
            listOf("version"), listOf("environments"), listOf("status"), listOf("status", "--environment", "test"),
            listOf("import-legacy", config.toString()),
        )

        commands.forEach { command ->
            val asText = run(dir, *command.toTypedArray(), "--state-folder", state.toString())
            val asJson = run(dir, *command.toTypedArray(), "--json", "--state-folder", state.toString())

            assertEquals(0, asText.exit, command.toString())
            assertEquals(0, asJson.exit, command.toString())
            assertFalse(asText.out.trimStart().startsWith("{"), "text for a person: ${asText.out}")
            assertEquals(1, asJson.out.trim().lines().size, "exactly one JSON document: ${asJson.out}")
            assertEquals(1, asJson.json["schemaVersion"]!!.jsonPrimitive.int)
            assertEquals(command.first(), asJson.json["command"]!!.jsonPrimitive.content)
            assertEquals("", asText.err)
            assertEquals("", asJson.err)
        }
    }

    @Test
    @Tag("RCI-014")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI014_UnitNegative_aFailureStillAnswersInOneDocumentAndWritesNothingElseToStandardOutput(@TempDir dir: Path) {
        val result = run(dir, "import-legacy", dir.resolve("missing.json").toString(), "--json", "--state-folder", dir.resolve("state").toString())

        assertEquals(1, result.out.trim().lines().size)
        assertEquals("refused", result.json["outcome"]!!.jsonPrimitive.content)
        assertTrue(result.json["message"]!!.jsonPrimitive.content.isNotBlank())
    }

    // RCI-015

    @Test
    @Tag("RCI-015")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI015_UnitPositive_success_failure_and_bad_input_have_their_own_exit_codes(@TempDir dir: Path) {
        val state = dir.resolve("state")
        assertEquals(0, run(dir, "version").exit)
        assertEquals(0, run(dir, "environments", "--state-folder", state.toString()).exit)

        // 1: a command that failed, here an import into a "state folder" that is a file
        val blocker = dir.resolve("blocker.txt").also { Files.writeString(it, "a file") }
        val failed = run(dir, "import-legacy", oldClient(dir).toString(), "--copy", "--state-folder", blocker.toString())
        assertEquals(ExitCodes.FAILED, failed.exit, failed.out + failed.err)

        // 2: refused or bad input
        assertEquals(ExitCodes.REFUSED, run(dir, "nonsense").exit)
        assertEquals(ExitCodes.REFUSED, run(dir, "version", "--nonsense").exit)
        assertEquals(ExitCodes.REFUSED, run(dir, "status", "--environment", "nowhere").exit)
        assertEquals(ExitCodes.REFUSED, run(dir, "import-legacy", dir.resolve("none.json").toString(), "--state-folder", state.toString()).exit)
    }

    @Test
    @Tag("RCI-015")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI015_UnitNegative_theCommandsThatOnlyReadNeverAnswerThatASignInIsNeededOrThatTheyAreNotFinished(@TempDir dir: Path) {
        assertEquals(3, ExitCodes.SIGN_IN_NEEDED)
        assertEquals(4, ExitCodes.NOT_FINISHED)
        val codes = listOf(
            run(dir, "version"), run(dir, "environments", "--state-folder", dir.toString()), run(dir, "status", "--state-folder", dir.toString()),
            run(dir, "nonsense"), run(dir, "import-legacy", dir.resolve("none.json").toString(), "--state-folder", dir.toString()),
        ).map { it.exit }

        assertFalse(ExitCodes.SIGN_IN_NEEDED in codes, codes.toString())
        assertFalse(ExitCodes.NOT_FINISHED in codes, codes.toString())
    }

    // RCI-004

    @Test
    @Tag("RCI-004")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI004_UnitPositive_theStateFolderOptionOverridesTheDefaultAndTheDefaultIsUsedWithoutIt(@TempDir dir: Path) {
        val chosenBase = dir.resolve("chosen").also { Files.createDirectories(it.resolve("environments/test")) }
        val standardBase = dir.resolve("standard").also { Files.createDirectories(it.resolve("environments/public")) }

        val withOption = run(standardBase, "environments", "--json", "--state-folder", chosenBase.toString())
        val withoutOption = run(chosenBase, "environments", "--json")
        val fallback = run(standardBase, "environments", "--json")

        fun exists(r: Run, name: String) = r.json["environments"]!!.jsonArray.map { it.jsonObject }.first { it["name"]!!.jsonPrimitive.content == name }["stateExists"]!!.jsonPrimitive.boolean
        assertTrue(exists(withOption, "test"))
        assertFalse(exists(withOption, "public"))
        assertTrue(exists(withoutOption, "test"), "without the option the default folder is used")
        assertFalse(exists(withoutOption, "public"))
        assertTrue(exists(fallback, "public"), "the default folder given to the program is used when no option is given")
    }

    @Test
    @Tag("RCI-004")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI004_UnitNegative_theCommandsThatOnlyReadWriteNothingAndCreateNoFolder(@TempDir dir: Path) {
        val missing = dir.resolve("never-made")
        val existing = Files.createDirectories(dir.resolve("existing/environments/test")).parent.parent
        Files.writeString(existing.resolve("environments/test/registration.json"), "{}")
        val before = snapshot(existing)

        listOf("version", "environments", "status", "status").forEachIndexed { i, command ->
            run(dir, command, "--state-folder", missing.toString())
            run(dir, command, "--state-folder", existing.toString(), *(if (i == 3) arrayOf("--environment", "test") else emptyArray()))
        }

        assertFalse(Files.exists(missing), "A read-only command must not create the folder")
        assertEquals(before, snapshot(existing))
    }

    // RCI-005

    @Test
    @Tag("RCI-005")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI005_UnitPositive_versionGivesTheProgramTheEngineJavaAndTheCarriedWorker(@TempDir dir: Path) {
        val answer = run(dir, "version", "--json").json

        assertEquals("rumble", answer["program"]!!.jsonPrimitive.content)
        assertNotEquals("unknown", answer["version"]!!.jsonPrimitive.content)
        assertTrue(answer["behaviorVersion"]!!.jsonPrimitive.int >= 1)
        assertEquals(System.getProperty("java.version"), answer["java"]!!.jsonPrimitive.content)
        val worker = answer["worker"]!!.jsonObject
        assertTrue(worker["embedded"]!!.jsonPrimitive.boolean)
        assertTrue(worker["size"]!!.jsonPrimitive.content.toLong() > 1_000_000)
        assertTrue(Regex("[0-9a-f]{64}").matches(worker["sha256"]!!.jsonPrimitive.content))
    }

    // RCI-006

    @Test
    @Tag("RCI-006")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI006_UnitPositive_everyKnownEnvironmentIsListedWithItsRepositoriesAndWhetherStateExists(@TempDir dir: Path) {
        Files.createDirectories(dir.resolve("environments/test"))

        val rows = run(dir, "environments", "--json", "--state-folder", dir.toString()).json["environments"]!!.jsonArray.map { it.jsonObject }

        assertEquals(listOf("public", "test"), rows.map { it["name"]!!.jsonPrimitive.content })
        val public = rows.first { it["name"]!!.jsonPrimitive.content == "public" }
        val test = rows.first { it["name"]!!.jsonPrimitive.content == "test" }
        assertEquals("rumble-bots", public["botsRepository"]!!.jsonPrimitive.content)
        assertEquals("rumble-data-test", test["dataRepository"]!!.jsonPrimitive.content)
        assertFalse(public["resettable"]!!.jsonPrimitive.boolean)
        assertTrue(test["resettable"]!!.jsonPrimitive.boolean)
        assertTrue(test["signInApp"]!!.jsonPrimitive.boolean)
        assertFalse(public["signInApp"]!!.jsonPrimitive.boolean)
        assertFalse(public["stateExists"]!!.jsonPrimitive.boolean)
        assertTrue(test["stateExists"]!!.jsonPrimitive.boolean)
    }

    // RCI-016

    @Test
    @Tag("RCI-016")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI016_UnitPositive_statusSaysWhatIsSavedAndNeverShowsTheToken(@TempDir dir: Path) {
        val env = RumbleEnvironments.TEST
        assertTrue(RegistrationStore(env, dir).save(RegistrationRecord("alice", "alice-1a2b3c4d", "test", Instant.parse("2026-11-01T10:00:00Z"))))
        saveSignIn(dir, "test")

        val saved = run(dir, "status", "--environment", "test", "--json", "--state-folder", dir.toString())
        val none = run(dir, "status", "--environment", "public", "--json", "--state-folder", dir.toString())

        assertEquals(0, saved.exit)
        assertEquals("test", saved.json["environment"]!!.jsonPrimitive.content)
        val registration = saved.json["registration"]!!.jsonObject
        assertTrue(registration["saved"]!!.jsonPrimitive.boolean)
        assertEquals("alice", registration["account"]!!.jsonPrimitive.content)
        assertEquals("alice-1a2b3c4d", registration["clientId"]!!.jsonPrimitive.content)
        assertEquals("saved", saved.json["signIn"]!!.jsonObject["state"]!!.jsonPrimitive.content)
        assertFalse(saved.out.contains(fakeToken))
        assertFalse(run(dir, "status", "--environment", "test", "--state-folder", dir.toString()).out.contains(fakeToken))
        assertFalse(none.json["registration"]!!.jsonObject["saved"]!!.jsonPrimitive.boolean)
        assertEquals("missing", none.json["signIn"]!!.jsonObject["state"]!!.jsonPrimitive.content)
    }

    @Test
    @Tag("RCI-016")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI016_UnitNegative_aSignInThatCannotBeUsedIsSaidSoWithTheReasonAndAnUnknownEnvironmentIsRefused(@TempDir dir: Path) {
        saveSignIn(dir, "test", SignInIdentity("someone-else", "C:/another/home", "Other OS"))

        val answer = run(dir, "status", "--environment", "test", "--json", "--state-folder", dir.toString())

        val signIn = answer.json["signIn"]!!.jsonObject
        assertEquals("unusable", signIn["state"]!!.jsonPrimitive.content)
        assertTrue(signIn["reason"]!!.jsonPrimitive.content.contains("Sign in again"))
        assertFalse(answer.out.contains(fakeToken))
        assertEquals(ExitCodes.REFUSED, run(dir, "status", "--environment", "elsewhere").exit)
    }

    // RCI-008

    @Test
    @Tag("RCI-008")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI008_UnitPositive_importLegacyPreviewsByDefaultAndCopiesOnlyWithTheCopyOption(@TempDir dir: Path) {
        val config = oldClient(dir)
        val state = dir.resolve("state")
        val oldBefore = snapshot(config.parent)

        val preview = run(dir, "import-legacy", config.toString(), "--json", "--state-folder", state.toString())
        assertEquals(0, preview.exit)
        assertEquals("plan", preview.json["outcome"]!!.jsonPrimitive.content)
        assertEquals("preview", preview.json["mode"]!!.jsonPrimitive.content)
        assertEquals(2, preview.json["files"]!!.jsonArray.size)
        assertFalse(Files.exists(state), "The preview must write nothing")

        val copied = run(dir, "import-legacy", config.toString(), "--copy", "--json", "--state-folder", state.toString())
        assertEquals(0, copied.exit)
        assertEquals("imported", copied.json["outcome"]!!.jsonPrimitive.content)
        assertEquals(2, copied.json["copied"]!!.jsonPrimitive.int)
        assertTrue(Files.exists(state.resolve("environments/public/legacy-client/import-record.json")))
        assertEquals("environments/public/legacy-client", copied.json["target"]!!.jsonPrimitive.content)

        val again = run(dir, "import-legacy", config.toString(), "--copy", "--json", "--state-folder", state.toString())
        assertEquals(0, again.json["copied"]!!.jsonPrimitive.int)
        assertEquals(oldBefore, snapshot(config.parent), "The old files must be unchanged")
    }

    @Test
    @Tag("RCI-008")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI008_UnitNegative_aMixedConfigurationIsRefusedWithCodeTwoAndNothingIsWritten(@TempDir dir: Path) {
        val mixed = oldClient(dir, data = "https://github.com/robocode-dev/rumble-data-test")
        val state = dir.resolve("state")

        val refused = run(dir, "import-legacy", mixed.toString(), "--copy", "--json", "--state-folder", state.toString())

        assertEquals(ExitCodes.REFUSED, refused.exit)
        assertEquals("refused", refused.json["outcome"]!!.jsonPrimitive.content)
        assertFalse(Files.exists(state))

        val empty = oldClient(dir.resolve("e"), withFiles = false)
        val nothing = run(dir, "import-legacy", empty.toString(), "--json", "--state-folder", state.toString())
        assertEquals(0, nothing.exit)
        assertEquals("nothing", nothing.json["outcome"]!!.jsonPrimitive.content)
    }

    // RCI-017

    @Test
    @Tag("RCI-017")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI017_UnitPositive_noAnswerHoldsTheTokenTheHomeFolderTheUserNameOrAnAbsolutePathOfThisComputer(@TempDir dir: Path) {
        val state = dir.resolve("state")
        val config = oldClient(dir)
        RegistrationStore(RumbleEnvironments.PUBLIC, state).save(RegistrationRecord("alice", "alice-1a2b3c4d", "public", Instant.parse("2026-11-01T10:00:00Z")))
        saveSignIn(state, "public")
        val user = System.getProperty("user.name").orEmpty()
        val home = System.getProperty("user.home").orEmpty()

        val answers = listOf(
            listOf("version"), listOf("environments"), listOf("status"), listOf("import-legacy", config.toString()),
            listOf("import-legacy", config.toString(), "--copy"), listOf("import-legacy", dir.resolve("none.json").toString()),
        ).flatMap { command -> listOf(false, true).map { asJson -> run(dir, *command.toTypedArray(), *(if (asJson) arrayOf("--json") else emptyArray()), "--state-folder", state.toString()) } }

        answers.forEach { answer ->
            val text = answer.out + answer.err
            assertFalse(text.contains(fakeToken), text)
            assertFalse(text.contains(home), text)
            if (user.length >= 3) assertFalse(text.lowercase().contains(user.lowercase()), text)
            assertFalse(Regex("[A-Za-z]:[\\\\/]").containsMatchIn(text), text)
            assertFalse(Regex("/(home|Users)/").containsMatchIn(text), text)
        }
    }

    @Test
    @Tag("RCI-017")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI017_UnitNegative_theRedactionRemovesHomeUserAndAbsolutePathsFromAnyText() {
        val redaction = Redaction("C:\\Users\\alice", "alice")

        val cleaned = redaction.clean("""in C:\Users\alice\work and C:/Users/alice/x, also D:\data\f.txt and /home/bob/y, by alice""")

        listOf("alice", "C:\\Users", "D:\\data", "/home/bob").forEach { assertFalse(cleaned.contains(it), cleaned) }
        assertEquals("plain words stay", redaction.clean("plain words stay"))
    }

}
