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
        try {
            val mouseLocation = MouseInfo.getPointerInfo()?.location
            val screens = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices

            val screenBounds = screens.map { it.defaultConfiguration.bounds }
                .firstOrNull { mouseLocation != null && it.contains(mouseLocation) }
                ?: screens.firstOrNull()?.defaultConfiguration?.bounds

            screenBounds?.let {
                setLocation(it.x + (it.width - width) / 2, it.y + (it.height - height) / 2)
            }
        } catch (e: Exception) {
            try {
                val bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .defaultScreenDevice.defaultConfiguration.bounds
                setLocation(bounds.x + (bounds.width - width) / 2, bounds.y + (bounds.height - height) / 2)
            } catch (_: Exception) {
                // Keep default position
            }
        }
    }
}