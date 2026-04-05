package dev.robocode.tankroyale.booter.process

import java.util.*

object TemplateManager {
    private val templates = mutableMapOf<String, String>()

    fun getTemplate(platform: String): String? {
        val key = platform.lowercase(Locale.ROOT)
        if (templates.containsKey(key)) {
            return templates[key]
        }

        val templateName = when {
            key.contains("jvm") || key.contains("java") || key.contains("kotlin") -> "jvm.boot"
            key.contains("dotnet") || key.contains(".net") || key.contains("csharp") || key.contains("c#") -> "dotnet.boot"
            key.contains("python") -> "python.boot"
            else -> return null
        }

        val templateContent = loadTemplateFromResources(templateName)
        if (templateContent != null) {
            templates[key] = templateContent
        }
        return templateContent
    }

    private fun loadTemplateFromResources(fileName: String): String? {
        val inputStream = javaClass.getResourceAsStream("/templates/$fileName")
        return inputStream?.bufferedReader()?.use { it.readText() }
    }
}
