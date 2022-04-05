package dev.robocode.tankroyale.gui.ui.extensions

import dev.robocode.tankroyale.gui.ui.ResourceBundles.STRINGS
import dev.robocode.tankroyale.gui.ui.ResourceBundles.UI_TITLES
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.GuiTask.enqueue
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
        button.addActionListener { event.fire(button) }
        add(button, layoutConstraints)
        return button
    }

    fun JComponent.setDefaultButton(button: JButton) {
        enqueue { // to avoid rootPane to be null, if called too early
            if (rootPane != null) {
                rootPane.defaultButton = button
            }
            button.requestFocus() // set the focus on the button
        }
    }

    fun JComponent.showMessage(msg: String) {
        JOptionPane.showMessageDialog(this, msg, UI_TITLES.get("message"), JOptionPane.INFORMATION_MESSAGE)
    }

    fun JComponent.showError(msg: String) {
        JOptionPane.showMessageDialog(this, msg, UI_TITLES.get("error"), JOptionPane.ERROR_MESSAGE)
    }
}