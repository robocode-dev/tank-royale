package dev.robocode.tankroyale.gui.ui.config

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ConfigSettings.SOUNDS_DIR
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addCheckBox
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.setDefaultButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.FileUtil
import net.miginfocom.swing.MigLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

object SoundConfigDialog : RcDialog(MainFrame, "sound_config_dialog") {

    init {
        contentPane.add(SoundConfigPanel)
        pack()
        setLocationRelativeTo(owner) // center on owner window
    }
}

object SoundConfigPanel : JPanel(MigLayout("fill")) {

    private val onOk = Event<JButton>().apply { subscribe(this) { SoundConfigDialog.dispose() } }

    private val onEnableSounds = Event<JCheckBox>()
        .apply { subscribe(this) { ConfigSettings.enableSounds = it.isSelected } }
    private val onEnableGunshot = Event<JCheckBox>()
        .apply { subscribe(this) { ConfigSettings.enableGunshotSound = it.isSelected } }
    private val onEnableBulletHit = Event<JCheckBox>()
        .apply { subscribe(this) { ConfigSettings.enableBulletHitSound = it.isSelected } }
    private val onEnableDeath = Event<JCheckBox>()
        .apply { subscribe(this) { ConfigSettings.enableDeathExplosionSound = it.isSelected } }
    private val onEnableWallCollision = Event<JCheckBox>()
        .apply { subscribe(this) { ConfigSettings.enableWallCollisionSound = it.isSelected } }
    private val onEnableBotCollision = Event<JCheckBox>()
        .apply { subscribe(this) { ConfigSettings.enableBotCollisionSound = it.isSelected } }
    private val onEnableBulletCollision = Event<JCheckBox>()
        .apply { subscribe(this) { ConfigSettings.enableBulletCollisionSound = it.isSelected } }

    init {
        val panel1 = JPanel(MigLayout("fill"))
        add(panel1, "wrap")
        panel1.apply {
            addCheckBox("option.sound.enable_sounds", onEnableSounds, "wrap").apply {
                isSelected = ConfigSettings.enableSounds
            }
        }

        val panel2 = JPanel(MigLayout("fill"))
        add(panel2, "wrap")
        panel2.apply {
            border = BorderFactory.createTitledBorder(Strings.get("option.sound.sounds_title"))
            addCheckBox("option.sound.enable_gunshot", onEnableGunshot, "wrap").apply {
                isSelected = ConfigSettings.enableGunshotSound
            }
            addCheckBox("option.sound.enable_bullet_hit", onEnableBulletHit, "wrap").apply {
                isSelected = ConfigSettings.enableBulletHitSound
            }
            addCheckBox("option.sound.enable_death", onEnableDeath, "wrap").apply {
                isSelected = ConfigSettings.enableDeathExplosionSound
            }
            addCheckBox("option.sound.enable_wall_collision", onEnableWallCollision, "wrap").apply {
                isSelected = ConfigSettings.enableWallCollisionSound
            }
            addCheckBox("option.sound.enable_bot_collision", onEnableBotCollision, "wrap").apply {
                isSelected = ConfigSettings.enableBotCollisionSound
            }
            addCheckBox("option.sound.enable_bullet_collision", onEnableBulletCollision, "wrap").apply {
                isSelected = ConfigSettings.enableBulletCollisionSound
            }
        }

        add(JPanel(MigLayout("fill")), "wrap").apply {
            addOkButton(onOk, "center").apply {
                setDefaultButton(this)
            }
        }

        showErrorIfSoundDirIsMissingOrEmpty()
    }

    private fun showErrorIfSoundDirIsMissingOrEmpty() {
        if (FileUtil.isMissingOrEmptyDir(SOUNDS_DIR)) {
            showError(String.format(Messages.get("sounds_dir_missing"), SOUNDS_DIR))
        }
    }
}
