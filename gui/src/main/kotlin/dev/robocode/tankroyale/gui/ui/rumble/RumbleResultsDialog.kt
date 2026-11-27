package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.rumble.BatchRow
import dev.robocode.tankroyale.rumble.BatchStanding
import dev.robocode.tankroyale.rumble.EnvironmentChoice
import dev.robocode.tankroyale.rumble.GetStarted
import dev.robocode.tankroyale.rumble.LibraryResultsPort
import dev.robocode.tankroyale.rumble.ResultCounts
import dev.robocode.tankroyale.rumble.ResultsController
import dev.robocode.tankroyale.rumble.ResultsQueue
import dev.robocode.tankroyale.rumble.ResultsState
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.RumbleLinks
import dev.robocode.tankroyale.rumble.RumbleMenuSwitch
import dev.robocode.tankroyale.rumble.SendAgainOutcome
import dev.robocode.tankroyale.rumble.SignInStore
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder

/**
 * The Rumble's Results and queue screen. It only shows the state of a [ResultsController], which reads the results this
 * computer keeps once, away from the event thread, and never writes to them. Every sentence comes from the resource files.
 */
class RumbleResultsDialog private constructor(environment: RumbleEnvironment, controllerFor: (RumbleResultsDialog) -> ResultsController) :
    RcDialog(MainFrame, "rumble_results_dialog") {

    private val content = WindowWidthPanel()
    private val actions = JPanel(GridLayout(1, 0, 8, 0)).apply { border = EmptyBorder(6, 12, 12, 12) }
    private val controller: ResultsController = controllerFor(this)

    init {
        title = text("rumble.results.title", title, environment.displayName)
        contentPane.add(JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply { border = null }, BorderLayout.CENTER)
        contentPane.add(actions, BorderLayout.SOUTH)
        setSize(760, 480)
        setLocationRelativeTo(owner)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) = controller.close()
        })
        render(controller.state)
        controller.start()
    }

    private fun text(key: String, vararg arguments: Any): String = String.format(Messages.get(key), *arguments)

    /** Shows [state]. Called on the event thread. */
    private fun render(state: ResultsState) {
        content.removeAll()
        actions.removeAll()
        when (state) {
            ResultsState.Reading -> put(wrappedText(text("rumble.results.reading")))
            is ResultsState.Failed -> {
                put(wrappedText(text("rumble.results.failed"), bold = true).apply { foreground = myBotsColor(good = false) })
                if (state.details.isNotBlank()) put(wrappedText(text("rumble.results.details", state.details)))
            }
            is ResultsState.Shown -> {
                val overview = state.overview
                put(countTiles(overview.counts) { key, arguments -> text(key, *arguments) })
                overview.refusal?.let { reason ->
                    put(wrappedText(text("rumble.results.refused", reason), bold = true).apply { foreground = myBotsColor(good = false) })
                    put(wrappedText(text("rumble.results.refused_version", overview.refusedVersion ?: text("rumble.results.refused_version_unknown"))))
                    put(wrappedText(text("rumble.results.refused_ways")))
                    actions.add(JButton(text("rumble.results.button.send_again")).apply { addActionListener { controller.sendAgain() } })
                    actions.add(JButton(text("rumble.results.button.feedback")).apply { addActionListener { Browser.browse(GetStarted.FEEDBACK_PAGE) } })
                }
                state.sentAgain?.let { outcome ->
                    put(wrappedText(text("rumble.results.sent_again." + outcome.name.lowercase())).apply {
                        foreground = myBotsColor(good = outcome == SendAgainOutcome.LIFTED || outcome == SendAgainOutcome.NOT_STOPPED)
                    })
                }
                if (ResultsQueue.waitsToBeSent(overview)) put(wrappedText(text("rumble.results.waiting_note", overview.counts.waiting)))
                if (ResultsQueue.needsSignIn(overview)) {
                    put(wrappedText(text("rumble.results.sign_in_needed"), bold = true))
                    actions.add(JButton(text("rumble.results.button.account")).apply { addActionListener { dispose(); RumbleAccountDialog.open() } })
                }
                if (overview.counts.setAside > 0) put(wrappedText(text("rumble.results.set_aside_note", overview.counts.setAside)))
                put(JLabel(text("rumble.results.batches")).apply { font = font.deriveFont(Font.BOLD) })
                if (overview.batches.isEmpty()) put(wrappedText(text("rumble.results.no_batches")))
                else put(resultsBatchTable(overview.batches, { key, arguments -> text(key, *arguments) }, { Hints.get(it) }) { Browser.browse(it) })
            }
        }
        content.revalidate()
        content.repaint()
        actions.revalidate()
        actions.repaint()
    }

    private fun put(component: Component) {
        content.add(component, "growx, wmin 10")
    }

    companion object {
        private var open: RumbleResultsDialog? = null

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

        private fun show(environment: RumbleEnvironment): RumbleResultsDialog {
            val background = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "rumble-results").apply { isDaemon = true } }
            val post = Executor { SwingUtilities.invokeLater(it) }
            val base = UserDataDirectory.get().toPath()
            val port = LibraryResultsPort(environment, environment.stateRoot(base)) { GetStarted.hasSavedSignIn(SignInStore(environment, base)) }
            return RumbleResultsDialog(environment) { self ->
                ResultsController(port, background, post) { state -> self.render(state) }
            }.apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosed(e: WindowEvent) = SwingUtilities.invokeLater { background.shutdown() }
                })
                isVisible = true
            }
        }
    }
}

/** The counts as small coloured tiles: kept in all, waiting, sent, accepted, rejected and set aside. */
internal fun countTiles(counts: ResultCounts, text: (String, Array<Any>) -> String): JPanel =
    JPanel(MigLayout("insets 0, gap 6, wrap 6", "[grow, fill, sg tile][grow, fill, sg tile][grow, fill, sg tile][grow, fill, sg tile][grow, fill, sg tile][grow, fill, sg tile]")).apply {
        isOpaque = false
        listOf(
            Triple("kept", counts.kept, CardTone.NEUTRAL),
            Triple("waiting", counts.waiting, CardTone.AMBER),
            Triple("sent", counts.sent, CardTone.BLUE),
            Triple("accepted", counts.accepted, CardTone.GREEN),
            Triple("rejected", counts.rejected, CardTone.RED),
            Triple("set_aside", counts.setAside, CardTone.NEUTRAL),
        ).forEach { (name, count, tone) ->
            // A tile that counts nothing stays neutral: a red tile for no rejected results would alarm for nothing.
            add(RumbleCard(MigLayout("insets 0, wrap 1, alignx center", "[center]"), if (count == 0) CardTone.NEUTRAL else tone, padding = 6).apply {
                add(JLabel(count.toString()).apply { font = font.deriveFont(Font.BOLD, font.size2D * 1.4f) })
                add(JLabel(text("rumble.results.count.$name", emptyArray())).apply { font = font.deriveFont(font.size2D * 0.9f) })
            })
        }
    }

/**
 * The sent batches, newest first: the batch, its battles, when it was sent, where it stands, and Open for its issue,
 * which is switched off for a batch without an issue on github.com. The Rumble's reasons for rejected results follow
 * their batch. It is built without a window, so a test can build it; [hint] reads a tooltip from the hint files.
 */
internal fun resultsBatchTable(rows: List<BatchRow>, text: (String, Array<Any>) -> String, hint: (String) -> String, onOpen: (String) -> Unit): JPanel =
    JPanel(MigLayout("insets 0, gapx 12, gapy 6, wrap 5, fillx", "[][][][grow, fill][]")).apply {
        isOpaque = false
        listOf("batch", "battles", "sent", "state").forEach { add(JLabel(text("rumble.results.column.$it", emptyArray())).apply { font = font.deriveFont(Font.BOLD) }) }
        add(JLabel())
        rows.forEach { row ->
            val page = row.issuePage?.takeIf { RumbleLinks.isGitHubPage(it) }
            // The first part of the id is enough to tell batches apart; the whole id is in the tooltip.
            add(HintLabel(row.batchId.take(SHORT_ID)).apply { toolTipText = String.format(hint("rumble.results.batch_id"), row.batchId) })
            add(JLabel(row.battles.toString()))
            add(JLabel(row.sentAt?.let { sentAt(it) } ?: text("rumble.results.not_known", emptyArray())))
            add(wrappedText(standing(row.standing, text)).apply {
                val answered = row.standing as? BatchStanding.Answered
                if (answered != null) foreground = myBotsColor(good = answered.rejected == 0 && answered.other == 0)
            }, "wmin 10")
            add(HintButton(text("rumble.results.button.open", emptyArray())).apply {
                isEnabled = page != null
                toolTipText = hint("rumble.results.open_issue")
                addActionListener { page?.let(onOpen) }
            })
            if (row.reasons.isNotEmpty()) {
                add(wrappedText(text("rumble.results.reasons", arrayOf(row.reasons.joinToString("; ")))).apply { foreground = myBotsColor(good = false) }, "skip 3, span 2, wmin 10")
            }
        }
    }

/** How many characters of a batch id the list shows. */
private const val SHORT_ID = 8

/** A label whose tooltip is the program's own yellow tooltip. */
internal class HintLabel(text: String) : JLabel(text) {
    override fun createToolTip() = RcToolTip()
}

/** Where a batch stands, in words from the resource files. */
internal fun standing(standing: BatchStanding, text: (String, Array<Any>) -> String): String = when (standing) {
    BatchStanding.WaitingForReceipt -> text("rumble.results.state.waiting", emptyArray())
    is BatchStanding.Answered -> buildString {
        append(
            if (standing.rejected == 0) text("rumble.results.state.accepted", arrayOf(standing.accepted))
            else text("rumble.results.state.rejected", arrayOf(standing.accepted, standing.rejected))
        )
        if (standing.other > 0) append(" ").append(text("rumble.results.state.other", arrayOf(standing.other)))
    }
}

/** When a batch was sent, in this computer's time zone. */
private fun sentAt(instant: String): String = try {
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault()).format(Instant.parse(instant))
} catch (e: Exception) {
    instant
}
