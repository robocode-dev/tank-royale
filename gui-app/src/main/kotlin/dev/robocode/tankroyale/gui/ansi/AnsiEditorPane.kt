package dev.robocode.tankroyale.gui.ansi

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JEditorPane
import javax.swing.text.StyledDocument

/**
 * The AnsiEditorPane is a specialized [JEditorPane] which will automatically set up an [AnsiEditorKit] and
 * [StyledDocument] for the pane.
 * It also sets the background color to dark grey to better see the ANSI colors, and enables anti-aliased text painting.
 */
class AnsiEditorPane : JEditorPane() {

    val ansiKit = AnsiEditorKit(fontSize = 12)
    val ansiDocument = ansiKit.createDefaultDocument() as StyledDocument

    init {
        isOpaque = true
        background = Color(0x28, 0x28, 0x28)

        editorKit = ansiKit
        document = ansiDocument
    }

    /** {@inheritDoc} */
    public override fun paintComponent(g: Graphics) {
        // Enable anti-aliased text painting
        val graphics2d = g as Graphics2D
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        super.paintComponent(g)
    }
}