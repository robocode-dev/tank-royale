package dev.robocode.tankroyale.gui.ui.rumble

import com.formdev.flatlaf.FlatLaf
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.rumble.CloseChoice
import dev.robocode.tankroyale.rumble.ContributeChoices
import dev.robocode.tankroyale.rumble.ContributeController
import dev.robocode.tankroyale.rumble.ContributeFix
import dev.robocode.tankroyale.rumble.ContributeMode
import dev.robocode.tankroyale.rumble.ContributeState
import dev.robocode.tankroyale.rumble.ContributeTexts
import dev.robocode.tankroyale.rumble.SessionControlRequest
import dev.robocode.tankroyale.rumble.SessionStatus
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.GridLayout
import java.awt.Point
import java.util.Locale
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder

/**
 * The Contribute battles screen. It only shows the state of the program's [ContributeController], which holds all the
 * behavior; the session itself runs as a program of its own, so closing this screen, or the whole program, does not end
 * it by itself. This class runs on the event thread and never waits for anything.
 */
class RumbleContributeDialog(environmentName: String, private val controller: ContributeController) :
    RcDialog(MainFrame, "rumble_contribute_dialog") {

    private val content = WindowWidthPanel()
    private val scroll = JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }
    private val actions = JPanel(GridLayout(1, 0, 8, 0)).apply { border = EmptyBorder(6, 12, 12, 12) }

    /** The state the screen shows now, and its Start button while it is choosing. */
    private var shown: ContributeState? = null
    private var startButton: JButton? = null

    init {
        title = text("rumble.download.title", title, environmentName)
        isModal = false
        contentPane.add(scroll, BorderLayout.CENTER)
        contentPane.add(actions, BorderLayout.SOUTH)
        setSize(620, 680)
        setLocationRelativeTo(owner)
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /** Shows [state]. Called on the event thread. */
    fun render(state: ContributeState) {
        val before = shown
        shown = state
        // A new battle count from the spinner is already shown by the spinner itself; building the screen again would
        // replace the spinner under the person's hand and stop a held arrow after one step.
        if (before is ContributeState.Choosing && state is ContributeState.Choosing && before.choices.battles != null && state.choices.battles != null &&
            before.copy(choices = before.choices.copy(battles = state.choices.battles)) == state
        ) {
            startButton?.isEnabled = state.choices.startable && state.bots != null
            return
        }
        val position: Point = scroll.viewport.viewPosition
        content.removeAll()
        actions.removeAll()
        when (state) {
            ContributeState.Looking -> put(wrapped(text("rumble.contribute.looking")))
            is ContributeState.Choosing -> renderChoosing(state)
            is ContributeState.AskingForProgram -> {
                put(wrapped(text("rumble.contribute.program.ask", state.plan.fileName, megabytes(state.plan.size))))
                act(button("rumble.contribute.button.fetch") { controller.fetchProgram() }, button("rumble.contribute.button.cancel") { controller.declineProgram() })
            }
            is ContributeState.Preparing -> put(wrapped(text(if (state.fetching) "rumble.contribute.program.fetching" else "rumble.contribute.preparing")))
            is ContributeState.Starting -> put(wrapped(text("rumble.contribute.starting")))
            is ContributeState.Running -> renderRunning(state)
        }
        content.revalidate()
        content.repaint()
        actions.revalidate()
        actions.repaint()
        SwingUtilities.invokeLater { scroll.viewport.viewPosition = if (state is ContributeState.Choosing || state is ContributeState.Running) position else Point(0, 0) }
    }

    private fun renderChoosing(state: ContributeState.Choosing) {
        state.last?.let { lastSession(it, state.lastUnexpected) }
        state.problem?.let { problem ->
            put(status(false, text(ContributeTexts.forProblem(problem))))
            state.program?.let { put(wrapped(text(ContributeTexts.forProgram(it)))) }
            if (state.details.isNotBlank()) put(wrapped(text("rumble.contribute.details", state.details)))
        }
        put(wrapped(text("rumble.contribute.intro")))
        val choices = state.choices

        put(wrapped(text("rumble.contribute.which"), bold = true))
        val modes = ButtonGroup()
        ContributeMode.entries.forEach { mode ->
            put(choice(JRadioButton().also { modes.add(it) }, text(ContributeTexts.forMode(mode)), choices.mode == mode, true) { if (it) controller.setMode(mode) })
        }
        val bots = state.bots
        if (bots == null) {
            put(button("rumble.contribute.button.read_again") { controller.readAgain() })
        } else if (choices.mode != ContributeMode.ALL) {
            put(wrapped(text("rumble.contribute.bots"), bold = true))
            if (bots.isEmpty()) put(wrapped(text("rumble.contribute.no_bots")))
            put(JPanel(MigLayout("insets 0, wrap 2", "[grow, fill][grow, fill]")).apply {
                bots.forEach { bot -> add(JCheckBox(bot, bot in choices.bots).apply { addActionListener { controller.setBot(bot, isSelected) } }) }
            })
        }

        put(wrapped(text("rumble.contribute.formats"), bold = true))
        put(JPanel(MigLayout("insets 0", "[][][]")).apply {
            ContributeChoices.RANKED_FORMATS.forEach { format ->
                add(JCheckBox(format, format in choices.formats).apply { addActionListener { controller.setFormat(format, isSelected) } })
            }
        })

        put(wrapped(text("rumble.contribute.how_long"), bold = true))
        val length = ButtonGroup()
        val count = JSpinner(SpinnerNumberModel(choices.battles ?: ContributeChoices.DEFAULT_BATTLES, 1, 100_000, 1)).apply {
            isEnabled = choices.battles != null
            addChangeListener { controller.setBattles(value as Int) }
        }
        put(JPanel(MigLayout("insets 0", "[][]")).apply {
            add(JRadioButton(text("rumble.contribute.battles"), choices.battles != null).also { length.add(it) }.apply {
                addActionListener { if (isSelected) controller.setBattles(count.value as Int) }
            })
            add(count, "w 90!")
        })
        put(JRadioButton(text("rumble.contribute.keep_running"), choices.battles == null).also { length.add(it) }.apply {
            addActionListener { if (isSelected) controller.setBattles(null) }
        })

        closeSetting()
        act(
            button("rumble.contribute.button.start") { controller.startSession(ConfigSettings.rumbleCloseChoice == CloseChoice.KEEP) }
                .apply { isEnabled = choices.startable && bots != null }.also { startButton = it },
            button("rumble.contribute.button.close") { isVisible = false },
        )
    }

    /** The summary of the last session: how it ended, what it kept, and where to fix why it could not run. */
    private fun lastSession(last: SessionStatus, unexpected: Boolean) {
        if (unexpected) {
            put(status(false, text("rumble.contribute.ended_unexpectedly")))
        } else {
            val outcome = last.ended?.outcome.orEmpty()
            put(status(ContributeTexts.good(outcome), text(ContributeTexts.forOutcome(outcome))))
            last.ended?.message?.takeIf { it.isNotBlank() && !ContributeTexts.good(outcome) }?.let { put(wrapped(text("rumble.contribute.details", it))) }
            when (ContributeTexts.fixFor(outcome)) {
                ContributeFix.ACCOUNT -> put(button("rumble.contribute.button.account") { RumbleAccountDialog.open() })
                null -> Unit
            }
        }
        counts(last)
    }

    private fun renderRunning(state: ContributeState.Running) {
        val status = state.status
        if (status == null) {
            put(wrapped(text("rumble.contribute.starting")))
        } else {
            put(wrapped(text(ContributeTexts.forState(status.state)), bold = true))
            if (status.state == "waiting") put(wrapped(text(ContributeTexts.forWaiting(status.waitingFor))))
            status.battle?.let { put(wrapped(text("rumble.contribute.battle", it.gameType, it.participants.joinToString(" vs ")))) }
            ContributeTexts.forPython(status.python)?.let { put(wrapped(text(it))) }
            put(wrapped(text(if (status.battleLimit != null) "rumble.contribute.limit" else "rumble.contribute.no_limit", status.formats.joinToString(), status.battleLimit ?: 0)))
            counts(status)
            put(wrapped(text(if (state.ownedHere) "rumble.contribute.owned_here" else "rumble.contribute.runs_on_its_own")))
        }
        state.asked?.let { put(wrapped(text(ContributeTexts.forRequest(it)))) }
        if (state.ownedHere && state.asked != SessionControlRequest.RELEASE) {
            put(button("rumble.contribute.button.release") { controller.release() })
        }
        closeSetting()
        val paused = status?.state == "paused" || status?.state == "pausing"
        val ending = status?.state == "stopping" || status?.state == "cancelling"
        act(
            if (paused) button("rumble.contribute.button.resume") { controller.resume() }
            else button("rumble.contribute.button.pause") { controller.pause() }.apply { isEnabled = !ending },
            button("rumble.contribute.button.stop") { controller.stop() }.apply { isEnabled = !ending },
            button("rumble.contribute.button.stop_now") { controller.stopNow() }.apply { isEnabled = status?.state != "cancelling" },
            button("rumble.contribute.button.close") { isVisible = false },
        )
    }

    private fun counts(status: SessionStatus) {
        put(wrapped(text("rumble.contribute.kept", status.kept, status.notKept)))
        val r = status.results
        put(wrapped(text("rumble.contribute.results", r.waiting, r.sent, r.accepted, r.rejected, r.setAside)))
    }

    /** The setting of what closing Tank Royale does with a session it started. */
    private fun closeSetting() {
        put(wrapped(text("rumble.contribute.close_setting"), bold = true))
        val choices = CloseChoice.entries
        put(JComboBox(choices.map { text(ContributeTexts.forClose(it)) }.toTypedArray()).apply {
            selectedIndex = choices.indexOf(ConfigSettings.rumbleCloseChoice)
            addActionListener { ConfigSettings.rumbleCloseChoice = choices[selectedIndex] }
        })
    }

    private fun megabytes(bytes: Long) = String.format(Locale.ROOT, "%.1f", bytes / (1024.0 * 1024.0))

    private fun choice(box: AbstractButton, label: String, ticked: Boolean, enabled: Boolean, changed: (Boolean) -> Unit): Component {
        box.isSelected = ticked
        box.isEnabled = enabled
        box.text = label
        box.addActionListener { changed(box.isSelected) }
        return box
    }

    /** A line that says at a glance whether it is good news or bad: green for success, red for failure. */
    private fun status(success: Boolean, message: String) =
        wrapped(message, bold = true).apply { foreground = statusColor(success) }

    /** Colors that can be read on both the light and the dark theme. */
    private fun statusColor(success: Boolean): Color {
        val dark = FlatLaf.isLafDark()
        return if (success) (if (dark) Color(0x6FCF73) else Color(0x1B7F2A)) else (if (dark) Color(0xFF7B72) else Color(0xB3261E))
    }

    private fun act(vararg buttons: JButton) = buttons.forEach { actions.add(it) }

    private fun put(component: Component) {
        content.add(component, "growx, wmin 10")
    }

    private fun wrapped(text: String, bold: Boolean = false) = wrappedText(text, bold)

    private fun button(key: String, action: () -> Unit) = JButton(text(key)).apply { addActionListener { action() } }
}
