package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** How a contribution session shows itself while it runs, as the person chose it. */
enum class SessionShow(val option: String) {
    /** An icon in the operating system's tray, or the session's window where there is no tray. */
    TRAY("tray"),

    /** A small window of its own. */
    WINDOW("window"),

    /** Nothing: the session is followed on the Contribute battles screen or with the command line. */
    NONE("none");

    companion object {
        /** The choice an option or a setting names; anything else is null. */
        fun of(text: String?): SessionShow? = entries.firstOrNull { it.option == text?.trim()?.lowercase() }
    }
}

/** An item of the session's menu and the request it sends, if any. */
enum class SessionMenuItem(val request: SessionControlRequest?) {
    SHOW_STATUS(null),
    PAUSE(SessionControlRequest.PAUSE),
    RESUME(SessionControlRequest.RESUME),
    STOP(SessionControlRequest.STOP),
    STOP_NOW(SessionControlRequest.STOP_NOW),
    OPEN_TANK_ROYALE(null),

    /** Closes the icon and the window of a session that ended, which lets the session program end. */
    CLOSE(null),
}

/** What the tray and the window of a session show and offer, from its status, without a window. */
object SessionView {

    /**
     * What a session that chose [show] actually shows: the tray only where the operating system offers one, else the
     * window; and nothing on a computer without a screen.
     */
    fun decide(show: SessionShow, headless: Boolean, trayOffered: Boolean): SessionShow = when {
        show == SessionShow.NONE || headless -> SessionShow.NONE
        show == SessionShow.TRAY && !trayOffered -> SessionShow.WINDOW
        else -> show
    }

    /**
     * The menu items for a session in [status], in order: the status, pause or resume as the session stands, stop and
     * stop now while it can still be stopped, Open Tank Royale only when the session knows how to open it, and Close
     * once it ended: the icon and the window stay until the person closes them, so the end is never missed.
     */
    fun menu(status: SessionStatus, canOpenTankRoyale: Boolean): List<SessionMenuItem> = buildList {
        add(SessionMenuItem.SHOW_STATUS)
        if (status.ended == null) {
            when (status.state) {
                "paused", "pausing" -> add(SessionMenuItem.RESUME)
                "running", "waiting", "starting" -> add(SessionMenuItem.PAUSE)
            }
            if (status.state !in setOf("stopping", "cancelling", "sending")) add(SessionMenuItem.STOP)
            if (status.state != "cancelling") add(SessionMenuItem.STOP_NOW)
        }
        if (canOpenTankRoyale) add(SessionMenuItem.OPEN_TANK_ROYALE)
        if (status.ended != null) add(SessionMenuItem.CLOSE)
    }

    /**
     * The key and arguments of the line that says where the session stands, for the tray's tip and the window's
     * headline: its state, and the battles kept out of the limit, or kept so far when it runs until stopped.
     */
    fun headline(status: SessionStatus): Pair<String, List<Any>> {
        val state = if (status.ended != null) "ended" else status.state
        return if (status.battleLimit != null) "session.headline.limit.$state" to listOf(status.kept, status.battleLimit)
        else "session.headline.$state" to listOf(status.kept)
    }

    /** The states a session can be in, for the test that each has its texts. */
    val STATES = listOf("starting", "running", "waiting", "pausing", "paused", "stopping", "cancelling", "sending", "ended")
}

/** The command that starts the desktop program again, which a session it starts is given for Open Tank Royale. */
object OpenCommand {
    private val AGENTS = listOf("-agentlib", "-agentpath", "-javaagent", "-Xrunjdwp", "-Xdebug")

    /**
     * The command for a program started as [command], the program file, with the Java settings [javaArguments], and
     * whose classes come from [codeSource]. An installed launcher is started as it is. Java started with a jar is
     * started again with its settings and that jar, since the operating system may not tell a program's own arguments
     * (Windows does not). Anything else gives no command, so Open Tank Royale is not offered.
     */
    fun of(command: String?, javaArguments: List<String>, codeSource: Path?): List<String> {
        if (command.isNullOrBlank()) return emptyList()
        // (A command whose program is not Java is an installed launcher, started as it is.)
        val name = Path.of(command).fileName?.toString()?.lowercase().orEmpty()
        val java = name == "java" || name == "java.exe" || name == "javaw" || name == "javaw.exe"
        if (!java) return listOf(command)
        val jar = codeSource?.takeIf { it.fileName?.toString()?.lowercase()?.endsWith(".jar") == true } ?: return emptyList()
        // A debugger or agent of the running program is not given to a new one, which could not bind its port.
        val settings = javaArguments.filterNot { arg -> AGENTS.any { arg.startsWith(it) } }
        return listOf(command) + settings + listOf("-jar", jar.toString())
    }
}

/**
 * Opens Tank Royale for a session: it asks a desktop program that follows the session to come to the front, and when
 * none takes the request within [waitMillis], it withdraws the request and starts Tank Royale with [command], the one
 * the desktop program gave the session when it started it. Without a command it offers nothing.
 */
class OpenTankRoyale(
    private val folder: SessionFolder,
    private val command: List<String>,
    private val start: (List<String>) -> Unit = { ProcessBuilder(it).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start() },
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
    private val waitMillis: Long = 3000,
) {
    /** Whether the session can open Tank Royale at all. */
    val offered: Boolean get() = command.isNotEmpty()

    /** Asks, waits and starts as described; returns whether a running desktop program took the request. Never on the event thread. */
    fun open(): Boolean {
        if (!offered) return false
        if (folder.askDesktopToShow()) {
            var waited = 0L
            while (waited < waitMillis) {
                if (folder.desktopRequestTaken()) return true
                sleep(STEP_MILLIS)
                waited += STEP_MILLIS
            }
            // Nobody took it; it is taken back, so a desktop program that starts later does not take it as new.
            if (!folder.takeDesktopRequest()) return true
        }
        start(command)
        return false
    }

    private companion object {
        const val STEP_MILLIS = 200L
    }
}

/**
 * The desktop program's own settings that a session's window follows: its theme and its language, read from its
 * settings file in the Tank Royale user folder when the window opens. A file that cannot be read gives the defaults.
 */
class DesktopSettings(private val userData: Path) {
    private fun read(): Properties = Properties().apply {
        try {
            Files.newBufferedReader(userData.resolve(FILE)).use { load(it) }
        } catch (e: IOException) {
            // Missing or unreadable: the defaults.
        } catch (e: IllegalArgumentException) {
            // A malformed file: the defaults.
        }
    }

    /** `light`, `dark` or `system`, as the desktop program keeps it. */
    fun theme(): String = read().getProperty("theme", "system").lowercase().takeIf { it in setOf("light", "dark", "system") } ?: "system"

    /** The language code, `en`, `da`, `es` or `ca`. */
    fun language(): String = read().getProperty("language", "en").lowercase().takeIf { it in setOf("en", "da", "es", "ca") } ?: "en"

    private companion object {
        const val FILE = "gui.properties"
    }
}
