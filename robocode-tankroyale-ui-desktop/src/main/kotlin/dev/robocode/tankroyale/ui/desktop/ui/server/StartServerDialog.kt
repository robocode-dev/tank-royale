package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.UIManager

@ImplicitReflectionSerializer
object StartServerDialog : JDialog(MainWindow, getWindowTitle()) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(300, 100)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(StartServerPanel)
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("start_server_dialog")
}

@ImplicitReflectionSerializer
private object StartServerPanel : JPanel(MigLayout("fill")) {

    private val portTextField = JTextField(5)

    init {
        add(portTextField, "wrap")
    }
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        StartServerDialog.isVisible = true
    }
}