package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import java.awt.Dimension
import java.util.*
import javax.swing.JLabel
import javax.swing.JSlider
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

object TpsSlider : JSlider() {

    init {
        minimum = 0
        maximum = 45

        paintLabels = true
        paintTicks = true
        majorTickSpacing = 5

        val labels = Hashtable<Int, JLabel>()
        labels[0] = JLabel("0")
        labels[5] = JLabel("5")
        labels[10] = JLabel("10")
        labels[15] = JLabel("15")
        labels[20] = JLabel("30")
        labels[25] = JLabel("50")
        labels[30] = JLabel("100")
        labels[35] = JLabel("200")
        labels[40] = JLabel("500")
        labels[45] = JLabel("max")
        labelTable = labels

        preferredSize = Dimension(300, 50)

        addChangeListener(TpsChangeListener())

        TpsEvents.onTpsChanged.subscribe(TpsSlider) { tpsEvent ->
            setTps(tpsEvent.tps)
            ConfigSettings.setTps(tpsEvent.tps)
        }

        setTps(ConfigSettings.getTps())
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
        if (tps !in -1..999) {
            throw IllegalArgumentException("tps must be in the range -1..999")
        }
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