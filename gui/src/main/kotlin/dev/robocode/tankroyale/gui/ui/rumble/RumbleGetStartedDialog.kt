package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.GetStarted
import dev.robocode.tankroyale.rumble.GetStartedButton
import dev.robocode.tankroyale.rumble.GetStartedDestination
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.SignInStore
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * The Rumble's Get started screen: what the Rumble is, and three cards that lead to making a bot, submitting a bot and
 * contributing battles, with a line that says it is a Beta and opens the feedback form. Where each button leads is
 * decided by [GetStarted]; this class only shows the cards and follows the answer. It never waits for the network.
 */
class RumbleGetStartedDialog private constructor(private val environment: RumbleEnvironment) :
    RcDialog(MainFrame, "rumble_get_started_dialog") {

    init {
        title = text("rumble.getstarted.title", title, environment.displayName)
        val content = WindowWidthPanel().apply {
            add(wrappedText(text("rumble.getstarted.intro")), "growx, wmin 10")
            add(JPanel(MigLayout("insets 0, gap 10, fillx", "[grow, fill, sg card][grow, fill, sg card][grow, fill, sg card]", "[grow, fill]")).apply {
                isOpaque = false
                add(card("make", GetStartedButton.MAKE_BOT, CardTone.BLUE), "wmin 10")
                add(card("submit", GetStartedButton.SUBMIT_BOT, CardTone.GREEN), "wmin 10")
                add(card("contribute", GetStartedButton.CONTRIBUTE, CardTone.AMBER), "wmin 10")
            }, "growx, wmin 10, gaptop 8")
            add(JPanel(MigLayout("insets 0, fillx", "[grow, fill][]")).apply {
                isOpaque = false
                add(wrappedText(text("rumble.getstarted.beta")), "wmin 10")
                add(button("rumble.getstarted.button.feedback", GetStartedButton.FEEDBACK))
            }, "growx, wmin 10, gaptop 8")
        }
        contentPane.add(JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }, BorderLayout.CENTER)
        setSize(720, 300)
        setLocationRelativeTo(owner)
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /** One card in its own colour: a bold title, a text that wraps, and its button at the bottom. */
    private fun card(name: String, which: GetStartedButton, tone: CardTone) =
        RumbleCard(MigLayout("insets 0, wrap 1, fill", "[grow, fill]", "[][grow, top][]"), tone).apply {
        add(JLabel(text("rumble.getstarted.$name.title")).apply { font = font.deriveFont(Font.BOLD) })
        add(wrappedText(text("rumble.getstarted.$name.text")), "wmin 10")
        add(button("rumble.getstarted.$name.button", which), "growx")
    }

    private fun button(key: String, which: GetStartedButton) = JButton(text(key)).apply { addActionListener { follow(which) } }

    /** Goes where [GetStarted] says, then closes this screen when another screen takes over. */
    private fun follow(which: GetStartedButton) {
        when (val destination = GetStarted.destination(which, GetStarted.hasSavedSignIn(SignInStore.inUserFolder(environment)))) {
            is GetStartedDestination.Page -> Browser.browse(destination.address)
            GetStartedDestination.SubmitScreen -> { dispose(); RumbleSubmitDialog.open() }
            GetStartedDestination.ContributeScreen -> { dispose(); RumbleContribute.open() }
            GetStartedDestination.AccountScreen -> { dispose(); RumbleAccountDialog.open() }
        }
    }

    companion object {
        /** Opens the screen for the environment the variable names. It needs no sign-in. */
        fun open() {
            when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
                is EnvironmentChoice.Unknown -> JOptionPane.showMessageDialog(MainFrame, String.format(Messages.get("rumble.account.unknown_environment"), choice.name))
                is EnvironmentChoice.Chosen -> {
                    // One Get started window at a time: choosing it again brings the open one to the front.
                    val shown = open?.takeIf { it.isDisplayable }
                    if (shown != null) shown.toFront() else open = RumbleGetStartedDialog(choice.environment).apply { isVisible = true }
                }
            }
        }

        private var open: RumbleGetStartedDialog? = null
    }
}
