package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.extensions.JMenuExt.addNewMenuItem
import dev.robocode.tankroyale.gui.ui.ResourceBundles.MENU
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.util.Event
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

object MainWindowMenu : JMenuBar() {

    // Public events
    val onStartBattle = MenuEvent()
    val onSetupRules = MenuEvent()
    val onShowServerLog = MenuEvent()
    val onServerConfig = MenuEvent()
    val onBotDirConfig = MenuEvent()
    val onStartServer = MenuEvent()
    val onRestartServer = MenuEvent()
    val onStopServer = MenuEvent()

    var startServerMenuItem: JMenuItem? = null
    var restartServerMenuItem: JMenuItem? = null
    var stopServerMenuItem: JMenuItem? = null

    init {
        add(JMenu(MENU.get("menu.battle")).apply {
            addNewMenuItem("item.start_battle", onStartBattle)
            addSeparator()
            addNewMenuItem("item.setup_rules", onSetupRules)
        })
        add(JMenu(MENU.get("menu.server")).apply {
            startServerMenuItem = addNewMenuItem("item.start_server", onStartServer)
            restartServerMenuItem = addNewMenuItem("item.restart_server", onRestartServer)
            stopServerMenuItem = addNewMenuItem("item.stop_server", onStopServer)

            addNewMenuItem("item.show_server_log", onShowServerLog)
            addSeparator()
            addNewMenuItem("item.select_server", onServerConfig)
            addSeparator()

            add(startServerMenuItem)
            add(restartServerMenuItem)
            add(stopServerMenuItem)

            updateServerState()
        })
        add(JMenu(MENU.get("menu.config")).apply {
            addNewMenuItem("item.bot_dir_config", onBotDirConfig)
        })

        ServerProcess.apply {
            onStarted.subscribe(this) { updateServerState() }
            onStopped.subscribe(this) { updateServerState() }
        }
    }

    private fun updateServerState() {
        startServerMenuItem?.isEnabled = !Server.isRunning()
        restartServerMenuItem?.isEnabled = Server.isRunning()
        stopServerMenuItem?.isEnabled = Server.isRunning()
    }
}

class MenuEvent : Event<JMenuItem>()