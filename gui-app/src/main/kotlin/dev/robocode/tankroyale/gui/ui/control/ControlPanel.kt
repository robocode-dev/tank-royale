package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.WrapLayout
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.tps.TpsField
import dev.robocode.tankroyale.gui.ui.tps.TpsSlider
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import javax.swing.JLabel
import javax.swing.JPanel

object ControlPanel : JPanel() {
    private val pauseResumeButton = addButton("pause", ControlEvents.onPauseResume)
    private val stopButton = addButton("stop", ControlEvents.onStop)
    private val restartButton = addButton("restart", ControlEvents.onRestart)

    init {
        RegisterWsProtocol

        layout = WrapLayout()

        add(TpsSlider)

        add(JPanel().apply {
            add(JLabel("TPS:"))
            add(TpsField)
        })

        ClientEvents.apply {
            onGamePaused.subscribe(ControlPanel) { setResumedText() }
            onGameResumed.subscribe(ControlPanel) { setPausedText() }
            onGameStarted.subscribe(ControlPanel) { setPausedText() }
        }

        ControlEvents.apply {
            onStop.subscribe(ControlPanel) {
                pauseResumeButton.isEnabled = false
                stopButton.isEnabled = false
                setDefaultButton(restartButton)
            }
            onRestart.subscribe(ControlPanel) {
                pauseResumeButton.isEnabled = true
                stopButton.isEnabled = true
            }
        }
    }

    private fun setPausedText() {
        pauseResumeButton.text = Strings.get("pause")
    }

    private fun setResumedText() {
        pauseResumeButton.text = Strings.get("resume")
    }
}
