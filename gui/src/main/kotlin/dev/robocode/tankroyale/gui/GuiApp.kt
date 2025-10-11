package dev.robocode.tankroyale.gui

import dev.robocode.tankroyale.common.util.Platform.isWindows
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.RcImages
import java.awt.Taskbar
import javax.swing.UIManager
import java.util.Locale

fun main() {
    try {
        Taskbar.getTaskbar().iconImage = RcImages.tankImage // for macOS
    } catch (_: UnsupportedOperationException) {
        // No nothing if the taskbar is unsupported
    }

    fixRenderingIssues() // set before Look and Feel

    applyGlobalUiScaleFromSettings() // Set HiDPI scale for the entire UI before L&F

    applyDefaultLocaleFromSettings() // Ensure JVM default locale follows GUI language

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    MainFrame.isVisible = true
}

private fun applyGlobalUiScaleFromSettings() {
    if (System.getProperty("sun.java2d.uiScale") == null) {
        val percent = ConfigSettings.uiScale.coerceIn(50, 400)
        val scale = percent / 100.0
        val value = String.format(Locale.US, "%.2f", scale)
        System.setProperty("sun.java2d.uiScale", value)
    }
}

private fun applyDefaultLocaleFromSettings() {
    val locale = when (ConfigSettings.language) {
        "es" -> Locale("es")
        "da" -> Locale("da")
        else -> Locale.ENGLISH
    }
    // Setting default Locale avoids fallback to system locale (e.g., da) for any UI using default locale
    Locale.setDefault(locale)
}

private fun fixRenderingIssues() {
    if (isWindows) {
        // Disable hardware acceleration to avoid issue with rendering
        System.setProperty("sun.java2d.d3d", "false") // disable Direct3D acceleration
        System.setProperty("sun.java2d.opengl", "false") // disable OpenGL acceleration
    }
}