package dev.robocode.tankroyale.ui.desktop.extensions

import javax.swing.InputVerifier
import javax.swing.JComponent
import javax.swing.JTextField

object JTextFieldExt {
    /**
     * Sets the input verifier.
     */
    fun JTextField.setInputVerifier(verifier: ((JComponent) -> Boolean)) {
        inputVerifier = object : InputVerifier() {
            override fun verify(input: JComponent): Boolean {
                return verifier.invoke(input)
            }
        }
    }
}