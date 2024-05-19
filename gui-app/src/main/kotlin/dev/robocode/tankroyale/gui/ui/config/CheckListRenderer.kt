package dev.robocode.tankroyale.gui.ui.config

import java.awt.Component
import javax.swing.*

class CheckListRenderer : JCheckBox(), ListCellRenderer<CheckListEntity> {
    override fun getListCellRendererComponent(
        list: JList<out CheckListEntity>,
        entity: CheckListEntity,
        index: Int,
        isSelected: Boolean,
        hasFocus: Boolean,
    ): Component {
        isEnabled = list.isEnabled
        setSelected(entity.isActive)
        font = list.font
        background = if (isSelected) list.selectionBackground else list.background
        foreground = if (isSelected) list.selectionForeground else list.foreground
        text = entity.label
        return this
    }
}