package net.robocode2.gui.extensions

import net.robocode2.gui.ui.ResourceBundles
import net.robocode2.gui.utils.Observable
import javax.swing.JMenu
import javax.swing.JMenuItem

object JMenuExt {

    fun JMenu.addNewMenuItem(menuResourceName: String, observable: Observable<JMenuItem>): JMenuItem {
        val menuItem = JMenuItem(ResourceBundles.MENU.get(menuResourceName))
        add(menuItem)
        menuItem.addActionListener { observable.notify(menuItem) }
        return menuItem
    }
}
