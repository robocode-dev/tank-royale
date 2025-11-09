package dev.robocode.tankroyale.gui.ui.components

import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.ImageIcon

object RcImages {

    val tankImage: Image = readImage("/gfx/Tank.png")
    val logoImage: Image = readImage("/gfx/Robocode-logo.png")

    private fun readImage(filePath: String): Image {
        val inputStream = javaClass.getResourceAsStream(filePath)
        return ImageIcon(ImageIO.read(inputStream)).image
    }
}