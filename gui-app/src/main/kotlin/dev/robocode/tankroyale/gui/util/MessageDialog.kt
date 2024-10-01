package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.ui.UiTitles
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.JOptionPane.YES_OPTION

object MessageDialog {

    fun showMessage(msg: String, component: Component? = null) {
        JOptionPane.showMessageDialog(component, msg, UiTitles.get("message"), JOptionPane.INFORMATION_MESSAGE)
    }

    fun showError(msg: String, component: Component? = null) {
        JOptionPane.showMessageDialog(component, msg, UiTitles.get("error"), JOptionPane.ERROR_MESSAGE)
    }

    fun showConfirm(msg: String, component: Component? = null): Boolean =
        JOptionPane.showConfirmDialog(component, msg, UiTitles.get("confirm"), JOptionPane.YES_NO_CANCEL_OPTION) == YES_OPTION
}