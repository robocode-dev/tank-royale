package dev.robocode.tankroyale.common.util

import dev.robocode.tankroyale.common.util.Platform.isWindows

object JavaExec {
    /**
     * The Java to start another Tank Royale program with: the full path of the Java this program runs on, so a program
     * started from a bundled Java, such as the installed desktop program's, needs no Java on the path, on every
     * operating system. On Windows it is the variant without a console window. A Java home without either program gives
     * the bare name, found on the path as before.
     */
    fun java(): String = javaIn(System.getProperty("java.home"), isWindows)

    /** The Java program of the Java home [javaHome], by its full path, or the bare name when it holds none. */
    fun javaIn(javaHome: String?, windows: Boolean): String {
        val bare = if (windows) "javaw" else "java"
        val bin = javaHome?.takeIf { it.isNotBlank() }?.let { java.nio.file.Path.of(it, "bin") } ?: return bare
        return listOf(if (windows) "javaw.exe" else "java", if (windows) "java.exe" else "java")
            .map { bin.resolve(it) }
            .firstOrNull { java.nio.file.Files.isRegularFile(it) }
            ?.toString() ?: bare
    }

    /**
     * Returns JVM arguments required to suppress restricted-access warnings for unnamed modules
     * when running JNA or other native-access libraries on Java 16+.
     *
     * On Java 15 and earlier the flag does not exist and must be omitted.
     */
    fun nativeAccessArgs(): List<String> =
        if (Runtime.version().feature() >= 16)
            listOf("--enable-native-access=ALL-UNNAMED")
        else
            emptyList()
}