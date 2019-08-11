package net.robocode2.gui.ui.battle

import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.ui.ResourceBundles
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import net.robocode2.gui.utils.Event
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

object BattlePanel : JPanel() {

    // Private events
    private val onStop = Event<JButton>()
    private val onRestart = Event<JButton>()
    private val onPauseResume = Event<JButton>()

    private var pauseResumeButton: JButton

    private var isPaused = false

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
            }  else {
                println("Client.pauseGame()")
                Client.pauseGame()
            }
        }
    }
}