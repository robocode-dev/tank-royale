package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.ui.extensions.JMenuExt.addNewMenuItem
import dev.robocode.tankroyale.gui.ui.ResourceBundles.MENU
import dev.robocode.tankroyale.gui.util.Event
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

object MainWindowMenu : JMenuBar() {

    // Public events
    val onSelectBots = Event<JMenuItem>()
    val onSetupRules = Event<JMenuItem>()
    val onShowServerLog = Event<JMenuItem>()
    val onServerConfig = Event<JMenuItem>()
    val onBotDirConfig = Event<JMenuItem>()

    init {
        add(JMenu(MENU.get("menu.battle")).apply {
            addNewMenuItem("item.start_battle", onSelectBots)
            addSeparator()
            addNewMenuItem("item.setup_rules", onSetupRules)
        })
        add(JMenu(MENU.get("menu.server")).apply {
            addNewMenuItem("item.show_server_log", onShowServerLog)
            addNewMenuItem("item.select_server", onServerConfig)
        })
        add(JMenu(MENU.get("menu.config")).apply {
            addNewMenuItem("item.bot_dir_config", onBotDirConfig)
        })
    }
}
