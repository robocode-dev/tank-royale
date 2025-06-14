package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.UiTitles
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import javax.swing.JFrame

open class RcFrame(title: String, isTitlePropertyName: Boolean = true) :
    JFrame(if (isTitlePropertyName) UiTitles.get(title) else title) {

    init {
        iconImage = RcImages.tankImage
    }

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        centerOnActiveScreen()
    }

    override fun setVisible(visible: Boolean) {
        super.setVisible(visible)
        if (visible) {
            centerOnActiveScreen()
        }
    }

    fun centerOnActiveScreen() {
        val mouseLocation = MouseInfo.getPointerInfo().location
        val targetScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
            .map { it.defaultConfiguration.bounds }
            .firstOrNull { it.contains(mouseLocation) }
        if (targetScreen != null) {
            val x = targetScreen.x + (targetScreen.width - width) / 2
            val y = targetScreen.y + (targetScreen.height - height) / 2
            setLocation(x, y)
        }
    }
}
