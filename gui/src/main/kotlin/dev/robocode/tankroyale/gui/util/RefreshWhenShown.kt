package dev.robocode.tankroyale.gui.util

/**
 * Brings a screen up to date each time it is shown, and at once after a change while it is showing. A screen that is
 * not showing does no work for a change that happened elsewhere, such as a download placing bots; it is read again
 * when it is next shown, so it never shows a list older than its last opening.
 */
class RefreshWhenShown(private val isShowing: () -> Boolean, private val refresh: () -> Unit) {

    /** Something the screen shows has changed. */
    fun changed() {
        if (isShowing()) refresh()
    }

    /** The screen is being shown. */
    fun shown() = refresh()
}
