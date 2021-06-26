package dev.robocode.tankroyale.gui.ui.components

import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.ImageIcon

object Icons {

    val robocodeImageIcon = readImageIcon("/gfx/Tank.png")

    private fun readImageIcon(filePath: String): Image {
        val iconStream = javaClass.getResourceAsStream(filePath)
        return ImageIcon(ImageIO.read(iconStream)).image
    }
}