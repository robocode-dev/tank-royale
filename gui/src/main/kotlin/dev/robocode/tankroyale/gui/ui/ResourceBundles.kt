package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import java.util.*


object MenuTitles {
    fun get(propertyName: String): String = ResourceBundles.MENU.get(propertyName)
}

object UiTitles {
    fun get(propertyName: String): String = ResourceBundles.UI_TITLES.get(propertyName)
}

object Strings {
    fun get(propertyName: String): String = ResourceBundles.STRINGS.get(propertyName)
}

object Messages {
    fun get(propertyName: String): String = ResourceBundles.MESSAGES.get(propertyName)
}

object Hints {
    fun get(propertyName: String): String = "<html>${ResourceBundles.HINTS.get(propertyName)}</html>"
}

object About {
    fun get(propertyName: String): String = ResourceBundles.ABOUT.get(propertyName)
}

private enum class ResourceBundles(private val resourceName: String) {

    UI_TITLES("UI titles"),
    STRINGS("Strings"),
    MESSAGES("Messages"),
    MENU("Menu"),
    HINTS("Hints"),
    ABOUT("About");

    private val supportedLocales = listOf(
        Locale.ENGLISH,
        Locale("es"),
        Locale("ca"),
        Locale("da"),
    )

    fun get(propertyName: String): String {
        return try {
            ResourceBundle.getBundle(resourceName, getLocale()).getString(propertyName)
        } catch (_: MissingResourceException) {
            try {
                ResourceBundle.getBundle(resourceName, Locale.ENGLISH).getString(propertyName)
            } catch (_: MissingResourceException) {
                "[$propertyName]"
            }
        }
    }

    private fun getLocale(): Locale {
        val lang = try { ConfigSettings.language } catch (_: Exception) { "en" }
        val selected = when (lang.lowercase(Locale.getDefault())) {
            "es" -> Locale("es")
            "ca" -> Locale("ca")
            "va" -> Locale("ca") // legacy alias: Valencian -> Catalan
            "da" -> Locale("da")
            else -> Locale.ENGLISH
        }
        return if (supportedLocales.contains(selected)) selected else Locale.ENGLISH
    }
}
