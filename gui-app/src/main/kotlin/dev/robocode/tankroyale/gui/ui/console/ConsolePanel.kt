package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.util.Clipboard
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.text.html.HTMLDocument

class ConsolePanel : JPanel() {

    private val editorPane = JEditorPane()
    private val scrollPane = JScrollPane(editorPane)

    private val editorKit = ConsoleHtmlEditorKit()
    private val document = editorKit.createDefaultDocument() as HTMLDocument

    private val ansiToHtml = AnsiColorToHtmlController()

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
        add(scrollPane)
    }

    fun clear() {
        editorPane.text = "<div>" // to avoid 2nd line break
    }

    fun append(text: String) {
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

    fun copyToClipboard() {
        // trick to get the text only without HTML tags
        editorPane.select(0, editorPane.text.length)

        // Replace no-break spaces with ordinary spaces
        val text = editorPane.selectedText.replace("\u00a0", " ")

        // copy the text to the clipboard
        Clipboard.set(text)
    }
}