package dev.robocode.tankroyale.gui.ui.components

import java.awt.Color
import javax.swing.JToolTip
import javax.swing.border.LineBorder

class RcToolTip : JToolTip() {

    init {
        border = LineBorder(Color.black)
    }

    override fun setTipText(tipText: String) {
        super.setTipText("""<html><body style="color: black; background-color: #FFFFCC;">$tipText</body></html>""")
    }
}