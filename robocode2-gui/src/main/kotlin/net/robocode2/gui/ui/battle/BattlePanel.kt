package net.robocode2.gui.ui.battle

import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.utils.Event
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

object BattlePanel : JPanel() {

    // Private events
    private val onStop = Event<JButton>()
    private val onRestart = Event<JButton>()
    private val onPause = Event<JButton>()

    init {
        val buttonPanel = JPanel().apply {
            addNewButton("battle.stop", onStop)
            addNewButton("battle.restart", onRestart)
            addNewButton("battle.pause", onPause)
        }

        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)
    }
}