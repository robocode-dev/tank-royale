package net.robocode2.gui.frames

import net.robocode2.gui.extensions.WindowExt.onClosing
import net.robocode2.gui.utils.Disposable
import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager

object MainWindow : JFrame(ResourceBundles.WINDOW_TITLES.get("main")) {

    var disposables = ArrayList<Disposable>()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(1000, 850)
        setLocationRelativeTo(null) // center on screen

        jMenuBar = MainWindowMenu

        disposables.add(MainWindowMenu.onNewBattle.invokeLater { SelectBots(this).isVisible = true })
        disposables.add(MainWindowMenu.onSetupRules.invokeLater { SetupRulesDialog(this).isVisible = true })

        onClosing { disposables.forEach { it.dispose() } }
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        MainWindow.isVisible = true
    }
}