package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import java.awt.EventQueue
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.UIManager

object ControlPanel : JPanel() {

    // Private events
    private val onStop = Event<JButton>()
    private val onRestart = Event<JButton>()
    private val onPauseResume = Event<JButton>()

    private var pauseResumeButton: JButton

    init {
        val buttonPanel = JPanel().apply {
            addButton("battle.stop", onStop)
            addButton(
                "battle.restart",
                onRestart
            )
            pauseResumeButton = addButton(
                "battle.pause",
                onPauseResume
            )
            add(TpsSlider)
        }

        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)

        Client.onGamePaused.subscribe { setResumedText() }
        Client.onGameResumed.subscribe { setPausedText() }
        Client.onGameStarted.subscribe { setPausedText() }

        onStop.subscribe { Client.stopGame(); setPausedText() }
        onRestart.subscribe { Client.restartGame() }

        onPauseResume.subscribe {
            if (Client.isGamePaused) {
                Client.resumeGame()
            } else {
                Client.pauseGame()
            }
        }
    }

    private fun setPausedText() {
        pauseResumeButton.text = STRINGS.get("battle.pause")
    }

    private fun setResumedText() {
        pauseResumeButton.text = STRINGS.get("battle.resume")
    }
}

private fun main() {
    ControlPanel.isVisible = true
}