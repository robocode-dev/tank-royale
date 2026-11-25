package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.rumble.CloseAction
import dev.robocode.tankroyale.rumble.CloseChoice
import dev.robocode.tankroyale.rumble.ContributeController
import dev.robocode.tankroyale.rumble.ContributeState
import dev.robocode.tankroyale.rumble.ContributeTexts
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.LibraryContributePort
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.RumbleProgram
import dev.robocode.tankroyale.rumble.SessionControlRequest
import dev.robocode.tankroyale.rumble.SessionFolder
import dev.robocode.tankroyale.rumble.SessionShow
import dev.robocode.tankroyale.rumble.SessionStatus
import java.awt.BorderLayout
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.swing.JOptionPane
import javax.swing.JWindow
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.UIManager

/**
 * The desktop program's side of a contribution session: one [ContributeController] for the whole program, which the
 * Contribute battles screen shows and which keeps following a session while the screen is closed. It looks for a
 * running session, or one that ended while the program was closed, when the program starts; while the screen is closed
 * it gives a short notice when a session's battles are done; and it asks what to do with the session when the program closes. Everything here runs
 * on the event thread.
 */
object RumbleContribute {
    private var environment: RumbleEnvironment? = null
    private var controller: ContributeController? = null
    private var dialog: RumbleContributeDialog? = null
    private var timer: Timer? = null
    private var firstLook = true

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /**
     * Looks for a session when the program starts, and follows it only when there is one, running or ended, so a person who
     * never used the Rumble gets no network request and no timer. Never waits for anything: the look is a small file read
     * away from the event thread.
     */
    fun startUp() {
        val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE)) as? EnvironmentChoice.Chosen ?: return
        val folder = SessionFolder.of(choice.environment, UserDataDirectory.get().toPath())
        Thread({
            val found = runCatching { folder.look() }.getOrNull() ?: return@Thread
            if (found.worthFollowing) SwingUtilities.invokeLater { controllerFor(choice.environment).start() }
        }, "rumble-session-startup").apply { isDaemon = true }.start()
    }

    /** Opens the Contribute battles screen. */
    fun open() {
        when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
            is EnvironmentChoice.Unknown -> JOptionPane.showMessageDialog(MainFrame, text("rumble.account.unknown_environment", choice.name))
            is EnvironmentChoice.Chosen -> {
                val controller = controllerFor(choice.environment)
                val shown = dialog ?: RumbleContributeDialog(choice.environment.displayName, controller).also { dialog = it }
                shown.render(controller.state)
                shown.isVisible = true
                shown.toFront()
                controller.start()
                // What the screen shows of an ended session has been seen.
                (controller.state as? ContributeState.Choosing)?.last?.let { markSeen(it) }
            }
        }
    }

    /**
     * Called when the program closes. When a session it owns runs, it asks the first time whether to keep contributing
     * and keeps the answer; to keep contributing it releases the session before it closes, and otherwise the session
     * stops after its battle because its owner is gone.
     */
    fun beforeExit() {
        val controller = controller ?: return
        val action = controller.closeAction(ConfigSettings.rumbleCloseChoice)
        val release = when (action) {
            CloseAction.NOTHING, CloseAction.LET_STOP -> false
            CloseAction.RELEASE -> true
            CloseAction.ASK -> {
                val keep = text("rumble.contribute.close_ask.keep")
                val stop = text("rumble.contribute.close_ask.stop")
                val answer = JOptionPane.showOptionDialog(
                    MainFrame, text("rumble.contribute.close_ask.message"), text("rumble.contribute.close_ask.title"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, arrayOf(keep, stop), keep,
                )
                // Closing the question without an answer keeps the session, so nothing is lost; the question comes again.
                if (answer == 1) ConfigSettings.rumbleCloseChoice = CloseChoice.STOP
                if (answer == 0) ConfigSettings.rumbleCloseChoice = CloseChoice.KEEP
                answer != 1
            }
        }
        if (release) {
            // Asked here and now, not on another thread, since the program ends right after. A session that is still
            // starting cannot be asked yet, so the program waits a little for it to take its folder.
            val folder = environment?.let { SessionFolder.of(it, UserDataDirectory.get().toPath()) } ?: return
            val until = System.nanoTime() + RELEASE_WAIT_NANOS
            while (runCatching { folder.ask(SessionControlRequest.RELEASE) }.getOrDefault(false).not() &&
                startingProcessAlive(controller.state) && System.nanoTime() < until
            ) {
                Thread.sleep(250)
            }
        }
    }

    /** Whether [state] is a session still starting whose process runs, so it can still take its folder. */
    private fun startingProcessAlive(state: ContributeState): Boolean =
        (state as? ContributeState.Starting)?.let { ProcessHandle.of(it.pid).map { p -> p.isAlive }.orElse(false) } ?: false

    private fun controllerFor(environment: RumbleEnvironment): ContributeController {
        controller?.let { return it }
        this.environment = environment
        val background = Executors.newCachedThreadPool { runnable -> Thread(runnable, "rumble-contribute").apply { isDaemon = true } }
        val post = Executor { SwingUtilities.invokeLater(it) }
        val api = HttpGitHubApi()
        val userData = UserDataDirectory.get().toPath()
        val port = LibraryContributePort(environment, userData, api, RumbleProgram(userData, Version.version, api))
        val made = ContributeController(port, environment, background, post, onState = ::changed, onDone = ::done)
        controller = made
        made.setShow(SessionShow.of(ConfigSettings.rumbleSessionShow) ?: SessionShow.TRAY)
        val folder = SessionFolder.of(environment, userData)
        // The session is followed every second while one runs or the screen is open; a look is a small file read.
        timer = Timer(1000) {
            val state = made.state
            if (dialog?.isVisible == true || state is ContributeState.Running || state is ContributeState.Starting) made.refresh()
            // The session's Open Tank Royale: the program that takes the request comes to the front with the screen.
            if (folder.takeDesktopRequest()) comeToFront()
        }.apply { start() }
        return made
    }

    /** Brings the main window to the front, also from the taskbar, and shows the Contribute battles screen. */
    private fun comeToFront() {
        if (MainFrame.extendedState and java.awt.Frame.ICONIFIED != 0) MainFrame.extendedState = MainFrame.extendedState and java.awt.Frame.ICONIFIED.inv()
        MainFrame.isVisible = true
        raise(MainFrame)
        open()
        dialog?.let { raise(it) }
    }

    /**
     * Brings [window] in front of other programs. Windows ignores a plain toFront from a program that is not the one in
     * use, so the window is put on top of all others for a moment, which Windows lets through, and then let go again.
     */
    private fun raise(window: java.awt.Window) {
        window.isAlwaysOnTop = true
        window.toFront()
        window.requestFocus()
        window.isAlwaysOnTop = false
    }

    private fun changed(state: ContributeState) {
        dialog?.render(state)
        if (firstLook && state != ContributeState.Looking) {
            firstLook = false
            atStart(state)
        }
    }

    /** What the program says when it starts: that a session runs, or how the one that ended meanwhile went. */
    private fun atStart(state: ContributeState) {
        when (state) {
            is ContributeState.Running -> notice(text("rumble.contribute.notice.running"))
            is ContributeState.Choosing -> {
                val last = state.last ?: return
                if (endOf(last) == ConfigSettings.rumbleSeenSessionEnd) return
                markSeen(last)
                notice(if (state.lastUnexpected) text("rumble.contribute.notice.unexpected") else summary(last))
            }
            else -> Unit
        }
    }

    private fun done(status: SessionStatus, unexpected: Boolean) {
        markSeen(status)
        notice(if (unexpected) text("rumble.contribute.notice.unexpected") else summary(status))
    }

    private fun summary(status: SessionStatus): String {
        val outcome = status.ended?.outcome.orEmpty()
        return text("rumble.contribute.notice.ended", text(ContributeTexts.forOutcome(outcome)), status.kept)
    }

    private fun endOf(status: SessionStatus) = status.ended?.at ?: "${status.pid}@${status.startedAt}"

    private fun markSeen(status: SessionStatus) {
        ConfigSettings.rumbleSeenSessionEnd = endOf(status)
    }

    /** A short notice at the lower right of the main window that goes away by itself; a click opens the screen. */
    private fun notice(message: String) {
        // The open screen shows the same already, and a notice would only lie behind it.
        if (dialog?.isVisible == true) return
        val window = JWindow(MainFrame as Window)
        val panel = RumbleCard(BorderLayout(), CardTone.BLUE, padding = 12).apply {
            add(wrappedText(message).apply { columns = 30 }, BorderLayout.CENTER)
        }
        val click = object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                window.dispose()
                open()
            }
        }
        panel.addMouseListener(click)
        panel.components.forEach { it.addMouseListener(click) }
        // The card's rounded corners show the window behind it, so that has the theme's background.
        window.background = UIManager.getColor("Panel.background")
        window.contentPane = panel
        window.pack()
        val frame = MainFrame.bounds
        window.setLocation(frame.x + frame.width - window.width - 24, frame.y + frame.height - window.height - 48)
        window.isVisible = true
        Timer(NOTICE_MILLIS) { window.dispose() }.apply { isRepeats = false; start() }
    }

    private const val NOTICE_MILLIS = 10_000
    private const val RELEASE_WAIT_NANOS = 20_000_000_000L
}
