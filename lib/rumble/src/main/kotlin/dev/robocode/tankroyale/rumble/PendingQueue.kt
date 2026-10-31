package dev.robocode.tankroyale.rumble

/**
 * Items waiting to be sent to one Rumble.
 *
 * A queue belongs to the environment it was created for for its whole life. There is no way to retarget it, so
 * switching environments in the program leaves an existing queue where it was.
 */
class PendingQueue<T>(val environment: RumbleEnvironment, items: List<T> = emptyList()) {
    private val items: MutableList<T> = items.toMutableList()

    fun add(item: T) {
        items.add(item)
    }

    /** The queued items, but only to a caller that names the environment the queue belongs to. */
    fun itemsFor(expected: RumbleEnvironment): List<T> {
        if (expected.name != environment.name) {
            throw EnvironmentRefused(
                "This queue belongs to the ${environment.displayName} Rumble, not the ${expected.displayName} Rumble."
            )
        }
        return items.toList()
    }
}
