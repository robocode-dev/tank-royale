package net.robocode2.gui.extensions

import io.reactivex.subjects.PublishSubject
import net.robocode2.gui.ResourceBundles
import javax.swing.JMenu
import javax.swing.JMenuItem

object JMenuExt {

    fun JMenu.addNewMenuItem(menuResourceName: String, publishSubject: PublishSubject<Unit>): JMenuItem {
        val menuItem = JMenuItem(ResourceBundles.MENU.get(menuResourceName))
        add(menuItem)
        menuItem.addActionListener { publishSubject.onNext(Unit) }
        return menuItem
    }
}
