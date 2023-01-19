package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

object DebugConfigDialog : RcDialog(MainFrame, "debug_config_dialog") {

    init {
        contentPane.add(Panel)
        pack()
        setLocationRelativeTo(MainFrame) // center on main window
    }
}

object Panel : JPanel(MigLayout("fill, insets 20", "", "[]20[]")) {

    private val onDismiss = Event<JButton>().apply { subscribe(this) { DebugConfigDialog.dispose() } }

    var selected = ServerSettings.initialPositionsEnabled

    init {
        val checkbox = JCheckBox(Strings.get("option.enable_initial_position.text"), selected).apply {
            toolTipText = Hints.get("option.enable_initial_position")
            addChangeListener {
                if (isSelected != selected) {
                    selected = isSelected
                    ServerSettings.initialPositionsEnabled = selected
                }
            }
        }

        add(checkbox, "cell 0 0")
        addButton("dismiss", onDismiss, "cell 0 1, center").apply {
            setDefaultButton(this)
        }
    }
}