package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
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
            addNewButton("battle.stop", onStop)
            addNewButton("battle.restart", onRestart)
            pauseResumeButton = addNewButton("battle.pause", onPauseResume)
        }

        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)

        Client.onGamePaused.subscribe { pauseResumeButton.text = STRINGS.get("battle.resume") }
        Client.onGameResumed.subscribe { pauseResumeButton.text = STRINGS.get("battle.pause") }
        Client.onGameStarted.subscribe { pauseResumeButton.text = STRINGS.get("battle.pause") }

        onStop.subscribe { Client.stopGame() }
        onRestart.subscribe { Client.restartGame() }

        onPauseResume.subscribe {
            if (Client.isGamePaused) {
                println("Client.resumeGame()")
                Client.resumeGame()
            } else {
                println("Client.pauseGame()")
                Client.pauseGame()
            }
        }
    }
}