package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.model.TpsChangedEvent
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

        TpsEventChannel.onTpsChanged.subscribe { tpsEvent -> setTps(tpsEvent.tps) }

        setTps(30) // FIXME: from settings
    }

    private fun getTps(): Int {
        if (value <= 15) { // 0 - 15
            return value
        }
        if (value <= 20) { // 15 - 30
            return 15 + (value - 15) * 3
        }
        if (value <= 25) { // 30 - 50
            return 30 + (value - 20) * 4
        }
        if (value <= 30) { // 50 - 100
            return 50 + (value - 25) * 10
        }
        if (value <= 35) { // 100 - 200
            return 100 + (value - 30) * 20
        }
        if (value <= 40) { // 200 - 500
            return 200 + (value - 35) * 60
        }
        if (value <= 44) { // 500 - 800
            return 500 + (value - 40) * 100
        }
        return -1 // maximum
    }

    private fun setTps(tps: Int) {
        if (!(tps in -1..999)) {
            throw IllegalArgumentException("tps must be in the range -1..999")
        }
        when {
            tps <= 15 -> { // 0 - 15
                value = tps
            }
            tps <= 30 -> { // 15 - 30
                value = 15 + (tps - 15) / 3
            }
            tps <= 50 -> { // 30 - 50
                value = 20 + (tps - 30) / 4
            }
            tps <= 100 -> { // 50 - 100
                value = 25 + (tps - 50) / 10
            }
            tps <= 200 -> { // 100 - 200
                value = 30 + (tps - 100) / 20
            }
            tps <= 500 -> { // 200 - 500
                value = 35 + (tps - 200) / 60
            }
            tps <= 999 -> { // 500 - 999
                value = 40 + (tps - 500) / 100
            }
            else -> {
                value = maximum
            }
        }
    }

    private class TpsChangeListener : ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            if (!valueIsAdjusting) { // avoid events while dragging
                TpsEventChannel.onTpsChanged.publish(TpsChangedEvent(getTps()))
            }
        }
    }
}