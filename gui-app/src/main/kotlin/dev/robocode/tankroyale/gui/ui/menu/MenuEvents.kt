package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.gui.util.Event
import javax.swing.JMenuItem

object MenuEvents {
    val onStartBattle = MenuEvent()
    val onSetupRules = MenuEvent()
    val onShowServerLog = MenuEvent()
    val onStartServer = MenuEvent()
    val onRestartServer = MenuEvent()
    val onStopServer = MenuEvent()
    val onServerConfig = MenuEvent()
    val onBotDirConfig = MenuEvent()
    val onDebugConfig = MenuEvent()
    val onAbout = MenuEvent()
}

class MenuEvent : Event<JMenuItem>()