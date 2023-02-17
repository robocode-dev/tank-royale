package dev.robocode.tankroyale.gui

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.Images
import java.awt.Taskbar
import javax.swing.UIManager

fun main() {
    try {
        Taskbar.getTaskbar().iconImage = Images.tankImage // for macOS
    } catch (ignore: UnsupportedOperationException) {}

    fixRenderingIssues() // set before Look and Feel

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    MainFrame.isVisible = true
}

private fun fixRenderingIssues() {
    val osName = System.getProperty("os.name", "generic")
    if (osName.startsWith("windows", ignoreCase = true)) {
        // Disable hardware acceleration to avoid issue with rendering
        System.setProperty("sun.java2d.d3d", "false") // disable Direct3D acceleration
        System.setProperty("sun.java2d.opengl", "false") // disable OpenGL acceleration
    }
}