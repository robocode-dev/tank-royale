package net.robocode2.gui.extensions

import net.robocode2.gui.utils.Observable
import net.robocode2.gui.ResourceBundles
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel

object JComponentExt {

    fun JComponent.addNewLabel(stringResourceName: String): JLabel {
        val label = JLabel(ResourceBundles.STRINGS.get(stringResourceName))
        add(label)
        return label
    }

    fun JComponent.addNewButton(
            stringResourceName: String, observable: Observable,
            layoutConstraints: String? = null): JButton {
        val button = JButton(ResourceBundles.STRINGS.get(stringResourceName))
        button.addActionListener { observable.notifyChange() }
        add(button, layoutConstraints)
        return button
    }
}
