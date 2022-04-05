package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.gui.ui.arena.ArenaPanel
import dev.robocode.tankroyale.gui.ui.components.WrapLayout
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.tps.TpsField
import dev.robocode.tankroyale.gui.ui.tps.TpsSlider
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

object ControlPanel : JPanel() {

    init {
        ControlEventHandlers

        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(ButtonPanel, BorderLayout.SOUTH)

        ClientEvents.apply {
            onGamePaused.subscribe(ControlPanel) { ButtonPanel.setResumedText() }
            onGameResumed.subscribe(ControlPanel) { ButtonPanel.setPausedText() }
            onGameStarted.subscribe(ControlPanel) { ButtonPanel.setPausedText() }
        }
    }

    private object ButtonPanel : JPanel() {
        val pauseResumeButton = addButton("battle.pause", ControlEvents.onPauseResume)

        init {
            layout = WrapLayout()

            addButton("battle.stop", ControlEvents.onStop)
            addButton("battle.restart", ControlEvents.onRestart)
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
