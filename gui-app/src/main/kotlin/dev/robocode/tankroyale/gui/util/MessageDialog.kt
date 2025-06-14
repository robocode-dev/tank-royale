package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.UiTitles
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.JOptionPane.YES_OPTION

object MessageDialog {

    fun showMessage(msg: String, parentComponent: Component? = MainFrame) {
        JOptionPane.showMessageDialog(parentComponent, msg, UiTitles.get("message"), JOptionPane.INFORMATION_MESSAGE)
    }

    fun showError(msg: String, parentComponent: Component? = MainFrame) {
        JOptionPane.showMessageDialog(parentComponent, msg, UiTitles.get("error"), JOptionPane.ERROR_MESSAGE)
    }

    fun showConfirm(msg: String, parentComponent: Component? = MainFrame): Boolean =
        JOptionPane.showConfirmDialog(parentComponent, msg, UiTitles.get("confirm"), JOptionPane.YES_NO_CANCEL_OPTION) == YES_OPTION
}