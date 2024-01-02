package dev.robocode.tankroyale.gui.ui.components

import javax.swing.JComboBox

open class RcComboBox<E>(items: Array<E>? = null) : JComboBox<E>(items) {
    override fun createToolTip() = RcToolTip()
}