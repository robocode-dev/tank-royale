package dev.robocode.tankroyale.gui.util

/**
 * Brings a screen up to date after a change, at once when it is showing, or when it is next shown otherwise. A screen
 * that is not showing then does no work for a change that happened elsewhere, such as a download saving its folder.
 */
class RefreshWhenShown(private val isShowing: () -> Boolean, private val refresh: () -> Unit) {
    private var pending = false

    /** Something the screen shows has changed. */
    fun changed() {
        if (isShowing()) {
            pending = false
            refresh()
        } else {
            pending = true
        }
    }

    /** The screen is being shown; it is brought up to date if something changed while it was not showing. */
    fun shown() {
        if (pending) {
            pending = false
            refresh()
        }
    }
}
