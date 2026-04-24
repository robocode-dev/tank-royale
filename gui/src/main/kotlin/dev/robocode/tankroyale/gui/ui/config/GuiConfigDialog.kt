package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.TankColorMode
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.arena.ArenaPanel
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showMessage
import dev.robocode.tankroyale.gui.ui.Strings
import net.miginfocom.swing.MigLayout
import java.util.Locale
import javax.swing.*

object GuiConfigDialog : RcDialog(MainFrame, "gui_config_dialog") {

    init {
        contentPane.add(GuiConfigPanel)
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }

    override fun setVisible(visible: Boolean) {
        if (visible) GuiConfigPanel.syncFromSettings()
        super.setVisible(visible)
    }
}

object GuiConfigPanel : JPanel(MigLayout("fill, insets 10", "[][grow]", "")) {

    private data class LanguageOption(val code: String, val label: String) {
        override fun toString(): String = label
    }

    private val onOk = Event<JButton>().apply { this.on(this@GuiConfigPanel) { onOkClicked() } }

    private val scaleOptions = arrayOf(100, 125, 150, 175, 200, 250, 300)
    private val scaleCombo = JComboBox(scaleOptions.map { "$it%" }.toTypedArray())

    private val languageOptions = arrayOf(
        LanguageOption("en", Strings.get("language.english", Locale.ENGLISH)),
        LanguageOption("es", Strings.get("language.spanish", Locale("es"))),
        LanguageOption("ca", Strings.get("language.catalan", Locale("ca"))),
        LanguageOption("da", Strings.get("language.danish", Locale("da"))),
    )
    private val languageCombo = JComboBox(languageOptions)

    private val maxCharsSpinner = JSpinner(SpinnerNumberModel(10000, 1000, 1000000, 1000))

    private val bootTimeoutSpinner = JSpinner(SpinnerNumberModel(30, 1, 600, 1)).apply {
        (editor as? JSpinner.DefaultEditor)?.textField?.columns = 4
    }

    private val botColorsRadio = JRadioButton(Strings.get("option.gui.tank_color_mode.bot_colors"))
    private val botColorsOnceRadio = JRadioButton(Strings.get("option.gui.tank_color_mode.bot_colors_once"))
    private val defaultColorsRadio = JRadioButton(Strings.get("option.gui.tank_color_mode.default_colors"))
    private val botColorsDebugRadio = JRadioButton(Strings.get("option.gui.tank_color_mode.bot_colors_when_debugging"))

    private val tankColorModeGroup = ButtonGroup().apply {
        add(botColorsRadio)
        add(botColorsOnceRadio)
        add(defaultColorsRadio)
        add(botColorsDebugRadio)
    }

    init {
        botColorsRadio.addActionListener { applyTankColorMode(TankColorMode.BOT_COLORS) }
        botColorsOnceRadio.addActionListener { applyTankColorMode(TankColorMode.BOT_COLORS_ONCE) }
        defaultColorsRadio.addActionListener { applyTankColorMode(TankColorMode.DEFAULT_COLORS) }
        botColorsDebugRadio.addActionListener { applyTankColorMode(TankColorMode.BOT_COLORS_WHEN_DEBUGGING) }

        addLanguageSelector()
        addUiScaleSelector()
        addConsoleMaxCharsSelector()
        addBootTimeoutSelector()
        addTankColorModeSelector()
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

    private fun addConsoleMaxCharsSelector() {
        addLabel("option.gui.console_max_characters")
        add(maxCharsSpinner, "wrap")
    }

    private fun addBootTimeoutSelector() {
        addLabel("option.gui.boot_timeout")
        add(bootTimeoutSpinner, "wrap")
    }

    private fun applyTankColorMode(mode: TankColorMode) {
        ConfigSettings.tankColorMode = mode
        ArenaPanel.repaint()
    }

    private fun addTankColorModeSelector() {
        val panel = JPanel(MigLayout("insets 6")).apply {
            border = BorderFactory.createTitledBorder(Strings.get("option.gui.tank_color_mode"))
            add(botColorsRadio, "wrap")
            add(botColorsOnceRadio, "wrap")
            add(defaultColorsRadio, "wrap")
            add(botColorsDebugRadio, "wrap")
        }
        add(panel, "span 2, growx, wrap")
    }

    internal fun syncFromSettings() {
        // Initialize UI scale
        val currentScale = ConfigSettings.uiScale
        val idxScale = scaleOptions.indexOfFirst { it == currentScale }.let { if (it >= 0) it else 0 }
        scaleCombo.selectedIndex = idxScale

        // Initialize language
        val currentLang = ConfigSettings.language
        val idxLang = languageOptions.indexOfFirst { it.code == currentLang }.let { if (it >= 0) it else 0 }
        languageCombo.selectedIndex = idxLang

        // Initialize console max characters
        maxCharsSpinner.value = ConfigSettings.consoleMaxCharacters

        // Initialize boot timeout
        bootTimeoutSpinner.value = ConfigSettings.bootTimeout

        // Initialize tank color mode
        when (ConfigSettings.tankColorMode) {
            TankColorMode.BOT_COLORS -> botColorsRadio.isSelected = true
            TankColorMode.BOT_COLORS_ONCE -> botColorsOnceRadio.isSelected = true
            TankColorMode.DEFAULT_COLORS -> defaultColorsRadio.isSelected = true
            TankColorMode.BOT_COLORS_WHEN_DEBUGGING -> botColorsDebugRadio.isSelected = true
        }
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

        // Save console max characters
        ConfigSettings.consoleMaxCharacters = maxCharsSpinner.value as Int

        // Save boot timeout
        ConfigSettings.bootTimeout = bootTimeoutSpinner.value as Int

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
