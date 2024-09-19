package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.util.MessageDialog
import javax.swing.InputVerifier
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class PortInputField(defaultValue: Int = 7654) : JTextField(5) {

    init {
        text = defaultValue.toString()
        inputVerifier = object : InputVerifier() {
            override fun verify(input: JComponent): Boolean {
                val textField = input as JTextField
                return try {
                    val value = textField.text.toInt()
                    value in 1000..65535
                } catch (_: NumberFormatException) {
                    false
                }
            }
        }
        (document as AbstractDocument).documentFilter = object : DocumentFilter() {

            override fun insertString(fb: FilterBypass, offset: Int, string: String, attr: AttributeSet?) {
                if ((fb.document.length + string.length) <= 5 && string.all { it.isDigit() }) {
                    super.insertString(fb, offset, string, attr)
                }
            }

            override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
                if ((fb.document.length - length + text.length) <= 5 && text.all { it.isDigit() }) {
                    super.replace(fb, offset, length, text, attrs)
                }
            }

            override fun remove(fb: FilterBypass, offset: Int, length: Int) {
                super.remove(fb, offset, length)
            }
        }

        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                validatePort()
            }

            override fun removeUpdate(e: DocumentEvent) {
                validatePort()
            }

            override fun changedUpdate(e: DocumentEvent) {
                validatePort()
            }
        })
    }

    private fun validatePort() {
        if (!inputVerifier.verify(this)) {
            MessageDialog.showError(String.format(Messages.get("valid_port_number_range"), 1000, 65535))
        }
    }

    fun getPort(): Int? {
        return try {
            val value = text.toInt()
            if (value in 1000..65535) value else null
        } catch (_: NumberFormatException) {
            null
        }
    }
}