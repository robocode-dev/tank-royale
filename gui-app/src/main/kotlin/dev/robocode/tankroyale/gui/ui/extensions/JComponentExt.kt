package dev.robocode.tankroyale.gui.ui.extensions

import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.MessageDialog
import java.awt.Container
import java.awt.EventQueue
import javax.swing.*

object JComponentExt {

    fun createLabel(stringResourceName: String) =
        object : JLabel(Strings.get(stringResourceName)) {
            override fun createToolTip() = RcToolTip()
        }

    fun JComponent.addLabel(stringResourceName: String, layoutConstraints: String? = null): JLabel {
        val label = createLabel(stringResourceName)
        add(label, layoutConstraints)
        return label
    }

    fun createButton(stringResourceName: String, event: Event<JButton>): JButton =
        object : JButton(Strings.get(stringResourceName)) {
            override fun createToolTip() = RcToolTip()
        }.apply {
            addActionListener { event.fire(this) }
        }

    fun JComponent.addButton(
        stringResourceName: String,
        event: Event<JButton>,
        layoutConstraints: String? = null
    ): JButton {
        val button = createButton(stringResourceName, event)
        add(button, layoutConstraints)
        return button
    }

    fun JComponent.createOkButton(event: Event<JButton>) = createButton("ok", event)

    fun JComponent.addOkButton(event: Event<JButton>, layoutConstraints: String? = null): JButton {
        val okButton = createOkButton(event)
        add(okButton, layoutConstraints)
        return okButton
    }

    fun JComponent.createCancelButton(event: Event<JButton>) = createButton("cancel", event)

    fun JComponent.addCancelButton(event: Event<JButton>, layoutConstraints: String? = null): JButton {
        val cancelButton = createCancelButton(event)
        add(cancelButton, layoutConstraints)
        return cancelButton
    }

    fun JComponent.createAddButton(event: Event<JButton>) = createButton("add", event)

    fun JComponent.addAddButton(event: Event<JButton>, layoutConstraints: String? = null): JButton {
        val addButton = createAddButton(event)
        add(addButton, layoutConstraints)
        return addButton
    }

    fun JComponent.createRemoveButton(event: Event<JButton>) = createButton("remove", event)

    fun JComponent.addRemoveButton(event: Event<JButton>, layoutConstraints: String? = null): JButton {
        val removeButton = createRemoveButton(event)
        add(removeButton, layoutConstraints)
        return removeButton
    }

    fun JComponent.createEditButton(event: Event<JButton>) = createButton("edit", event)

    fun JComponent.addEditButton(event: Event<JButton>, layoutConstraints: String? = null): JButton {
        val editButton = createEditButton(event)
        add(editButton, layoutConstraints)
        return editButton
    }

    fun JComponent.createCheckBox(stringResourceName: String, event: Event<JCheckBox>) =
        object : JCheckBox(Strings.get(stringResourceName)) {
            override fun createToolTip() = RcToolTip()
        }.apply {
            addActionListener { event.fire(this) }
        }

    fun JComponent.addCheckBox(
        stringResourceName: String,
        event: Event<JCheckBox>,
        layoutConstraints: String?
    )
            : JCheckBox {
        val checkBox = createCheckBox(stringResourceName, event)
        add(checkBox, layoutConstraints)
        return checkBox
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
        container.components.forEach { component ->
            component.isEnabled = enable
            if (component is Container) {
                enableAll(component, enable)
            }
        }
    }
}