package dev.robocode.tankroyale.gui.ui.components

import javax.swing.JSlider

open class RcSlider : JSlider() {
    override fun createToolTip() = RcToolTip()
}