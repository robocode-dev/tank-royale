package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.botproject.Finding
import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.rumble.AccountScheduler
import dev.robocode.tankroyale.rumble.BotProgress
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.GitHubSignIn
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.HttpGitHubAuthTransport
import dev.robocode.tankroyale.rumble.LauncherState
import dev.robocode.tankroyale.rumble.LibrarySendPort
import dev.robocode.tankroyale.rumble.RumbleLinks
import dev.robocode.tankroyale.rumble.SignInFlow
import dev.robocode.tankroyale.rumble.SignInStore
import dev.robocode.tankroyale.rumble.SubmissionReadiness
import dev.robocode.tankroyale.rumble.SubmissionResult
import dev.robocode.tankroyale.rumble.Tone
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.rumble.FoundBot
import dev.robocode.tankroyale.rumble.LibrarySubmitPort
import dev.robocode.tankroyale.rumble.ReviewInfo
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.SubmitController
import dev.robocode.tankroyale.rumble.SubmitState
import dev.robocode.tankroyale.rumble.SubmitTexts
import net.miginfocom.swing.MigLayout
import com.formdev.flatlaf.FlatLaf
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.Scrollable
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * The first three steps of Submit or update bot: choose the project, check it and review what will be published. It
 * only shows the state of a [SubmitController], which holds all the behavior and does every read of the disk away
 * from the event thread. This class runs on the event thread and never waits for anything. Sending comes later, so
 * nothing is sent from here.
 */
class RumbleSubmitDialog private constructor(private val scheduling: ScheduledExecutorService, controllerFor: (RumbleSubmitDialog) -> SubmitController) :
    RcDialog(MainFrame, "rumble_submit_dialog") {

    /** A panel that is as wide as the window, so wrapped texts wrap at the window's edge and never run out over it. */
    private class TrackingPanel : JPanel(MigLayout("insets 12, wrap 1, fillx", "[grow, fill]")), Scrollable {
        override fun getPreferredScrollableViewportSize(): Dimension = preferredSize
        override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 16
        override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 64
        override fun getScrollableTracksViewportWidth() = true
        override fun getScrollableTracksViewportHeight() = false
    }

    private val content = TrackingPanel()
    private val controller: SubmitController = controllerFor(this)

    init {
        contentPane.add(JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null })
        setSize(560, 520)
        setLocationRelativeTo(owner)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                controller.close()
                // After the close has run on the event thread, so a result still queued there finds a working timer.
                SwingUtilities.invokeLater { scheduling.shutdown() }
            }
        })
        render(controller.state)
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /** Shows [state]. Called on the event thread. */
    private fun render(state: SubmitState) {
        content.removeAll()
        when (state) {
            is SubmitState.Choose -> renderChoose(state)
            is SubmitState.Working -> put(wrapped(text(SubmitTexts.forState(state))))
            is SubmitState.Problems -> renderProblems(state)
            is SubmitState.Checked -> {
                put(wrapped(text("rumble.submit.step_check"), bold = true))
                put(status(true, text(SubmitTexts.forState(state))))
                put(wrapped(foundText(state.found)))
                state.notes.forEach { put(wrapped(text("rumble.submit.notes", it))) }
                put(button("rumble.submit.button.review") { controller.review() })
                put(button("rumble.submit.button.back") { controller.back() })
            }
            is SubmitState.Review -> renderReview(state)
            is SubmitState.Copy -> renderCopy(state)
            is SubmitState.SendReady -> renderSendReady(state)
            is SubmitState.Sending -> put(wrapped(text(SubmitTexts.forState(state))))
            is SubmitState.SendEnded -> renderEnded(state)
        }
        content.revalidate()
        content.repaint()
    }

    private fun renderChoose(state: SubmitState.Choose) {
        put(wrapped(text(SubmitTexts.forState(state)), bold = true))
        put(wrapped(text("rumble.submit.choose_help")))
        put(wrapped(state.folder?.toString() ?: text("rumble.submit.choose_none")))
        put(button("rumble.submit.button.choose") { chooseFolder() })
        state.found?.let { put(status(true, foundText(it))) }
        state.problem?.let { problem(it) }
        if (state.found != null) put(button("rumble.submit.button.check") { controller.check() })
    }

    private fun renderProblems(state: SubmitState.Problems) {
        put(status(false, text(SubmitTexts.forState(state))))
        state.findings.forEach { problem(it) }
        state.notes.forEach { put(wrapped(text("rumble.submit.notes", it))) }
        put(button("rumble.submit.button.check_again") { controller.check() })
        put(button("rumble.submit.button.back") { controller.back() })
    }

    private fun renderReview(state: SubmitState.Review) {
        val review: ReviewInfo = state.review
        put(wrapped(text(SubmitTexts.forState(state)), bold = true))
        put(wrapped(text("rumble.submit.review_intro")))
        put(wrapped(text("rumble.submit.review_name", review.name, review.version ?: text("rumble.submit.unknown"))))
        put(wrapped(text("rumble.submit.review_language", review.language)))
        put(wrapped(review.license?.let { text("rumble.submit.review_license", it) } ?: text("rumble.submit.review_no_license")))
        put(wrapped(text("rumble.submit.review_files", review.files.size)))
        put(JScrollPane(wrapped(review.files.joinToString("\n"))).apply { preferredSize = Dimension(100, 110) })
        put(wrapped(text("rumble.submit.public_notice"), bold = true))
        put(confirmation(text("rumble.submit.confirm_public"), state.publicConfirmed) { controller.setPublicConfirmed(it) })
        put(confirmation(text("rumble.submit.confirm_author"), state.authorConfirmed) { controller.setAuthorConfirmed(it) })
        put(button("rumble.submit.button.next") { controller.next() }.apply { isEnabled = state.canContinue })
        put(button("rumble.submit.button.back") { controller.back() })
    }

    private fun renderCopy(state: SubmitState.Copy) {
        val readiness = state.readiness
        put(wrapped(text(SubmitTexts.forState(state)), bold = true))
        put(toned(text(SubmitTexts.forReadiness(readiness)), SubmitTexts.toneOf(readiness)))
        readinessButton(readiness)?.let { put(it) }
        when (readiness) {
            is SubmissionReadiness.NotRumblesRepository -> detail(readiness.repositoryUrl)
            is SubmissionReadiness.SignInNeeded -> detail(readiness.message)
            is SubmissionReadiness.Failed -> {
                detail(readiness.message)
                put(button("rumble.submit.button.check_now") { controller.checkNow() })
            }
            else -> Unit
        }
        put(button("rumble.submit.button.back") { controller.back() })
    }

    private fun renderSendReady(state: SubmitState.SendReady) {
        val ready = state.ready
        put(wrapped(text(SubmitTexts.forState(state)), bold = true))
        put(wrapped(text("rumble.submit.send_intro")))
        put(wrapped(text("rumble.submit.public_notice"), bold = true))
        put(toned(text(SubmitTexts.forContent(ready.content)), SubmitTexts.contentTone(state)))
        // What the Rumble's rules would refuse stops the send; a lower version is only a note.
        ready.rules?.let { rules ->
            rules.problems.forEach { finding -> SubmitTexts.forRule(finding.code)?.let { put(status(false, text(it, *finding.values.toTypedArray()))) } }
            rules.warnings.forEach { finding -> SubmitTexts.forRule(finding.code)?.let { put(wrapped(text(it, *finding.values.toTypedArray()))) } }
        }
        put(wrapped(launchersText(ready.launchers)))
        ready.existing?.let { existing ->
            put(wrapped(text("rumble.submit.existing", existing.version)))
            progress(existing.progress).forEach { put(it) }
        }
        put(button(if (ready.existing != null) "rumble.submit.button.update" else "rumble.submit.button.send") { controller.send() }.apply { isEnabled = state.canSend })
        if (ready.existing != null) put(button("rumble.submit.button.check_now") { controller.checkNow() })
        put(button("rumble.submit.button.back") { controller.back() })
    }

    private fun renderEnded(state: SubmitState.SendEnded) {
        val result = state.result
        put(wrapped(text(SubmitTexts.forState(state)), bold = true))
        put(toned(text(SubmitTexts.forResult(result)), SubmitTexts.toneOf(result)))
        when (result) {
            is SubmissionResult.Submitted -> {
                put(wrapped(result.pullRequestUrl))
                put(wrapped(launchersText(state.launchers)))
                state.progress?.let { progress(it).forEach { line -> put(line) } } ?: put(wrapped(text("rumble.submit.progress_reading")))
                put(pageButton("rumble.submit.button.open_pr", result.pullRequestUrl))
                put(button("rumble.submit.button.check_now") { controller.checkNow() })
                put(button("rumble.submit.button.close") { dispose() })
            }
            is SubmissionResult.SetupNeeded -> {
                put(wrapped(text(SubmitTexts.forReadiness(result.readiness))))
                readinessButton(result.readiness)?.let { put(it) }
                put(button("rumble.submit.button.back") { controller.back() })
            }
            is SubmissionResult.Refused -> {
                detail(result.message)
                put(button("rumble.submit.button.back") { controller.back() })
            }
            is SubmissionResult.SignInNeeded -> {
                detail(result.message)
                put(button("rumble.submit.button.back") { controller.back() })
            }
            is SubmissionResult.Failed -> {
                detail(result.message)
                put(button("rumble.submit.button.back") { controller.back() })
            }
        }
    }

    /** Where the pull request stands, with the label and the colour of what it says. */
    /**
     * Where the pull request stands. A pull request sent back by the moderation is said on a line of its own, and the
     * moderation's reason on the next, without code marks.
     */
    private fun progress(progress: BotProgress): List<Component> = when (progress) {
        is BotProgress.SentBack -> listOfNotNull(
            wrapped(text(SubmitTexts.forProgress(progress)), bold = true).apply { foreground = statusColor(false) },
            progress.reason.takeIf { it.isNotBlank() }?.let { wrapped(text("rumble.submit.progress_reason", SubmitTexts.plainReason(it))) },
        )
        is BotProgress.InCatalog -> listOf(toned(text("rumble.submit.progress_label") + " " + text(SubmitTexts.forProgress(progress), progress.version), SubmitTexts.toneOf(progress)))
        else -> listOf(toned(text("rumble.submit.progress_label") + " " + text(SubmitTexts.forProgress(progress)), SubmitTexts.toneOf(progress)))
    }

    private fun launchersText(launchers: LauncherState): String = when (launchers) {
        LauncherState.Same -> text(SubmitTexts.forLaunchers(launchers))
        is LauncherState.Differs -> text(SubmitTexts.forLaunchers(launchers), launchers.files.joinToString(", "))
        is LauncherState.NotChecked -> text(SubmitTexts.forLaunchers(launchers))
    }

    /** What a lower layer said, as a labelled detail and never as a sentence of the program's own. */
    private fun detail(message: String) {
        if (message.isNotBlank()) put(wrapped(text("rumble.submit.details", message)))
    }

    /** A button that opens a page of GitHub's own in the browser, and never any other address. */
    /** The button that opens GitHub's page where the person sets up what [readiness] says is missing, or null. */
    private fun readinessButton(readiness: SubmissionReadiness?) = when (readiness) {
        is SubmissionReadiness.NoCopy -> pageButton("rumble.submit.button.open_fork", readiness.pageUrl)
        is SubmissionReadiness.AppNotInstalled -> pageButton("rumble.submit.button.open_install", readiness.installUrl)
        is SubmissionReadiness.CopyBehind -> pageButton("rumble.submit.button.open_copy", readiness.copyUrl)
        else -> null
    }

    private fun pageButton(key: String, address: String) =
        button(key) { if (RumbleLinks.isGitHubPage(address)) Browser.browse(address) }

    private fun toned(message: String, tone: Tone): Component = when (tone) {
        Tone.GOOD -> status(true, message)
        Tone.BAD -> status(false, message)
        Tone.NEUTRAL -> wrapped(message)
    }

    /** One problem: what the code means from the resources, then the file and the library's own sentences as labelled details. */
    private fun problem(finding: Finding) {
        put(wrapped(text(SubmitTexts.forCode(finding.code)), bold = true).apply { foreground = statusColor(false) })
        if (finding.file.isNotBlank()) put(wrapped(text("rumble.submit.file", finding.file)))
        if (finding.message.isNotBlank()) put(wrapped(text("rumble.submit.details", finding.message)))
        if (finding.fix.isNotBlank() && finding.fix != finding.message) put(wrapped(text("rumble.submit.fix", finding.fix)))
    }

    /** A line that says at a glance whether it is good news or bad: green for success, red for failure, with the word in the text. */
    private fun status(success: Boolean, message: String) =
        wrapped(text(if (success) "rumble.submit.status_success" else "rumble.submit.status_failed", message), bold = true)
            .apply { foreground = statusColor(success) }

    /** Colors that can be read on both the light and the dark theme. */
    private fun statusColor(success: Boolean): Color {
        val dark = FlatLaf.isLafDark()
        return if (success) (if (dark) Color(0x6FCF73) else Color(0x1B7F2A)) else (if (dark) Color(0xFF7B72) else Color(0xB3261E))
    }

    private fun foundText(found: FoundBot): String {
        val version = found.version ?: text("rumble.submit.unknown")
        return if (found.isTeam) text("rumble.submit.found_team", found.name, version)
        else text("rumble.submit.found", found.name, version, found.language ?: text("rumble.submit.unknown"))
    }

    private fun chooseFolder() {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            ConfigSettings.botDirectories.firstOrNull { it.enabled && File(it.path).isDirectory }?.let { currentDirectory = File(it.path) }
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) controller.choose(chooser.selectedFile.toPath())
    }

    /** A check box with a text that wraps; the text can be clicked too. */
    private fun confirmation(label: String, ticked: Boolean, changed: (Boolean) -> Unit): Component {
        val box = JCheckBox().apply { isSelected = ticked }
        box.addActionListener { changed(box.isSelected) }
        val words = wrapped(label).apply {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) = box.doClick()
            })
        }
        return JPanel(MigLayout("insets 0", "[][grow, fill]", "[top]")).apply {
            add(box)
            add(words, "growx, wmin 10")
        }
    }

    /** Adds a component that is as wide as the window and never wider, so nothing runs out over the edge. */
    private fun put(component: Component) {
        content.add(component, "growx, wmin 10")
    }

    /** A text that wraps at the window's edge and can be selected and copied. */
    private fun wrapped(text: String, bold: Boolean = false) = JTextArea(text).apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
        isOpaque = false
        border = null
        font = UIManager.getFont("Label.font").let { if (bold) it.deriveFont(Font.BOLD) else it }
    }

    private fun button(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }

    companion object {
        /** Opens the wizard for the environment the variable names. The wizard never waits for the disk. */
        fun open() {
            when (val choice = RumbleMenuSwitch.environment(System.getenv(RumbleMenuSwitch.ENVIRONMENT_VARIABLE))) {
                is EnvironmentChoice.Unknown -> JOptionPane.showMessageDialog(MainFrame, String.format(Messages.get("rumble.account.unknown_environment"), choice.name))
                is EnvironmentChoice.Chosen -> show(choice)
            }
        }

        private fun show(choice: EnvironmentChoice.Chosen) {
            val environment = choice.environment
            val staging = environment.stateRoot(UserDataDirectory.get().toPath()).resolve("staging")
            val background = Executors.newCachedThreadPool { runnable -> Thread(runnable, "rumble-submit").apply { isDaemon = true } }
            val scheduling = Executors.newSingleThreadScheduledExecutor { runnable -> Thread(runnable, "rumble-submit-timer").apply { isDaemon = true } }
            val api = HttpGitHubApi()
            val flow = SignInFlow(environment, GitHubSignIn(environment, HttpGitHubAuthTransport()), SignInStore(environment, UserDataDirectory.get().toPath()), api)
            val scheduler = AccountScheduler { seconds, task ->
                val future = scheduling.schedule(task, seconds, TimeUnit.SECONDS)
                AutoCloseable { future.cancel(false) }
            }
            val post = Executor { SwingUtilities.invokeLater(it) }
            RumbleSubmitDialog(scheduling) { self ->
                SubmitController(LibrarySubmitPort(staging), LibrarySendPort(environment, flow, api), background, post, scheduler) { state -> self.render(state) }
            }.isVisible = true
        }
    }
}
