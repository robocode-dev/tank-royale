package dev.robocode.tankroyale.rumble

import java.time.Duration
import java.time.Instant

/**
 * The tokens of a GitHub sign-in. They are never shown: the text form hides them, and the library never writes them
 * to disk or to a log. Only the host side of Rumble may hold them.
 */
class GitHubTokens(
    val accessToken: String,
    /** Renews the sign-in without any secret; null when GitHub gave none. */
    val refreshToken: String?,
    /** When the access token stops working; null when it does not expire. */
    val expiresAt: Instant?,
    /** When the refresh token stops working; null when unknown or it does not expire. */
    val refreshTokenExpiresAt: Instant?,
) {
    /** True when the access token has expired, or will within [margin]. */
    fun isExpired(now: Instant, margin: Duration = Duration.ofMinutes(1)): Boolean =
        expiresAt != null && !now.isBefore(expiresAt.minus(margin))

    override fun toString(): String =
        "GitHubTokens(access=hidden, refresh=${if (refreshToken != null) "hidden" else "none"}, expiresAt=$expiresAt)"
}

/** The code a person enters on GitHub's page to approve a sign-in. */
class DeviceCode internal constructor(
    /** The short code to show the person, for example `F94F-59CF`. */
    val userCode: String,
    /** The address the person opens to enter the code. */
    val verificationUri: String,
    /** When the code stops working. */
    val expiresAt: Instant,
    /** The fewest seconds GitHub wants between checks. */
    val intervalSeconds: Int,
    internal val deviceCode: String,
) {
    override fun toString(): String = "DeviceCode(userCode=$userCode, verificationUri=$verificationUri, expiresAt=$expiresAt)"
}
