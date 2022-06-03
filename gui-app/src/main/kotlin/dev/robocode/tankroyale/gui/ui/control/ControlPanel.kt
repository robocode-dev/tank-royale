package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameStarted
import dev.robocode.tankroyale.gui.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.settings.ConfigSettings.DEFAULT_TPS
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.tps.TpsEvents
import dev.robocode.tankroyale.gui.ui.tps.TpsField
import dev.robocode.tankroyale.gui.ui.tps.TpsSlider
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.GuiTask.enqueue
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

object ControlPanel : JPanel() {

    private val pauseResumeButton = addButton("pause", ControlEvents.onPauseResume)
    private val nextButton = addButton("next_turn", ControlEvents.onNextTurn).apply {
        isEnabled = false
    }
    private val stopButton = addButton("stop", ControlEvents.onStop)

    private val onDefaultTps = Event<JButton>()

    init {
        addButton("restart", ControlEvents.onRestart)

        RegisterWsProtocol

        add(TpsSlider)
        add(JLabel("TPS:"))
        add(TpsField)
        addButton("default_tps", onDefaultTps)

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
                enablePauseResumeAndStopButtons()
            }
            onRestart.subscribe(ControlPanel) {
                enablePauseResumeAndStopButtons()
            }
            onGameStarted.subscribe(ControlPanel) {
                enablePauseResumeAndStopButtons()
            }
        }

        onDefaultTps.subscribe(ControlPanel) {
            TpsEvents.onTpsChanged.fire(TpsChangedEvent(DEFAULT_TPS))
        }

        enqueue {
            setDefaultButton(pauseResumeButton)
        }
    }

    private fun enablePauseResumeAndStopButtons() {
        pauseResumeButton.isEnabled = true
        stopButton.isEnabled = true
    }

    private fun setPausedText() {
        pauseResumeButton.text = Strings.get("pause")
    }

    private fun setResumedText() {
        pauseResumeButton.text = Strings.get("resume")
    }
}