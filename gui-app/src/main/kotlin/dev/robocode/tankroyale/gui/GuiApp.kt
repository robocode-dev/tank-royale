package dev.robocode.tankroyale.gui

import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.components.Images
import java.awt.Taskbar
import javax.swing.UIManager

fun main() {
    try {
        Taskbar.getTaskbar().iconImage = Images.tankImage // for macOS
    } catch (ignore: UnsupportedOperationException) {}

    fixRenderingIssues() // set before Look and Feel

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    MainWindow.isVisible = true
}

private fun fixRenderingIssues() {
    System.setProperty("sun.java2d.d3d", "false") // turn off use of Direct3D
    System.setProperty("sun.java2d.noddraw=true", "true") // no use of Direct Draw

    System.setProperty("sun.java2d.opengl", "true") // turn on use of OpenGL
}