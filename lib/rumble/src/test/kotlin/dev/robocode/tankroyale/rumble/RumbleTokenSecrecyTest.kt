package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class RumbleTokenSecrecyTest {

    private val access = "ghu_SECRETACCESS123"
    private val refresh = "ghr_SECRETREFRESH456"
    private val device = "DEVICE_SECRET_789"
    private val secrets = listOf(access, refresh, device)

    /** Runs every sign-in path with the secrets present and returns all text the library produced. */
    private fun everythingProduced(): List<String> {
        val texts = mutableListOf<String>()
        val flow = GitHubSignIn(
            RumbleEnvironments.TEST,
            FakeTransport(
                deviceCodeAnswer(deviceCode = device),
                tokenAnswer(access = access, refresh = refresh),
                answer("error" to "weird_error", "error_description" to "the token $access was rejected", "access_token_hint" to access),
                answer("error" to access, "refresh_token" to refresh),
                answer("error" to "bad_refresh_token", "refresh_token" to refresh),
                answer("error_description" to "device $device", status = 500),
                tokenAnswer(access = access, refresh = refresh),
                IOException("offline while sending $refresh"),
            ),
            MutableClock(START),
        )

        val code = flow.start()
        texts += code.toString()
        val signedIn = flow.check(code)
        texts += signedIn.toString()
        val tokens = (signedIn as SignInStep.SignedIn).tokens
        texts += tokens.toString()
        repeat(2) { texts += (flow.check(code) as SignInStep.Failed).message }
        val refused = flow.refresh(tokens)
        texts += refused.toString()
        texts += (flow.check(code) as SignInStep.Failed).message
        val renewed = flow.refresh(tokens)
        texts += renewed.toString()
        texts += (flow.refresh(tokens) as RefreshResult.Failed).message
        texts += SignInStep.SlowDown(10).toString() + SignInStep.Cancelled + SignInStep.Expired
        return texts
    }

    @Test
    @Tag("RCL-030")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL030_UnitPositive_noTokenAppearsInAnyTextMessageOrConsoleOutput() {
        val outBuffer = ByteArrayOutputStream()
        val errBuffer = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err
        val texts: List<String>
        try {
            System.setOut(PrintStream(outBuffer, true))
            System.setErr(PrintStream(errBuffer, true))
            texts = everythingProduced()
        } finally {
            System.setOut(originalOut)
            System.setErr(originalErr)
        }

        assertTrue(texts.size >= 10, "The scenario should have produced text for every path")
        (texts + outBuffer.toString() + errBuffer.toString()).forEach { text ->
            secrets.forEach { secret -> assertFalse(text.contains(secret), "'$secret' leaked into: $text") }
        }
        assertEquals("", outBuffer.toString())
        assertEquals("", errBuffer.toString())
    }

    @Test
    @Tag("RCL-030")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL030_UnitNegative_theSignInSourcesUseNoFilesConsoleOrLogging() {
        val files = listOf("GitHubSignIn.kt", "GitHubTokens.kt", "GitHubAuthTransport.kt", "GitHubApi.kt", "Registration.kt")
        val forbidden = listOf("java.nio.file", "java.io.File", "FileWriter", "FileOutputStream", "println", "System.out", "System.err", "Logger")
        val sourceRoot = Path.of("src", "main", "kotlin", "dev", "robocode", "tankroyale", "rumble")

        files.forEach { name ->
            val text = Files.readString(sourceRoot.resolve(name))
            forbidden.forEach { word -> assertFalse(text.contains(word), "$name uses $word") }
        }

        // Sending a bot must read the staged files, so it may use files, but it never prints or logs anything.
        val printing = listOf("println", "System.out", "System.err", "Logger", "printStackTrace")
        listOf("BotSubmission.kt", "SubmissionProgress.kt", "SubmissionPrecheck.kt", "RumbleRules.kt", "WorkerHost.kt", "WorkerEnvironment.kt").forEach { name ->
            val text = Files.readString(sourceRoot.resolve(name))
            printing.forEach { word -> assertFalse(text.contains(word), "$name uses $word") }
        }
    }
}
