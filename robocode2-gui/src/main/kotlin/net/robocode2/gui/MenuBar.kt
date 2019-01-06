package net.robocode2.gui

import io.reactivex.subjects.PublishSubject
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class MenuBar : JMenuBar() {

    val newBattleEvent: PublishSubject<Unit> = PublishSubject.create()
    val setupRulesEvent: PublishSubject<Unit> = PublishSubject.create()

    init {
        val battleMenu = JMenu("Battle")
        add(battleMenu)

        battleMenu.add(createMenuItem("New Battle", newBattleEvent))
        battleMenu.addSeparator()
        battleMenu.add(createMenuItem("Setup Rules", setupRulesEvent))
    }

    private fun createMenuItem(text: String, subject: PublishSubject<Unit>): JMenuItem {
        val menuItem = JMenuItem(text)
        menuItem.addActionListener { subject.onNext(Unit) }
        return menuItem
    }
}
