package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ansi.AnsiEditorPane
import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.util.Clipboard
import dev.robocode.tankroyale.gui.util.EDT
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.EscapedTextDecoder
import java.awt.BorderLayout
import javax.swing.*

open class ConsolePanel : JPanel() {

    private val ansiEditorPane = AnsiEditorPane()
    private val scrollPane = JScrollPane(ansiEditorPane).apply {
        border = null
    }

    private val onOk = Event<JButton>().apply {
        subscribe(this) {
            val parentFrame = SwingUtilities.getAncestorOfClass(JFrame::class.java, ansiEditorPane) as JFrame
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
        layout = BorderLayout()
        apply {
            add(scrollPane)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }

    fun clear() {
        ansiEditorPane.text = ""
    }

    fun append(text: String, turnNumber: Int? = null) {
        val ansi = AnsiTextBuilder()

        turnNumber?.let {
            ansi.cyan().text(turnNumber - 1).defaultColor().text(' ')
        }

        val unescapedText = EscapedTextDecoder.unescape(text)
        ansi.text(unescapedText)

        EDT.enqueue {
            ansiEditorPane.apply {
                ansiKit.insertAnsi(ansiDocument, ansi.build())

                // Scroll to bottom
                caretPosition = ansiDocument.length
            }
        }
    }

    fun appendBanner(banner: String) {
        append(AnsiTextBuilder().brightGreen().text(banner).defaultColor().newline().build())
    }

    fun appendInfo(info: String, turnNumber: Int? = null) {
        append(AnsiTextBuilder().brightGreen().text(info).defaultColor().newline().build(), turnNumber)
    }

    fun appendError(error: String, turnNumber: Int? = null) {
        append(AnsiTextBuilder().brightRed().text(error).defaultColor().newline().build(), turnNumber)
    }

    fun scrollToBottom() {
        scrollPane.verticalScrollBar.apply { value = maximum }
    }

    private fun copyToClipboard() {
        // trick to get the text only without HTML tags
        ansiEditorPane.select(0, ansiEditorPane.text.length)

        // Replace no-break spaces with ordinary spaces
        val text = ansiEditorPane.selectedText.replace("\u00a0", " ")

        // copy the text to the clipboard
        Clipboard.set(text)
    }
}