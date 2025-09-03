package dev.robocode.tankroyale.gui.ui

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

private enum class ResourceBundles(private val resourceName: String) {

    UI_TITLES("UI titles"),
    STRINGS("Strings"),
    MESSAGES("Messages"),
    MENU("Menu"),
    HINTS("Hints");

    private val supportedLocales = listOf(Locale.ENGLISH)

    private var currentLocale = getLocale()

    fun get(propertyName: String): String {
        return try {
            ResourceBundle.getBundle(resourceName, currentLocale).getString(propertyName)
        } catch (e: MissingResourceException) {
            try {
                ResourceBundle.getBundle(resourceName, Locale.ENGLISH).getString(propertyName)
            } catch (e: MissingResourceException) {
                "[$propertyName]"
            }
        }
    }

    private fun getLocale(): Locale {
        val locale = Locale.getDefault()
        return when (supportedLocales.contains(locale)) {
            true -> locale
            false -> Locale.ENGLISH
        }
    }
}
