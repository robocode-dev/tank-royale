package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.util.WsUrl
import javax.swing.*
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Window
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class EditRemoteServerDialog(serverUrl: String, owner: Window?) : RcDialog(owner, "edit_remote_server_dialog") {

    init {
        contentPane.add(EditRemoteServerPanel(this, serverUrl))
        pack()
        setLocationRelativeTo(owner)
    }
}

private class EditRemoteServerPanel(val dialog: EditRemoteServerDialog, var serverUrl: String) : JPanel(MigLayout("insets 10, fillx", "[right][grow]", "[]10[]10[]20[]")) {

    private val serverUrlField = JTextField(20)
    private val controllerSecretField = JTextField(20)
    private val botSecretField = JTextField(20)

    private val okButton = JButton(Strings.get("ok"))
    private val cancelButton = JButton(Strings.get("cancel"))

    private val backedUpServerData = ServerSettings.getRemoteServerData(serverUrl)

    init {
        setupComponents()
        layoutComponents()
        addListeners()
    }

    private fun setupComponents() {
        serverUrlField.apply {
            text = backedUpServerData.serverUrl
            setCaretPosition(text.length)
        }
        controllerSecretField.text = backedUpServerData.controllerSecret
        botSecretField.text = backedUpServerData.botSecret
    }


    private fun layoutComponents() {
        addServerUrlField()
        addControllerSecretField()
        addBotSecretField()
        addButtonPanel()
    }

    private fun addServerUrlField() {
        addLabel("option.server.remote_server_url")
        add(serverUrlField, "growx, wrap")
    }

    private fun addControllerSecretField() {
        addLabel("option.server.controller_secret")
        add(controllerSecretField, "growx, wrap")
    }

    private fun addBotSecretField() {
        addLabel("option.server.bot_secret")
        add(botSecretField, "growx, wrap")
    }

    private fun addButtonPanel() {
        val buttonPanel = JPanel(MigLayout("insets 0, center"))
        buttonPanel.add(okButton)
        buttonPanel.add(cancelButton)
        add(buttonPanel, "span 2, center")
    }

    private fun addListeners() {
        okButton.addActionListener {
            saveServerSettings()
            closeDialog()
        }
        cancelButton.addActionListener { closeDialog() }
        addServerUrlDocumentListener()
    }

    private fun saveServerSettings() {
        ServerSettings.updateRemoteServer(serverUrl, serverUrlField.text, controllerSecretField.text, botSecretField.text)

        serverUrl = serverUrlField.text // replace, as OK button might be hit again
    }

    private fun addServerUrlDocumentListener() {
        serverUrlField.document.addDocumentListener(object : DocumentListener {

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
                val valid = isValidServerUrl()
                serverUrlField.background = if (valid) lightGreen else lightRed
                okButton.isEnabled = valid
            }
        })
    }

    private fun closeDialog() {
        dialog.dispose()
    }

    private fun trimmedServerUrlText() = serverUrlField.text?.trim() ?: ""

    private fun isValidServerUrl() = WsUrl.isValidWsUrl(trimmedServerUrlText())
}
