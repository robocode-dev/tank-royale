package dev.robocode.tankroyale.gui.ui

import java.util.*


enum class ResourceBundles(private val resourceName: String) {

    UI_TITLES("UI titles"),
    STRINGS("Strings"),
    MESSAGES("Messages"),
    MENU("Menu");

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