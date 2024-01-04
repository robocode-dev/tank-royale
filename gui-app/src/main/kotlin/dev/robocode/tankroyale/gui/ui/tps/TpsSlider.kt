package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.components.RcSlider
import java.awt.Dimension
import java.util.*
import javax.swing.JLabel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

object TpsSlider : RcSlider() {

    init {
        minimum = 0
        maximum = 45

        paintLabels = true
        paintTicks = true
        majorTickSpacing = 5

        labelTable= Hashtable<Int, JLabel>().apply {
            this[0] = JLabel("0")
            this[5] = JLabel("5")
            this[10] = JLabel("10")
            this[15] = JLabel("15")
            this[20] = JLabel("30")
            this[25] = JLabel("50")
            this[30] = JLabel("100")
            this[35] = JLabel("200")
            this[40] = JLabel("500")
            this[45] = JLabel("max")
        }

        val size = preferredSize
        preferredSize = Dimension(size.width.coerceAtLeast(350), size.height) // width gets too small on Linux

        addChangeListener(TpsChangeListener())

        TpsEvents.onTpsChanged.subscribe(TpsSlider) {
            setTps(it.tps)
            ConfigSettings.tps = it.tps
        }

        setTps(ConfigSettings.tps)
    }

    private fun getTps(): Int {
        if (value <= 15) return value // 0 - 15
        if (value <= 20) return 15 + (value - 15) * 3 // 15 - 30
        if (value <= 25) return 30 + (value - 20) * 4 // 30 - 50
        if (value <= 30) return 50 + (value - 25) * 10 // 50 - 100
        if (value <= 35) return 100 + (value - 30) * 20 // 100 - 200
        if (value <= 40) return 200 + (value - 35) * 60 // 200 - 500
        if (value <= 44) return 500 + (value - 40) * 100 // 500 - 800
        return -1 // maximum
    }

    private fun setTps(tps: Int) {
        require(tps in -1..999) { "tps must be in the range -1..999" }
        value = when {
            tps < 0 -> maximum
            tps <= 15 -> tps // 0 - 15
            tps <= 30 -> 15 + (tps - 15) / 3 // 15 - 30
            tps <= 50 -> 20 + (tps - 30) / 4 // 30 - 50
            tps <= 100 -> 25 + (tps - 50) / 10 // 50 - 100
            tps <= 200 -> 30 + (tps - 100) / 20 // 100 - 200
            tps <= 500 -> 35 + (tps - 200) / 60 // 200 - 500
            else -> 40 + (tps - 500) / 100 // 500 - 999
        }
    }

    private class TpsChangeListener : ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            if (!valueIsAdjusting) { // avoid events while dragging
                TpsEvents.onTpsChanged.fire(TpsChangedEvent(getTps()))
            }
        }
    }
}