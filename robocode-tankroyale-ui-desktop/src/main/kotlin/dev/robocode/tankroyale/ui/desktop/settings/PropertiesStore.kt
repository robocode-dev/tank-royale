package dev.robocode.tankroyale.ui.desktop.settings

import java.io.*
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
                        try {
                            writer.append("${k}=${get(k)}\n")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            sortedProperties.putAll(properties) // Use our properties
            sortedProperties.store(output, title)
        }
    }
}