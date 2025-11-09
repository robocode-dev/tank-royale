package dev.robocode.tankroyale.common.util

object Platform {

    enum class PlatformType {
        Windows, Mac, Linux, Solaris, Other
    }

    val operatingSystemType: PlatformType by lazy {
        val platform = System.getProperty("os.name", "generic").lowercase()
        when {
            platform.contains("mac") || platform.contains("darwin") -> PlatformType.Mac
            platform.contains("win") -> PlatformType.Windows
            platform.contains("nux") || platform.contains("nix") || platform.contains("aix") -> PlatformType.Linux
            platform.contains("sunos") -> PlatformType.Solaris
            else -> PlatformType.Other
        }
    }

    val isWindows = operatingSystemType == PlatformType.Windows
}