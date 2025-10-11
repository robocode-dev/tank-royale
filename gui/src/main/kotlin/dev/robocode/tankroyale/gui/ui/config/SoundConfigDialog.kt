package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ConfigSettings.SOUNDS_DIR
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.components.RcSlider
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addCheckBox
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.util.FileUtil
import net.miginfocom.swing.MigLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSlider

object SoundConfigDialog : RcDialog(MainFrame, "sound_config_dialog") {

    init {
        contentPane.add(SoundConfigPanel)
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }
}

object SoundConfigPanel : JPanel(MigLayout("fill")) {

    // Volume slider constants
    private const val VOLUME_MIN = 0
    private const val VOLUME_MAX = 100
    private const val VOLUME_MAJOR_TICK = 25
    private const val VOLUME_MINOR_TICK = 5

    private val onOk = Event<JButton>().apply {
        subscribe(this@SoundConfigPanel) { SoundConfigDialog.dispose() }
    }

    // Helper to wire a checkbox event to a settings setter
    private fun checkboxEvent(setter: (Boolean) -> Unit) = Event<JCheckBox>().apply {
        subscribe(this@SoundConfigPanel) { setter(it.isSelected) }
    }

    private val onEnableSounds = checkboxEvent { ConfigSettings.enableSounds = it }
    private val onEnableGunshot = checkboxEvent { ConfigSettings.enableGunshotSound = it }
    private val onEnableBulletHit = checkboxEvent { ConfigSettings.enableBulletHitSound = it }
    private val onEnableDeath = checkboxEvent { ConfigSettings.enableDeathExplosionSound = it }
    private val onEnableWallCollision = checkboxEvent { ConfigSettings.enableWallCollisionSound = it }
    private val onEnableBotCollision = checkboxEvent { ConfigSettings.enableBotCollisionSound = it }
    private val onEnableBulletCollision = checkboxEvent { ConfigSettings.enableBulletCollisionSound = it }

    init {
        add(createLeftColumn(), "growx, pushx")
        add(createVolumePanel(), "gapleft 16, top, growy, pushy")

        // Buttons spanning both columns
        addOkButton(onOk, "newline, span 2, alignx center, gaptop para, wrap").apply {
            setDefaultButton(this)
        }

        showErrorIfSoundDirIsMissingOrEmpty()
    }

    private fun createLeftColumn(): JPanel {
        val leftColumn = JPanel(MigLayout("fill, ins 0, wrap"))
        leftColumn.add(createMasterTogglePanel(), "growx")
        leftColumn.add(createSoundTogglesPanel(), "growx")
        return leftColumn
    }

    private fun createMasterTogglePanel(): JPanel {
        val panel = JPanel(MigLayout("fill"))
        panel.addCheckBox("option.sound.enable_sounds", onEnableSounds, "wrap").apply {
            isSelected = ConfigSettings.enableSounds
        }
        return panel
    }

    private fun createSoundTogglesPanel(): JPanel {
        val panel = JPanel(MigLayout("fill"))
        panel.border = BorderFactory.createTitledBorder(Strings.get("option.sound.sounds_title"))
        addSoundCheckbox(panel, "option.sound.enable_gunshot", ConfigSettings.enableGunshotSound, onEnableGunshot)
        addSoundCheckbox(panel, "option.sound.enable_bullet_hit", ConfigSettings.enableBulletHitSound, onEnableBulletHit)
        addSoundCheckbox(panel, "option.sound.enable_death", ConfigSettings.enableDeathExplosionSound, onEnableDeath)
        addSoundCheckbox(panel, "option.sound.enable_wall_collision", ConfigSettings.enableWallCollisionSound, onEnableWallCollision)
        addSoundCheckbox(panel, "option.sound.enable_bot_collision", ConfigSettings.enableBotCollisionSound, onEnableBotCollision)
        addSoundCheckbox(panel, "option.sound.enable_bullet_collision", ConfigSettings.enableBulletCollisionSound, onEnableBulletCollision)
        return panel
    }

    private fun addSoundCheckbox(panel: JPanel, key: String, selected: Boolean, event: Event<JCheckBox>) {
        panel.addCheckBox(key, event, "wrap").apply { isSelected = selected }
    }

    private fun createVolumePanel(): JPanel {
        val panel = JPanel(MigLayout("fill"))
        panel.border = BorderFactory.createTitledBorder(Strings.get("option.sound.volume"))

        val volumeSlider = RcSlider().apply {
            minimum = VOLUME_MIN
            maximum = VOLUME_MAX
            value = ConfigSettings.soundVolume
            orientation = JSlider.VERTICAL
            inverted = false
            paintTicks = true
            majorTickSpacing = VOLUME_MAJOR_TICK
            minorTickSpacing = VOLUME_MINOR_TICK
            toolTipText = Strings.get("option.sound.volume")
        }

        panel.add(volumeSlider, "center, growy, pushy")
        volumeSlider.addChangeListener {
            if (!volumeSlider.valueIsAdjusting) {
                ConfigSettings.soundVolume = volumeSlider.value
            }
        }
        return panel
    }

    private fun showErrorIfSoundDirIsMissingOrEmpty() {
        if (FileUtil.isMissingOrEmptyDir(SOUNDS_DIR)) {
            showError(String.format(Messages.get("sounds_dir_missing"), SOUNDS_DIR))
        }
    }
}
