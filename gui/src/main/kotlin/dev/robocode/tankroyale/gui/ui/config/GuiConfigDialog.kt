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

    private data class LanguageOption(val code: String, val label: String) {
        override fun toString(): String = label
    }

    private val onOk = Event<JButton>().apply { subscribe(this) { onOkClicked() } }

    private val scaleOptions = arrayOf(100, 125, 150, 175, 200, 250, 300)
    private val scaleCombo = JComboBox(scaleOptions.map { "$it%" }.toTypedArray())

    private val languageOptions = arrayOf(
        LanguageOption("en", Strings.get("language.english")),
        LanguageOption("es", Strings.get("language.spanish")),
        LanguageOption("ca", Strings.get("language.catalan")),
        LanguageOption("da", Strings.get("language.danish")),
    )
    private val languageCombo = JComboBox(languageOptions)

    init {
        addLanguageSelector()
        addUiScaleSelector()
        setInitialSelections()
        addOkButton(onOk, "span 2, alignx center, gaptop para, wrap").apply {
            setDefaultButton(this)
        }
    }

    private fun addLanguageSelector() {
        addLabel("option.gui.language")
        add(languageCombo, "wrap")
    }

    private fun addUiScaleSelector() {
        addLabel("option.gui.ui_scale")
        add(scaleCombo, "wrap")
    }

    private fun setInitialSelections() {
        // Initialize UI scale
        val currentScale = ConfigSettings.uiScale
        val idxScale = scaleOptions.indexOfFirst { it == currentScale }.let { if (it >= 0) it else 0 }
        scaleCombo.selectedIndex = idxScale

        // Initialize language
        val currentLang = ConfigSettings.language
        val idxLang = languageOptions.indexOfFirst { it.code == currentLang }.let { if (it >= 0) it else 0 }
        languageCombo.selectedIndex = idxLang
    }

    private fun onOkClicked() {
        var restartMsgShown = false

        // Save scale
        val percent = selectedScalePercent()
        val previousScale = ConfigSettings.uiScale
        if (percent != previousScale) {
            ConfigSettings.uiScale = percent
            showMessage(Strings.get("restart_required_to_apply_ui_scale"))
            restartMsgShown = true
        }

        // Save language
        val newLang = selectedLanguageCode()
        val prevLang = ConfigSettings.language
        if (newLang != prevLang) {
            ConfigSettings.language = newLang
            if (!restartMsgShown) {
                showMessage(Strings.get("restart_required_to_apply_language"))
            }
        }
        GuiConfigDialog.dispose()
    }

    private fun selectedScalePercent(): Int {
        val selected = scaleCombo.selectedItem as? String ?: return 100
        return selected.removeSuffix("%" ).toIntOrNull() ?: 100
    }

    private fun selectedLanguageCode(): String {
        val selected = languageCombo.selectedItem as? LanguageOption ?: return "en"
        return selected.code
    }
}
