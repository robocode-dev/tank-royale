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
}