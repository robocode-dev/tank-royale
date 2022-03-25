package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.gui.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.components.JLimitedTextField
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.extensions.JTextFieldExt.onChange
import dev.robocode.tankroyale.gui.ui.extensions.JTextFieldExt.setInputVerifier
import javax.swing.SwingUtilities.invokeLater

object TpsField : JLimitedTextField(3) {

    var tps: Int = ConfigSettings.tps

    init {
        setInputVerifier { tpsInputVerifier() }
        onChange { tpsInputVerifier() }

        TpsEvents.onTpsChanged.subscribe(this) {
            if (it.tps != tps) {
                tps = it.tps
                updateText()
            }
        }

        updateText()
    }

    private fun updateText() {
        invokeLater {
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
            showMessage(String.format(ResourceBundles.MESSAGES.get("tps_range")))
        }
        return valid
    }
}