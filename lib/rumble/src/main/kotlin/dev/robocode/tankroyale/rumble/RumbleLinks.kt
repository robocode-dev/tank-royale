package dev.robocode.tankroyale.rumble

/**
 * Addresses of other people's pages that the desktop program may open in the browser for the person. The program only
 * opens them; it never creates an account, fills anything in or sends anything to them.
 */
object RumbleLinks {
    /** GitHub's own page for creating an account. */
    const val CREATE_GITHUB_ACCOUNT = "https://github.com/signup"

    /** The central guide for taking part in the Rumble, which the Rumble menu's Help opens. */
    const val GUIDE = "https://robocode.dev/rumble/"

    /**
     * The page the Rumble menu's Rankings opens: the dashboard of [environment], as the environment list names it, or
     * null when that is not an https page, so the program never opens anything else. A program that a developer has not
     * set to the development Rumble talks to the public Beta, so it never opens a test page.
     */
    fun rankings(environment: RumbleEnvironment): String? = environment.dashboard.takeIf { isHttpsPage(it) }

    /** True only for an https address with a host and nothing before the host. */
    fun isHttpsPage(address: String): Boolean {
        val uri = try {
            java.net.URI(address)
        } catch (e: java.net.URISyntaxException) {
            return false
        }
        return uri.scheme == "https" && !uri.host.isNullOrEmpty() && uri.userInfo == null
    }

    /**
     * True only for an https address on github.com itself, with nothing before the host. The wizard opens the pages the
     * library names for making the copy and installing the App, and refuses anything else.
     */
    fun isGitHubPage(address: String): Boolean {
        val uri = try {
            java.net.URI(address)
        } catch (e: java.net.URISyntaxException) {
            return false
        }
        return uri.scheme == "https" && uri.host == "github.com" && uri.userInfo == null && uri.port == -1
    }
}
