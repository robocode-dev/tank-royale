package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.DebugConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onAbout
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onBotDirConfig
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onDebugConfig
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onRestartServer
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onServerConfig
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onSetupRules
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onShowServerLog
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onStartServer
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents.onStopServer
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.ui.server.ServerEventChannel
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow

object MenuActions {
    init {
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
        onDebugConfig.invokeLater(this) {
            DebugConfigDialog.isVisible = true
        }
    }
}