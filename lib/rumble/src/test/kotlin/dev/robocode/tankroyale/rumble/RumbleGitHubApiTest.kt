package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path

class RumbleGitHubApiTest {

    private val api = HttpGitHubApi()

    @Test
    @Tag("RCL-040")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL040_UnitPositive_theTokenGoesOnlyInTheAuthorizationHeaderOfRequestsToGitHubsApi() {
        val get = api.request("GET", "/repos/robocode-dev/rumble-data-test/contents/clients/alice.json", SECRET_TOKEN, null)
        val post = api.request("POST", "/repos/robocode-dev/rumble-data-test/issues", SECRET_TOKEN, """{"title":"[register] alice"}""")

        val patch = api.request("PATCH", "/repos/robocode-dev/rumble-bots-test/pulls/3", SECRET_TOKEN, """{"title":"Update Scout to 1.0.4"}""")

        listOf(get, post, patch).forEach { request ->
            assertEquals("api.github.com", request.uri().host)
            assertEquals("https", request.uri().scheme)
            assertFalse(request.uri().toString().contains(SECRET_TOKEN))
            assertEquals("Bearer $SECRET_TOKEN", request.headers().firstValue("Authorization").get())
            assertEquals(1, request.headers().map().filterKeys { it.equals("Authorization", true) }.size)
        }
        assertEquals("PATCH", patch.method())
        assertEquals("POST", post.method())
        assertEquals("application/json", post.headers().firstValue("Content-Type").get())
        assertFalse(api.request("GET", "/user", null, null).headers().firstValue("Authorization").isPresent)
    }

    @Test
    @Tag("RCL-040")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL040_UnitNegative_noTokenAppearsInAnAddressResultMessageOrConsoleOutput(@TempDir base: Path) {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err
        val texts = mutableListOf<String>()
        val addresses = mutableListOf<String>()
        try {
            System.setOut(PrintStream(out, true))
            System.setErr(PrintStream(err, true))
            fun flow(configure: FakeRumbleGitHub.() -> Unit): RegistrationResult {
                val github = FakeRumbleGitHub().apply(configure)
                val clock = MutableClock(START)
                val result = Registration(
                    RumbleEnvironments.TEST, github, RegistrationStore(RumbleEnvironments.TEST, base), clock,
                    sleep = { clock.advanceMillis(it) },
                ).register(tokens())
                github.requests.forEach { addresses += it.second }
                github.postedBodies.forEach { addresses += it }
                texts += result.toString()
                (result as? RegistrationResult.Failed)?.let { texts += it.message }
                (result as? RegistrationResult.SignInNeeded)?.let { texts += it.message }
                return result
            }
            flow { registeredClientId = "alice-1" }
            flow { workflowRegistersAfterFileChecks = 2 }
            flow { tokenAccepted = false }
            flow { offline = true }
            flow { createStatus = 404 }
            flow { comments = """[{"user":{"login":"github-actions[bot]"},"body":"Registration refused: leaked $SECRET_TOKEN"}]""" }
            flow { }
        } finally {
            System.setOut(originalOut)
            System.setErr(originalErr)
        }

        assertTrue(texts.size >= 7)
        // The refusal reason repeats the workflow's own words, so it can contain whatever the workflow wrote; the token
        // reaches the library only as the caller's own value, and the test above shows it is never in an address.
        addresses.forEach { assertFalse(it.contains(SECRET_TOKEN), it) }
        texts.filterNot { it.contains("Refused") }.forEach { assertFalse(it.contains(SECRET_TOKEN), it) }
        assertEquals("", out.toString())
        assertEquals("", err.toString())
    }

    @Test
    @Tag("Sanity")
    fun testSanity_anApiPathMustStartWithASlash() {
        assertThrows(IllegalArgumentException::class.java) { api.request("GET", "user", null, null) }
    }
}
