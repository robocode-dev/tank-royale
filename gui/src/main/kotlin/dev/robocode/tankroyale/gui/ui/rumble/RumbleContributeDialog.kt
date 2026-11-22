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
import java.awt.Font
import java.time.Duration
import java.time.Instant
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.Timer
import javax.swing.UIManager
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

    /** The battle running, when it started (from the session, or when this screen first saw it), and the clocks showing it. */
    private var battleStartedAt: Instant? = null
    private var battleSeen: String? = null
    private var battleSeenAt: Instant? = null
    private val elapsedLabel = JLabel()
    private val checkedLabel = JLabel().apply { font = font.deriveFont(font.size2D * 0.85f) }

    /** Moves the clocks on every second while the screen is shown, so it is plain that the status is followed live. */
    private val clock = Timer(1000) { if (isVisible) tick() }.apply { start() }

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
        // A session that ended while this screen followed it stays shown as done until the person asks for a new one.
        val ended = (state as? ContributeState.Choosing)?.last
        if (before is ContributeState.Running && ended != null) {
            doneSession = sessionKey(ended)
        }
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
            is ContributeState.Choosing ->
                if (ended != null && doneSession == sessionKey(ended)) renderDone(state, ended) else renderChoosing(state)
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
            last.ended?.message?.takeIf { it.isNotBlank() && !ContributeTexts.ordinary(outcome) }?.let { put(wrapped(text("rumble.contribute.details", it))) }
            when (ContributeTexts.fixFor(outcome)) {
                ContributeFix.ACCOUNT -> put(button("rumble.contribute.button.account") { RumbleAccountDialog.open() })
                null -> Unit
            }
        }
        counts(last)
    }

    /** The session this screen followed and that ended, shown as done until the person asks for a new session. */
    private var doneSession: String? = null

    private fun sessionKey(status: SessionStatus) = "${status.pid}@${status.startedAt}"

    /** How the session this screen followed ended, in the same look as while it ran, with its counts. */
    private fun renderDone(state: ContributeState.Choosing, last: SessionStatus) {
        val outcome = last.ended?.outcome.orEmpty()
        val good = !state.lastUnexpected && ContributeTexts.good(outcome)
        put(headline("\u25CF " + text("rumble.contribute.done"), statusColor(good)))
        put(wrapped(text(if (state.lastUnexpected) "rumble.contribute.ended_unexpectedly" else ContributeTexts.forOutcome(outcome))))
        if (!state.lastUnexpected) {
            last.ended?.message?.takeIf { it.isNotBlank() && !ContributeTexts.ordinary(outcome) }?.let { put(small(text("rumble.contribute.details", it))) }
            if (ContributeTexts.fixFor(outcome) == ContributeFix.ACCOUNT) put(button("rumble.contribute.button.account") { RumbleAccountDialog.open() })
        }
        tiles(last)
        act(
            button("rumble.contribute.button.new_session") {
                doneSession = null
                shown = null
                render(controller.state)
            },
            button("rumble.contribute.button.close") { isVisible = false },
        )
    }

    private fun renderRunning(state: ContributeState.Running) {
        val status = state.status
        if (status == null) {
            put(headline(text("rumble.contribute.starting"), stateColor("starting")))
            putBar(JProgressBar().apply { isIndeterminate = true })
        } else {
            // What the session is doing, in its colour, first; then how far it is, the battle running and the counts.
            put(headline("\u25CF " + text(ContributeTexts.forState(status.state)), stateColor(status.state)))
            if (status.state == "waiting") put(wrapped(text(ContributeTexts.forWaiting(status.waitingFor))))
            putBar(progress(status))
            status.battle?.let { battle ->
                put(wrapped(text("rumble.contribute.current", battle.gameType, battle.participants.joinToString(" vs ")), bold = true))
                battleStartedAt = battle.startedAt.takeIf { it.isNotBlank() }?.let { runCatching { Instant.parse(it) }.getOrNull() }
                    ?: battleSeenAt.takeIf { battleSeen == battle.battleId } ?: Instant.now()
                if (battleSeen != battle.battleId) {
                    battleSeen = battle.battleId
                    battleSeenAt = battleStartedAt
                }
                put(elapsedLabel)
            } ?: run { battleStartedAt = null }
            tiles(status)
            ContributeTexts.forPython(status.python)?.let { put(small(text(it))) }
            put(small(text(if (status.battleLimit != null) "rumble.contribute.limit" else "rumble.contribute.no_limit", status.formats.joinToString(), status.battleLimit ?: 0)))
            put(small(text(if (state.ownedHere) "rumble.contribute.owned_here" else "rumble.contribute.runs_on_its_own")))
        }
        put(checkedLabel)
        tick()
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

    /** The session's state in large letters and its colour, which tells at a glance that it runs, waits, pauses or stops. */
    private fun headline(message: String, color: Color) = JLabel(message).apply {
        font = font.deriveFont(Font.BOLD, font.size2D * 1.5f)
        foreground = color
    }

    /** How far the session is: "Battle X of N" with a bar, or the battles kept so far for a session that runs until stopped. */
    private fun progress(status: SessionStatus): JProgressBar {
        val limit = status.battleLimit
        // While no battle runs yet, such as while the session starts or sets up Python, the bar keeps moving.
        val busy = status.battle == null && (status.state == "starting" || status.state == "sending" || status.python == "setting-up")
        return if (limit != null) {
            val current = (status.kept + if (status.battle != null) 1 else 0).coerceAtMost(limit).coerceAtLeast(1)
            JProgressBar(0, limit).apply {
                value = status.kept
                isIndeterminate = busy
                // No "Battle X of N" while no battle has started; the headline says that the session starts.
                isStringPainted = !busy
                string = text("rumble.contribute.progress", current, limit)
            }
        } else {
            JProgressBar().apply {
                isIndeterminate = busy || status.state == "running"
                isStringPainted = !busy
                string = text("rumble.contribute.progress_open", status.kept)
            }
        }
    }

    /** A progress bar tall enough to be seen at a glance. */
    private fun putBar(bar: JProgressBar) {
        content.add(bar, "growx, wmin 10, h 22!")
    }

    /**
     * The counts as small tiles that can be read at a glance, in two groups: the battles of this session, and the
     * results on this computer from every session, which is where waiting, sent and accepted results are counted.
     */
    private fun tiles(status: SessionStatus) {
        val r = status.results
        put(small(text("rumble.contribute.tiles.session")).apply { font = font.deriveFont(Font.BOLD) })
        put(tileRow(listOf("rumble.contribute.tile.kept" to status.kept, "rumble.contribute.tile.not_kept" to status.notKept)))
        put(small(text("rumble.contribute.tiles.all")).apply { font = font.deriveFont(Font.BOLD) })
        put(tileRow(listOfNotNull(
            "rumble.contribute.tile.waiting" to r.waiting,
            "rumble.contribute.tile.sent" to r.sent,
            "rumble.contribute.tile.accepted" to r.accepted,
            "rumble.contribute.tile.rejected" to r.rejected,
            ("rumble.contribute.tile.set_aside" to r.setAside).takeIf { r.setAside > 0 },
        )))
    }

    private fun tileRow(counts: List<Pair<String, Int>>): JPanel {
        return JPanel(MigLayout("insets 0, gap 6, wrap 5", "[grow, fill, sg tile][grow, fill, sg tile][grow, fill, sg tile][grow, fill, sg tile][grow, fill, sg tile]")).apply {
            counts.forEach { (key, count) ->
                add(JPanel(MigLayout("insets 6, wrap 1, alignx center", "[center]")).apply {
                    border = BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor") ?: Color.GRAY)
                    add(JLabel(count.toString()).apply {
                        font = font.deriveFont(Font.BOLD, font.size2D * 1.4f)
                        // Good news in green and bad news in red, but only when there is any: a red zero would alarm for nothing.
                        if (count > 0) when (key) {
                            "rumble.contribute.tile.accepted" -> foreground = statusColor(true)
                            "rumble.contribute.tile.rejected", "rumble.contribute.tile.not_kept" -> foreground = statusColor(false)
                        }
                    })
                    add(JLabel(text(key)).apply { font = font.deriveFont(font.size2D * 0.9f) })
                })
            }
        }
    }

    private fun small(message: String) = wrapped(message).apply { font = font.deriveFont(font.size2D * 0.9f) }

    /** Colours for the session's states that can be read on both the light and the dark theme. */
    private fun stateColor(state: String): Color {
        val dark = FlatLaf.isLafDark()
        return when (state) {
            "running", "sending" -> if (dark) Color(0x6FCF73) else Color(0x1B7F2A)
            "waiting", "pausing", "paused" -> if (dark) Color(0xE6C35C) else Color(0x8A6D00)
            "stopping", "cancelling" -> if (dark) Color(0xF0A060) else Color(0xA14A00)
            else -> UIManager.getColor("Label.foreground") ?: Color.GRAY
        }
    }

    /** Moves the clocks on: how long the battle has run, and how long ago the session was last looked at. */
    private fun tick() {
        val now = Instant.now()
        val started = battleStartedAt
        elapsedLabel.text = if (started != null) text("rumble.contribute.elapsed", duration(Duration.between(started, now))) else ""
        val looked = controller.lastLook
        checkedLabel.text = if (looked != null) text("rumble.contribute.checked", Duration.between(looked, now).seconds.coerceAtLeast(0)) else ""
    }

    private fun duration(d: Duration): String {
        val seconds = d.seconds.coerceAtLeast(0)
        return if (seconds >= 3600) String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
        else String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)
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
