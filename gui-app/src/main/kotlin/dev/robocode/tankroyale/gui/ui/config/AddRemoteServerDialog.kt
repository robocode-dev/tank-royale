package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import javax.swing.*
import net.miginfocom.swing.MigLayout

object AddRemoteServerDialog : RcDialog(SelectServerDialog, "add_remote_server_dialog") {

    init {
        contentPane.add(RemoteServerPanel())
        pack()
        setLocationRelativeTo(owner)
    }
}

private class RemoteServerPanel : JPanel(MigLayout("insets 10, fillx", "[right][grow]", "[]10[]10[]20[]")) {

    private val serverUrlField = JTextField(20)
    private val controllerSecretField = JTextField(20)
    private val botSecretField = JTextField(20)

    private val okButton = JButton(Strings.get("ok"))
    private val cancelButton = JButton(Strings.get("cancel"))

    init {
        setupComponents()
        layoutComponents()
        addListeners()
    }

    private fun setupComponents() {
        serverUrlField.text = "ws://"
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
        okButton.addActionListener { closeDialog() }
        cancelButton.addActionListener { closeDialog() }
    }

    private fun closeDialog() {
        AddRemoteServerDialog.dispose()
    }
}