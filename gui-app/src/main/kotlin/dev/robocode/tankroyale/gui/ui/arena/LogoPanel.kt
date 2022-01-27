package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.ui.components.Images
import java.awt.*
import javax.swing.JPanel

object LogoPanel : JPanel() {

    private const val robocodeText = "Robocode Tank Royale"
    private const val robocodeTextSize = 40

    private const val mottoText = "Build the best - destroy the rest!"
    private const val mottoTextSize = 20

    private const val textSpacing = 10

    init {
        background = Color(0x28, 0x28, 0x28)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D

        val logo = Images.logoImage

        val logoWidth = logo.getWidth(null) / 2
        val logoHeight = logo.getHeight(null) / 2

        val logoHeight2 = logoHeight + robocodeTextSize + mottoTextSize + 2 * textSpacing

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

        g2.color = Color.green
        g2.font = Font(Font.SANS_SERIF, Font.PLAIN, robocodeTextSize)

        val robocodeTextX = (width - g2.fontMetrics.stringWidth(robocodeText)) / 2
        val robocodeTextY = logoY + logoHeight + robocodeTextSize + textSpacing

        g2.drawString(robocodeText, robocodeTextX, robocodeTextY)

        g2.font = Font(Font.SANS_SERIF, Font.PLAIN, mottoTextSize)

        val mottoTextX = (width - g2.fontMetrics.stringWidth(mottoText)) / 2
        val mottoTextY = robocodeTextY + mottoTextSize + textSpacing

        g2.drawString(mottoText, mottoTextX, mottoTextY)
    }
}