package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.util.Clipboard
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.text.html.HTMLDocument


open class ConsoleFrame(title: String, isTitlePropertyName: Boolean = true) : RcFrame(title, isTitlePropertyName) {

    private val editorPane = JEditorPane()
    private val scrollPane = JScrollPane(editorPane)

    private val editorKit = ConsoleHtmlEditorKit()
    private val document = editorKit.createDefaultDocument() as HTMLDocument

    private val ansiToHtml = AnsiColorToHtmlController()

    private val onOk = Event<JButton>().apply { subscribe(this) { dispose() } }
    private val onClear = Event<JButton>().apply { subscribe(this) { clear() } }
    private val onCopyToClipboard = Event<JButton>().apply { subscribe(this) { copyToClipboard() } }

    init {
        setDisposeOnEnterKeyPressed()

        editorPane.editorKit = editorKit
        editorPane.document = document

        editorPane.apply {
            contentType = "text/html"
            isEditable = false
            background = Color(0x282828)
        }

        clear() // to avoid 2nd line break

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
        editorPane.text = "<div>" // to avoid 2nd line break
    }

    open fun append(text: String) {
        var html = text
            .replace(" ", "&nbsp;") // in lack of the css style `white-space: pre`
            .replace("\n", "<br>")
            .replace("\r", "")
            .replace("\t", "&#9;")

        html = ansiToHtml.process(html)

        editorKit.insertHTML(document, document.length, html, 0, 0, null)

        // Scroll to bottom
        editorPane.caretPosition = document.length
    }

    private fun setDisposeOnEnterKeyPressed() {
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

        // Replace no-break spaces with ordinary spaces
        val text = editorPane.selectedText.replace("\u00a0", " ")

        // copy the text to the clipboard
        Clipboard.set(text)
    }
}
