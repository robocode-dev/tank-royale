package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.ui.Messages
import java.awt.Desktop
import java.net.URI

object Browser {

    fun browse(url: String) {
        if (Desktop.isDesktopSupported()) {
            val desktop: Desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI.create(url))
            } else {
                MessageDialog.showError(String.format(Messages.get("desktop_browser_not_supported"), JavaVersion.vendor))
            }
        } else {
            MessageDialog.showError(String.format(Messages.get("desktop_not_supported"), JavaVersion.vendor))
        }
    }
}
