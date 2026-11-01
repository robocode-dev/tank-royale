package dev.robocode.tankroyale.rumble

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
import java.time.Instant
import kotlin.streams.toList

class RumbleSignInStoreTest {

    private val test = RumbleEnvironments.TEST
    private val alice = SignInIdentity("alice", "/home/alice", "Linux")
    private val refreshToken = "ghr_SAVED_REFRESH_TOKEN_123"
    private val expires = Instant.parse("2027-04-01T10:00:00Z")

    private fun store(
        base: Path,
        env: RumbleEnvironment = test,
        identity: SignInIdentity = alice,
        protection: FileProtection = JavaFileProtection,
        enabled: Boolean = true,
    ) = SignInStore(env, base, identity, protection, enabled)

    private fun filesBelow(base: Path): List<String> =
        Files.walk(base).use { paths ->
            paths.filter { Files.isRegularFile(it) }.map { base.relativize(it).joinToString("/") }.sorted().toList()
        }

    private fun signInFile(base: Path, env: RumbleEnvironment = test): Path = env.stateRoot(base).resolve("signin.bin")

    // RCL-031

    @Test
    @Tag("RCL-031")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL031_UnitPositive_aNewStoreLoadsWhatWasSavedAndTheFileHoldsNoReadableToken(@TempDir base: Path) {
        assertEquals(SaveResult.Saved, store(base).save(refreshToken, expires))

        val loaded = assertInstanceOf(LoadResult.Found::class.java, store(base).load())

        assertEquals(refreshToken, loaded.refreshToken)
        assertEquals(expires, loaded.refreshTokenExpiresAt)
        assertEquals(listOf("environments/test/signin.bin"), filesBelow(base))
        val raw = String(Files.readAllBytes(signInFile(base)), Charsets.ISO_8859_1)
        assertFalse(raw.contains("SAVED_REFRESH"))
        assertFalse(raw.contains("refreshToken"))
        assertFalse(loaded.toString().contains("SAVED_REFRESH"))
    }

    @Test
    @Tag("RCL-031")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL031_UnitNegative_nothingIsWrittenOrLoadedWhenSavingIsSwitchedOff(@TempDir base: Path) {
        val off = store(base, enabled = false)

        assertTrue(off.save(refreshToken, expires) is SaveResult.NotSaved)
        assertEquals(LoadResult.Disabled, off.load())
        assertEquals(emptyList<String>(), filesBelow(base))

        store(base).save(refreshToken, expires)
        assertEquals(LoadResult.Disabled, off.load())
    }

    // RCL-032

    @Test
    @Tag("RCL-032")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL032_UnitPositive_theFileIsOwnerOnlyOnThisFileSystemAndLeavesNoTemporaryFile(@TempDir base: Path) {
        assertEquals(SaveResult.Saved, store(base).save(refreshToken, null))

        assertTrue(JavaFileProtection.isOwnerOnly(signInFile(base)))
        assertEquals(listOf("environments/test/signin.bin"), filesBelow(base))
        assertEquals(SaveResult.Saved, store(base).save("ghr_SECOND_VALUE_456", null))
        assertEquals("ghr_SECOND_VALUE_456", (store(base).load() as LoadResult.Found).refreshToken)
    }

    @Test
    @Tag("RCL-032")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL032_UnitNegative_aFileSystemThatCannotRestrictAccessGetsNothingWritten(@TempDir base: Path) {
        val protection = object : FileProtection {
            var sizeWhenChecked = -1L
            override fun createOwnerOnly(file: Path) {
                Files.createFile(file)
            }

            override fun isOwnerOnly(file: Path): Boolean {
                sizeWhenChecked = Files.size(file)
                return false
            }
        }

        val result = store(base, protection = protection).save(refreshToken, expires)

        val notSaved = assertInstanceOf(SaveResult.NotSaved::class.java, result)
        assertTrue(notSaved.reason.contains("private to you"))
        assertEquals(0L, protection.sizeWhenChecked, "The rights must be checked before any token is written")
        assertEquals(emptyList<String>(), filesBelow(base))
    }

    @Test
    @Tag("RCL-032")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL032_UnitNegative_rightsThatChangeAfterTheMoveRemoveTheFile(@TempDir base: Path) {
        val protection = object : FileProtection {
            override fun createOwnerOnly(file: Path) {
                Files.createFile(file)
            }

            // Fine for the temporary file, wrong once the file is in its final place.
            override fun isOwnerOnly(file: Path): Boolean = file.fileName.toString().endsWith(".tmp")
        }

        val result = store(base, protection = protection).save(refreshToken, expires)

        assertTrue(result is SaveResult.NotSaved)
        assertEquals(emptyList<String>(), filesBelow(base))
    }

    // RCL-033

    @Test
    @Tag("RCL-033")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL033_UnitPositive_aMissingFileIsMissingNotAProblem(@TempDir base: Path) {
        assertEquals(LoadResult.Missing, store(base).load())
    }

    @Test
    @Tag("RCL-033")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL033_UnitNegative_everyKindOfUntrustworthyFileIsRefusedWithAReasonAndNoToken(@TempDir base: Path) {
        store(base).save(refreshToken, expires)
        val original = Files.readAllBytes(signInFile(base))

        fun assertUnusable(result: LoadResult, because: String) {
            val unusable = assertInstanceOf(LoadResult.Unusable::class.java, result, because)
            assertTrue(unusable.reason.contains("Sign in again"), because)
            assertFalse(unusable.reason.contains("SAVED_REFRESH"), because)
        }

        val changed = original.clone().also { it[it.size - 3] = (it[it.size - 3].toInt() xor 0x55).toByte() }
        Files.write(signInFile(base), changed)
        assertUnusable(store(base).load(), "one changed byte")

        Files.write(signInFile(base), original.copyOf(10))
        assertUnusable(store(base).load(), "cut short")

        Files.write(signInFile(base), ByteArray(0))
        assertUnusable(store(base).load(), "empty")

        Files.write(signInFile(base), original)
        assertUnusable(store(base, identity = SignInIdentity("bob", "/home/alice", "Linux")).load(), "another user")
        assertUnusable(store(base, identity = SignInIdentity("alice", "/home/bob", "Linux")).load(), "another home folder")
        assertUnusable(store(base, identity = SignInIdentity("alice", "/home/alice", "Windows 11")).load(), "another system")

        val publicFile = signInFile(base, RumbleEnvironments.PUBLIC)
        Files.createDirectories(publicFile.parent)
        Files.copy(signInFile(base), publicFile)
        assertUnusable(store(base, env = RumbleEnvironments.PUBLIC).load(), "moved to another environment")

        val open = object : FileProtection by JavaFileProtection {
            override fun isOwnerOnly(file: Path) = false
        }
        assertUnusable(store(base, protection = open).load(), "rights that let others use it")

        assertEquals(refreshToken, (store(base).load() as LoadResult.Found).refreshToken)
    }

    // RCL-034

    @Test
    @Tag("RCL-034")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL034_UnitPositive_signingOutRemovesTheFilesAndAnEmptyForgetIsHarmless(@TempDir base: Path) {
        store(base).forget()
        store(base).save(refreshToken, expires)
        Files.createFile(signInFile(base).resolveSibling("signin.bin.tmp"))

        store(base).forget()

        assertEquals(emptyList<String>(), filesBelow(base))
        assertEquals(LoadResult.Missing, store(base).load())
    }

    /** Uses the real Tank Royale user folder of whoever runs it, then removes what it made. Run with -Drumble.live=true. */
    @Test
    @Tag("Sanity")
    fun testSanity_savingInTheRealUserFolderWorksOnThisComputerWhenAskedTo() {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to use the real user folder")
        val real = SignInStore.inUserFolder(test)
        val folder = test.stateRoot(dev.robocode.tankroyale.common.util.UserDataDirectory.get().toPath())
        assumeTrue(!Files.exists(folder.resolve("signin.bin")), "A real saved sign-in exists here; not touching it")
        try {
            assertEquals(SaveResult.Saved, real.save(refreshToken, expires))
            assertEquals(refreshToken, (SignInStore.inUserFolder(test).load() as LoadResult.Found).refreshToken)
            assertTrue(JavaFileProtection.isOwnerOnly(folder.resolve("signin.bin")))
            println("LIVE saved sign-in in: " + folder)
        } finally {
            real.forget()
            Files.deleteIfExists(folder.resolve("signin.lock"))
            Files.deleteIfExists(folder)
            Files.deleteIfExists(folder.parent)
        }
        assertFalse(Files.exists(folder))
    }
}
