package net.robocode2.gui.ui

import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.WindowExt.onClosing
import net.robocode2.gui.ui.battle.ArenaPanel
import net.robocode2.gui.ui.battle.BattleDialog
import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager

object MainWindow : JFrame(ResourceBundles.UI_TITLES.get("main_window")), AutoCloseable {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(800, 600)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(ArenaPanel())

        jMenuBar = MainWindowMenu

        MainWindowMenu.onNewBattle.invokeLater {
            val dialog = BattleDialog
            dialog.selectBotsTab()
            dialog.isVisible = true
        }

        MainWindowMenu.onSetupRules.invokeLater {
            val dialog = BattleDialog
            dialog.selectSetupRulesTab()
            dialog.isVisible = true
        }

        onClosing {
            close()
        }
    }

    override fun close() {
        Client.close()
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        MainWindow.isVisible = true
    }
}