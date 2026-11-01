package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** A clock the tests move by hand. */
class MutableClock(var now: Instant) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId?): Clock = this
    override fun instant(): Instant = now
    fun advanceMillis(millis: Long) {
        now = now.plusMillis(millis)
    }
}

/** Answers sign-in requests from a prepared list and records what was asked. An [IOException] answer simulates being offline. */
class FakeTransport(vararg answers: Any) : GitHubAuthTransport {
    private val queue = ArrayDeque(answers.toList())
    val requests = mutableListOf<Pair<String, Map<String, String>>>()

    override fun postForm(url: String, form: Map<String, String>): AuthResponse {
        requests += url to form
        check(queue.isNotEmpty()) { "No prepared answer for request ${requests.size}" }
        return when (val next = queue.removeFirst()) {
            is IOException -> throw next
            is AuthResponse -> next
            else -> error("Unexpected prepared answer $next")
        }
    }
}

fun answer(vararg fields: Pair<String, String>, status: Int = 200) = AuthResponse(status, fields.toMap())

fun deviceCodeAnswer(deviceCode: String = "device-code-1", userCode: String = "F94F-59CF", expiresIn: Int = 900, interval: Int = 5) =
    answer(
        "device_code" to deviceCode, "user_code" to userCode, "verification_uri" to "https://github.com/login/device",
        "expires_in" to expiresIn.toString(), "interval" to interval.toString(),
    )

fun tokenAnswer(access: String = "access-1", refresh: String? = "refresh-1", expiresIn: Int = 28800) =
    answer(
        *listOfNotNull(
            "access_token" to access, "token_type" to "bearer", "expires_in" to expiresIn.toString(),
            refresh?.let { "refresh_token" to it }, refresh?.let { "refresh_token_expires_in" to "15811200" },
        ).toTypedArray()
    )

val START: Instant = Instant.parse("2026-11-01T10:00:00Z")
