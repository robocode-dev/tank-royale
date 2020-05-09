package dev.robocode.tankroyale.ui.desktop.ui.extensions

import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.UI_TITLES
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.ui.desktop.util.Event
import javax.swing.*

object JComponentExt {

    fun JComponent.addLabel(stringResourceName: String, layoutConstraints: String? = null): JLabel {
        val label = JLabel(STRINGS.get(stringResourceName) + ':')
        add(label, layoutConstraints)
        return label
    }

    fun JComponent.addButton(
        stringResourceName: String, event: Event<JButton>, layoutConstraints: String? = null
    ): JButton {
        val button = JButton(STRINGS.get(stringResourceName))
        button.addActionListener { event.publish(button) }
        add(button, layoutConstraints)
        return button
    }

    fun JComponent.showMessage(msg: String) {
        JOptionPane.showMessageDialog(this, msg, UI_TITLES.get("message"), JOptionPane.INFORMATION_MESSAGE)
    }

    fun JComponent.showQuestion(msg: String) {
        JOptionPane.showMessageDialog(this, msg, UI_TITLES.get("question"), JOptionPane.QUESTION_MESSAGE)
    }

    fun JComponent.showWarning(msg: String) {
        JOptionPane.showMessageDialog(this, msg, UI_TITLES.get("warning"),JOptionPane.WARNING_MESSAGE)
    }

    fun JComponent.showError(msg: String) {
        JOptionPane.showMessageDialog(this, msg, UI_TITLES.get("error"),JOptionPane.ERROR_MESSAGE)
    }
}
