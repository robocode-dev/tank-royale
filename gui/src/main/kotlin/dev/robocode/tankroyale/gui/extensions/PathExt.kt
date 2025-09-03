package dev.robocode.tankroyale.gui.extensions

import java.nio.file.Path

object PathExt {

    fun Path.getFileExtension(): String? {
        val filename = toString()
        val lastIndex = filename.lastIndexOf('.')
        if (lastIndex == -1) {
            return null
        }
        return filename.substring(lastIndex + 1)
    }
}