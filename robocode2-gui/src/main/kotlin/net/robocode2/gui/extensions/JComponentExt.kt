package net.robocode2.gui.extensions

import net.robocode2.gui.ResourceBundles
import net.robocode2.gui.utils.Observable
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
            stringResourceName: String, observable: Observable<JButton>, layoutConstraints: String? = null): JButton {
        val button = JButton(ResourceBundles.STRINGS.get(stringResourceName))
        button.addActionListener { observable.notifyChange(button) }
        add(button, layoutConstraints)
        return button
    }
}
