package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ansi.AnsiEditorKit
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JEditorPane
import javax.swing.text.StyledDocument

class AnsiEditorPane : JEditorPane() {

    val ansiKit = AnsiEditorKit()
    val ansiDocument = ansiKit.createDefaultDocument() as StyledDocument

    init {
        editorKit = ansiKit
        document = ansiDocument

        contentType = ansiKit.contentType
        isEditable = false
        isOpaque = true // required for setting the background color on some systems
        background = Color(0x28, 0x28, 0x28)
    }

    // Enable text anti-aliased painting
    public override fun paintComponent(g: Graphics) {
        val graphics2d = g as Graphics2D
        graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        super.paintComponent(g)
    }
}