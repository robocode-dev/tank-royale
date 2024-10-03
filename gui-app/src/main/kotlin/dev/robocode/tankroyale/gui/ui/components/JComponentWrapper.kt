package dev.robocode.tankroyale.gui.ui.components

import javax.swing.JComponent
import java.awt.Graphics

open class JComponentWrapper<T : JComponent>(val component: T) : JComponent() {

    init {
        layout = null
        add(component)
    }

    override fun createToolTip() = RcToolTip()

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        component.paint(g)
    }

    override fun getPreferredSize() = component.preferredSize

    override fun getMinimumSize() = component.minimumSize

    override fun getMaximumSize() = component.maximumSize

    override fun doLayout() {
        component.setBounds(0, 0, width, height)
    }
}
