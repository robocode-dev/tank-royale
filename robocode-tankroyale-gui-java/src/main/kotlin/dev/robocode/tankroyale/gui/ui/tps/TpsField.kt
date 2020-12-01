package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.components.JLimitedTextField
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.extensions.JTextFieldExt.onChange
import dev.robocode.tankroyale.gui.ui.extensions.JTextFieldExt.setInputVerifier
import javax.swing.SwingUtilities.invokeLater

object TpsField : JLimitedTextField(3) {

    var tps: Int = 30

    init {
        setInputVerifier { tpsInputVerifier() }
        onChange { tpsInputVerifier() }

        TpsChannel.onTpsChange.subscribe { tpsEvent ->
            if (tpsEvent.source != this) {
                tps = tpsEvent.tps
                invokeLater {
                    text = if (tps == Int.MAX_VALUE) "max" else tps.toString()
                }
            }
        }
    }

    private fun tpsInputVerifier(): Boolean {
        val tpsString = text.trim().toLowerCase()
        val tps: Int?
        val valid: Boolean

        when (tpsString) {
            "" -> {
                tps = this.tps
                valid = true
            }
            "m", "ma", "max" -> {
                tps = Int.MAX_VALUE
                valid = true
            }
            else -> {
                tps = try {
                    tpsString.toInt()
                } catch (e: NumberFormatException) {
                    null
                }
                valid = tps != null && tps in 0..999
            }
        }
        if (valid) {
            this.tps = tps!!
            TpsChannel.onTpsChange.publish(TpsEvent(this, tps))
        } else {
            showMessage(String.format(ResourceBundles.MESSAGES.get("tps_range")))
        }
        return valid
    }
}