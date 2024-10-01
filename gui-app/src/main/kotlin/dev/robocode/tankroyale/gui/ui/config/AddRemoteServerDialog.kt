package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import javax.swing.*
import net.miginfocom.swing.MigLayout

object AddRemoteServerDialog : RcDialog(SelectServerDialog, "add_new_url_dialog") {
    init {
        contentPane.add(AddRemoteServerPanel())
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }
}

class AddRemoteServerPanel : JPanel() {
    private val serverUrlField = JTextField(20)
    private val controllerSecretField = JTextField(20)
    private val botSecretField = JTextField(20)
    private val okButton = JButton(Strings.get("ok"))
    private val cancelButton = JButton(Strings.get("cancel"))

    init {
        layoutComponents()
        addListeners()
    }

    private fun layoutComponents() {
        layout = MigLayout("insets 10, fillx", "[right][grow]", "[]10[]10[]20[]")

        add(JLabel("Server URL:"))
        add(serverUrlField, "growx, wrap")

        add(JLabel("Controller Secret:"))
        add(controllerSecretField, "growx, wrap")

        add(JLabel("Bot Secret:"))
        add(botSecretField, "growx, wrap")

        val buttonPanel = JPanel(MigLayout("insets 0, center"))
        buttonPanel.add(okButton)
        buttonPanel.add(cancelButton)

        add(buttonPanel, "span 2, center")
    }

    private fun addListeners() {
        okButton.addActionListener {
            // Handle OK button click
            dispose()
        }

        cancelButton.addActionListener { dispose() }
    }

    private fun dispose() {
        AddRemoteServerDialog.dispose()
    }
}

fun main() {
    AddRemoteServerDialog.isVisible = true
}