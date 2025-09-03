package dev.robocode.tankroyale.gui.util

object JavaVersion {

    val version: String by lazy { System.getProperty("java.version") }
    val vendor: String by lazy { System.getProperty("java.vendor") }
    val wordSize: String by lazy { fetchArch() }

    private fun fetchArch(): String {
        val model = System.getProperty("sun.arch.data.model")
        return if (model != null) {
            try {
                val numBits = model.toInt()
                "$numBits-bit"
            } catch (ignore: NumberFormatException) {
                "?"
            }
        } else {
            "?"
        }
    }
}