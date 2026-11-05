package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.gui.util.Clipboard
import dev.robocode.tankroyale.rumble.AccountController
import dev.robocode.tankroyale.rumble.AccountRegistration
import dev.robocode.tankroyale.rumble.AccountScheduler
import dev.robocode.tankroyale.rumble.AccountState
import dev.robocode.tankroyale.rumble.AccountTexts
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.GitHubSignIn
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.HttpGitHubAuthTransport
import dev.robocode.tankroyale.rumble.LibraryAccountPort
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.SignInFlow
import dev.robocode.tankroyale.rumble.SignInStore
import net.miginfocom.swing.MigLayout
import java.awt.Font
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.Timer

/**
 * The GitHub account screen of the Rumble menu. It only shows the state of an [AccountController], which holds all the
 * behavior and does every call to GitHub and the disk away from the event thread. This class runs on the event thread
 * and never waits for anything.
 */
class RumbleAccountDialog private constructor(private val scheduling: ScheduledExecutorService, controllerFor: (RumbleAccountDialog) -> AccountController) :
    RcDialog(MainFrame, "rumble_account_dialog") {

    private val content = JPanel(MigLayout("insets 12, wrap 1, fillx", "[grow, fill]"))
    private val controller: AccountController = controllerFor(this)
    private var countdown: Timer? = null

    init {
        contentPane.add(content)
        setSize(480, 300)
        isResizable = false
        setLocationRelativeTo(owner)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                countdown?.stop()
                controller.close()
                scheduling.shutdown()
            }
        })
        controller.open()
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /** Shows [state]. Called on the event thread. */
    private fun render(state: AccountState) {
        countdown?.stop()
        content.removeAll()
        when (state) {
            AccountState.Starting -> content.add(JLabel(text(AccountTexts.forState(state))))
            is AccountState.SignedOut -> {
                content.add(heading(AccountTexts.forState(state)))
                state.notice?.let { notice -> content.add(JLabel(html(text(AccountTexts.forNotice(notice))))) }
                detail(state.detail)
                content.add(button("rumble.account.button.sign_in") { controller.signIn() })
            }
            is AccountState.Unavailable -> {
                content.add(heading(AccountTexts.forState(state)))
            }
            is AccountState.WaitingForCode -> renderWaiting(state)
            is AccountState.SignedIn -> {
                val line = state.account?.let { text(AccountTexts.forState(state), it) } ?: text("rumble.account.signed_in_unknown")
                content.add(JLabel(line).apply { font = font.deriveFont(Font.BOLD) })
                content.add(JLabel(html(registrationText(state.registration))))
                detail(registrationDetail(state.registration))
                content.add(button("rumble.account.button.sign_out") { controller.signOut() })
            }
        }
        content.revalidate()
        content.repaint()
    }

    private fun renderWaiting(state: AccountState.WaitingForCode) {
        content.add(heading(AccountTexts.forState(state)))
        content.add(JLabel(text("rumble.account.step_open")))
        content.add(button("rumble.account.button.open_page") {
            Clipboard.set(state.code)
            Browser.browse(state.address)
        })
        content.add(JLabel(text("rumble.account.step_enter")))
        // A read-only field, so the code can be selected with the mouse and copied with Ctrl+C like any other text.
        content.add(JTextField(state.code).apply {
            isEditable = false
            border = null
            horizontalAlignment = JTextField.CENTER
            font = font.deriveFont(Font.BOLD, 28f)
            addFocusListener(object : FocusAdapter() {
                override fun focusGained(e: FocusEvent) = selectAll()
            })
        })
        content.add(button("rumble.account.button.copy_code") { Clipboard.set(state.code) })
        val remaining = JLabel()
        content.add(remaining)
        fun update() {
            val left = Duration.between(Instant.now(), state.expiresAt).coerceAtLeast(Duration.ZERO)
            remaining.text = text("rumble.account.waiting_expires", "%d:%02d".format(left.toMinutes(), left.toSecondsPart()))
        }
        update()
        countdown = Timer(1000) { update() }.apply { start() }
        content.add(button("rumble.account.button.cancel") { controller.cancel() })
    }

    private fun registrationText(registration: AccountRegistration): String {
        val key = AccountTexts.forRegistration(registration)
        return when (registration) {
            is AccountRegistration.Registered -> text(key, registration.clientId)
            else -> text(key)
        }
    }

    /** What a lower layer said about a registration, shown only as a labelled detail. */
    private fun registrationDetail(registration: AccountRegistration): String? = when (registration) {
        is AccountRegistration.Refused -> registration.reason
        is AccountRegistration.Failed -> registration.message
        is AccountRegistration.SignInNeeded -> registration.message
        else -> null
    }

    /** Adds what a lower layer said, under the label from the resources. The sentence itself is not ours. */
    private fun detail(said: String?) {
        said?.takeIf { it.isNotBlank() }?.let { content.add(JLabel(html(text("rumble.account.details", it)))) }
    }

    private fun html(text: String) = "<html>${escape(text)}</html>"

    private fun heading(key: String) = JLabel(text(key)).apply { font = font.deriveFont(Font.BOLD) }

    private fun button(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }

    private fun escape(text: String) = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    companion object {
        /** Opens the screen for the environment the variable names. The screen never waits for GitHub. */
        fun open() {
            when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
                is EnvironmentChoice.Unknown -> javax.swing.JOptionPane.showMessageDialog(
                    MainFrame, String.format(Messages.get("rumble.account.unknown_environment"), choice.name),
                )
                is EnvironmentChoice.Chosen -> show(choice)
            }
        }

        private fun show(choice: EnvironmentChoice.Chosen) {
            val environment = choice.environment
            val background = Executors.newCachedThreadPool { runnable -> Thread(runnable, "rumble-account").apply { isDaemon = true } }
            val scheduling = Executors.newSingleThreadScheduledExecutor { runnable -> Thread(runnable, "rumble-account-timer").apply { isDaemon = true } }
            val api = HttpGitHubApi()
            val flow = SignInFlow(environment, GitHubSignIn(environment, HttpGitHubAuthTransport()), SignInStore(environment, UserDataDirectory.get().toPath()), api)
            val port = LibraryAccountPort(environment, flow, api)
            val scheduler = AccountScheduler { seconds, task ->
                val future = scheduling.schedule(task, seconds, TimeUnit.SECONDS)
                AutoCloseable { future.cancel(false) }
            }
            val post = Executor { SwingUtilities.invokeLater(it) }
            RumbleAccountDialog(scheduling) { self ->
                AccountController(port, background, post, scheduler) { state -> self.render(state) }
            }.isVisible = true
        }
    }
}
