package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.PortInputField
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.JPanel
import javax.swing.*
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addCancelButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.util.Event


object ServerConfigDialog : RcDialog(MainFrame, "server_config_dialog") {

    init {
        contentPane.add(ServerConfigPanel())
        pack()
        setLocationRelativeTo(MainFrame) // center on main window
    }
}

class ServerConfigPanel : JPanel() {

    val selectedServerLabel = JLabel(ServerSettings.currentServerUrl).apply {
        font = Font(font.family, Font.BOLD, font.size)
        foreground = Color(0x00, 0x7f, 0x00)
    }

    val remoteServerComboBox = JComboBox(arrayOf("ws://localhost:7656")).apply {
        preferredSize = Dimension(150, preferredSize.height)
    }

    val onOk = Event<JButton>()
    val onCancel = Event<JButton>()
    val onTest = Event<JButton>()
    val onAdd = Event<JButton>()
    val onRemove = Event<JButton>()

    init {
        setLayout(MigLayout("insets 10, fillx", "[grow]", "[]10[]10[]10[]"))

        // Selected server
        addLabel("selected_server", "split 2")
        add(selectedServerLabel, "growx, wrap")

        // Local server group
        val localServerPanel = JPanel(MigLayout("insets 10, fillx", "[right][grow]", "[][]")).apply {
            setBorder(BorderFactory.createTitledBorder(Strings.get("local_server")))
            addLabel("port")
            add(PortInputField(), "wrap")
//            add(JCheckBox("Secure server (WSS)"), "span 2")
        }
        add(localServerPanel, "growx, wrap")

        // Remote server group
        val remoteServerPanel = JPanel(MigLayout("insets 10, fillx", "[right][grow][]", "[][]")).apply {
            setBorder(BorderFactory.createTitledBorder(Strings.get("remote_server")))
            addLabel("server")
            add(remoteServerComboBox, "growx")

            addButton("test", onTest, "wrap")
            addButton("add", onAdd, "skip 1, split 2")
            addButton("remove", onRemove)
        }
        add(remoteServerPanel, "growx, wrap")

        // OK and Cancel buttons
        val buttonPanel = JPanel(MigLayout("insets 0, center")).apply {
            addOkButton(onOk, "split 2")
            addCancelButton(onCancel)
        }
        add(buttonPanel, "growx")
    }
}


fun main() {
    ServerConfigDialog.isVisible = true
}