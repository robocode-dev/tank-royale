package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.ui.config.AddNewUrlDialog.onOk
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.WsUrl
import net.miginfocom.swing.MigLayout
import java.awt.Color
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object AddNewUrlDialog : RcDialog(SelectServerDialog, "add_new_url_dialog") {

    val onOk = Event<JButton>()

    var newUrl: String = ""

    init {
        contentPane.add(AddNewUrlPanel)
        pack()
        setLocationRelativeTo(MainFrame) // center on main window

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
        val okButton = addOkButton(onOk)

        AddNewUrlDialog.apply {
            setDefaultButton(okButton)

            onOk.subscribe(AddNewUrlPanel) {
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
