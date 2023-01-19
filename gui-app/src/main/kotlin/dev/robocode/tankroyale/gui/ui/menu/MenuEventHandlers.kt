package dev.robocode.tankroyale.gui.ui.menu

import dev.robocode.tankroyale.gui.ui.about.AboutBox
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.DebugConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.config.SoundConfigDialog
import dev.robocode.tankroyale.gui.ui.newbattle.NewBattleDialog
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerEventTriggers
import dev.robocode.tankroyale.gui.ui.server.ServerLogFrame
import dev.robocode.tankroyale.gui.util.Browser
import dev.robocode.tankroyale.gui.util.GuiTask.enqueue

object MenuEventHandlers {

    private const val HELP_URL = "https://robocode-dev.github.io/tank-royale/articles/gui.html"

    init {
        MenuEventTriggers.apply {
            onSetupRules.subscribe(this) {
                SetupRulesDialog.isVisible = true
            }
            onStartBattle.subscribe(this) {
                enqueue {
                    NewBattleDialog.isVisible = true
                }
                Server.connectOrStart()
            }
            onShowServerLog.subscribe(this) {
                ServerLogFrame.isVisible = true
            }
            onServerConfig.subscribe(this) {
                SelectServerDialog.isVisible = true
            }
            onBotDirConfig.subscribe(this) {
                BotRootDirectoriesConfigDialog.isVisible = true
            }
            onStartServer.subscribe(this) {
                ServerEventTriggers.onStartServer.fire(Unit)
                ServerLogFrame.isVisible = true
            }
            onStopServer.subscribe(this) {
                ServerEventTriggers.onStopServer.fire(Unit)
                ServerLogFrame.isVisible = false
            }
            onRebootServer.subscribe(this) {
                ServerEventTriggers.onRebootServer.fire(false /* user initiated */)
            }
            onDebugConfig.subscribe(this) {
                DebugConfigDialog.isVisible = true
            }
            onSoundConfig.subscribe(this) {
                SoundConfigDialog.isVisible = true
            }
            onHelp.subscribe(this)  {
                Browser.browse(HELP_URL)
            }
            onAbout.subscribe(this) {
                AboutBox.isVisible = true
            }
        }
    }
}