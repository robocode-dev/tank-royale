package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** How a contribution session shows itself, without a window: the choice, what the tray and window offer, and Open Tank Royale. */
class RumbleSessionViewTest {

    @TempDir
    lateinit var dir: Path

    private fun status(state: String = "running", ended: SessionEnded? = null, limit: Int? = 10) = SessionStatus(
        environment = "test", pid = 1, startedAt = "2026-11-23T10:00:00Z", state = state, battleLimit = limit, kept = 3,
        updatedAt = "2026-11-23T10:01:00Z", ended = ended,
    )

    @Test
    @Tag("RCL-155")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL155_UnitPositive_theSessionShowsItselfInTheTrayUnlessChosenOtherwiseAndIsGivenTheCommandThatOpensTankRoyale() {
        assertEquals(SessionShow.TRAY, ContributeChoices().show)
        val arguments = ContributeChoices().arguments(RumbleEnvironments.TEST, 42, listOf("C:\\Tank Royale\\Tank Royale.exe", "--x"))
        assertEquals(listOf("--show", "tray"), arguments.windowed(2).first { it[0] == "--show" })
        assertEquals(listOf("C:\\Tank Royale\\Tank Royale.exe", "--x"), arguments.filter { it.startsWith("--open-command=") }.map { it.removePrefix("--open-command=") })
        assertEquals("window", ContributeChoices(show = SessionShow.WINDOW).arguments(RumbleEnvironments.TEST, null).let { it[it.indexOf("--show") + 1] })

        // The command that starts the desktop program again: the installed launcher as it is, or Java with its settings
        // and the jar, since Windows does not tell a program's own arguments.
        assertEquals(listOf("C:\\Tank Royale\\Tank Royale.exe"), OpenCommand.of("C:\\Tank Royale\\Tank Royale.exe", listOf("-Xmx1g"), Path.of("C:\\Tank Royale\\app\\gui.jar")))
        val java = "C:\\Java\\bin\\java.exe"
        val jar = Path.of("C:\\Code\\gui\\build\\libs\\gui-all.jar")
        assertEquals(listOf(java, "-Drumble.jar=C:\\r.jar", "-jar", jar.toString()), OpenCommand.of(java, listOf("-Drumble.jar=C:\\r.jar"), jar))
    }

    @Test
    @Tag("RCL-155")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL155_UnitNegative_anUnknownChoiceIsNoneOfThemAndNoCommandMeansNoOpenCommandArgument() {
        assertNull(SessionShow.of("balloon"))
        assertNull(SessionShow.of(null))
        assertEquals(SessionShow.NONE, SessionShow.of(" None "))
        assertFalse(ContributeChoices().arguments(RumbleEnvironments.TEST, null).any { it.startsWith("--open-command") })
        // A debugger of the running program is not given to a new one.
        assertEquals(
            listOf("java", "-Xmx1g", "-jar", "gui.jar"),
            OpenCommand.of("java", listOf("-agentlib:jdwp=transport=dt_socket,suspend=y", "-Xmx1g", "-javaagent:x.jar", "-agentpath:/a.so", "-Xrunjdwp:x", "-Xdebug"), Path.of("gui.jar")),
        )
        // Java whose classes are not in a jar, as when run from the build's class folders, or no program at all: no command.
        assertEquals(emptyList<String>(), OpenCommand.of("/usr/bin/java", emptyList(), Path.of("/code/gui/build/classes")))
        assertEquals(emptyList<String>(), OpenCommand.of(null, emptyList(), null))
    }

    @Test
    @Tag("RCL-156")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL156_UnitPositive_theTrayOffersTheSessionsOwnRequestsAsItStands() {
        assertEquals(SessionShow.TRAY, SessionView.decide(SessionShow.TRAY, headless = false, trayOffered = true))
        assertEquals(
            listOf(SessionMenuItem.SHOW_STATUS, SessionMenuItem.PAUSE, SessionMenuItem.STOP, SessionMenuItem.STOP_NOW, SessionMenuItem.OPEN_TANK_ROYALE),
            SessionView.menu(status("running"), canOpenTankRoyale = true),
        )
        assertEquals(SessionMenuItem.RESUME, SessionView.menu(status("paused"), true)[1])
        assertEquals(SessionControlRequest.PAUSE, SessionMenuItem.PAUSE.request)
        assertEquals(SessionControlRequest.RESUME, SessionMenuItem.RESUME.request)
        assertEquals(SessionControlRequest.STOP, SessionMenuItem.STOP.request)
        assertEquals(SessionControlRequest.STOP_NOW, SessionMenuItem.STOP_NOW.request)
        assertEquals("session.headline.limit.running" to listOf<Any>(3, 10), SessionView.headline(status("running")))
        assertEquals("session.headline.running" to listOf<Any>(3), SessionView.headline(status("running", limit = null)))
    }

    @Test
    @Tag("RCL-156")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL156_UnitNegative_withoutATrayTheWindowWithoutAScreenNothingAndAnEndedSessionOffersNoControls() {
        assertEquals(SessionShow.WINDOW, SessionView.decide(SessionShow.TRAY, headless = false, trayOffered = false))
        assertEquals(SessionShow.NONE, SessionView.decide(SessionShow.TRAY, headless = true, trayOffered = false))
        assertEquals(SessionShow.NONE, SessionView.decide(SessionShow.WINDOW, headless = true, trayOffered = true))
        assertEquals(SessionShow.NONE, SessionView.decide(SessionShow.NONE, headless = false, trayOffered = true))
        val ended = status("running", ended = SessionEnded("battles-done", "2026-11-23T11:00:00Z", ""))
        // An ended session offers no controls, only Close, so its icon and window stay until the person closes them.
        assertEquals(listOf(SessionMenuItem.SHOW_STATUS, SessionMenuItem.CLOSE), SessionView.menu(ended, canOpenTankRoyale = false))
        assertEquals(listOf(SessionMenuItem.SHOW_STATUS, SessionMenuItem.OPEN_TANK_ROYALE, SessionMenuItem.CLOSE), SessionView.menu(ended, canOpenTankRoyale = true))
        assertFalse(SessionMenuItem.CLOSE in SessionView.menu(status("running"), true), "a running session is not closed from its icon")
        assertEquals("session.headline.limit.ended", SessionView.headline(ended).first)
        assertFalse(SessionMenuItem.STOP in SessionView.menu(status("stopping"), false), "a session that stops is not asked again")
        assertFalse(SessionMenuItem.STOP_NOW in SessionView.menu(status("cancelling"), false))
    }

    @Test
    @Tag("RCL-157")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL157_UnitPositive_theWindowFollowsTheDesktopProgramsThemeAndLanguage() {
        Files.writeString(dir.resolve("gui.properties"), "theme=light\nlanguage=da\n")
        assertEquals("light", DesktopSettings(dir).theme())
        assertEquals("da", DesktopSettings(dir).language())
        assertEquals(9, SessionView.STATES.size)
    }

    @Test
    @Tag("RCL-157")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL157_UnitNegative_missingOrUnknownSettingsGiveTheDefaults() {
        assertEquals("system", DesktopSettings(dir).theme(), "no settings file")
        assertEquals("en", DesktopSettings(dir).language())
        Files.writeString(dir.resolve("gui.properties"), "theme=purple\nlanguage=klingon\n")
        assertEquals("system", DesktopSettings(dir).theme())
        assertEquals("en", DesktopSettings(dir).language())
    }

    @Test
    @Tag("RCL-158")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL158_UnitPositive_aRunningDesktopProgramTakesTheRequestAndNothingIsStarted() {
        val folder = SessionFolder(dir.resolve("session"))
        val started = mutableListOf<List<String>>()
        // A desktop program takes the request while the session waits.
        val open = OpenTankRoyale(folder, listOf("tank-royale.exe"), start = { started += it }, sleep = { folder.takeDesktopRequest() })
        assertTrue(open.offered)
        assertTrue(open.open())
        assertEquals(emptyList<List<String>>(), started)
        // Only one of two desktop programs takes one request.
        assertTrue(folder.askDesktopToShow())
        assertTrue(folder.takeDesktopRequest())
        assertFalse(folder.takeDesktopRequest())
    }

    @Test
    @Tag("RCL-158")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL158_UnitNegative_withoutADesktopProgramTankRoyaleIsStartedAndWithoutACommandNothingIsOffered() {
        val folder = SessionFolder(dir.resolve("session"))
        val started = mutableListOf<List<String>>()
        val open = OpenTankRoyale(folder, listOf("java", "-jar", "gui.jar"), start = { started += it }, sleep = { }, waitMillis = 1000)
        assertFalse(open.open())
        assertEquals(listOf(listOf("java", "-jar", "gui.jar")), started)
        assertTrue(folder.desktopRequestTaken(), "the request is taken back, so a desktop program that starts later does not take it as new")
        val none = OpenTankRoyale(folder, emptyList(), start = { started += it }, sleep = { })
        assertFalse(none.offered)
        assertFalse(none.open())
        assertEquals(1, started.size)
        assertFalse(SessionMenuItem.OPEN_TANK_ROYALE in SessionView.menu(status(), canOpenTankRoyale = none.offered))
    }
}
