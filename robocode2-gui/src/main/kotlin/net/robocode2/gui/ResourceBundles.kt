package net.robocode2.gui

import java.lang.IllegalArgumentException
import java.util.*

enum class ResourceBundles(val resourceName: String) {

    WINDOW_TITLES("WindowTitles"),
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
                throw ResourceBundleException("Could not find the property name \"$propertyName\" in the resource bundle \"$resourceName\"")
            }
        }
    }

    fun setLocate(locale: Locale) {
        currentLocale = when (supportedLocales.contains(locale)) {
            true -> locale
            false -> throw IllegalArgumentException("Locale not supported")
        }
    }

    private fun getLocale(): Locale {
        val locale = Locale.getDefault()
        return when (supportedLocales.contains(locale)) {
            true -> locale
            false -> Locale.ENGLISH
        }
    }

    class ResourceBundleException(msg: String) : RuntimeException(msg)
}