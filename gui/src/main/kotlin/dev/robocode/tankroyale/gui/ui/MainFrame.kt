package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.common.event.On
import dev.robocode.tankroyale.gui.audio.SoundActions
import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.recorder.AutoRecorder
import dev.robocode.tankroyale.gui.ui.menu.Menu
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.recorder.RecorderProcess
import dev.robocode.tankroyale.gui.ui.arena.BattlePanel
import dev.robocode.tankroyale.gui.ui.arena.LogoPanel
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.control.ControlEventHandlers
import dev.robocode.tankroyale.gui.ui.control.ControlPanel
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import java.awt.BorderLayout
import javax.swing.JPanel

object MainFrame : RcFrame("main_frame") {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(1050, 800)

        contentPane.add(MainPanel)

        jMenuBar = Menu

        @Suppress("UNUSED_EXPRESSION")
        BattlePanel // make sure the battle panel is initialized before being used the first time

        ClientEvents.apply {
            onGameStarted+= On(MainFrame) { MainPanel.showArena() }
            onGameEnded+= On(MainFrame) { MainPanel.showLogo() }
            onGameAborted+= On(MainFrame) { MainPanel.showLogo() }
            onPlayerChanged+= On(MainFrame) { MainPanel.showArena() }
        }
        ServerEvents.onStopped+= On(MainFrame) { MainPanel.showLogo() }

        onClosing { close() }
        Runtime.getRuntime().addShutdownHook(Thread { close() })
    }

    private fun close() {
        Client.close()
        BootProcess.stop()
        RecorderProcess.stop()
        ServerProcess.stop()
    }

    private object MainPanel : JPanel() {
        init {
            @Suppress("UnusedExpression")
            ControlEventHandlers
            @Suppress("UnusedExpression")
            SoundActions
            @Suppress("UnusedExpression")
            AutoRecorder

            layout = BorderLayout()
            add(LogoPanel, BorderLayout.CENTER)
            add(ControlPanel, BorderLayout.SOUTH)

            ControlPanel.isVisible = false
        }

        fun showLogo() {
            remove(BattlePanel)
            add(LogoPanel, BorderLayout.CENTER)

            refresh()
        }

        fun showArena() {
            remove(LogoPanel)
            add(BattlePanel, BorderLayout.CENTER)

            ControlPanel.isVisible = true
            refresh()
        }

        private fun refresh() {
            validate()
            repaint()
        }
    }
}
