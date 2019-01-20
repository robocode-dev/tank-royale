package net.robocode2.gui.settings

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
        val output = FileOutputStream(fileName)
        output.use {
            // Properties are sorted by overriding the keys() method
            val wrapper = object : Properties() {
                override fun keys(): Enumeration<Any>? {
                    return Collections.enumeration(TreeSet<Any>(keys))
                }
            }
            wrapper.putAll(properties) // Use our properties
            wrapper.store(output, title)
        }
    }
}