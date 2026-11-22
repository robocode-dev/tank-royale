package dev.robocode.tankroyale.gui.ui.rumble

import com.formdev.flatlaf.FlatLaf
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LayoutManager
import java.awt.RenderingHints
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

/** The colour of a card on the Rumble screens, chosen by what the card is about. */
enum class CardTone(private val light: Int, private val dark: Int) {
    /** Something to read or learn, and plain information. */
    BLUE(0x2F6FD1, 0x6EA2F0),

    /** Good news, or the step that sends something. */
    GREEN(0x1B7F2A, 0x6FCF73),

    /** Something that runs or waits. */
    AMBER(0xB26A00, 0xF0B45A),

    /** Bad news. */
    RED(0xB3261E, 0xFF7B72),

    /** Nothing to point out. */
    NEUTRAL(0x7A8494, 0x8C96A5);

    /** The tone's colour on the current theme, light or dark. */
    fun accent(): Color = Color(if (FlatLaf.isLafDark()) dark else light)
}

/**
 * A panel drawn as a card: a soft tint of its tone over the window's background, a thin edge and a coloured stripe at
 * the left. The colours are worked out each time it is painted, so the card follows a change between the light and the
 * dark theme by itself, and text on it keeps the theme's own colour, so it stays readable on both.
 */
class RumbleCard(layout: LayoutManager, tone: CardTone = CardTone.NEUTRAL, private val padding: Int = 10) : JPanel(layout) {

    var tone: CardTone = tone
        set(value) {
            field = value
            repaint()
        }

    init {
        isOpaque = false
        border = EmptyBorder(padding, padding + STRIPE, padding, padding)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val accent = tone.accent()
            val base = UIManager.getColor("Panel.background") ?: background
            val dark = FlatLaf.isLafDark()
            val w = width - 1
            val h = height - 1
            g2.color = mix(base, accent, if (dark) 0.16 else 0.09)
            g2.fillRoundRect(0, 0, w, h, ARC, ARC)
            g2.color = mix(base, accent, if (dark) 0.45 else 0.35)
            g2.drawRoundRect(0, 0, w, h, ARC, ARC)
            g2.clip = java.awt.geom.RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat() + 1, ARC.toFloat(), ARC.toFloat())
            g2.color = accent
            g2.fillRect(0, 0, STRIPE, height)
        } finally {
            g2.dispose()
        }
        super.paintComponent(g)
    }

    companion object {
        private const val STRIPE = 4
        private const val ARC = 10

        /** [base] with [part] of [over] laid on it. */
        internal fun mix(base: Color, over: Color, part: Double): Color {
            fun channel(a: Int, b: Int) = (a + (b - a) * part).toInt().coerceIn(0, 255)
            return Color(channel(base.red, over.red), channel(base.green, over.green), channel(base.blue, over.blue))
        }
    }
}
