package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.rumble.DownloadTexts
import dev.robocode.tankroyale.rumble.PlatformCheck
import dev.robocode.tankroyale.rumble.PlatformDownloads
import dev.robocode.tankroyale.rumble.PlatformReason
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

/**
 * Says what this computer is missing for one platform, when the person ticks a platform that did not pass its check.
 * It offers the platform's official download page, setting up Python when only the environment is missing, and Show
 * anyway, which ticks the platform and installs nothing. Closing it leaves the platform unticked. Nothing here runs a
 * command itself: the screen does that when the person chooses to set up Python.
 */
internal class RumblePlatformDialog(
    owner: Window,
    check: PlatformCheck,
    commands: List<List<String>>,
    open: (String) -> Unit,
    setUp: () -> Unit,
    showAnyway: () -> Unit,
) : RcDialog(owner, "rumble_platform_dialog") {

    init {
        val content = WindowWidthPanel()
        fun put(component: Component) = content.add(component, "growx, wmin 10")
        val unknown = text("rumble.download.unknown")
        put(wrappedText(text(DownloadTexts.forPlatformExplanation(check.reason), text(DownloadTexts.forPlatformTool(check.platform)),
            check.platform.catalogName, check.found ?: unknown, check.needed ?: unknown)))
        if (check.reason == PlatformReason.NO_VENV) put(wrappedText(text("rumble.download.platform.venv_package")))
        val offerSetUp = check.reason == PlatformReason.NO_ENVIRONMENT && commands.isNotEmpty()
        if (offerSetUp) {
            put(wrappedText(text("rumble.download.platform.commands")))
            // One line per command, never broken inside a path, and quoted as it would be typed, so a folder with a
            // space in its name looks as it is run. A long line scrolls sideways.
            val lines = JTextArea(commands.joinToString("\n") { command -> command.joinToString(" ") { quoted(it) } }).apply {
                isEditable = false
                lineWrap = false
                font = Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").size)
            }
            put(JScrollPane(lines, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS).apply {
                // Room for every line and the sideways scroll bar, so no line hides behind it.
                val tall = lines.preferredSize.height + horizontalScrollBar.preferredSize.height + insets.top + insets.bottom
                preferredSize = Dimension(10, tall)
                minimumSize = Dimension(10, tall)
            })
        }

        val buttons = JPanel(GridLayout(0, 1, 0, 6)).apply { border = EmptyBorder(6, 12, 12, 12) }
        fun button(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }.also { buttons.add(it) }
        val first = if (offerSetUp) {
            button("rumble.download.button.set_up") { dispose(); setUp() }
        } else {
            button("rumble.download.button.download_page") { open(PlatformDownloads.page(check.platform)) }
        }
        button("rumble.download.button.show_anyway") { dispose(); showAnyway() }
        button("rumble.download.button.close") { dispose() }

        val scroll = JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }
        contentPane.add(scroll, BorderLayout.CENTER)
        contentPane.add(buttons, BorderLayout.SOUTH)
        fitToContent(scroll, content)
        setLocationRelativeTo(owner)
        isModal = true
        // The first button has the focus, so no caret blinks in the text; the text can still be selected and copied.
        addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent) {
                first.requestFocusInWindow()
            }
        })
    }

    /** As tall as the text needs at the dialog's width, and no taller than a screen can comfortably show. */
    private fun fitToContent(scroll: JScrollPane, content: JPanel) {
        pack()
        setSize(WIDTH, height)
        validate()
        // Wrapped texts only know their height once they have their width, so the size is worked out until it holds.
        repeat(3) {
            val around = height - scroll.viewport.height
            val wanted = (content.preferredSize.height + around).coerceIn(MIN_HEIGHT, MAX_HEIGHT)
            if (wanted == height) return
            setSize(WIDTH, wanted)
            validate()
        }
    }

    /** An argument as it would be typed: in quotes when it holds a space or is empty. */
    private fun quoted(argument: String) =
        if (argument.isEmpty() || argument.any { it.isWhitespace() }) "\"" + argument + "\"" else argument

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    private companion object {
        const val WIDTH = 520
        const val MIN_HEIGHT = 200
        const val MAX_HEIGHT = 600
    }
}
