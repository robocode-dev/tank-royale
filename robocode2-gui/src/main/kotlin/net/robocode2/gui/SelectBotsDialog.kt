package net.robocode2.gui

import net.miginfocom.swing.MigLayout
import java.awt.EventQueue
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.UIManager


class SelectBots(frame: JFrame? = null) : JDialog(frame, ResourceBundles.WINDOW_TITLES.get("select_bots")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        setSize(400, 250)
        minimumSize = size
        setLocationRelativeTo(null) // center on screen

        contentPane = JPanel(MigLayout("insets 10, fill"))
    }

    private fun close() {
        isVisible = false
        dispose()

//        onClose.onNext(Unit)
    }

}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectBots().isVisible = true
    }
}