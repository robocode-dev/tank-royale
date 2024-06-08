package dev.robocode.tankroyale.booter.util

/**
 * Helper class to check the operating system this Java VM runs in.
 *
 * Please keep the notes below as a pseudo-license:
 *
 * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
 * compare to http://svn.terracotta.org/svn/tc/dso/tags/2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
 * http://www.docjar.com/html/api/org/apache/commons/lang/SystemUtils.java.html
 *
 * Converted into a Kotlin version by Flemming N. Larsen
 */
object OperatingSystemCheck {

    enum class OperatingSystemType {
        Windows, Mac, Linux, Solaris, Other
    }

    private var operatingSystem: OperatingSystemType? = null

    fun getOperatingSystemType(): OperatingSystemType {
        if (operatingSystem == null) {
            val osName = System.getProperty("os.name", "generic").lowercase()
            operatingSystem = when {
                osName.contains("mac") || osName.contains("darwin") -> OperatingSystemType.Mac
                osName.contains("win") -> OperatingSystemType.Windows
                osName.contains("nux") || osName.contains("nix") || osName.contains("aix") -> OperatingSystemType.Linux
                osName.contains("sunos") -> OperatingSystemType.Solaris
                else -> OperatingSystemType.Other
            }
        }
        return operatingSystem!!
    }
}