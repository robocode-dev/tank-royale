package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.look.RobocodeLook
import dev.robocode.tankroyale.rumble.DesktopSettings
import dev.robocode.tankroyale.rumble.OpenTankRoyale
import dev.robocode.tankroyale.rumble.SessionFolder
import dev.robocode.tankroyale.rumble.SessionMenuItem
import dev.robocode.tankroyale.rumble.SessionShow
import dev.robocode.tankroyale.rumble.SessionStatus
import dev.robocode.tankroyale.rumble.SessionView
import java.awt.AWTException
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.GridLayout
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.CountDownLatch
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.WindowConstants

/** The texts of a session's tray icon and window in [language], from the session program's own resource files. */
internal class SessionTexts(language: String) {
    private val bundle = ResourceBundle.getBundle(
        "dev.robocode.tankroyale.rumble.cli.SessionView", Locale.forLanguageTag(language),
        // English is the base file; the computer's own language never stands in for the one chosen.
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES),
    )

    fun get(key: String, vararg arguments: Any): String = String.format(bundle.getString(key), *arguments)

    fun headline(status: SessionStatus): String = SessionView.headline(status).let { (key, arguments) -> get(key, *arguments.toTypedArray()) }

    fun menu(item: SessionMenuItem): String = get("session.menu.${item.name.lowercase()}")
}

/**
 * Shows a running contribution session in the operating system's tray or in a small window of its own, as the person
 * chose, with its controls, and gives a notice when the battles are done. What it shows and offers is decided by
 * [SessionView]; this class only shows it. Every control sends the session's own request through its [folder], the same
 * as the command line's session commands. It is a convenience: a session works the same when it cannot show anything.
 */
internal class SessionDisplay(
    private val show: SessionShow,
    private val status: () -> SessionStatus?,
    private val folder: SessionFolder,
    private val open: OpenTankRoyale,
    private val settings: DesktopSettings,
) {
    private var shown = SessionShow.NONE
    private lateinit var texts: SessionTexts
    private var tray: TrayIcon? = null
    private var window: JFrame? = null
    private var timer: Timer? = null
    private var menuShown: List<SessionMenuItem> = emptyList()
    private var last: SessionStatus? = null
    private var filled: List<Any?>? = null
    private val closed = CountDownLatch(1)

    private companion object {
        /** The window is never narrower than this, so it does not jump in width as its texts change. */
        const val MIN_WIDTH = 460
    }

    @Volatile
    private var over = false

    /** Shows the session as chosen, or nothing where it cannot. Called once, when the session starts. */
    fun start() {
        // A session that shows nothing never loads the windowing toolkit, so a terminal session stays a plain program.
        if (show == SessionShow.NONE) return
        val trayOffered = !GraphicsEnvironment.isHeadless() && runCatching { SystemTray.isSupported() }.getOrDefault(false)
        val decided = SessionView.decide(show, GraphicsEnvironment.isHeadless(), trayOffered)
        if (decided == SessionShow.NONE) return
        runCatching { SwingUtilities.invokeAndWait { begin(decided) } }
    }

    private fun begin(decided: SessionShow) {
        texts = SessionTexts(settings.language())
        RobocodeLook.setup(settings.theme())
        shown = decided
        if (decided == SessionShow.TRAY && !addTray()) shown = SessionShow.WINDOW
        if (shown == SessionShow.WINDOW) openWindow()
        timer = Timer(1000) { refresh() }.apply { start() }
        refresh()
    }

    private fun addTray(): Boolean {
        val icon = TrayIcon(RobocodeLook.tankImage, texts.get("session.title"), PopupMenu()).apply {
            isImageAutoSize = true
            // A click on the icon shows the status window.
            addActionListener { openWindow() }
        }
        return try {
            SystemTray.getSystemTray().add(icon)
            tray = icon
            true
        } catch (e: AWTException) {
            false
        }
    }

    /** Brings the status up to date in the tray's tip and menu and in the window. On the event thread. */
    private fun refresh(final: SessionStatus? = null) {
        val now = final ?: status() ?: return
        // Once ended, the icon's menu shows Close; the final status is kept for the window's later repaints.
        if (final != null) last = final
        val headline = texts.headline(now)
        tray?.let { icon ->
            icon.toolTip = "${texts.get("session.title")}\n$headline"
            val items = SessionView.menu(now, open.offered)
            if (items != menuShown) {
                menuShown = items
                icon.popupMenu = PopupMenu().apply { items.forEach { item -> add(MenuItem(texts.menu(item)).apply { addActionListener { act(item) } }) } }
            }
        }
        window?.let { fill(it, now) }
    }

    /** Does what a menu item or a button asks; the request is written away from the event thread. */
    private fun act(item: SessionMenuItem) {
        when (item) {
            SessionMenuItem.SHOW_STATUS -> openWindow()
            SessionMenuItem.OPEN_TANK_ROYALE -> Thread({ runCatching { open.open() } }, "rumble-session-open").apply { isDaemon = true }.start()
            SessionMenuItem.CLOSE -> close()
            else -> item.request?.let { request -> Thread({ runCatching { folder.ask(request) } }, "rumble-session-ask").apply { isDaemon = true }.start() }
        }
    }

    private fun openWindow() {
        val shown = window ?: JFrame(texts.get("session.title")).apply {
            iconImage = RobocodeLook.tankImage
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            addWindowListener(object : java.awt.event.WindowAdapter() {
                override fun windowClosed(e: java.awt.event.WindowEvent) {
                    window = null
                    // Closing the window of a session that ended closes all of it; while it runs, it is only hidden and
                    // comes back at the end, so the end is never missed.
                    if (over) close()
                }
            })
        }.also { window = it }
        if (!shown.isVisible) shown.setLocationByPlatform(true)
        // Filled before it shows, so its size comes from its content in the fonts of this computer.
        (last ?: status())?.let { fill(shown, it) }
        shown.isVisible = true
        // Windows ignores a plain toFront from a program in the background; on top of all others for a moment, it shows.
        shown.isAlwaysOnTop = true
        shown.toFront()
        shown.isAlwaysOnTop = false
    }

    /** The window's content: where the session stands, the running battle, the counts, and the controls as buttons. */
    private fun fill(frame: JFrame, now: SessionStatus) {
        // Rebuilt only when what it shows changed, so a click or the keyboard focus is never lost to a rebuild.
        val shownNow = listOf(texts.headline(now), now.battle, now.notKept, now.results, SessionView.menu(now, open.offered))
        if (shownNow == filled && frame.contentPane.componentCount > 0) return
        filled = shownNow
        val lines = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(12, 12, 6, 12)
            add(JLabel(texts.headline(now)).apply { font = font.deriveFont(Font.BOLD) })
            add(javax.swing.Box.createVerticalStrut(6))
            val battle = now.battle
            add(JLabel(if (battle != null) texts.get("session.window.battle", battle.gameType, battle.participants.joinToString(", ")) else texts.get("session.window.no_battle")))
            add(JLabel(texts.get("session.window.not_kept", now.notKept)))
            val r = now.results
            add(JLabel(texts.get("session.window.results", r.waiting, r.sent, r.accepted, r.rejected)))
        }
        // Two buttons a row, so their texts are never cut short.
        val buttons = JPanel(GridLayout(0, 2, 6, 6)).apply {
            border = BorderFactory.createEmptyBorder(6, 12, 12, 12)
            SessionView.menu(now, open.offered).filter { it != SessionMenuItem.SHOW_STATUS }.forEach { item ->
                add(JButton(texts.menu(item)).apply { addActionListener { act(item) } })
            }
        }
        frame.contentPane.removeAll()
        frame.contentPane.add(lines, BorderLayout.CENTER)
        frame.contentPane.add(buttons, BorderLayout.SOUTH)
        frame.contentPane.revalidate()
        frame.contentPane.repaint()
        // As wide as the texts need, and as high as the content, which changes as the session goes on: pack() counts the window's frame, whose size is known only to the operating system, and gives every button
        // the width its text needs; the window never gets narrower, so it does not jump as its texts change.
        val before = frame.width
        frame.pack()
        frame.setSize(maxOf(before, frame.width, MIN_WIDTH), frame.height)
    }

    /** Takes away the icon and the window, which lets the session program end. On the event thread. */
    private fun close() {
        tray?.let { runCatching { SystemTray.getSystemTray().remove(it) } }
        tray = null
        window?.dispose()
        window = null
        filled = null
        closed.countDown()
    }

    /**
     * The session ended with [final]: the tray gives its notice, and the icon and any open window show the end until
     * the person closes them, so the end is never missed; the session program waits until then. It already let go of
     * its session folder, so a new session can start meanwhile. Called on the session's own thread.
     */
    fun ended(final: SessionStatus) {
        if (shown == SessionShow.NONE) return
        var waiting = false
        runCatching {
            SwingUtilities.invokeAndWait {
                over = true
                timer?.stop()
                refresh(final)
                tray?.displayMessage(texts.get("session.notice.title"), texts.get("session.notice.text", final.kept, final.results.accepted), TrayIcon.MessageType.INFO)
                // Windows often does not show a Java program's notice, so the end is also shown in the session's own
                // window, which stays until the person closes it.
                openWindow()
                waiting = tray != null || window != null
            }
        }
        if (waiting) runCatching { closed.await() }
    }
}
