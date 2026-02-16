package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.common.event.On
import dev.robocode.tankroyale.gui.ansi.AnsiEditorPane
import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.util.Clipboard
import java.awt.BorderLayout
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.*
import javax.swing.Timer

open class ConsolePanel : JPanel() {

    internal val ansiEditorPane = AnsiEditorPane()
    private val scrollPane = JScrollPane(ansiEditorPane).apply {
        border = null
    }

    private val onOk = Event<JButton>().apply {
        this += On(this@ConsolePanel) {
            val parentFrame = SwingUtilities.getAncestorOfClass(JFrame::class.java, ansiEditorPane) as JFrame
            parentFrame.dispose()
        }
    }
    private val onClear = Event<JButton>().apply {
        this += On(this@ConsolePanel) { clear() }
    }
    private val onCopyToClipboard = Event<JButton>().apply {
        this += On(this@ConsolePanel) { copyToClipboard() }
    }

    protected val okButton = JPanel().addOkButton(onOk)
    private val clearButton = JPanel().addButton("clear", onClear)
    private val copyToClipboardButton = JPanel().addButton("copy_to_clipboard", onCopyToClipboard)

    private val logQueue = ConcurrentLinkedQueue<String>()

    // Flush queue regularly. Set initialDelay to 0 so the first flush can occur immediately
    // (helps tests that rely on a short wait after appending).
    private val flushTimer = Timer(100) { flushLogQueue() }.apply {
        initialDelay = 0
        start()
    }

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
        logQueue.clear()
        ansiEditorPane.text = ""
    }

    fun append(text: String, turnNumber: Int? = null) {
        val ansi = AnsiTextBuilder()

        turnNumber?.let {
            ansi.cyan().text(turnNumber).defaultColor().text(' ')
        }

        ansi.text(text)

        logQueue.add(ansi.build())
    }

    private fun flushLogQueue() {
        if (logQueue.isEmpty()) return

        val sb = StringBuilder()
        while (!logQueue.isEmpty()) {
            sb.append(logQueue.poll())
        }
        val fullText = sb.toString()

        ansiEditorPane.apply {
            ansiKit.insertAnsi(ansiDocument, fullText)

            val maxChars = ConfigSettings.consoleMaxCharacters
            if (ansiDocument.length > maxChars) {
                val overflow = ansiDocument.length - maxChars
                ansiDocument.remove(0, overflow)
            }

            // Scroll to bottom
            caretPosition = ansiDocument.length
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
