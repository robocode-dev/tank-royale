package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.tps.TpsField
import dev.robocode.tankroyale.gui.ui.tps.TpsSlider
import dev.robocode.tankroyale.gui.util.GuiTask.enqueue
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import javax.swing.JLabel
import javax.swing.JPanel

object ControlPanel : JPanel() {

    private val pauseResumeButton = addButton("pause", ControlEvents.onPauseResume)
    private val nextButton = addButton("next_turn", ControlEvents.onNextTurn).apply {
        isEnabled = false
    }
    private val stopButton = addButton("stop", ControlEvents.onStop)

    init {
        addButton("restart", ControlEvents.onRestart)

        RegisterWsProtocol

        add(TpsSlider)
        add(JLabel("TPS:"))
        add(TpsField)

        ClientEvents.apply {
            onGamePaused.subscribe(ControlPanel) {
                setResumedText()

                nextButton.isEnabled = true
                setDefaultButton(nextButton)
            }
            onGameResumed.subscribe(ControlPanel) {
                setPausedText()

                nextButton.isEnabled = false
            }
            onGameStarted.subscribe(ControlPanel) {
                setPausedText()
            }
        }

        ControlEvents.apply {
            onStop.subscribe(ControlPanel) {
                pauseResumeButton.isEnabled = false
                stopButton.isEnabled = false
            }
            onRestart.subscribe(ControlPanel) {
                pauseResumeButton.isVisible = true
                stopButton.isEnabled = true
            }
        }

        ServerEvents.onStarted.subscribe(ControlPanel) {
            pauseResumeButton.isEnabled = true
            stopButton.isEnabled = true
        }

        enqueue {
            setDefaultButton(pauseResumeButton)
        }
    }

    private fun setPausedText() {
        pauseResumeButton.text = Strings.get("pause")
    }

    private fun setResumedText() {
        pauseResumeButton.text = Strings.get("resume")
    }
}