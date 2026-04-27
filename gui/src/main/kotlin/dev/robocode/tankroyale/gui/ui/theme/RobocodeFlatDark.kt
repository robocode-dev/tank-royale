package dev.robocode.tankroyale.gui.ui.theme

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLaf
import javax.swing.UIDefaults
import javax.swing.plaf.ColorUIResource

class RobocodeFlatDark : FlatDarkLaf() {

    override fun getDefaults(): UIDefaults {
        val defaults = super.getDefaults()
        with(RobocodeDarkColors) {
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
            defaults["TextField.background"]            = ColorUIResource(INPUT_BG)
            defaults["TextField.inactiveBackground"]    = ColorUIResource(INPUT_BG)
            defaults["TextField.disabledBackground"]    = ColorUIResource(INPUT_BG)
            defaults["TextField.foreground"]            = ColorUIResource(FOREGROUND)
            defaults["TextArea.background"]             = ColorUIResource(INPUT_BG)
            defaults["TextArea.disabledBackground"]     = ColorUIResource(INPUT_BG)
            defaults["TextArea.foreground"]             = ColorUIResource(FOREGROUND)
            defaults["TextPane.background"]             = ColorUIResource(INPUT_BG)
            defaults["TextPane.foreground"]             = ColorUIResource(FOREGROUND)
            defaults["EditorPane.background"]           = ColorUIResource(INPUT_BG)
            defaults["EditorPane.foreground"]           = ColorUIResource(FOREGROUND)
        }
        return defaults
    }

    companion object {
        fun setup() {
            FlatLaf.setGlobalExtraDefaults(mapOf("@accentColor" to "#00CC00"))
            FlatLaf.setup(RobocodeFlatDark())
        }
    }
}
