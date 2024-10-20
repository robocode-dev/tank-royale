package dev.robocode.tankroyale.gui.util

import java.nio.file.Files
import java.nio.file.Paths

object FileUtil {

    /**
     * Checks if a directory is missing or empty.
     *
     * @return `true` if the directory is missing or empty; `false` otherwise.
     */
    fun isMissingOrEmptyDir(filename: String): Boolean {
        val path = Paths.get(filename)
        if (!Files.exists(path)) {
            return true
        }
        if (Files.isDirectory(path)) {
            Files.list(path).use { entries -> return !entries.findFirst().isPresent }
        }
        return true
    }

    /**
     * Searches for a file in the current directory that matches the given prefix and suffix.
     *
     * This function scans the current working directory for files whose names start with
     * the specified [startsWith] string and end with the specified [endsWith] string.
     * It returns the path of the first matching file as a string, or null if no matching file is found.
     *
     * @param prefix The prefix that the filename should start with.
     * @param suffix The suffix that the filename should end with.
     * @return The string representation of the path to the first matching file, or `null` if no match is found.
     * @throws java.io.IOException If an I/O error occurs when opening the directory.
     */
    fun findFirstInCurrentDirectory(prefix: String, suffix: String): String? {
        return Paths.get("").let { currentPath ->
            Files.list(currentPath)
                .filter { it.fileName.toString().startsWith(prefix) && it.fileName.toString().endsWith(suffix) }
                .findFirst()
                .map { it.toString() }
                .orElse(null)
        }
    }
}