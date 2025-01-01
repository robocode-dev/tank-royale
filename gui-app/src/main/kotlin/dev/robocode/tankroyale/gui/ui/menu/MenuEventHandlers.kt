package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.DebugConfigDialog
import dev.robocode.tankroyale.gui.ui.config.ServerConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.config.SoundConfigDialog
import dev.robocode.tankroyale.gui.ui.newbattle.NewBattleDialog
import dev.robocode.tankroyale.gui.ui.server.RemoteServer
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerEventTriggers
import dev.robocode.tankroyale.gui.ui.server.ServerLogFrame
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.gui.util.MessageDialog
import dev.robocode.tankroyale.gui.util.isRemoteEndpoint

object MenuEventHandlers {

    private const val HELP_URL = "https://robocode-dev.github.io/tank-royale/tutorial/getting-started.html"

    init {
        MenuEventTriggers.apply {
            onSetupRules.subscribe(this) {
                SetupRulesDialog.isVisible = true
            }
            onStartBattle.subscribe(this) {
                startBattle()
            }
            onShowServerLog.subscribe(this) {
                ServerLogFrame.isVisible = true
            }
            onBotDirConfig.subscribe(this) {
                BotRootDirectoriesConfigDialog.isVisible = true
            }
            onStartServer.subscribe(this) {
                ServerEventTriggers.onStartLocalServer.fire(Unit)
                ServerLogFrame.isVisible = true
            }
            onStopServer.subscribe(this) {
                ServerEventTriggers.onStopLocalServer.fire(Unit)
                ServerLogFrame.isVisible = false
            }
            onRebootServer.subscribe(this) {
                ServerEventTriggers.onRebootLocalServer.fire(false /* user initiated */)
            }
            onServerConfig.subscribe(this) {
                ServerConfigDialog().isVisible = true
            }
            onDebugConfig.subscribe(this) {
                DebugConfigDialog.isVisible = true
            }
            onSoundConfig.subscribe(this) {
                SoundConfigDialog.isVisible = true
            }
            onHelp.subscribe(this) {
                Browser.browse(HELP_URL)
            }
            onAbout.subscribe(this) {
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
}