package dev.robocode.tankroyale.gui

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.Images
import java.awt.Taskbar
import java.util.*
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
    val osName = System.getProperty("os.name", "generic").lowercase(Locale.ENGLISH)
    val isMac = osName.contains("mac") || osName.contains("darwin")
    if (!isMac) {
        System.setProperty("sun.java2d.d3d", "false") // turn off use of Direct3D
        System.setProperty("sun.java2d.noddraw", "true") // no use of Direct Draw
        System.setProperty("sun.java2d.opengl", "true") // turn on use of OpenGL
    }
}