package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ansi.AnsiEditorKit
import dev.robocode.tankroyale.gui.ansi.AnsiEscCode
import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.util.Clipboard
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.text.StyledDocument

open class ConsolePanel : JPanel() {

    private val editorPane = JEditorPane()
    private val scrollPane = JScrollPane(editorPane).apply {
        border = null
    }

    private val ansiKit = AnsiEditorKit()
    private val document = ansiKit.createDefaultDocument() as StyledDocument

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
    private val clearButton = JPanel().addButton("clear", onClear)
    private val copyToClipboardButton = JPanel().addButton("copy_to_clipboard", onCopyToClipboard)

    protected open val buttonPanel
        get() = JPanel().apply {
            add(okButton)
            add(clearButton)
            add(copyToClipboardButton)
        }

    init {
        editorPane.editorKit = ansiKit
        editorPane.document = document

        editorPane.apply {
            contentType = ansiKit.contentType
            isEditable = false
            isOpaque = true // required for setting the background color on some systems
            background = Color(0x28, 0x28, 0x28)
        }

        clear() // to avoid 2nd line break

        layout = BorderLayout()
        apply {
            add(scrollPane)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }

    fun clear() {
        editorPane.text = ""
    }

    fun append(text: String?, turnNumber: Int? = null) {
        val ansi = AnsiTextBuilder()

        turnNumber?.let {
            ansi.cyan().text(turnNumber - 1).default().text(' ')
        }
        text?.let {
            ansi.text(text
                .replace("\\n", "\n")
                .replace("\\t", "\t")
            )
        }

        ansiKit.insertAnsi(document, ansi.build())

        // Scroll to bottom
        editorPane.caretPosition = document.length
    }

    fun appendBanner(banner: String) {
        append("${AnsiEscCode.BRIGHT_GREEN}$banner${AnsiEscCode.DEFAULT}\n")
    }

    fun appendInfo(info: String, turnNumber: Int? = null) {
        append("${AnsiEscCode.BRIGHT_GREEN}> $info${AnsiEscCode.DEFAULT}\n", turnNumber)
    }

    fun appendError(error: String, turnNumber: Int? = null) {
        append("${AnsiEscCode.BRIGHT_RED}> $error${AnsiEscCode.DEFAULT}\n", turnNumber)
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