package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.PortInputField
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel
import javax.swing.*

object ServerConfigDialog : RcDialog(MainFrame, "server_config_dialog") {

    init {
        contentPane.add(ServerConfigPanel())
        pack()
        setLocationRelativeTo(MainFrame) // center on main window
    }
}

class ServerConfigPanel : JPanel() {
    init {
        setLayout(MigLayout("insets 10, fillx", "[grow]", "[]10[]10[]10[]"))

        // Selected server
        add(JLabel("Selected server:"), "split 2")
        add(JTextField(), "growx, wrap")

        // Local server group
        val localServerPanel = JPanel(MigLayout("insets 10, fillx", "[right][grow]", "[][]")).apply {
            setBorder(BorderFactory.createTitledBorder("Local server"))
            add(JLabel("Port:"))
            add(PortInputField(), "wrap")
            add(JCheckBox("Secure server (WSS)"), "span 2")
        }
        add(localServerPanel, "growx, wrap")

        // Remote server group
        val remoteServerPanel = JPanel(MigLayout("insets 10, fillx", "[right][grow][]", "[][]")).apply {
            setBorder(BorderFactory.createTitledBorder("Remote server"))
            add(JLabel("Server:"))
            add(JComboBox(arrayOf("ws://localhost:7656")), "growx")
            add(JButton("Test"), "wrap")
            add(JButton("Add"), "skip 1, split 2")
            add(JButton("Remove"))
        }
        add(remoteServerPanel, "growx, wrap")

        // OK and Cancel buttons
        val buttonPanel = JPanel(MigLayout("insets 0, center")).apply {
            add(JButton("OK"), "split 2")
            add(JButton("Cancel"))
        }
        add(buttonPanel, "growx")
    }
}


fun main() {
    ServerConfigDialog.isVisible = true
}