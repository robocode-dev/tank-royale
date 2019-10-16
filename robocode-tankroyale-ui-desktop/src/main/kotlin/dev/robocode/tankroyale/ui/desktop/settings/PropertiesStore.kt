package dev.robocode.tankroyale.ui.desktop.settings

import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.Writer
import java.util.*


open class PropertiesStore(private val title: String, private val fileName: String) {

    protected val properties = Properties()

    fun load(): Boolean {
        val file = File(fileName)
        val alreadyExists = file.createNewFile()
        val input = FileInputStream(file)
        input.use {
            val tmp = Properties()
            tmp.load(input)
            properties.putAll(tmp)
        }
        return alreadyExists
    }

    open fun save() {
        val output = FileWriter(fileName)
        output.use {
            val sortedProperties = object : Properties() {
                override fun store(writer: Writer, comments: String) {
                    keys.stream().map { k -> k }.sorted().forEach { k ->
                        writer.append("${k}=${get(k)}\n")
                    }
                }
            }
            sortedProperties.putAll(properties) // Use our properties
            sortedProperties.store(output, title)
        }
    }
}