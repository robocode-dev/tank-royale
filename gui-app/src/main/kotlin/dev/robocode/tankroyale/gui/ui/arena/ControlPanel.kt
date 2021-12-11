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

    private val onStop = Event<JButton>()
    private val onRestart = Event<JButton>()
    private val onPauseResume = Event<JButton>()

    init {
        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(ButtonPanel, BorderLayout.SOUTH)

        Client.apply {
            onGamePaused.subscribe(ControlPanel) { ButtonPanel.setResumedText() }
            onGameResumed.subscribe(ControlPanel) { ButtonPanel.setPausedText() }
            onGameStarted.subscribe(ControlPanel) { ButtonPanel.setPausedText() }
        }

        onStop.subscribe(ControlPanel) { Client.stopGame(); ButtonPanel.setPausedText() }
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

    private object ButtonPanel : JPanel() {
        val pauseResumeButton = addButton("battle.pause", onPauseResume)

        init {
            layout = WrapLayout()

            addButton("battle.stop", onStop)
            addButton("battle.restart", onRestart)
            add(TpsSlider)

            add(JPanel().apply {
                add(JLabel("TPS:"))
                add(TpsField)
            })
        }

        fun setPausedText() {
            pauseResumeButton.text = STRINGS.get("battle.pause")
        }

        fun setResumedText() {
            pauseResumeButton.text = STRINGS.get("battle.resume")
        }
    }
}
