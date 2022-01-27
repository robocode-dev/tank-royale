package dev.robocode.tankroyale.gui.ui.extensions

import javax.swing.InputVerifier
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

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

    fun JTextField.onChange(handler: () -> Unit) {
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                handler.invoke()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                handler.invoke()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                handler.invoke()
            }
        })
    }
}