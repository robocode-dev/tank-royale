package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.common.Event
import javax.swing.JMenuItem

object MenuEventTriggers {
    val onStartBattle = MenuEvent()
    val onReplayFromFile = MenuEvent()
    val onSetupRules = MenuEvent()
    val onShowServerLog = MenuEvent()
    val onStartServer = MenuEvent()
    val onStopServer = MenuEvent()
    val onRebootServer = MenuEvent()
    val onServerConfig = MenuEvent()
    val onBotDirConfig = MenuEvent()
    val onDebugConfig = MenuEvent()
    val onSoundConfig = MenuEvent()
    val onHelp = MenuEvent()
    val onAbout = MenuEvent()
}

class MenuEvent : Event<JMenuItem>()