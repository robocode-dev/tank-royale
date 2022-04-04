package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.DebugConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.newbattle.NewBattleDialog
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerEventTriggers
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow
import java.awt.EventQueue

object MenuEventHandlers {
    init {
        with(MenuEventTriggers) {
            onSetupRules.subscribe(this) {
                SetupRulesDialog.isVisible = true
            }
            onStartBattle.subscribe(this) {
                EventQueue.invokeLater {
                    NewBattleDialog.isVisible = true
                }
                Server.connectOrStart()
            }
            onShowServerLog.subscribe(this) {
                ServerLogWindow.isVisible = true
            }
            onServerConfig.subscribe(this) {
                SelectServerDialog.isVisible = true
            }
            onBotDirConfig.subscribe(this) {
                BotRootDirectoriesConfigDialog.isVisible = true
            }
            onAbout.subscribe(this) {
                AboutBox.isVisible = true
            }
            onStartServer.subscribe(this) {
                ServerEventTriggers.onStartServer.fire(Unit)
                ServerLogWindow.isVisible = true
            }
            onStopServer.subscribe(this) {
                ServerEventTriggers.onStopServer.fire(Unit)
                ServerLogWindow.isVisible = false
            }
            onRebootServer.subscribe(this) {
                ServerEventTriggers.onRebootServer.fire(false /* user initiated */)
            }
            onDebugConfig.subscribe(this) {
                DebugConfigDialog.isVisible = true
            }
        }
    }
}