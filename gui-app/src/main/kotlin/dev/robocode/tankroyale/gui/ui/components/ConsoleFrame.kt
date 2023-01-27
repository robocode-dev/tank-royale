package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.util.Clipboard
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*


open class ConsoleFrame(title: String, isTitlePropertyName: Boolean = true) : RcFrame(title, isTitlePropertyName) {

    protected val editorPane = JEditorPane()
    private val scrollPane = JScrollPane(editorPane)

    private val onOk = Event<JButton>().apply { subscribe(this) { dispose() } }
    private val onClear = Event<JButton>().apply { subscribe(this) { clear() } }
    private val onCopyToClipboard = Event<JButton>().apply { subscribe(this) { copyToClipboard() } }

    init {
        setDisposeOnEnterKeyPressed()

        editorPane.apply {
            isEditable = false
            foreground = Color.WHITE
            background = Color(0x28, 0x28, 0x28)

            font = Font(Font.MONOSPACED, Font.BOLD, 12)
        }

        val buttonPanel = JPanel().apply {
            addOkButton(onOk)
            addButton("clear", onClear)
            addButton("copy_to_clipboard", onCopyToClipboard)
        }

        contentPane.apply {
            layout = BorderLayout()
            add(scrollPane)
            add(buttonPanel, BorderLayout.SOUTH)
        }

        onActivated {
            // Scroll to the bottom
            val scrollBar = scrollPane.verticalScrollBar
            scrollBar.value = scrollBar.maximum
        }
    }

    fun clear() {
        editorPane.text = null
    }

    fun append(line: String) {
        val regex = Regex("\u001B\\[[^m]+m") // TODO: Use JTextPane with colors
        val result = regex.replace(line, "")
        editorPane.text += result

        // Scroll to bottom
        editorPane.caretPosition = editorPane.document.length
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

    private fun copyToClipboard() {
        // trick to get the text only without HTML tags
        editorPane.select(0, editorPane.text.length)
        val text = editorPane.selectedText

        // copy the text to the clipboard
        Clipboard.set(text)
    }
}
