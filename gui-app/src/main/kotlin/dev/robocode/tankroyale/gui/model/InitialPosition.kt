package dev.robocode.tankroyale.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class InitialPosition(
    val x: Double?, val y: Double?, val angle: Double?) {

    companion object {
        fun fromString(initialPosition: String?): InitialPosition? {
            if (initialPosition == null || initialPosition.isBlank()) return null
            val values = initialPosition.trim().split("\\s*,\\s*|\\s+".toRegex()).toTypedArray()
            return parseInitialPosition(values)
        }

        private fun parseInitialPosition(values: Array<String>): InitialPosition? {
            if (values.isEmpty()) return null
            val x: Double? = parseDouble(values[0])
            if (values.size < 2) {
                return InitialPosition(x, null, null)
            }
            val y: Double? = parseDouble(values[1])
            var angle: Double? = null
            if (values.size >= 3) {
                angle = parseDouble(values[2])
            }
            return InitialPosition(x, y, angle)
        }

        private fun parseDouble(str: String?): Double? {
            return if (str == null) null
            else try {
                str.trim().toDouble()
            } catch (ex: NumberFormatException) {
                null
            }
        }
    }
}
