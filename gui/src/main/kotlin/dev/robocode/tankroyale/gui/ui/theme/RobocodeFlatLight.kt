package dev.robocode.tankroyale.gui.ui.theme

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import javax.swing.UIDefaults
import javax.swing.plaf.ColorUIResource

class RobocodeFlatLight : FlatLightLaf() {

    override fun getDefaults(): UIDefaults {
        val defaults = super.getDefaults()
        with(RobocodeLightColors) {
            defaults["Panel.background"]                = ColorUIResource(COMPONENT_BG)
            defaults["window"]                          = ColorUIResource(WINDOW_BG)
            defaults["Label.foreground"]                = ColorUIResource(FOREGROUND)
            defaults["Component.accentColor"]           = ColorUIResource(ACCENT)
            defaults["List.selectionBackground"]        = ColorUIResource(SELECTION_BG)
            defaults["List.selectionForeground"]        = ColorUIResource(SELECTION_FG)
            defaults["Table.selectionBackground"]       = ColorUIResource(SELECTION_BG)
            defaults["Table.selectionForeground"]       = ColorUIResource(SELECTION_FG)
            defaults["Tree.selectionBackground"]        = ColorUIResource(SELECTION_BG)
            defaults["Tree.selectionForeground"]        = ColorUIResource(SELECTION_FG)
            defaults["ScrollBar.thumb"]                 = ColorUIResource(SCROLLBAR_THUMB)
            defaults["Component.borderColor"]           = ColorUIResource(BORDER)
            defaults["ToggleSwitch.onColor"]            = ColorUIResource(TOGGLE_ON)
        }
        return defaults
    }

    companion object {
        fun setup() {
            FlatLaf.setGlobalExtraDefaults(mapOf("@accentColor" to "#009900"))
            FlatLaf.setup(RobocodeFlatLight())
        }
    }
}
