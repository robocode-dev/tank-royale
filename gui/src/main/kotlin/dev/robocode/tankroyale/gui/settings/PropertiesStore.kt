package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.common.Event
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.Writer
import java.util.*


open class PropertiesStore(private val title: String, private val fileName: String) {

    val onSaved = Event<Unit>()

    private val properties = Properties()
    private val backedUpProperties = Properties()

    // Determine a per-user writable base directory for settings to avoid permission issues
    // on platforms like Windows where Program Files is read-only for standard users.
    private val baseDir: File by lazy {
        val os = System.getProperty("os.name").lowercase()
        when {
            os.contains("win") -> {
                val localAppData = System.getenv("LOCALAPPDATA")
                    ?: System.getenv("APPDATA")
                    ?: File(System.getProperty("user.home"), "AppData${File.separator}Local").absolutePath
                File(localAppData, "Robocode Tank Royale")
            }

            os.contains("mac") -> File(
                System.getProperty("user.home"),
                "Library${File.separator}Application Support${File.separator}Robocode Tank Royale"
            )

            else -> {
                val xdg = System.getenv("XDG_CONFIG_HOME")
                val base = if (xdg.isNullOrBlank()) File(System.getProperty("user.home"), ".config") else File(xdg)
                File(base, "robocode-tank-royale")
            }
        }.also { it.mkdirs() }
    }

    protected fun load(): Boolean {
        val file = File(baseDir, fileName)
        // Ensure parent exists and create the file if missing
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
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
        if (properties == backedUpProperties) return

        val file = File(baseDir, fileName)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        val output = FileWriter(file)
        output.use {
            val sortedProperties = object : Properties() {
                override fun store(writer: Writer, comments: String) {
                    keys.stream().map { k -> k }.sorted().forEach { k ->
                        val value = "${get(k)}".replace("\\", "\\\\")
                        writer.append("${k}=${value}\n")
                    }
                }
            }
            sortedProperties.putAll(properties) // Use our properties
            sortedProperties.store(output, title)
        }
        onSaved.fire(Unit)
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
}