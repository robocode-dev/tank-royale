package dev.robocode.tankroyale.gui.ui.config

import java.awt.Component
import javax.swing.*

/** A row with a check box; [suffix] adds a few words after the row's text, such as what a folder holds. */
class CheckListRenderer(private val suffix: (String) -> String = { "" }) : JCheckBox(), ListCellRenderer<CheckListEntity> {
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
        text = entity.label + suffix(entity.label)
        return this
    }
}