package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.server.AddNewUrlDialog.onComplete
import dev.robocode.tankroyale.ui.desktop.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object AddNewUrlDialog : JDialog(SelectServerDialog, ResourceBundles.UI_TITLES.get("add_new_url_dialog")) {

    val onComplete = Event<JButton>()

    var newUrl: String = ""

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(300, 100)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(AddNewUrlPanel)

        onActivated {
            AddNewUrlPanel.urlTextField.text = ""
            AddNewUrlPanel.urlTextField.background = AddNewUrlPanel.urlTextFieldDefaultBackground
        }

        onClosing {
            Client.close()
        }
    }
}

private object AddNewUrlPanel : JPanel(MigLayout("fill")) {

    // Private events
    val urlTextField = JTextField(50)

    val urlTextFieldDefaultBackground: Color = urlTextField.background

    init {
        add(urlTextField)
        val okButton = addButton("ok", onComplete)
        AddNewUrlDialog.rootPane.defaultButton = okButton

        onComplete.subscribe {
            if (isValidWsUrl()) {
                AddNewUrlDialog.newUrl = urlTextField.text
                AddNewUrlDialog.dispose()
            } else {
                AddNewUrlDialog.newUrl = ""
            }
        }

        urlTextField.document.addDocumentListener(object : DocumentListener {

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
                val valid= isValidWsUrl()
                urlTextField.background = if (valid) lightGreen else lightRed
                okButton.isEnabled = valid
            }
        })
    }

    private fun isValidWsUrl(): Boolean {
        val url = urlTextField.text.trim()
        return !url.isBlank() &&
                url.matches(Regex("^(ws://)?(\\p{L})?(\\p{L}|\\.|[-])*(\\p{L})(:\\d{1,5})?$"))
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        AddNewUrlDialog.isVisible = true
    }
}