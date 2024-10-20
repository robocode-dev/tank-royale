package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

object DebugConfigDialog : RcDialog(MainFrame, "debug_config_dialog") {

    init {
        contentPane.add(DebugConfigPanel())
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }
}

private class DebugConfigPanel : JPanel(MigLayout("fill, insets 20", "", "[]20[]")) {

    val onOkButton = Event<JButton>()

    init {
        addInitialPositionCheckbox()
        addOkButton()
    }

    private fun addInitialPositionCheckbox() {
        add(createInitialPositionCheckbox(), "cell 0 0")
    }

    @Suppress("UNCHECKED_CAST")
    private fun addOkButton() {
        onOkButton.subscribe(this) { closeDialog() }
        addOkButton(onOkButton, "cell 0 1, center").apply {
            setDefaultButton(this)
        }
    }

    private fun createInitialPositionCheckbox() =
        JCheckBox(getInitialPositionText(), ServerSettings.initialPositionsEnabled).apply {
            toolTipText = getInitialPositionHint()
            addChangeListener { updateInitialPositionSetting(isSelected) }
        }

    private fun getInitialPositionText() = Strings.get("option.enable_initial_position.text")

    private fun getInitialPositionHint() = Hints.get("option.enable_initial_position")

    private fun updateInitialPositionSetting(isEnabled: Boolean) {
        ServerSettings.initialPositionsEnabled = isEnabled
    }

    private fun closeDialog() {
        DebugConfigDialog.dispose()
    }
}