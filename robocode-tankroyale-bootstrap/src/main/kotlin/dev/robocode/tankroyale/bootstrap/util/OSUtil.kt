package dev.robocode.tankroyale.bootstrap.util

import java.util.*

object OSUtil {

    enum class OSType {
        Windows, MacOS, Linux, Other
    }

    private var detectedOS: OSType? = null

    fun getOsType(): OSType {
        if (detectedOS == null) {
            val os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
            detectedOS = if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
                OSType.MacOS
            } else if (os.indexOf("win") >= 0) {
                OSType.Windows
            } else if (os.indexOf("nux") >= 0) {
                OSType.Linux
            } else {
                OSType.Other
            }
        }
        return detectedOS!!
    }
}