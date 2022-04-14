package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.UiTitles
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.KeyStroke

open class RcFrame(titlePropertyName: String) : JFrame(UiTitles.get(titlePropertyName)) {
    init {
        iconImage = Images.tankImage
    }

    protected fun setDisposeOnEnterKeyPressed() {
        val inputMap = rootPane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
        val enter = "enter"
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter)
        rootPane.actionMap.put(enter, object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                dispose()
            }
        })
    }
}