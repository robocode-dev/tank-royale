package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.booter.BotIdentity
import dev.robocode.tankroyale.gui.booter.BotMatcher
import dev.robocode.tankroyale.gui.client.ClientEvents
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Window
import javax.swing.*

/**
 * Modal dialog shown while waiting for expected bots to connect to the server.
 *
 * @param owner the parent window
 * @param expectedIdentities the list of expected bot identities (may contain duplicates)
 * @param timeoutSeconds how many seconds to wait before showing a timeout error
 * @param onSuccess called when all expected identities are matched in the bot list
 * @param onCancel called when the user cancels or the dialog is closed without success
 */
class BootProgressDialog(
    owner: Window?,
    expectedIdentities: List<BotIdentity>,
    private val timeoutSeconds: Int = 30,
    private val onSuccess: () -> Unit,
    private val onCancel: () -> Unit,
) : JDialog(owner, "Waiting for bots to connect...", ModalityType.APPLICATION_MODAL) {

    private val matcher = BotMatcher(expectedIdentities)

    private var elapsedHalfSeconds = 0
    private var timedOut = false

    private val statusListModel = DefaultListModel<String>()
    private val statusList = JList(statusListModel)
    private val elapsedLabel = JLabel("Elapsed: 0s / ${timeoutSeconds}s")
    private val elapsedSeconds: Int get() = elapsedHalfSeconds / 2
    private val cancelButton = JButton("Cancel")
    private val retryButton = JButton("Retry")

    private val statusScrollPane = JScrollPane(statusList)
    private val centerPanel = JPanel(BorderLayout())

    private val timer = Timer(500) { onTimerTick() }

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        isResizable = false
        size = Dimension(420, 320)
        setLocationRelativeTo(owner)

        buildNormalLayout()

        cancelButton.addActionListener { handleCancel() }
        retryButton.addActionListener { handleRetry() }

        updateStatusList()
    }

    override fun setVisible(visible: Boolean) {
        if (visible) {
            subscribeToEvents()
            timer.start()
        } else {
            timer.stop()
            unsubscribeFromEvents()
        }
        super.setVisible(visible)
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    private fun buildNormalLayout() {
        contentPane.removeAll()
        contentPane.layout = MigLayout("fill, insets 10", "[grow]", "[grow][pref]")

        centerPanel.add(statusScrollPane, BorderLayout.CENTER)
        contentPane.add(centerPanel, "grow, wrap")

        val bottomPanel = JPanel(MigLayout("insets 0", "[grow][]", "[]"))
        bottomPanel.add(elapsedLabel, "left")
        bottomPanel.add(cancelButton, "right")
        contentPane.add(bottomPanel, "growx")

        contentPane.revalidate()
        contentPane.repaint()
    }

    private fun buildTimeoutLayout(pendingLines: List<String>) {
        contentPane.removeAll()
        contentPane.layout = MigLayout("fill, insets 10", "[grow]", "[pref][grow][pref]")

        val errorLabel = JLabel("<html><b>Timeout — the following bots did not connect:</b></html>")
        contentPane.add(errorLabel, "wrap")

        val pendingModel = DefaultListModel<String>()
        pendingLines.forEach { pendingModel.addElement(it) }
        val pendingList = JList(pendingModel)
        contentPane.add(JScrollPane(pendingList), "grow, wrap")

        val bottomPanel = JPanel(MigLayout("insets 0", "[grow][][]", "[]"))
        bottomPanel.add(elapsedLabel, "left")
        bottomPanel.add(retryButton, "right")
        bottomPanel.add(cancelButton, "right")
        contentPane.add(bottomPanel, "growx")

        contentPane.revalidate()
        contentPane.repaint()
    }

    // -------------------------------------------------------------------------
    // Event subscription
    // -------------------------------------------------------------------------

    private fun subscribeToEvents() {
        ClientEvents.onBotListUpdate.on(this) { update ->
            SwingUtilities.invokeLater {
                matcher.update(update.bots)
                updateStatusList()
                if (matcher.isComplete) {
                    timer.stop()
                    unsubscribeFromEvents()
                    dispose()
                    onSuccess()
                }
            }
        }
    }

    private fun unsubscribeFromEvents() {
        ClientEvents.onBotListUpdate.off(this)
    }

    // -------------------------------------------------------------------------
    // Timer tick
    // -------------------------------------------------------------------------

    private fun onTimerTick() {
        elapsedHalfSeconds++
        elapsedLabel.text = "Elapsed: ${elapsedSeconds}s / ${timeoutSeconds}s"

        if (!timedOut && elapsedSeconds >= timeoutSeconds) {
            timedOut = true
            timer.stop()
            val pendingLines = matcher.pending.map { (identity, count) ->
                "\u23F3 ${identity.name} ${identity.version} ($count pending)"
            }
            buildTimeoutLayout(pendingLines)
        }
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    private fun handleCancel() {
        timer.stop()
        unsubscribeFromEvents()
        dispose()
        onCancel()
    }

    private fun handleRetry() {
        timedOut = false
        elapsedHalfSeconds = 0
        elapsedLabel.text = "Elapsed: 0s / ${timeoutSeconds}s"
        buildNormalLayout()
        updateStatusList()
        timer.start()
    }

    // -------------------------------------------------------------------------
    // Status list update
    // -------------------------------------------------------------------------

    private fun updateStatusList() {
        statusListModel.clear()
        matcher.expected.forEach { (identity, expectedCount) ->
            val connectedCount = matcher.connected[identity] ?: 0
            val icon = if (connectedCount >= expectedCount) "\u2705" else "\u23F3"
            val label = if (expectedCount > 1) {
                "$icon ${identity.name} ${identity.version} ($connectedCount/$expectedCount connected)"
            } else {
                "$icon ${identity.name} ${identity.version}"
            }
            statusListModel.addElement(label)
        }
    }
}
