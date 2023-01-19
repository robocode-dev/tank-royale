package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*


open class ConsoleFrame(title: String, isTitlePropertyName: Boolean = true) : RcFrame(title, isTitlePropertyName) {

    private val textArea = JTextArea()
    private val scrollPane = JScrollPane(textArea)

    init {
        setDisposeOnEnterKeyPressed()

        textArea.apply {
            isEditable = false
            foreground = Color.WHITE
            background = Color(0x28, 0x28, 0x28)

            font = Font(Font.MONOSPACED, Font.BOLD, 12)
        }

        contentPane.apply {
            layout = BorderLayout()
            add(scrollPane)
        }

        onActivated {
            // Scroll to the bottom
            val scrollBar = scrollPane.verticalScrollBar
            scrollBar.value = scrollBar.maximum
        }
    }

    fun clear() {
        textArea.text = null
    }

    fun append(line: String) {
        val regex = Regex("\u001B\\[[^m]+m") // TODO: Use JTextPane with colors
        val result = regex.replace(line, "")
        textArea.append(result)

        // Scroll to bottom
        textArea.caretPosition = textArea.document.length
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
