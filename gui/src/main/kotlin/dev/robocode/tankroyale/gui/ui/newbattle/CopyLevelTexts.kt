package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.rumble.CopyLevel
import dev.robocode.tankroyale.rumble.CopyLevels
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.RenderingHints
import javax.swing.Icon

/** The words of each copy level, from the resource files. */
object CopyLevelTexts {
    fun words(level: CopyLevel): String = Strings.get(
        when (level) {
            CopyLevel.FREE_TO_COPY -> "bot_info.copy_level.free_to_copy"
            CopyLevel.COPY_WITH_CONDITIONS -> "bot_info.copy_level.copy_with_conditions"
            CopyLevel.DO_NOT_COPY -> "bot_info.copy_level.do_not_copy"
        }
    )

    /** The words of the level of a declared [license]. */
    fun wordsOf(license: String): String = words(CopyLevels.ofDeclared(license))
}

/**
 * The shape of a copy level, drawn in the text colour so it reads in every theme: a circle for free to copy, a triangle
 * for copy with conditions and a square for do not copy. The shape goes with the words, never instead of them.
 */
class CopyLevelIcon(private val level: CopyLevel, private val size: Int = 10) : Icon {
    override fun getIconWidth() = size

    override fun getIconHeight() = size

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = c?.foreground ?: g.color
            when (level) {
                CopyLevel.FREE_TO_COPY -> g2.fillOval(x, y, size, size)
                CopyLevel.COPY_WITH_CONDITIONS -> g2.fillPolygon(Polygon(intArrayOf(x + size / 2, x + size, x), intArrayOf(y, y + size, y + size), 3))
                CopyLevel.DO_NOT_COPY -> g2.fillRect(x + 1, y + 1, size - 2, size - 2)
            }
        } finally {
            g2.dispose()
        }
    }
}
