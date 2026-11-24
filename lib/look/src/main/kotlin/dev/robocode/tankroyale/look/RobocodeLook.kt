package dev.robocode.tankroyale.look

import java.awt.Toolkit

/**
 * The look of Tank Royale's windows, shared by the desktop program and the windows of a contribution session, so both
 * look the same. The theme is the desktop program's setting: `light`, `dark` or `system`, which follows the operating
 * system where it can tell.
 */
object RobocodeLook {

    /** The theme [theme] stands for, `light` or `dark`; `system` asks the operating system, and anything else is dark. */
    fun resolveTheme(theme: String): String = when (theme) {
        "light" -> "light"
        "system" -> if (isOsDark()) "dark" else "light"
        else -> "dark"
    }

    /** The tank picture of Tank Royale's windows and icons. */
    val tankImage: java.awt.Image by lazy {
        javax.imageio.ImageIO.read(RobocodeLook::class.java.getResource("/gfx/Tank.png") ?: error("Missing /gfx/Tank.png"))
    }

    /** Sets up the look and feel for [theme], as the desktop program's setting holds it. */
    fun setup(theme: String) {
        if (resolveTheme(theme) == "light") RobocodeFlatLight.setup() else RobocodeFlatDark.setup()
    }

    private fun isOsDark(): Boolean {
        return try {
            val os = System.getProperty("os.name", "").lowercase()
            when {
                "win" in os -> isWindowsDark()
                "mac" in os -> {
                    val style = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.application.appearanceMode") as? String
                    style?.lowercase()?.contains("dark") ?: true
                }
                else -> true
            }
        } catch (_: Exception) {
            true
        }
    }

    private fun isWindowsDark(): Boolean {
        return try {
            val process = ProcessBuilder(
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme"
            ).start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            // AppsUseLightTheme = 0x0 means dark mode ON, 0x1 means light mode
            output.contains("0x0")
        } catch (_: Exception) {
            true
        }
    }
}
