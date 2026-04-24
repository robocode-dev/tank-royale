package dev.robocode.tankroyale.gui.ansi

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JEditorPane
import javax.swing.UIManager
import javax.swing.text.StyledDocument

/**
 * The AnsiEditorPane is a specialized [JEditorPane] which will automatically set up an [AnsiEditorKit] and
 * [StyledDocument] for the pane.
 * The background color and ANSI color scheme adapt to the active FlatLaf theme (dark/light).
 * Anti-aliased text painting is enabled for readability.
 */
class AnsiEditorPane : JEditorPane() {

    val ansiKit = AnsiEditorKit(fontSize = 12)
    val ansiDocument = ansiKit.createDefaultDocument() as StyledDocument

    private var initialized = false

    init {
        isOpaque = true
        editorKit = ansiKit
        document = ansiDocument
        initialized = true
        applyThemeColors()  // apply initial theme without calling super.updateUI()
    }

    override fun updateUI() {
        super.updateUI()
        if (!initialized) return
        applyThemeColors()
    }

    private fun applyThemeColors() {
        val isDark = isDarkLaf()
        background = if (isDark) DARK_BG else (UIManager.getColor("Panel.background") ?: LIGHT_BG)
        ansiKit.ansiColors = if (isDark) DefaultAnsiColors else LightAnsiColors
    }

    /** Detects dark LAF by luminance of Panel.background — works with any LAF, no FlatLaf API dependency. */
    private fun isDarkLaf(): Boolean {
        val bg = UIManager.getColor("Panel.background") ?: return true
        val luminance = (0.299 * bg.red + 0.587 * bg.green + 0.114 * bg.blue) / 255.0
        return luminance < 0.5
    }

    /** {@inheritDoc} */
    public override fun paintComponent(g: Graphics) {
        // Enable anti-aliased text painting
        val graphics2d = g as Graphics2D
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        super.paintComponent(g)
    }

    companion object {
        private val DARK_BG = Color(0x28, 0x28, 0x28)
        private val LIGHT_BG = Color(0xf0, 0xf4, 0xff)  // fallback if UIManager key unavailable
    }
}
