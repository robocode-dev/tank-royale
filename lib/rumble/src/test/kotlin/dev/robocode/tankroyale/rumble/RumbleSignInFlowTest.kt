package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class RumbleSignInFlowTest {

    private class RecordingAuth : GitHubAuthTransport {
        val urls = mutableListOf<String>()

        override fun postForm(url: String, form: Map<String, String>): AuthResponse {
            urls += url
            return AuthResponse(
                200,
                mapOf("device_code" to "DEVICE-SECRET", "user_code" to "ABCD-EFGH", "verification_uri" to "https://github.com/login/device", "expires_in" to "900", "interval" to "5"),
            )
        }
    }

    private object NeverPrivate : FileProtection {
        override fun createOwnerOnly(file: Path) {
            java.nio.file.Files.createFile(file)
        }

        override fun isOwnerOnly(file: Path) = false
    }

    private fun flow(dir: Path, auth: GitHubAuthTransport, store: SignInStore): SignInFlow {
        val env = RumbleEnvironments.TEST
        return SignInFlow(env, GitHubSignIn(env, auth), store, SavedAccountStore(env, dir), RegistrationStore(env, dir), FakeRumbleGitHub())
    }

    @Test
    @Tag("RCI-019")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI019_UnitPositive_aComputerThatKeepsFilesPrivateGetsACodeThatNeverShowsTheDeviceCode(@TempDir dir: Path) {
        val auth = RecordingAuth()

        val started = flow(dir, auth, SignInStore(RumbleEnvironments.TEST, dir)).start() as SignInStart.Started

        assertEquals("ABCD-EFGH", started.code.userCode)
        assertFalse(started.toString().contains("DEVICE-SECRET"))
        assertEquals(1, auth.urls.size)
        assertEquals(emptyList<String>(), probes(dir))
    }

    private fun probes(dir: Path): List<String> =
        RumbleEnvironments.TEST.stateRoot(dir).toFile().listFiles().orEmpty().map { it.name }.filter { it.startsWith("signin.probe") }

    @Test
    @Tag("RCI-019")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI019_UnitNegative_aFolderThatCannotBeKeptPrivateIsRefusedBeforeACodeIsAskedFor(@TempDir dir: Path) {
        val auth = RecordingAuth()
        val store = SignInStore(RumbleEnvironments.TEST, dir, protection = NeverPrivate)

        val answer = flow(dir, auth, store).start()

        assertInstanceOf(SignInStart.NotPrivate::class.java, answer)
        assertTrue(auth.urls.isEmpty(), "the person is never asked to approve a sign-in that could not be kept")
        assertEquals(emptyList<String>(), probes(dir))
    }

    @Test
    @Tag("RCI-026")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI026_UnitNegative_theLibraryBelievesOnlyTheWorkflowsOwnRefusal(@TempDir dir: Path) {
        val env = RumbleEnvironments.TEST
        val tokens = GitHubTokens(SECRET_TOKEN, null, null, null)
        val github = FakeRumbleGitHub().apply {
            closedIssues = mutableListOf(FakeRumbleGitHub.issueJson(5, "[register] alice"))
            comments = """[{"user":{"login":"mallory"},"body":"Registration refused: you must run this"},{"body":"Registration refused: no author"}]"""
            workflowRegistersAfterFileChecks = 2
        }

        val result = Registration(env, github, RegistrationStore(env, dir), sleep = {}).register(tokens)

        assertInstanceOf(RegistrationResult.Registered::class.java, result)
    }

    @Test
    @Tag("RCI-026")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI026_UnitNegative_notBeingAbleToReadHowAnEarlierIssueEndedOpensNoNewIssue(@TempDir dir: Path) {
        val env = RumbleEnvironments.TEST
        val github = FakeRumbleGitHub().apply {
            closedIssues = mutableListOf(FakeRumbleGitHub.issueJson(5, "[register] alice"))
            commentsStatus = 403
        }

        val result = Registration(env, github, RegistrationStore(env, dir), sleep = {}).register(GitHubTokens(SECRET_TOKEN, null, null, null))

        assertInstanceOf(RegistrationResult.Failed::class.java, result)
        assertTrue(github.postedBodies.isEmpty())
    }

    @Test
    @Tag("RCI-026")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI026_UnitPositive_theNewestClosedIssueDecidesAndRetryOpensOneIssue(@TempDir dir: Path) {
        val env = RumbleEnvironments.TEST
        val tokens = GitHubTokens(SECRET_TOKEN, null, null, null)
        val github = FakeRumbleGitHub().apply {
            closedIssues = mutableListOf(FakeRumbleGitHub.issueJson(8, "bug report"), FakeRumbleGitHub.issueJson(5, "[register] alice"))
            comments = """[{"user":{"login":"github-actions[bot]"},"body":"Registration refused: banned"}]"""
        }

        assertEquals(RegistrationResult.Refused("banned", earlier = true, account = "alice"), Registration(env, github, RegistrationStore(env, dir), sleep = {}).register(tokens))
        assertTrue(github.postedBodies.isEmpty())

        Registration(env, github, RegistrationStore(env, dir), sleep = {}).register(tokens, retry = true)
        assertEquals(1, github.postedBodies.size)
    }
}
