package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.JPanel
import javax.swing.KeyStroke

open class ConsoleFrame(
    title: String, isTitlePropertyName: Boolean = true,
    protected val consolePanel: ConsolePanel = ConsolePanel()
) : RcFrame(title, isTitlePropertyName) {

    init {
        setDisposeOnEnterKeyPressed()

        contentPane.add(consolePanel)

        onActivated {
            consolePanel.scrollToBottom()
            isFocusable = true // in order to close it by pressing enter
        }
    }

    fun clear() {
        consolePanel.clear()
    }

    fun append(text: String) {
        consolePanel.append(text)
    }

    // note that window must be in focus!
    private fun setDisposeOnEnterKeyPressed() {
        rootPane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).apply {
            val enter = "enter"

            put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter)

            rootPane.actionMap.put(enter, object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    dispose()
                }
            })
        }
    }
}
