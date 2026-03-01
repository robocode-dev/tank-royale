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
            onSetupRules.on(this) {
                SetupRulesDialog.isVisible = true
            }
            onStartBattle.on(this) {
                startBattle()
            }
            onReplayFromFile.on(this) {
                startReplayFromFile()
            }
            onShowServerLog.on(this) {
                ServerLogFrame.isVisible = true
            }
            onBotDirConfig.on(this) {
                BotRootDirectoriesConfigDialog.isVisible = true
            }
            onStartServer.on(this) {
                ServerEventTriggers.onStartLocalServer(Unit)
                ServerLogFrame.isVisible = true
            }
            onStopServer.on(this) {
                ServerEventTriggers.onStopLocalServer(Unit)
                ServerLogFrame.isVisible = false
            }
            onRebootServer.on(this) {
                ServerEventTriggers.onRebootLocalServer(false /* user initiated */)
            }
            onServerConfig.on(this) {
                ServerConfigDialog().isVisible = true
            }
            onDebugConfig.on(this) {
                DebugConfigDialog.isVisible = true
            }
            onSoundConfig.on(this) {
                SoundConfigDialog.isVisible = true
            }
            onGuiConfig.on(this) {
                GuiConfigDialog.isVisible = true
            }
            onHelp.on(this) {
                Browser.browse(HELP_URL)
            }
            onAbout.on(this) {
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
