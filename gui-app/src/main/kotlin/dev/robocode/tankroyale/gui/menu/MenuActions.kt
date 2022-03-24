package dev.robocode.tankroyale.gui.menu

import dev.robocode.tankroyale.gui.menu.MenuEvents.onBotDirConfig
import dev.robocode.tankroyale.gui.menu.MenuEvents.onServerConfig
import dev.robocode.tankroyale.gui.menu.MenuEvents.onSetupRules
import dev.robocode.tankroyale.gui.menu.MenuEvents.onShowServerLog
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow

object MenuActions {
    init {
        onSetupRules.invokeLater(this) { SetupRulesDialog.isVisible = true }
        onShowServerLog.invokeLater(this) { ServerLogWindow.isVisible = true }
        onServerConfig.invokeLater(this) { SelectServerDialog.isVisible = true }
        onBotDirConfig.invokeLater(this) { BotRootDirectoriesConfigDialog.isVisible = true }
    }
}