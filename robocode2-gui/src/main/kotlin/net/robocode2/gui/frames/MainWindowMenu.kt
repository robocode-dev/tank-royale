package net.robocode2.gui.frames

import net.robocode2.gui.extensions.JMenuExt.addNewMenuItem
import net.robocode2.gui.utils.Observable
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

object MainWindowMenu : JMenuBar() {

    // Public events
    val onNewBattle = Observable<JMenuItem>()
    val onSetupRules = Observable<JMenuItem>()

    init {
        val menuBundle = ResourceBundles.MENU

        val battleMenu = JMenu(menuBundle.get("menu.battle"))
        add(battleMenu)

        battleMenu.addNewMenuItem("item.new_battle", onNewBattle)
        battleMenu.addSeparator()
        battleMenu.addNewMenuItem("item.setup_rules", onSetupRules)
    }
}
