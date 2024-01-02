package dev.robocode.tankroyale.gui.ui.components

import java.awt.LayoutManager
import javax.swing.JPanel

open class RcPanel(layoutManager: LayoutManager? = null) : JPanel(layoutManager) {
    override fun createToolTip() = RcToolTip()
}