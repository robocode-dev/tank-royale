package dev.robocode.tankroyale.gui.ansi;

import java.awt.Color
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.text.StyledDocument

object TestFrame : JFrame() {

    init {
        setBounds(100, 100, 1000, 800)

        val editorPane = JEditorPane().apply {
            background = Color(0x28, 0x28, 0x28)

            val ansiKit = AnsiEditorKit()
            val ansiDoc = ansiKit.createDefaultDocument() as StyledDocument

            editorKit = ansiKit
            document = ansiDoc

            val ansi = AnsiTextBuilder()
//            ansi.red().text("foo").white().bold().text("bar").blue().italic().text("hest")

            ansi.text("line 1\nline2")

            val text = ansi.build()

            ansiKit.insertAnsi(ansiDoc, text, 0)
        }

        val scrollPane = JScrollPane(editorPane)

        contentPane.add(scrollPane)
    }
}

fun main() {
    TestFrame.isVisible = true
}