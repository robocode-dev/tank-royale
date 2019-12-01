package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.ui.desktop.util.Event
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

object BattlePanel : JPanel() {

    // Private events
    private val onStop = Event<JButton>()
    private val onRestart = Event<JButton>()
    private val onPauseResume = Event<JButton>()

    private var pauseResumeButton: JButton

    init {
        val buttonPanel = JPanel().apply {
            addButton("battle.stop", onStop)
            addButton("battle.restart", onRestart)
            pauseResumeButton = addButton("battle.pause", onPauseResume)
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