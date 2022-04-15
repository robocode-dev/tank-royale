package dev.robocode.tankroyale.gui

import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.components.Images
import java.awt.Taskbar
import javax.swing.UIManager

fun main() {
    try {
        Taskbar.getTaskbar().iconImage = Images.tankImage // for macOS
    } catch (ignore: UnsupportedOperationException) {}

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    MainWindow.isVisible = true
}