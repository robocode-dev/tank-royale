package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.DebugConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow

object MenuActions {
    init {
        with(MenuEvents) {
            onSetupRules.invokeLater(this) {
                SetupRulesDialog.isVisible = true
            }
            onShowServerLog.invokeLater(this) {
                ServerLogWindow.isVisible = true
            }
            onServerConfig.invokeLater(this) {
                SelectServerDialog.isVisible = true
            }
            onBotDirConfig.invokeLater(this) {
                BotRootDirectoriesConfigDialog.isVisible = true
            }
            onAbout.invokeLater(this) {
                AboutBox.isVisible = true
            }
            onStartServer.invokeLater(this) {
                ServerLogWindow.isVisible = true
                ServerEvents.onStartServer.fire(Unit)
            }
            onRestartServer.invokeLater(this) {
                ServerLogWindow.isVisible = true
                ServerEvents.onRestartServer.fire(Unit)
            }
            onStopServer.invokeLater(this) {
                ServerLogWindow.isVisible = false
                ServerEvents.onStopServer.fire(Unit)
            }
            onDebugConfig.invokeLater(this) {
                DebugConfigDialog.isVisible = true
            }
        }
    }
}