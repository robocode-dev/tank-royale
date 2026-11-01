package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.http.HttpClient

class RumbleSignInTest {

    private val test = RumbleEnvironments.TEST
    private fun signIn(transport: GitHubAuthTransport, clock: MutableClock = MutableClock(START), env: RumbleEnvironment = test) =
        GitHubSignIn(env, transport, clock)

    // RCL-025

    @Test
    @Tag("RCL-025")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL025_UnitPositive_aCodeIsGivenAndEachCheckGivesOneTypedAnswer() {
        val clock = MutableClock(START)
        val transport = FakeTransport(
            deviceCodeAnswer(),
            answer("error" to "authorization_pending"),
            answer("error" to "slow_down", "interval" to "10"),
            answer("error" to "slow_down"),
            tokenAnswer(),
        )
        val flow = signIn(transport, clock)

        val code = flow.start()

        assertEquals("F94F-59CF", code.userCode)
        assertEquals("https://github.com/login/device", code.verificationUri)
        assertEquals(START.plusSeconds(900), code.expiresAt)
        assertEquals(5, code.intervalSeconds)
        assertEquals(GitHubSignIn.DEVICE_CODE_URL, transport.requests[0].first)
        assertEquals(mapOf("client_id" to "Iv23likgKJHOPyEQABJs"), transport.requests[0].second)

        assertEquals(SignInStep.Waiting, flow.check(code))
        assertEquals(SignInStep.SlowDown(10), flow.check(code))
        assertEquals(SignInStep.SlowDown(5 + 5), flow.check(code, intervalSeconds = 5))
        val signedIn = assertInstanceOf(SignInStep.SignedIn::class.java, flow.check(code))
        assertEquals("access-1", signedIn.tokens.accessToken)
        assertEquals("refresh-1", signedIn.tokens.refreshToken)
        assertEquals(START.plusSeconds(28800), signedIn.tokens.expiresAt)
        assertEquals(START.plusSeconds(15811200), signedIn.tokens.refreshTokenExpiresAt)
        assertEquals(GitHubSignIn.TOKEN_URL, transport.requests[1].first)
        assertEquals("urn:ietf:params:oauth:grant-type:device_code", transport.requests[1].second["grant_type"])
        assertEquals("device-code-1", transport.requests[1].second["device_code"])
    }

    @Test
    @Tag("RCL-025")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL025_UnitNegative_expiredDeniedAndFailedAnswersAreTypedAndExplainedInPlainWords() {
        val flow = signIn(
            FakeTransport(
                deviceCodeAnswer(),
                answer("error" to "expired_token"),
                answer("error" to "access_denied"),
                answer("error" to "device_flow_disabled"),
                answer("error" to "incorrect_device_code"),
                answer(status = 500),
                IOException("offline"),
            )
        )
        val code = flow.start()

        assertEquals(SignInStep.Expired, flow.check(code))
        assertEquals(SignInStep.Denied, flow.check(code))
        assertTrue((flow.check(code) as SignInStep.Failed).message.contains("switched off"))
        assertTrue((flow.check(code) as SignInStep.Failed).message.contains("incorrect_device_code"))
        assertTrue((flow.check(code) as SignInStep.Failed).message.contains("HTTP 500"))
        assertTrue((flow.check(code) as SignInStep.Failed).message.contains("cannot be reached"))
    }

    @Test
    @Tag("RCL-025")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL025_UnitNegative_noCodeIsGivenWhenGitHubRefusesOrCannotBeReached() {
        val refused = assertThrows(SignInFailed::class.java) {
            signIn(FakeTransport(answer("error" to "device_flow_disabled", status = 400))).start()
        }
        assertTrue(refused.message!!.contains("device_flow_disabled"))

        val incomplete = assertThrows(SignInFailed::class.java) {
            signIn(FakeTransport(answer("device_code" to "x", "user_code" to "ABCD-1234"))).start()
        }
        assertTrue(incomplete.message!!.contains("unexpected answer"))

        val offline = assertThrows(SignInFailed::class.java) { signIn(FakeTransport(IOException("offline"))).start() }
        assertTrue(offline.message!!.contains("cannot be reached"))
    }

    // RCL-026

    @Test
    @Tag("RCL-026")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL026_UnitPositive_aSignInIsRenewedWithItsRefreshTokenAndNoSecret() {
        val clock = MutableClock(START)
        val transport = FakeTransport(tokenAnswer(access = "access-2", refresh = "refresh-2", expiresIn = 28800))
        val old = GitHubTokens("access-1", "refresh-1", START.minusSeconds(60), START.plusSeconds(1000))

        val renewed = assertInstanceOf(RefreshResult.Renewed::class.java, signIn(transport, clock).refresh(old))

        assertEquals("access-2", renewed.tokens.accessToken)
        assertEquals("refresh-2", renewed.tokens.refreshToken)
        assertEquals(START.plusSeconds(28800), renewed.tokens.expiresAt)
        val (url, form) = transport.requests.single()
        assertEquals(GitHubSignIn.TOKEN_URL, url)
        assertEquals(mapOf("client_id" to "Iv23likgKJHOPyEQABJs", "grant_type" to "refresh_token", "refresh_token" to "refresh-1"), form)
        assertFalse(form.keys.any { it.contains("secret") })
        assertTrue(old.isExpired(START))
        assertFalse(renewed.tokens.isExpired(START))
    }

    @Test
    @Tag("RCL-026")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL026_UnitNegative_aRevokedOrMissingRefreshTokenAsksForANewSignInAndANetworkFailureCanBeRetried() {
        val tokens = GitHubTokens("a", "r", null, null)

        val revoked = signIn(FakeTransport(answer("error" to "bad_refresh_token"))).refresh(tokens)
        assertTrue((revoked as RefreshResult.Refused).message.contains("Sign in again"))

        val transport = FakeTransport()
        val none = signIn(transport).refresh(GitHubTokens("a", null, null, null))
        assertTrue((none as RefreshResult.Refused).message.contains("Sign in again"))
        assertEquals(0, transport.requests.size)

        val offline = signIn(FakeTransport(IOException("offline"))).refresh(tokens)
        assertTrue((offline as RefreshResult.Failed).message.contains("cannot be reached"))

        val strange = signIn(FakeTransport(answer(status = 502))).refresh(tokens)
        assertInstanceOf(RefreshResult.Failed::class.java, strange)
    }

    // RCL-028

    @Test
    @Tag("RCL-028")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL028_UnitPositive_anEnvironmentWithASignInAppUsesItsPublicIdentifierAndNoSecret() {
        val transport = FakeTransport(deviceCodeAnswer(), answer("error" to "authorization_pending"), tokenAnswer())
        val flow = signIn(transport)
        val code = flow.start()
        flow.check(code)
        flow.refresh(GitHubTokens("a", "r", null, null))

        assertEquals("Iv23likgKJHOPyEQABJs", RumbleEnvironments.TEST.signInClientId)
        assertTrue(transport.requests.all { it.second["client_id"] == "Iv23likgKJHOPyEQABJs" })
        assertTrue(transport.requests.none { request -> request.second.keys.any { it.contains("secret") } })
    }

    @Test
    @Tag("RCL-028")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL028_UnitNegative_anEnvironmentWithoutASignInAppRefusesBeforeContactingGitHub() {
        val transport = FakeTransport()
        val flow = signIn(transport, env = RumbleEnvironments.PUBLIC)

        assertNull(RumbleEnvironments.PUBLIC.signInClientId)
        val refusal = assertThrows(SignInUnavailable::class.java) { flow.start() }
        assertTrue(refusal.message!!.contains("not available"))
        assertTrue(refusal.message!!.contains("Public Beta"))
        assertInstanceOf(RefreshResult.Failed::class.java, flow.refresh(GitHubTokens("a", "r", null, null)))
        assertEquals(0, transport.requests.size)
    }

    // RCL-029

    private fun waiting(vararg answers: Any, expiresIn: Int = 900, interval: Int = 5, cancelAfterChecks: Int = Int.MAX_VALUE):
        Triple<SignInStep, List<Long>, FakeTransport> {
        val clock = MutableClock(START)
        val transport = FakeTransport(deviceCodeAnswer(expiresIn = expiresIn, interval = interval), *answers)
        val flow = signIn(transport, clock)
        val code = flow.start()
        val sleeps = mutableListOf<Long>()
        val result = flow.awaitSignIn(
            code,
            sleep = { millis -> sleeps += millis; clock.advanceMillis(millis) },
            cancelled = { transport.requests.size - 1 >= cancelAfterChecks },
        )
        return Triple(result, sleeps, transport)
    }

    @Test
    @Tag("RCL-029")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL029_UnitPositive_theLoopWaitsTheIntervalSlowsDownWhenAskedAndReturnsWhenSignedIn() {
        val (result, sleeps, transport) = waiting(
            answer("error" to "authorization_pending"),
            answer("error" to "slow_down", "interval" to "10"),
            answer("error" to "authorization_pending"),
            tokenAnswer(),
        )

        assertInstanceOf(SignInStep.SignedIn::class.java, result)
        assertEquals(listOf(5000L, 5000L, 10000L, 10000L), sleeps)
        assertEquals(5, transport.requests.size)
    }

    @Test
    @Tag("RCL-029")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL029_UnitPositive_theLoopReturnsAsSoonAsAccessIsDenied() {
        val (result, sleeps, _) = waiting(answer("error" to "access_denied"))

        assertEquals(SignInStep.Denied, result)
        assertEquals(listOf(5000L), sleeps)
    }

    @Test
    @Tag("RCL-029")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL029_UnitNegative_theLoopStopsWhenTheCodeExpiresOrTheCallerCancels() {
        val pending = answer("error" to "authorization_pending")
        val (expired, expiredSleeps, expiredTransport) = waiting(pending, pending, pending, expiresIn = 12)
        assertEquals(SignInStep.Expired, expired)
        assertEquals(listOf(5000L, 5000L, 5000L), expiredSleeps)
        assertEquals(1 + 2, expiredTransport.requests.size)

        val (cancelled, cancelSleeps, cancelTransport) = waiting(pending, pending, pending, cancelAfterChecks = 1)
        assertEquals(SignInStep.Cancelled, cancelled)
        assertEquals(listOf(5000L), cancelSleeps)
        assertEquals(2, cancelTransport.requests.size)

        val (neverStarted, noSleeps, _) = waiting(cancelAfterChecks = 0)
        assertEquals(SignInStep.Cancelled, neverStarted)
        assertEquals(emptyList<Long>(), noSleeps)
    }

    // Not an acceptance criterion: the shape of the real requests

    @Test
    @Tag("Sanity")
    fun testSanity_theRealRequestsAreFormEncodedPostsToGitHubWithAJsonAnswerRequested() {
        val transport = HttpGitHubAuthTransport(HttpClient.newHttpClient())

        val request = transport.buildRequest(GitHubSignIn.TOKEN_URL, mapOf("client_id" to "a b", "grant_type" to "x&y"))

        assertEquals("POST", request.method())
        assertEquals("https://github.com/login/oauth/access_token", request.uri().toString())
        assertEquals("application/json", request.headers().firstValue("Accept").get())
        assertEquals("application/x-www-form-urlencoded", request.headers().firstValue("Content-Type").get())
        assertEquals(
            "client_id=a+b&grant_type=x%26y".length.toLong(),
            request.bodyPublisher().get().contentLength(),
        )
    }
}
