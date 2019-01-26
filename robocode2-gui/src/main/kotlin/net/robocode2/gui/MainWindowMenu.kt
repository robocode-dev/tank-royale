package net.robocode2.gui

import net.robocode2.gui.extensions.JMenuExt.addNewMenuItem
import net.robocode2.gui.utils.Observable
import javax.swing.JMenu
import javax.swing.JMenuBar

object MainWindowMenu : JMenuBar() {

    // Public events
    val onWewBattle = Observable()
    val onSetupRules = Observable()

    init {
        val menuBundle = ResourceBundles.MENU

        val battleMenu = JMenu(menuBundle.get("menu.battle"))
        add(battleMenu)

        battleMenu.addNewMenuItem("item.new_battle", onWewBattle)
        battleMenu.addSeparator()
        battleMenu.addNewMenuItem("item.setup_rules", onSetupRules)
    }
}
