package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.extensions.PathExt.getFileExtension
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId

object ResourceUtil {

    fun getResourceFile(resourceName: String): File? {
        var file: File? = null
        val resource = javaClass.classLoader.getResource(resourceName)
            ?: throw FileNotFoundException("Could not find resource file: $resourceName")
        if (resource.toString().startsWith("jar:")) {
            try {
                val inputStream = javaClass.classLoader.getResourceAsStream(resourceName)
                if (inputStream != null) {
                    val filename = "${LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()}"
                    val fileExt = Paths.get(resourceName).getFileExtension()
                    val tempFilePath = Files.createTempFile(filename, ".$fileExt")
                    Files.copy(inputStream, tempFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                    file = tempFilePath.toFile()
                    file.deleteOnExit() // Delete temp file when JVM exists
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        } else {
            // this will probably work in your IDE, but not from a JAR
            file = File(resource.file)
        }
        return file
    }
}