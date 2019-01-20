package net.robocode2.gui.extensions

import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.beans.PropertyChangeEvent
import javax.swing.InputVerifier
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document


object JTextFieldExt {

    /**
     * Adds a change listener.
     * This method wraps a document listener which are firing to a ChangeEvent to allow lambda expressions.
     * It can handle removing and adding documents to the JTextField, which is a special scenario.
     * Prevents a problem/bug where too many events are being fired.
     *
     * Link: https://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
     */
    fun JTextField.addChangeListener(l: ((ChangeEvent) -> Unit)) {
        val documentListener = object : DocumentListener {
            var lastChange = 0
            var lastNotifiedChange = 0

            override fun insertUpdate(e: DocumentEvent?) {
                changedUpdate(e)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                changedUpdate(e)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                lastChange++
                SwingUtilities.invokeLater {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange
                        l.invoke(ChangeEvent(this))
                    }
                }
            }
        }
        addPropertyChangeListener("document") { e: PropertyChangeEvent ->
            val d1 = e.oldValue as Document
            val d2 = e.newValue as Document
            d1.removeDocumentListener(documentListener)
            d2.addDocumentListener(documentListener)
            documentListener.changedUpdate(null)
        }
        document.addDocumentListener(documentListener)
    }

    /**
     * Adds a focus listener for handling lost focus events
     */
    fun JTextField.addFocusLostListener(l: ((FocusEvent) -> Unit)) {
        addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) {
                l.invoke(e)
            }
        })
    }

    /**
     * Sets the input verifier.
     */
    fun JTextField.setInputVerifier(l: ((JComponent) -> Boolean)) {
        inputVerifier = object : InputVerifier() {
            override fun verify(input: JComponent): Boolean {
                return l.invoke(input)
            }
        }
    }
}