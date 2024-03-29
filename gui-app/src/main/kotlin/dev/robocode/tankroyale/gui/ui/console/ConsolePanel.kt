package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ansi.AnsiEditorPane
import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import dev.robocode.tankroyale.gui.ansi.esc_code.CommandCode
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.util.Clipboard
import dev.robocode.tankroyale.gui.util.EDT
import dev.robocode.tankroyale.gui.util.Event
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
        ansi.text(
            text.replace("\\n", "\n")
                .replace("\\t", "\t")
        )

        EDT.enqueue {
            ansiEditorPane.apply {
                ansiKit.insertAnsi(ansiDocument, ansi.build())

                // Scroll to bottom
                caretPosition = ansiDocument.length
            }
        }
    }

    fun appendBanner(banner: String) {
        append("${CommandCode.BRIGHT_GREEN}$banner${CommandCode.DEFAULT}\n")
    }

    fun appendInfo(info: String, turnNumber: Int? = null) {
        append("${CommandCode.BRIGHT_GREEN}> $info${CommandCode.DEFAULT}\n", turnNumber)
    }

    fun appendError(error: String, turnNumber: Int? = null) {
        append("${CommandCode.BRIGHT_RED}> $error${CommandCode.DEFAULT}\n", turnNumber)
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