package net.robocode2.gui.ui

import net.robocode2.gui.extensions.WindowExt.onClosing
import net.robocode2.gui.ui.battle.ArenaPanel
import net.robocode2.gui.ui.battle.BattleDialog
import net.robocode2.gui.utils.Disposable
import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager

object MainWindow : JFrame(ResourceBundles.UI_TITLES.get("main_window")) {

    var disposables = ArrayList<Disposable>()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(800, 600)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(ArenaPanel())

        jMenuBar = MainWindowMenu

        disposables.add(MainWindowMenu.onNewBattle.invokeLater {
            val dialog = BattleDialog
            dialog.selectBotsTab()
            dialog.isVisible = true
        })
        disposables.add(MainWindowMenu.onSetupRules.invokeLater {
            val dialog = BattleDialog
            dialog.selectSetupRulesTab()
            dialog.isVisible = true
        })

        onClosing { disposables.forEach { it.dispose() } }
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        MainWindow.isVisible = true
    }
}