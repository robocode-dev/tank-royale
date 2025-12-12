package dev.robocode.tankroyale.gui

import dev.robocode.tankroyale.common.util.Platform.isWindows
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.RcImages
import java.awt.Taskbar
import javax.swing.UIManager
import java.util.Locale

fun main(args: Array<String>) {
    if (args.contains("-v") || args.contains("--version")) {
        println("Robocode Tank Royale GUI ${Version.version}")
        return
    }
    val scale = try { ConfigSettings.uiScale } catch (_: Exception) { 100 }

    // set ui scale factor for high dpi displays, but only if not already set (via command line)
    setIfPropertyMissing("sun.java2d.uiScale") { (scale/100).toString() } // default

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
    setIfPropertyMissing("sun.java2d.uiScale") {
        val percent = ConfigSettings.uiScale.coerceIn(50, 400)
        val scale = percent / 100.0
        String.format(Locale.US, "%.2f", scale)
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
        setIfPropertyMissing("sun.java2d.d3d") { "false" } // disable Direct3D acceleration
        setIfPropertyMissing("sun.java2d.opengl") { "false" } // disable OpenGL acceleration
    }
}

private fun setIfPropertyMissing(propertyName: String, valueProvider: () -> String) {
    if (System.getProperty(propertyName) == null) {
        System.setProperty(propertyName, valueProvider())
    }
}