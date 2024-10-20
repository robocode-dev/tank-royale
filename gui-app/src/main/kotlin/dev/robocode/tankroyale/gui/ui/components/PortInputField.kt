package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.util.MessageDialog
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class PortInputField(defaultValue: Short = 7654) : JTextField(5) {

    var port: Short = defaultValue
        private set

    private val events: MutableList<PortUpdatedEvent> = mutableListOf()

    init {
        text = defaultValue.toString()

        setCaretPosition(getText().length)

        addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) {
                if (!verify()) {
                    MessageDialog.showError(String.format(Messages.get("valid_port_number_range"), 1000, 65535))
                }
            }
        })

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
                setPort()
            }

            override fun removeUpdate(e: DocumentEvent) {
                setPort()
            }

            override fun changedUpdate(e: DocumentEvent) {
                setPort()
            }
        })
    }

    private fun verify(): Boolean {
        return try {
            val value = text.toInt()
            value in 1000..65535
        } catch (_: NumberFormatException) {
            false
        }
    }

    private fun firePortUpdateEvent() {
        events.forEach { it(port) }
    }

    private fun setPort() {
        if (verify()) {
            port = text.toShort()
            firePortUpdateEvent()
        }
    }

    fun addPortUpdatedHandler(event: PortUpdatedEvent) {
        events.add(event)
    }
}

typealias PortUpdatedEvent = (Short) -> Unit