package dev.robocode.tankroyale.gui.ui.components

import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.UIManager

internal fun inactiveBg() =
    UIManager.getColor("TextField.inactiveBackground") ?: UIManager.getColor("TextField.background")

internal fun plainBorder() = BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor") ?: Color.GRAY),
    BorderFactory.createEmptyBorder(2, 4, 2, 4)
)
