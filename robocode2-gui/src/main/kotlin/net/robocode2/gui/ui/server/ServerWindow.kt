package net.robocode2.gui.ui.server

import net.robocode2.gui.extensions.WindowExt.onClosing
import net.robocode2.gui.ui.ResourceBundles
import java.awt.Color
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea

object ServerWindow : JFrame(getWindowTitle()), AutoCloseable {

    private var textArea = JTextArea()

    init {
        setSize(700, 550)
        setLocationRelativeTo(null) // center on screen

        textArea.isEditable = false
        textArea.background = Color(0x28, 0x28, 0x28)
        textArea.foreground = Color.WHITE

        val scrollPane = JScrollPane(textArea)
        contentPane.add(scrollPane)

        onClosing {
        }
    }

    override fun close() {
    }

    fun clear() {
        textArea.text = null
    }

    fun append(line: String) {
        val regex = Regex("\u001B\\[[^m]+m")

        val result = regex.replace(line, "")

        textArea.append(result)

        // Scroll to bottom
        textArea.caretPosition = textArea.document.length
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("server_window")
}