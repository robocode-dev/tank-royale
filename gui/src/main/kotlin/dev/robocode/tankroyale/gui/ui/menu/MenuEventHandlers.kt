package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.common.event.On
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
            onSetupRules += On(this) {
                SetupRulesDialog.isVisible = true
            }
            onStartBattle += On(this) {
                startBattle()
            }
            onReplayFromFile += On(this) {
                startReplayFromFile()
            }
            onShowServerLog += On(this) {
                ServerLogFrame.isVisible = true
            }
            onBotDirConfig += On(this) {
                BotRootDirectoriesConfigDialog.isVisible = true
            }
            onStartServer += On(this) {
                ServerEventTriggers.onStartLocalServer(Unit)
                ServerLogFrame.isVisible = true
            }
            onStopServer += On(this) {
                ServerEventTriggers.onStopLocalServer(Unit)
                ServerLogFrame.isVisible = false
            }
            onRebootServer += On(this) {
                ServerEventTriggers.onRebootLocalServer(false /* user initiated */)
            }
            onServerConfig += On(this) {
                ServerConfigDialog().isVisible = true
            }
            onDebugConfig += On(this) {
                DebugConfigDialog.isVisible = true
            }
            onSoundConfig += On(this) {
                SoundConfigDialog.isVisible = true
            }
            onGuiConfig += On(this) {
                GuiConfigDialog.isVisible = true
            }
            onHelp += On(this) {
                Browser.browse(HELP_URL)
            }
            onAbout += On(this) {
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
