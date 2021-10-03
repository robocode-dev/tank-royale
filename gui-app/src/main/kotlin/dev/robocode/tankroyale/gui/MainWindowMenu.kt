package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.extensions.JMenuExt.addNewMenuItem
import dev.robocode.tankroyale.gui.ui.ResourceBundles.MENU
import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerEventChannel
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

    private val onStartServer = MenuEvent()
    private val onRestartServer = MenuEvent()
    private val onStopServer = MenuEvent()

    private val onAbout = MenuEvent()

    private var startServerMenuItem: JMenuItem? = null
    private var restartServerMenuItem: JMenuItem? = null
    private var stopServerMenuItem: JMenuItem? = null

    init {
        add(JMenu(MENU.get("menu.battle")).apply {
            addNewMenuItem("item.start_battle", onStartBattle)
            addSeparator()
            addNewMenuItem("item.setup_rules", onSetupRules)
        })

        val serverMenu = JMenu(MENU.get("menu.server")).apply {
            onStartServer.invokeLater(this) { ServerEventChannel.onStartServer.fire(Unit) }
            onRestartServer.invokeLater(this) { ServerEventChannel.onRestartServer.fire(Unit) }
            onStopServer.invokeLater(this) { ServerEventChannel.onStopServer.fire(Unit) }

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
        }
        add(serverMenu)

        add(JMenu(MENU.get("menu.config")).apply {
            addNewMenuItem("item.bot_dir_config", onBotDirConfig)
        })

        add(JMenu(MENU.get("menu.help")).apply {
            addNewMenuItem("item.about", onAbout)
        })

        onAbout.invokeLater(this) { AboutBox.isVisible = true }

        ServerProcess.apply {
            onStarted.subscribe(MainWindowMenu) { updateServerState() }
            onStopped.subscribe(MainWindowMenu) { updateServerState() }
        }
    }

    private fun updateServerState() {
        startServerMenuItem?.isEnabled = !Server.isRunning()
        restartServerMenuItem?.isEnabled = ServerProcess.isRunning()
        stopServerMenuItem?.isEnabled = ServerProcess.isRunning()
    }
}

class MenuEvent : Event<JMenuItem>()