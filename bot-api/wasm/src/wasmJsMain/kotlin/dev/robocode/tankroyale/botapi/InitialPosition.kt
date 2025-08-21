package dev.robocode.tankroyale.botapi

/**
 * Initial starting position containing a start coordinate (x,y) and the shared direction of the body, gun, and radar.
 *
 * The initial position is only used when debugging to request the server to let a bot start at a specific position.
 * Note that initial starting positions must be enabled at the server-side; otherwise the initial starting position
 * is ignored.
 */
class InitialPosition(
    /**
     * Returns the x coordinate;
     *
     * @return The x coordinate or `null` if no x coordinate is specified and a random value must be used.
     */
    val x: Double?,

    /**
     * Returns the y coordinate;
     *
     * @return The y coordinate or `null` if no y coordinate is specified and a random value must be used.
     */
    val y: Double?,

    /**
     * Returns the shared direction of the body, gun, and radar;
     *
     * @return The direction or `null` if no direction is specified and a random value must be used.
     */
    val direction: Double?
) {

    /**
     * {@inheritDoc}
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InitialPosition

        if (x != other.x) return false
        if (y != other.y) return false
        if (direction != other.direction) return false

        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun hashCode(): Int {
        var result = x?.hashCode() ?: 0
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + (direction?.hashCode() ?: 0)
        return result
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        if (x == null && y == null && direction == null) return ""
        val strX = x?.toString() ?: ""
        val strY = y?.toString() ?: ""
        val strDirection = direction?.toString() ?: ""
        return "$strX,$strY,$strDirection"
    }

    companion object {
        /**
         * Creates a new instance of the InitialPosition class from a string.
         *
         * @param initialPosition is comma and/or white-space separated string.
         * @return An InitialPosition instance.
         */
        fun fromString(initialPosition: String?): InitialPosition? {
            if (initialPosition == null || initialPosition.isBlank()) return null
            // Treat strings containing only commas and/or whitespace as empty
            val trimmed = initialPosition.trim()
            if (trimmed.replace(Regex("[,\\s]"), "").isEmpty()) return null

            val values = trimmed.split(Regex("\\s*,\\s*|\\s+"))
            return parseInitialPosition(values.toTypedArray())
        }

        private fun parseInitialPosition(values: Array<String>): InitialPosition? {
            if (values.isEmpty()) return null

            val x = parseDouble(values[0])
            if (values.size < 2) {
                return InitialPosition(x, null, null)
            }
            val y = parseDouble(values[1])
            var direction: Double? = null
            if (values.size >= 3) {
                direction = parseDouble(values[2])
            }
            return InitialPosition(x, y, direction)
        }

        private fun parseDouble(str: String?): Double? {
            if (str == null) return null
            return try {
                str.trim().toDouble()
            } catch (ex: NumberFormatException) {
                null
            }
        }
    }
}