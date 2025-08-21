package dev.robocode.tankroyale.botapi

/**
 * Initial starting position containing a start coordinate (x,y) and the shared direction of the body, gun, and radar.
 *
 * The initial position is only used when debugging to request the server to let a bot start at a specific position.
 * Note that initial starting positions must be enabled at the server-side; otherwise the initial starting position
 * is ignored.
 */
class InitialPosition(val x: Double?, val y: Double?, val direction: Double?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as InitialPosition
        return x == other.x && y == other.y && direction == other.direction
    }

    override fun hashCode(): Int {
        var result = x?.hashCode() ?: 0
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + (direction?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        if (x == null && y == null && direction == null) return ""
        val sx = x?.toString() ?: ""
        val sy = y?.toString() ?: ""
        val sd = direction?.toString() ?: ""
        return "$sx,$sy,$sd"
    }

    companion object {
        /**
         * Creates a new instance of the InitialPosition class from a string.
         *
         * @param initialPosition is comma and/or white-space separated string.
         * @return An InitialPosition instance or null if input represents an empty position.
         */
        fun fromString(initialPosition: String?): InitialPosition? {
            if (initialPosition == null || initialPosition.isBlank()) return null
            val trimmed = initialPosition.trim()
            // Treat strings containing only commas and/or whitespace as empty
            if (trimmed.replace(Regex("[,\\s]"), "").isEmpty()) return null

            val values = trimmed.split(Regex("\\s*,\\s*|\\s+"))
            return parseInitialPosition(values)
        }

        private fun parseInitialPosition(values: List<String>): InitialPosition? {
            if (values.isEmpty()) return null

            val x = parseDouble(values.getOrNull(0))
            val y = if (values.size >= 2) parseDouble(values[1]) else null
            val direction = if (values.size >= 3) parseDouble(values[2]) else null
            return InitialPosition(x, y, direction)
        }

        private fun parseDouble(str: String?): Double? {
            if (str == null) return null
            return try {
                str.trim().toDouble()
            } catch (_: Throwable) {
                null
            }
        }
    }
}
