package dev.robocode.tankroyale.gui.ui.rumble

import com.formdev.flatlaf.FlatLaf
import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.rumble.BotProgress
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.GitHubSignIn
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.HttpGitHubAuthTransport
import dev.robocode.tankroyale.rumble.LibraryMyBotsPort
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.rumble.MyBotRow
import dev.robocode.tankroyale.rumble.MyBots
import dev.robocode.tankroyale.rumble.MyBotStanding
import dev.robocode.tankroyale.rumble.MyBotsController
import dev.robocode.tankroyale.rumble.MyBotsState
import dev.robocode.tankroyale.rumble.MyBotsTexts
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.RumbleLinks
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.SignInFlow
import dev.robocode.tankroyale.rumble.SignInStore
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder

/**
 * The Rumble's My bots screen. It only shows the state of a [MyBotsController], which reads the list once, away from
 * the event thread. Every sentence comes from the resource files.
 */
class RumbleMyBotsDialog private constructor(environment: RumbleEnvironment, controllerFor: (RumbleMyBotsDialog) -> MyBotsController) :
    RcDialog(MainFrame, "rumble_my_bots_dialog") {

    private val content = WindowWidthPanel()
    private val actions = JPanel(GridLayout(1, 0, 8, 0)).apply { border = EmptyBorder(6, 12, 12, 12) }
    private val controller: MyBotsController = controllerFor(this)

    init {
        title = text("rumble.mybots.title", title, environment.displayName)
        contentPane.add(JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }, BorderLayout.CENTER)
        contentPane.add(actions, BorderLayout.SOUTH)
        setSize(760, 420)
        setLocationRelativeTo(owner)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) = controller.close()
        })
        render(controller.state)
        controller.start()
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /** Shows [state]. Called on the event thread. */
    private fun render(state: MyBotsState) {
        content.removeAll()
        actions.removeAll()
        when (state) {
            MyBotsState.Reading -> put(wrappedText(text("rumble.mybots.reading")))
            is MyBotsState.SignInNeeded -> {
                put(wrappedText(text("rumble.mybots.sign_in_needed"), bold = true))
                details(state.details)
                actions.add(button("rumble.mybots.button.account") { dispose(); RumbleAccountDialog.open() })
            }
            is MyBotsState.Failed -> {
                put(wrappedText(text("rumble.mybots.failed"), bold = true).apply { foreground = myBotsColor(good = false) })
                details(state.details)
            }
            is MyBotsState.Shown -> {
                put(wrappedText(text("rumble.mybots.signed_in_as", state.account)))
                // Retired bots and closed submissions are hidden unless the person asks for them; the choice is remembered.
                val inactive = state.rows.count { MyBots.isInactive(it) }
                if (inactive > 0) put(JCheckBox(text("rumble.mybots.show_inactive", inactive), ConfigSettings.rumbleMyBotsShowInactive).apply {
                    isOpaque = false
                    addActionListener {
                        ConfigSettings.rumbleMyBotsShowInactive = isSelected
                        render(state)
                    }
                })
                val shown = MyBots.shown(state.rows, ConfigSettings.rumbleMyBotsShowInactive)
                when {
                    state.rows.isEmpty() -> put(wrappedText(text("rumble.mybots.none")))
                    shown.isEmpty() -> put(wrappedText(text("rumble.mybots.hidden")))
                    else -> put(table(shown))
                }
                actions.add(button("rumble.mybots.button.submit_new") { dispose(); RumbleSubmitDialog.open() })
            }
        }
        content.revalidate()
        content.repaint()
        actions.revalidate()
        actions.repaint()
    }

    private fun details(details: String) {
        if (details.isNotBlank()) put(wrappedText(text("rumble.mybots.details", details)))
    }

    private fun table(rows: List<MyBotRow>) =
        myBotsTable(rows, { key, arguments -> text(key, *arguments) }, hint = { Hints.get(it) }, onOpen = { Browser.browse(it) }, onUpdate = { dispose(); RumbleSubmitDialog.open() })

    private fun put(component: Component) {
        content.add(component, "growx, wmin 10")
    }

    private fun button(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }

    companion object {
        private var open: RumbleMyBotsDialog? = null

        /** Opens the screen for the environment the variable names, or brings the open one to the front. */
        fun open() {
            when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
                is EnvironmentChoice.Unknown -> JOptionPane.showMessageDialog(MainFrame, String.format(Messages.get("rumble.account.unknown_environment"), choice.name))
                is EnvironmentChoice.Chosen -> {
                    val shown = open?.takeIf { it.isDisplayable }
                    if (shown != null) shown.toFront() else open = show(choice.environment)
                }
            }
        }

        private fun show(environment: RumbleEnvironment): RumbleMyBotsDialog {
            val background = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "rumble-my-bots").apply { isDaemon = true } }
            val post = Executor { SwingUtilities.invokeLater(it) }
            val api = HttpGitHubApi()
            val flow = SignInFlow(environment, GitHubSignIn(environment, HttpGitHubAuthTransport()), SignInStore(environment, UserDataDirectory.get().toPath()), api)
            return RumbleMyBotsDialog(environment) { self ->
                MyBotsController(LibraryMyBotsPort(environment, flow, api), background, post) { state -> self.render(state) }
            }.apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosed(e: WindowEvent) = SwingUtilities.invokeLater { background.shutdown() }
                })
                isVisible = true
            }
        }
    }
}

/**
 * The list of My bots: a header line, then per bot its name, version, language, where it stands, Open and Update. It is
 * built without a window, so a test can build it. [text] reads a text from the resource files and [hint] a tooltip, which is shown in the program's own tooltip; [onOpen] is handed a
 * row's page only when it is an https page on github.com, and Open is switched off for a row without one.
 */
internal fun myBotsTable(rows: List<MyBotRow>, text: (String, Array<Any>) -> String, hint: (String) -> String, onOpen: (String) -> Unit, onUpdate: () -> Unit): JPanel =
    JPanel(MigLayout("insets 0, gapx 12, gapy 6, wrap 6, fillx", "[][][][grow, fill][][]")).apply {
        isOpaque = false
        listOf("name", "version", "language", "state").forEach { add(JLabel(text("rumble.mybots.column.$it", emptyArray())).apply { font = font.deriveFont(Font.BOLD) }) }
        add(JLabel())
        add(JLabel())
        rows.forEach { row ->
            val page = row.page?.takeIf { RumbleLinks.isGitHubPage(it) }
            add(JLabel(row.name))
            add(JLabel(row.version))
            add(JLabel(row.language ?: text("rumble.mybots.unknown_language", emptyArray())))
            add(standing(row, text), "wmin 10")
            add(HintButton(text("rumble.mybots.button.open", emptyArray())).apply {
                isEnabled = page != null
                // Says which page it is: the newest pull request, or the bot's folder in the bot repository.
                toolTipText = hint(if (page?.contains("/pull/") == true) "rumble.my_bots.open_pull" else "rumble.my_bots.open_folder")
                addActionListener { page?.let(onOpen) }
            })
            add(HintButton(text("rumble.mybots.button.update", emptyArray())).apply {
                toolTipText = hint("rumble.my_bots.update")
                addActionListener { onUpdate() }
            })
        }
    }

/** A button whose tooltip is the program's own yellow tooltip, as on every other screen. */
internal class HintButton(label: String) : JButton(label) {
    override fun createToolTip() = RcToolTip()
}

/** Where a bot stands, in green when it is in the rankings and red when it was sent back or closed. */
private fun standing(row: MyBotRow, text: (String, Array<Any>) -> String): Component {
    val (key, arguments) = MyBotsTexts.forStanding(row.standing)
    var line = text(key, arguments.toTypedArray<Any>())
    row.inRumble?.let { line += " " + text("rumble.mybots.in_rumble_now", arrayOf(it)) }
    val progress = (row.standing as? MyBotStanding.Progress)?.progress
    return wrappedText(line).apply {
        when (progress) {
            is BotProgress.InCatalog -> foreground = myBotsColor(good = true)
            is BotProgress.SentBack, BotProgress.Closed, is BotProgress.Failed -> foreground = myBotsColor(good = false)
            else -> {}
        }
    }
}

/** Colours that can be read on both the light and the dark theme, the same as on the other Rumble screens. */
internal fun myBotsColor(good: Boolean): Color {
    val dark = FlatLaf.isLafDark()
    return if (good) (if (dark) Color(0x6FCF73) else Color(0x1B7F2A)) else (if (dark) Color(0xFF7B72) else Color(0xB3261E))
}