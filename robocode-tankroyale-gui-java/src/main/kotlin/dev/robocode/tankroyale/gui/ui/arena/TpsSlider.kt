package dev.robocode.tankroyale.gui.ui.arena

import java.util.*
import javax.swing.JLabel
import javax.swing.JSlider

object TpsSlider : JSlider() {

    private const val MAX_TPS_SLIDER_VALUE = 61

    init {
        minimum = 0
        maximum = MAX_TPS_SLIDER_VALUE

        paintLabels = true
        paintTicks = true
//        minorTickSpacing = MAX_TPS_SLIDER_VALUE
        majorTickSpacing = 5

        val labels = Hashtable<Int, JLabel>()
        labels[0] = JLabel("0")
        labels[5] = JLabel("5")
        labels[10] = JLabel("10")
        labels[15] = JLabel("15")
        labels[20] = JLabel("20")
        labels[25] = JLabel("25")
        labels[30] = JLabel("30")
        labels[35] = JLabel("40")
        labels[40] = JLabel("50")
        labels[45] = JLabel("65")
        labels[50] = JLabel("90")
        labels[55] = JLabel("150")
        labels[60] = JLabel("1000")
        labelTable = labels
    }
}