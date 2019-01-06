package net.robocode2.gui

import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager

class Robocode2Gui : JFrame() {

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        createUi()
    }

    private fun createUi() {
        defaultCloseOperation = EXIT_ON_CLOSE

        title = "Robocode 2"
        setSize(1000, 850)
        setLocationRelativeTo(null) // center on screen

        val menu = MenuBar()
        jMenuBar = menu

        menu.newBattleEvent.subscribe { System.out.println("Hejsa") }
    }
}

fun main() {
    EventQueue.invokeLater {
        Robocode2Gui().isVisible = true
    }
}