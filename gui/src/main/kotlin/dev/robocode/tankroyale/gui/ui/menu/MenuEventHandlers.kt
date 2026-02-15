package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.DebugConfigDialog
import dev.robocode.tankroyale.gui.ui.config.ServerConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.config.SoundConfigDialog
import dev.robocode.tankroyale.gui.ui.config.GuiConfigDialog
import dev.robocode.tankroyale.gui.ui.newbattle.NewBattleDialog
import dev.robocode.tankroyale.gui.ui.replay.ReplayFileChooser
import dev.robocode.tankroyale.gui.ui.server.RemoteServer
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerEventTriggers
import dev.robocode.tankroyale.gui.ui.server.ServerLogFrame
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.gui.util.MessageDialog
import dev.robocode.tankroyale.gui.util.isRemoteEndpoint

object MenuEventHandlers {

    private const val HELP_URL = "https://robocode.dev/"

    init {
        MenuEventTriggers.apply {
            onSetupRules += this to {
                SetupRulesDialog.isVisible = true
            }
            onStartBattle += this to {
                startBattle()
            }
            onReplayFromFile += this to {
                startReplayFromFile()
            }
            onShowServerLog += this to {
                ServerLogFrame.isVisible = true
            }
            onBotDirConfig += this to {
                BotRootDirectoriesConfigDialog.isVisible = true
            }
            onStartServer += this to {
                ServerEventTriggers.onStartLocalServer.fire(Unit)
                ServerLogFrame.isVisible = true
            }
            onStopServer += this to {
                ServerEventTriggers.onStopLocalServer.fire(Unit)
                ServerLogFrame.isVisible = false
            }
            onRebootServer += this to {
                ServerEventTriggers.onRebootLocalServer.fire(false /* user initiated */)
            }
            onServerConfig += this to {
                ServerConfigDialog().isVisible = true
            }
            onDebugConfig += this to {
                DebugConfigDialog.isVisible = true
            }
            onSoundConfig += this to {
                SoundConfigDialog.isVisible = true
            }
            onGuiConfig += this to {
                GuiConfigDialog.isVisible = true
            }
            onHelp += this to {
                Browser.browse(HELP_URL)
            }
            onAbout += this to {
                AboutBox.isVisible = true
            }
        }
    }

    private fun startBattle() {
        val serverUrl = ServerSettings.serverUrl()
        if (isRemoteEndpoint(serverUrl) && !RemoteServer.isRunning(serverUrl)) {
            MessageDialog.showError(String.format(Messages.get("cannot_connect_to_remote_server"), serverUrl))

            ServerConfigDialog().isVisible = true

        } else if (Server.connectOrStart()) {
            NewBattleDialog.isVisible = true
        }
    }

    private fun startReplayFromFile() {
        ReplayFileChooser.chooseAndStartReplay()
    }
}
