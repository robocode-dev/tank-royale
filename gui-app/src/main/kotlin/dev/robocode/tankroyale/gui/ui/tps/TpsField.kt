package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.components.RcLimitedTextField
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.extensions.JTextFieldExt.onChange
import dev.robocode.tankroyale.gui.ui.extensions.JTextFieldExt.setInputVerifier
import java.awt.EventQueue

object TpsField : RcLimitedTextField(3) {

    var tps: Int = ConfigSettings.tps

    init {
        setInputVerifier { tpsInputVerifier() }
        onChange { tpsInputVerifier() }

        TpsEvents.onTpsChanged.subscribe(TpsField) {
            if (it.tps != tps) {
                tps = it.tps
                updateText()
            }
        }

        updateText()
    }

    private fun updateText() {
        EventQueue.invokeLater {
            text = if (tps == -1) "max" else tps.toString()
        }
    }

    private fun tpsInputVerifier(): Boolean {
        val tpsString = text.trim().lowercase()
        val tps: Int?
        val valid: Boolean

        when (tpsString) {
            "" -> {
                tps = this.tps
                valid = true
            }
            "m", "ma", "max" -> {
                tps = -1
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
            if (tps != this.tps) {
                this.tps = tps!!
                TpsEvents.onTpsChanged.fire(TpsChangedEvent(tps))
            }
        } else {
            showMessage(String.format(Messages.get("tps_range")))
        }
        return valid
    }
}