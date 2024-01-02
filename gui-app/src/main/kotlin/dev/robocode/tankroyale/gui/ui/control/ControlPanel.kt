package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameStarted
import dev.robocode.tankroyale.gui.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.settings.ConfigSettings.DEFAULT_TPS
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.tps.TpsEvents
import dev.robocode.tankroyale.gui.ui.tps.TpsField
import dev.robocode.tankroyale.gui.ui.tps.TpsSlider
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import java.awt.EventQueue
import javax.swing.JButton
import javax.swing.JPanel

object ControlPanel : JPanel() {

    private val pauseResumeButton = addButton("pause", ControlEvents.onPauseResume).apply {
        toolTipText = Hints.get("control.pause")
    }

    private val nextButton = addButton("next_turn", ControlEvents.onNextTurn).apply {
        toolTipText = Hints.get("control.next_turn")
        isEnabled = false
    }
    private val stopButton = addButton("stop", ControlEvents.onStop).apply {
        toolTipText = Hints.get("control.stop")
    }

    private val onDefaultTps = Event<JButton>()

    init {
        addButton("restart", ControlEvents.onRestart).apply {
            toolTipText = Hints.get("control.restart")
        }

        RegisterWsProtocol

        add(TpsSlider)
        val tpsLabel = addLabel("tps_label")
        add(TpsField)
        addButton("default_tps", onDefaultTps).apply {
            toolTipText = Hints.get("control.default_tps").format(DEFAULT_TPS)
        }

        val tpsHint = Hints.get("control.tps")

        TpsSlider.toolTipText = tpsHint
        tpsLabel.toolTipText = tpsHint
        TpsField.toolTipText = tpsHint

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
                enablePauseResumeAndStopButtons(false)
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

        EventQueue.invokeLater {
            setDefaultButton(pauseResumeButton)
        }
    }

    private fun enablePauseResumeAndStopButtons(enable: Boolean = true) {
        pauseResumeButton.isEnabled = enable
        stopButton.isEnabled = enable
    }

    private fun setPausedText() {
        pauseResumeButton.apply {
            text = Strings.get("pause")
            toolTipText = Hints.get("control.pause")
        }
    }

    private fun setResumedText() {
        pauseResumeButton.apply {
            text = Strings.get("resume")
            toolTipText = Hints.get("control.resume")
        }
    }
}