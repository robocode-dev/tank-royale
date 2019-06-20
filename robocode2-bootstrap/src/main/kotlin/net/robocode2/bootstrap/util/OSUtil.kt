package net.robocode2.bootstrap.util

import java.util.*

object OSUtil {

    enum class OSType {
        Windows, MacOS, Linux, Other
    }

    private var detectedOS: OSType? = null

    fun getOsType(): OSType {
        if (detectedOS == null) {
            val OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
            detectedOS = if (OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0) {
                OSUtil.OSType.MacOS
            } else if (OS.indexOf("win") >= 0) {
                OSUtil.OSType.Windows
            } else if (OS.indexOf("nux") >= 0) {
                OSUtil.OSType.Linux
            } else {
                OSUtil.OSType.Other
            }
        }
        return detectedOS!!
    }
}