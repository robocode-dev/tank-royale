package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.util.Clipboard
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.text.html.HTMLDocument

open class ConsolePanel : JPanel() {

    private val editorPane = JEditorPane()
    private val scrollPane = JScrollPane(editorPane)

    private val editorKit = ConsoleHtmlEditorKit()
    private val document = editorKit.createDefaultDocument() as HTMLDocument

    private val ansiToHtml = AnsiColorToHtmlController()

    private val onOk = Event<JButton>().apply {
        subscribe(this) {
            val parentFrame = SwingUtilities.getAncestorOfClass(JFrame::class.java, editorPane) as JFrame
            parentFrame.dispose()
        }
    }
    private val onClear = Event<JButton>().apply {
        subscribe(this) { clear() }
    }
    private val onCopyToClipboard = Event<JButton>().apply {
        subscribe(this) { copyToClipboard() }
    }

    protected val okButton = JPanel().addOkButton(onOk)
    protected val clearButton = JPanel().addButton("clear", onClear)
    protected val copyToClipboardButton = JPanel().addButton("copy_to_clipboard", onCopyToClipboard)

    protected open val buttonPanel get() = JPanel().apply {
        add(okButton)
        add(clearButton)
        add(copyToClipboardButton)
    }

    init {
        editorPane.editorKit = editorKit
        editorPane.document = document

        editorPane.apply {
            contentType = "text/html"
            isEditable = false
            background = Color(0x282828)
        }

        clear() // to avoid 2nd line break

        layout = BorderLayout()
        apply {
            add(scrollPane)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }

    fun clear() {
        editorPane.text = "<div>" // to avoid 2nd line break
    }

    fun append(text: String?, cssClass: String? = null, turnNumber: Int? = null) {
        var html = text
        if (html != null) {
            html = html
                .replace("\\n", "<br>")
                .replace("\\t", "&#9;")
            if (cssClass != null) {
                html = "<span class=\"$cssClass\">$html</span>"
            }
            if (turnNumber != null) {
                html = "<span class=\"linenumber\">$turnNumber:</span> $html"
            }
            append(html)
        }
    }

    private fun append(text: String) {
        var html = text
            .replace(" ", "&nbsp;") // in lack of the css style `white-space: pre`
            .replace("\n", "<br>")
            .replace("\r", "")
            .replace("\t", "&#9;")

        html = "<span>${ansiToHtml.process(html)}</span>"

        editorKit.insertHTML(document, document.length, html, 0, 0, null)

        // Scroll to bottom
        editorPane.caretPosition = document.length
    }

    fun scrollToBottom() {
        scrollPane.verticalScrollBar.apply { value = maximum }
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