package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.gui.ui.components.WrapLayout
import dev.robocode.tankroyale.gui.ui.tps.TpsField
import dev.robocode.tankroyale.gui.ui.tps.TpsSlider
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

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

            add(JPanel().apply {
                add(JLabel("TPS:"))
                add(TpsField)
            })
        }

        buttonPanel.layout = WrapLayout()

        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)

        Client.apply {
            onGamePaused.subscribe(ControlPanel) { setResumedText() }
            onGameResumed.subscribe(ControlPanel) { setPausedText() }
            onGameStarted.subscribe(ControlPanel) { setPausedText() }
        }

        onStop.subscribe(ControlPanel) { Client.stopGame(); setPausedText() }
        onRestart.subscribe(ControlPanel) { Client.restartGame() }

        onPauseResume.subscribe(ControlPanel) {
            Client.apply {
                if (isGamePaused) {
                    resumeGame()
                } else {
                    pauseGame()
                }
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