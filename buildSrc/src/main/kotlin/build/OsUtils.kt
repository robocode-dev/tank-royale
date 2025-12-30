package build

/**
 * Helper functions for OS detection
 */

fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("win")

fun isMacOS(): Boolean = System.getProperty("os.name").lowercase().contains("mac")

fun isLinux(): Boolean = System.getProperty("os.name").lowercase().contains("linux")

