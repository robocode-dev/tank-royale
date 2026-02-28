package dev.robocode.tankroyale.common.util

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
                    val ext = Paths.get(resourceName).fileName?.toString()
                        ?.substringAfterLast('.', "")
                        ?.takeIf { it.isNotEmpty() }
                    val tempFilePath = if (ext != null) Files.createTempFile(filename, ".$ext")
                                       else Files.createTempFile(filename, "")
                    Files.copy(inputStream, tempFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                    file = tempFilePath.toFile()
                    file.deleteOnExit()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        } else {
            file = File(resource.file)
        }
        return file
    }
}
