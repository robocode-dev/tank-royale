package dev.robocode.tankroyale.gui.ui.components

import javax.swing.JList
import javax.swing.ListModel

open class RcList<E>(dataModel: ListModel<E>? = null) : JList<E>(dataModel) {
    override fun createToolTip() = RcToolTip()
}