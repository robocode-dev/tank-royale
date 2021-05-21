package dev.robocode.tankroyale.gui.ui.extensions

import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.util.Event
import javax.swing.JMenu
import javax.swing.JMenuItem

object JMenuExt {

    fun JMenu.addNewMenuItem(menuResourceName: String, event: Event<JMenuItem>): JMenuItem {
        val menuItem = JMenuItem(ResourceBundles.MENU.get(menuResourceName))
        add(menuItem)
        menuItem.addActionListener { event.fire(menuItem) }
        return menuItem
    }
}
