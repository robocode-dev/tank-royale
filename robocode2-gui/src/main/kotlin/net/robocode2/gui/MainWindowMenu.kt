package net.robocode2.gui

import io.reactivex.subjects.PublishSubject
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import net.robocode2.gui.extensions.JMenuExt.addNewMenuItem

object MainWindowMenu : JMenuBar() {

    val onWewBattle: PublishSubject<Unit> = PublishSubject.create()
    val onSetupRules: PublishSubject<Unit> = PublishSubject.create()

    init {
        val menuBundle = ResourceBundles.MENU

        val battleMenu = JMenu(menuBundle.get("menu.battle"))
        add(battleMenu)

        battleMenu.addNewMenuItem("item.new_battle", onWewBattle)
        battleMenu.addSeparator()
        battleMenu.addNewMenuItem("item.setup_rules", onSetupRules)
    }
}
