package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.common.util.UserDataDirectory
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.*


open class PropertiesStore(private val title: String, private val fileName: String) {

    val onSaved = Event<Unit>()

    private val properties = Properties()
    private val backedUpProperties = Properties()

    // Use platform-specific user data directory for storing settings
    private val baseDir: File by lazy {
        UserDataDirectory.get()
    }

    protected fun load(): Boolean {
        val file = File(baseDir, fileName)
        // Ensure a parent exists and create the file if missing
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        val alreadyExists = file.createNewFile()
        val tmp = read(file.readBytes())
        properties.clear()
        properties.putAll(tmp)
        return alreadyExists
    }

    open fun save() {
        // Check if properties content has changed by comparing as maps
        if (properties.size == backedUpProperties.size &&
            properties.all { (key, value) -> backedUpProperties[key] == value }
        ) {
            return
        }

        val file = File(baseDir, fileName)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        file.writeBytes(write(properties, title))

        // Update backup to reflect the current saved state
        backedUpProperties.clear()
        backedUpProperties.putAll(properties)

        onSaved(Unit)
    }

    fun propertyNames(): Set<String> = properties.stringPropertyNames()

    fun backup() {
        load()
        backedUpProperties.apply {
            clear()
            putAll(properties)
        }
    }

    fun restore() {
        properties.apply {
            clear()
            putAll(backedUpProperties)
        }
        save()
    }

    protected fun load(propertyName: String, defaultValue: String): String {
        load()
        return properties.getProperty(propertyName, defaultValue)
    }

    protected fun load(propertyName: String): String? {
        load()
        return properties.getProperty(propertyName)
    }

    protected fun save(propertyName: String, value: String) {
        val changed = load(propertyName) != value
        if (changed) {
            properties.setProperty(propertyName, value)
            save()
        }
    }

    protected fun set(propertyName: String, value: String) {
        properties.setProperty(propertyName, value)
    }

    protected fun getPropertyAsSet(propertyName: String): Set<String> {
        load()
        return HashSet(properties.getProperty(propertyName, "").split(",").filter { it.isNotBlank() })
    }

    protected fun setPropertyBySet(propertyName: String, value: Set<String>) {
        properties.setProperty(propertyName, value.filter { it.isNotBlank() }.joinToString(","))
        save()
    }

    protected fun loadIndexedProperties(propertyName: String): ArrayList<String> {
        load()

        val props = ArrayList<String>()
        var index = 0
        while (true) {
            val value = properties["$propertyName.$index"] as String? ?: break
            props.add(value)
            index++
        }
        return props
    }

    protected fun saveIndexedProperties(propertyName: String, props: List<String>) {
        removeIndexedProperties(propertyName)

        props.withIndex().forEach { (index, prop) ->
            properties["$propertyName.$index"] = prop
        }
        save()
    }

    private fun removeIndexedProperties(propertyName: String) {
        var index = 0
        while (properties.remove("$propertyName.$index") != null) {
            index++
        }
    }

    companion object {
        /**
         * The settings as they are written to the file: sorted, one per line, with every letter outside ASCII written as
         * a Unicode escape, so a path such as a home folder named Jørgen is read back as it was written, by this version
         * and by older ones, which read the file as ISO-8859-1.
         */
        internal fun write(properties: Properties, title: String): ByteArray {
            val sortedProperties = object : Properties() {
                override fun store(writer: Writer, comments: String) {
                    keys.stream().map { k -> k }.sorted().forEach { k ->
                        val value = asciiOnly("${get(k)}")
                        writer.append("${k}=${value}\n")
                    }
                }
            }
            sortedProperties.putAll(properties)
            val writer = StringWriter()
            sortedProperties.store(writer, title)
            return writer.toString().toByteArray(StandardCharsets.UTF_8)
        }

        /** Writes each character outside ASCII as a Unicode escape, which every reader of a properties file understands. */
        private fun asciiOnly(text: String): String = buildString {
            text.forEach { c -> if (c.code < 0x80) append(c) else append("\\u%04x".format(c.code)) }
        }

        /**
         * Reads the settings from the bytes of the file. [write] writes only ASCII, but a file some earlier versions
         * wrote may hold UTF-8, and one written by still older versions a single-byte charset, read as ISO-8859-1 as before.
         */
        internal fun read(bytes: ByteArray): Properties {
            val text = try {
                StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString()
            } catch (_: CharacterCodingException) {
                String(bytes, StandardCharsets.ISO_8859_1)
            }
            return Properties().apply { load(StringReader(text)) }
        }
    }
}
