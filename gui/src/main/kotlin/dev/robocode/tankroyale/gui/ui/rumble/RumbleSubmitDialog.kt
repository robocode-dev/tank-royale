package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.botproject.Finding
import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.rumble.EnvironmentChoice
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
class RumbleSubmitDialog private constructor(controllerFor: (RumbleSubmitDialog) -> SubmitController) :
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
            override fun windowClosed(e: WindowEvent) = controller.close()
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
            is SubmitState.SendingComesLater -> {
                put(wrapped(text(SubmitTexts.forState(state)), bold = true))
                put(button("rumble.submit.button.close") { dispose() })
            }
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
            val staging = choice.environment.stateRoot(UserDataDirectory.get().toPath()).resolve("staging")
            val background = Executors.newCachedThreadPool { runnable -> Thread(runnable, "rumble-submit").apply { isDaemon = true } }
            val post = Executor { SwingUtilities.invokeLater(it) }
            RumbleSubmitDialog { self -> SubmitController(LibrarySubmitPort(staging), background, post) { state -> self.render(state) } }.isVisible = true
        }
    }
}
