package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addNewButton
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.server.NewEndpointDialog.onComplete
import dev.robocode.tankroyale.ui.desktop.ui.server.NewEndpointPanel.endpointTextField
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

@ImplicitReflectionSerializer
object NewEndpointDialog : JDialog(SelectServerDialog, getWindowTitle()) {

    val onComplete = Event<JButton>()

    var newEndpoint: String = ""

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(300, 100)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(NewEndpointPanel)

        onActivated {
            endpointTextField.text = ""
            endpointTextField.background = NewEndpointPanel.endpointTextFieldDefaultBackground
        }

        onClosing {
            Client.close()
        }
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("new_endpoint_dialog")
}

@ImplicitReflectionSerializer
private object NewEndpointPanel : JPanel(MigLayout("fill")) {

    // Private events
    val endpointTextField = JTextField(50)

    val endpointTextFieldDefaultBackground: Color = endpointTextField.background

    init {
        add(endpointTextField)
        val okButton = addNewButton("ok", NewEndpointDialog.onComplete)
        NewEndpointDialog.rootPane.defaultButton = okButton

        onComplete.subscribe {
            if (isValidEndpoint()) {
                NewEndpointDialog.newEndpoint = endpointTextField.text
                NewEndpointDialog.dispose()
            } else {
                NewEndpointDialog.newEndpoint = ""
            }
        }

        endpointTextField.document.addDocumentListener(object : DocumentListener {

            val lightRed = Color(0xFF, 0xAA, 0xAA)
            val lightGreen = Color(0xAA, 0xFF, 0xAA)

            override fun insertUpdate(e: DocumentEvent?) {
                validate()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                validate()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                validate()
            }

            fun validate() {
                val valid= isValidEndpoint()
                endpointTextField.background = if (valid) lightGreen else lightRed
                okButton.isEnabled = valid
            }
        })
    }

    private fun isValidEndpoint(): Boolean {
        val endpoint = endpointTextField.text.trim()
        return !endpoint.isBlank() &&
                endpoint.matches(Regex("^(ws://)?(\\p{L})?(\\p{L}|\\.|[-])*(\\p{L})(:\\d{1,5})?$"))
    }
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        NewEndpointDialog.isVisible = true
    }
}