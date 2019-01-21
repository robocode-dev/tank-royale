package net.robocode2.gui

import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager

object MainWindow : JFrame(ResourceBundles.WINDOW_TITLES.get("main")) {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(1000, 850)
        setLocationRelativeTo(null) // center on screen

        jMenuBar = MainWindowMenu

        MainWindowMenu.onSetupRules.subscribe {
            EventQueue.invokeLater {
                RulesWindow(this).isVisible = true
            }
        }
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        MainWindow.isVisible = true
    }
}