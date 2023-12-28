package dev.robocode.tankroyale.gui.ui.components

import java.awt.Color
import javax.swing.JToolTip
import javax.swing.border.LineBorder

class RCToolTip : JToolTip() {

    init {
        isOpaque = true
        background = Color(0xff, 0xff, 0x80)
        foreground = Color.black
        border = LineBorder(Color.black)
    }
}