package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.ResourceBundles
import javax.swing.JFrame

open class RcFrame(titlePropertyName: String) : JFrame(ResourceBundles.UI_TITLES.get(titlePropertyName)) {
    init {
        iconImage = Icons.robocodeImageIcon
    }
}