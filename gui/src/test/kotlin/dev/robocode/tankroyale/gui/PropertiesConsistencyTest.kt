package dev.robocode.tankroyale.gui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import java.util.regex.Pattern

class PropertiesConsistencyTest : StringSpec({
    "All locale .properties files must contain the same keys per base name" {
        val candidates = listOf(
            Path.of("src", "main", "resources"),
            Path.of("gui", "src", "main", "resources")
        )
        val resourcesDir: Path = candidates.firstOrNull { Files.isDirectory(it) }
            ?: throw IllegalArgumentException(
                "Resources directory not found. Tried: " + candidates.joinToString(", ")
            )

        val dirFile = resourcesDir.toFile()
        val propertiesFiles: List<File> = dirFile.listFiles { _, name -> name.endsWith(".properties") }?.toList()
            ?: emptyList()

        // Group by base name without locale (e.g., Strings, UI titles, Messages)
        val baseNameRegex = Pattern.compile("^(.+?)(?:_[a-zA-Z]{2}(?:_[A-Z]{2})?)?\\.properties$")

        data class Entry(val baseName: String, val file: File, val keys: Set<String>)

        fun baseNameOf(file: File): String {
            val m = baseNameRegex.matcher(file.name)
            return if (m.matches()) m.group(1) else file.name.removeSuffix(".properties")
        }

        fun loadKeys(file: File): Set<String> = Properties().run {
            FileInputStream(file).use { fis -> this.load(fis) }
            this.stringPropertyNames()
        }

        val entries = propertiesFiles.map { f -> Entry(baseNameOf(f), f, loadKeys(f)) }
        val groups = entries.groupBy { it.baseName }

        val missingByFile = mutableMapOf<File, Set<String>>()

        for ((_, group) in groups) {
            if (group.isEmpty()) continue

            // Prefer the base (non-locale) file as the reference if present; else use union
            val base = group.find { it.file.name.endsWith(".properties") && !it.file.name.matches(".*_[a-zA-Z]{2}(?:_[A-Z]{2})?\\.properties".toRegex()) }
            val referenceKeys: Set<String> = base?.keys ?: group.flatMap { it.keys }.toSet()

            group.forEach { e ->
                val missing = referenceKeys - e.keys
                if (missing.isNotEmpty()) missingByFile[e.file] = missing
            }
        }

        if (missingByFile.isNotEmpty()) {
            val message = buildString {
                appendLine("Missing keys detected in locale .properties files:")
                missingByFile.entries
                    .sortedBy { it.key.name }
                    .forEach { (file, missing) ->
                        appendLine(" - ${file.name}: ${missing.sorted().joinToString(", ")}")
                    }
                appendLine("Ensure that all files sharing the same first name contain the same set of keys.")
            }
            // Use Kotest matcher to show message while failing the test
            println(message)
        }

        // Assert: no missing keys
        missingByFile.values.flatten().toSet().shouldBeEmpty()
    }
})
