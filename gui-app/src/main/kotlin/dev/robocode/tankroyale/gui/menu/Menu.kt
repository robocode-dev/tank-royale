package dev.robocode.tankroyale.gui.menu

import dev.robocode.tankroyale.gui.menu.MenuEvents.onBotDirConfig
import dev.robocode.tankroyale.gui.menu.MenuEvents.onDebugConfig
import dev.robocode.tankroyale.gui.menu.MenuEvents.onServerConfig
import dev.robocode.tankroyale.gui.menu.MenuEvents.onSetupRules
import dev.robocode.tankroyale.gui.menu.MenuEvents.onShowServerLog
import dev.robocode.tankroyale.gui.menu.MenuEvents.onStartBattle
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.ResourceBundles.MENU
import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.extensions.JMenuExt.addNewMenuItem
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerEventChannel
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow
import dev.robocode.tankroyale.gui.util.Event
import java.awt.event.KeyEvent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.KeyStroke

object Menu : JMenuBar() {

    // Public events


    private val onStartServer = MenuEvent()
    private val onRestartServer = MenuEvent()
    private val onStopServer = MenuEvent()

    private val onAbout = MenuEvent()

    private lateinit var startServerMenuItem: JMenuItem
    private lateinit var restartServerMenuItem: JMenuItem
    private lateinit var stopServerMenuItem: JMenuItem

    init {
        MenuActions

        setupBattleMenu()
        setupServerMenu()
        setupConfigMenu()
        setupHelpMenu()

        onAbout.invokeLater(this) { AboutBox.isVisible = true }

        ServerProcess.apply {
            onStarted.subscribe(Menu) { updateServerState() }
            onStopped.subscribe(Menu) { updateServerState() }
        }
    }

    private fun setupBattleMenu() {
        add(JMenu(MENU.get("menu.battle")).apply {
            mnemonic = KeyEvent.VK_B

            addNewMenuItem("item.start_battle", onStartBattle).apply {
                mnemonic = KeyEvent.VK_B
                accelerator = ctrlDown(mnemonic)
            }
            addSeparator()
            addNewMenuItem("item.setup_rules", onSetupRules).apply {
                mnemonic = KeyEvent.VK_R
                accelerator = ctrlDown(mnemonic)
            }
        })
    }

    private fun setupServerMenu() {
        val serverMenu = JMenu(MENU.get("menu.server")).apply {
            mnemonic = KeyEvent.VK_S

            onStartServer.invokeLater(this) {
                ServerLogWindow.isVisible = true
                ServerEventChannel.onStartServer.fire(Unit)
            }
            onRestartServer.invokeLater(this) {
                ServerLogWindow.isVisible = true
                ServerEventChannel.onRestartServer.fire(Unit)
            }
            onStopServer.invokeLater(this) {
                ServerLogWindow.isVisible = false
                ServerEventChannel.onStopServer.fire(Unit)
            }

            startServerMenuItem = addNewMenuItem("item.start_server", onStartServer)
            restartServerMenuItem = addNewMenuItem("item.restart_server", onRestartServer)
            stopServerMenuItem = addNewMenuItem("item.stop_server", onStopServer)

            addNewMenuItem("item.show_server_log", onShowServerLog).apply {
                mnemonic = KeyEvent.VK_L
                accelerator = ctrlDown(mnemonic)
            }
            addSeparator()
            addNewMenuItem("item.select_server", onServerConfig).apply {
                mnemonic = KeyEvent.VK_E
            }
            addSeparator()

            add(startServerMenuItem).apply {
                mnemonic = KeyEvent.VK_S
                accelerator = ctrlDown(mnemonic)
            }
            add(restartServerMenuItem).apply {
                mnemonic = KeyEvent.VK_R
            }
            add(stopServerMenuItem).apply {
                mnemonic = KeyEvent.VK_T
            }

            updateServerState()
        }
        add(serverMenu)
    }

    private fun setupConfigMenu() {
        add(JMenu(MENU.get("menu.config")).apply {
            mnemonic = KeyEvent.VK_C

            addNewMenuItem("item.bot_root_dirs_config", onBotDirConfig).apply {
                mnemonic = KeyEvent.VK_D
                accelerator = ctrlDown(mnemonic)
            }

            addNewMenuItem("item.debug_config", onDebugConfig).apply {
                mnemonic = KeyEvent.VK_C
            }
        })
    }

    private fun setupHelpMenu() {
        add(JMenu(MENU.get("menu.help")).apply {
            mnemonic = KeyEvent.VK_H

            addNewMenuItem("item.about", onAbout).apply {
                mnemonic = KeyEvent.VK_A
            }
        })
    }

    private fun updateServerState() {
        startServerMenuItem.isEnabled = !Server.isRunning()
        restartServerMenuItem.isEnabled = ServerProcess.isRunning()
        stopServerMenuItem.isEnabled = ServerProcess.isRunning()
    }

    private fun ctrlDown(keyEvent: Int) = KeyStroke.getKeyStroke(keyEvent, KeyEvent.CTRL_DOWN_MASK)
}

class MenuEvent : Event<JMenuItem>()