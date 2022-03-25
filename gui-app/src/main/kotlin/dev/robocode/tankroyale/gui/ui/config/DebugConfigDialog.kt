package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.config.DebugConfigDialog.onDismiss
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

object DebugConfigDialog : RcDialog(MainWindow, "debug_config_dialog") {

    val onDismiss = Event<JButton>().apply { subscribe(this) { dispose() } }

    init {
        contentPane.add(Panel)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window
    }
}

object Panel : JPanel(MigLayout("fill, insets 20", "", "[]20[]")) {

    var selected = ServerSettings.initialPositionsEnabled

    init {
        val checkbox = JCheckBox(ResourceBundles.STRINGS.get("option.enable_initial_position.text"), selected).apply {
            toolTipText = ResourceBundles.STRINGS.get("option.enable_initial_position.hint")
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