package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.rumble.DownloadTexts
import dev.robocode.tankroyale.rumble.PlatformCheck
import dev.robocode.tankroyale.rumble.PlatformDownloads
import dev.robocode.tankroyale.rumble.PlatformReason
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
import java.awt.Window
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
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
        fun put(component: java.awt.Component) = content.add(component, "growx, wmin 10")
        val unknown = text("rumble.download.unknown")
        put(wrappedText(text(DownloadTexts.forPlatformExplanation(check.reason), text(DownloadTexts.forPlatformTool(check.platform)),
            check.platform.catalogName, check.found ?: unknown, check.needed ?: unknown)))
        if (check.reason == PlatformReason.NO_VENV) put(wrappedText(text("rumble.download.platform.venv_package")))
        val offerSetUp = check.reason == PlatformReason.NO_ENVIRONMENT && commands.isNotEmpty()
        if (offerSetUp) {
            put(wrappedText(text("rumble.download.platform.commands")))
            put(wrappedText(commands.joinToString("\n") { it.joinToString(" ") }).apply {
                font = Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").size)
            })
        }

        val buttons = JPanel(GridLayout(0, 1, 0, 6)).apply { border = EmptyBorder(6, 12, 12, 12) }
        fun button(key: String, action: () -> Unit) = buttons.add(JButton(text(key)).apply { addActionListener { action() } })
        if (offerSetUp) {
            button("rumble.download.button.set_up") { dispose(); setUp() }
        } else {
            button("rumble.download.button.download_page") { open(PlatformDownloads.page(check.platform)) }
        }
        button("rumble.download.button.show_anyway") { dispose(); showAnyway() }
        button("rumble.download.button.close") { dispose() }

        contentPane.add(JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }, BorderLayout.CENTER)
        contentPane.add(buttons, BorderLayout.SOUTH)
        setSize(520, 380)
        setLocationRelativeTo(owner)
        isModal = true
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)
}
