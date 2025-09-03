package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.ui.components.RcImages
import java.awt.*
import javax.swing.JPanel

object LogoPanel : JPanel() {
    private fun readResolve(): Any = LogoPanel

    private const val ROBOCODE_TEXT = "Robocode Tank Royale"
    private const val ROBOCODE_TEXT_SIZE = 40

    private const val MOTTO_TEXT = "Build the best - destroy the rest!"
    private const val MOTTO_TEXT_SIZE = 20

    private const val TEXT_SPACING = 10

    private val textColor = Color(0x377B37)

    init {
        background = Color(0x282828)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D

        val logo = RcImages.logoImage

        val logoWidth = logo.getWidth(null) / 2
        val logoHeight = logo.getHeight(null) / 2

        val logoHeight2 = logoHeight + ROBOCODE_TEXT_SIZE + MOTTO_TEXT_SIZE + 2 * TEXT_SPACING

        val logoX = (width - logoWidth) / 2
        val logoY = (height - logoHeight2) / 2

        g2.setRenderingHints(
            RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )
        )

        g2.color = background
        g2.fillRect(0, 0, width, height)
        g2.drawImage(logo, logoX, logoY, logoWidth, logoHeight, background, null)

        g2.color = textColor
        g2.font = Font(Font.SANS_SERIF, Font.PLAIN, ROBOCODE_TEXT_SIZE)

        val robocodeTextX = (width - g2.fontMetrics.stringWidth(ROBOCODE_TEXT)) / 2
        val robocodeTextY = logoY + logoHeight + ROBOCODE_TEXT_SIZE + TEXT_SPACING

        g2.drawString(ROBOCODE_TEXT, robocodeTextX, robocodeTextY)

        g2.font = Font(Font.SANS_SERIF, Font.PLAIN, MOTTO_TEXT_SIZE)

        val mottoTextX = (width - g2.fontMetrics.stringWidth(MOTTO_TEXT)) / 2
        val mottoTextY = robocodeTextY + MOTTO_TEXT_SIZE + TEXT_SPACING

        g2.drawString(MOTTO_TEXT, mottoTextX, mottoTextY)
    }
}