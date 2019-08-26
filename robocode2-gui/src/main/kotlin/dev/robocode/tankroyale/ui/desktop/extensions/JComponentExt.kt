package dev.robocode.tankroyale.ui.desktop.extensions

import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.utils.Event
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel

object JComponentExt {

    fun JComponent.addNewLabel(stringResourceName: String, layoutConstraints: String? = null): JLabel {
        val label = JLabel(ResourceBundles.STRINGS.get(stringResourceName) + ':')
        add(label, layoutConstraints)
        return label
    }

    fun JComponent.addNewButton(
        stringResourceName: String, event: Event<JButton>, layoutConstraints: String? = null
    ): JButton {
        val button = JButton(ResourceBundles.STRINGS.get(stringResourceName))
        button.addActionListener { event.publish(button) }
        add(button, layoutConstraints)
        return button
    }
}
