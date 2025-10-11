package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.Strings
import net.miginfocom.swing.MigLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel

object GuiConfigDialog : RcDialog(MainFrame, "gui_config_dialog") {

    init {
        contentPane.add(GuiConfigPanel)
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }
}

object GuiConfigPanel : JPanel(MigLayout("fill, insets 10", "[][grow]", "")) {

    private val onOk = Event<JButton>().apply { subscribe(this) { onOkClicked() } }

    private val scaleOptions = arrayOf(100, 125, 150, 175, 200, 250, 300)

    private val scaleCombo = JComboBox(scaleOptions.map { "$it%" }.toTypedArray())

    init {
        addLabel("option.gui.ui_scale")
        add(scaleCombo, "wrap")

        // Initialize selection from settings
        val current = ConfigSettings.uiScale
        val idx = scaleOptions.indexOfFirst { it == current }.let { if (it >= 0) it else 0 }
        scaleCombo.selectedIndex = idx

        addOkButton(onOk, "span 2, alignx center, gaptop para, wrap").apply {
            setDefaultButton(this)
        }
    }

    private fun onOkClicked() {
        val selectedText = scaleCombo.selectedItem as String
        val percent = selectedText.removeSuffix("%").toIntOrNull() ?: 100
        val previous = ConfigSettings.uiScale
        if (percent != previous) {
            ConfigSettings.uiScale = percent
            showMessage(Strings.get("restart_required_to_apply_ui_scale"))
        }
        GuiConfigDialog.dispose()
    }
}
