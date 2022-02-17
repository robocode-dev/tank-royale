package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.MainWindow
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.ui.server.AddNewUrlDialog.onComplete
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Color
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object AddNewUrlDialog : RcDialog(SelectServerDialog, "add_new_url_dialog") {

    val onComplete = Event<JButton>()

    var newUrl: String = ""

    init {
        contentPane.add(AddNewUrlPanel)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window

        onActivated {
            AddNewUrlPanel.apply {
                urlTextField.text = ""
                urlTextField.background = urlTextFieldDefaultBackground
            }
        }
    }
}

private object AddNewUrlPanel : JPanel(MigLayout("fill")) {

    // Private events
    val urlTextField = JTextField(30)

    val urlTextFieldDefaultBackground: Color = urlTextField.background

    init {
        add(urlTextField)
        val okButton = addButton("ok", onComplete)

        AddNewUrlDialog.apply {
            setDefaultButton(okButton)

            onComplete.subscribe(AddNewUrlDialog) {
                if (isValidWsUrl) {
                    newUrl = urlTextField.text
                    dispose()
                } else {
                    newUrl = ""
                }
            }

            onActivated {
                okButton.requestFocus()
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
                val valid = isValidWsUrl
                urlTextField.background = if (valid) lightGreen else lightRed
                okButton.isEnabled = valid
            }
        })
    }

    private val isValidWsUrl get() = WsUrl.isValidWsUrl(urlTextField.text)
}
