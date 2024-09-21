package dev.robocode.tankroyale.gui.ui.extensions

import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.MessageDialog
import java.awt.Container
import java.awt.EventQueue
import javax.swing.*


object JComponentExt {

    fun JComponent.addLabel(stringResourceName: String, layoutConstraints: String? = null): JLabel {
        val label = object : JLabel(Strings.get(stringResourceName) + ':') {
            override fun createToolTip() = RcToolTip()
        }
        add(label, layoutConstraints)
        return label
    }

    fun JComponent.addButton(
        stringResourceName: String,
        event: Event<JButton>,
        layoutConstraints: String? = null
    ): JButton {
        val button = object : JButton(Strings.get(stringResourceName)) {
            override fun createToolTip() = RcToolTip()
        }
        button.addActionListener { event.fire(button) }
        add(button, layoutConstraints)
        return button
    }

    fun JComponent.addOkButton(event: Event<JButton>, layoutConstraints: String? = null): JButton {
        return addButton("ok", event, layoutConstraints)
    }

    fun JComponent.addCancelButton(event: Event<JButton>, layoutConstraints: String? = null): JButton {
        return addButton("cancel", event, layoutConstraints)
    }

    fun JComponent.addCheckBox(
        stringResourceName: String, event: Event<JCheckBox>, layoutConstraints: String? = null
    ): JCheckBox {
        val checkbox = object: JCheckBox(Strings.get(stringResourceName)) {
            override fun createToolTip() = RcToolTip()
        }
        checkbox.addActionListener { event.fire(checkbox) }
        add(checkbox, layoutConstraints)
        return checkbox
    }

    fun JComponent.setDefaultButton(button: JButton) {
        EventQueue.invokeLater { // to avoid rootPane to be null, if called too early
            if (rootPane != null) {
                rootPane.defaultButton = button
            }
            button.requestFocus() // set the focus on the button
        }
    }

    fun JComponent.showMessage(msg: String) {
        MessageDialog.showMessage(msg, this)
    }

    fun JComponent.showError(msg: String) {
        MessageDialog.showError(msg, this)
    }

    fun JComponent.enableAll(enable: Boolean = true) {
        enableAll(this, enable)
    }

    private fun enableAll(container: Container, enable: Boolean) {
        val components = container.components
        for (component in components) {
            component.isEnabled = enable
            if (component is Container) {
                enableAll(component, enable)
            }
        }
    }
}