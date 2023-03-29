package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.KeyStroke


open class ConsoleFrame(title: String, isTitlePropertyName: Boolean = true,
                        protected val consolePanel: ConsolePanel = ConsolePanel())
    : RcFrame(title, isTitlePropertyName) {

    private val onOk = Event<JButton>().apply {
        subscribe(this) { dispose() }
    }
    private val onClear = Event<JButton>().apply {
        subscribe(this) { consolePanel.clear() }
    }
    private val onCopyToClipboard = Event<JButton>().apply {
        subscribe(this) { consolePanel.copyToClipboard() }
    }

    init {
        setDisposeOnEnterKeyPressed()

        val buttonPanel = JPanel().apply {
            addOkButton(onOk)
            addButton("clear", onClear)
            addButton("copy_to_clipboard", onCopyToClipboard)
        }

        contentPane.apply {
            layout = BorderLayout()
            add(consolePanel)
            add(buttonPanel, BorderLayout.SOUTH)
        }

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
