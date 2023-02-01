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
object OSCheck {

    enum class OSType {
        Windows, MacOS, Linux, Solaris, Other
    }

    private var os: OSType? = null

    fun getOsType(): OSType {
        if (this.os == null) {
            val os = System.getProperty("os.name", "generic").lowercase()
            this.os =
                if (os.contains("mac") || os.contains("darwin")) {
                    OSType.MacOS
                } else if (os.contains("win")) {
                    OSType.Windows
                } else if (os.contains("nux") || os.contains("nix")  || os.contains("aix")) {
                    OSType.Linux
                } else if (os.contains("sunos")) {
                    OSType.Solaris
                } else {
                    OSType.Other
                }
        }
        return os!!
    }
}