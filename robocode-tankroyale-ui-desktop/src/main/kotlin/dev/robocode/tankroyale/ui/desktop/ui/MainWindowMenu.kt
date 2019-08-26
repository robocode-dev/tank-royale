package dev.robocode.tankroyale.ui.desktop.ui

import dev.robocode.tankroyale.ui.desktop.extensions.JMenuExt.addNewMenuItem
import dev.robocode.tankroyale.ui.desktop.utils.Event
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

object MainWindowMenu : JMenuBar() {

    // Public events
    val onNewBattle = Event<JMenuItem>()
    val onSetupRules = Event<JMenuItem>()
    val onShowServerLog = Event<JMenuItem>()
    val onServerConfig = Event<JMenuItem>()

    init {
        add(JMenu(ResourceBundles.MENU.get("menu.battle")).apply {
            addNewMenuItem("item.new_battle", onNewBattle)
            addSeparator()
            addNewMenuItem("item.setup_rules", onSetupRules)
        })
        add(JMenu(ResourceBundles.MENU.get("menu.server")).apply {
            addNewMenuItem("item.show_server_log", onShowServerLog)
            addNewMenuItem("item.server_config", onServerConfig)
        })
    }
}
