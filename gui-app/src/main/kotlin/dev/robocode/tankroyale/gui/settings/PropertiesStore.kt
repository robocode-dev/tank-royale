package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.gui.util.Event
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.Writer
import java.util.*


open class PropertiesStore(private val title: String, private val fileName: String) {

    val onSaved = Event<Unit>()

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
                        run {
                            val value = "${get(k)}".replace("\\", "\\\\")
                            writer.append("${k}=${value}\n")
                        }
                    }
                }
            }
            sortedProperties.putAll(properties) // Use our properties
            sortedProperties.store(output, title)
        }
        onSaved.fire(Unit)
    }

    protected fun getPropertyAsSet(propertyName: String): Set<String> =
        HashSet(properties.getProperty(propertyName, "").split(",").filter { it.isNotBlank() })

    protected fun setPropertyBySet(propertyName: String, value: Set<String>) {
        properties.setProperty(propertyName, value.filter { it.isNotBlank() }.joinToString(","))
    }
}