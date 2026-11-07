package dev.robocode.tankroyale.rumble

/**
 * Addresses of other people's pages that the desktop program may open in the browser for the person. The program only
 * opens them; it never creates an account, fills anything in or sends anything to them.
 */
object RumbleLinks {
    /** GitHub's own page for creating an account. */
    const val CREATE_GITHUB_ACCOUNT = "https://github.com/signup"

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
