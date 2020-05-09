package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.bootstrap.BotEntry
import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.model.BotListUpdate
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JListExt.toList
import dev.robocode.tankroyale.ui.desktop.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Font
import java.util.*
import javax.swing.*
import kotlin.collections.ArrayList

@UnstableDefault
@ImplicitReflectionSerializer
object SelectBotsForBootUpDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("boot_up_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(600, 620)

        setLocationRelativeTo(null) // center on screen

        val selectBotsAndStartPanel = SelectBotsForBootUpPanel

        contentPane.add(selectBotsAndStartPanel)

        onActivated {
            selectBotsAndStartPanel.apply {
                updateAvailableBots()
                clearSelectedBots()
            }
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
object SelectBotsForBootUpPanel : JPanel(MigLayout("fill")) {
    // Private events
    private val onOK = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val selectPanel = SelectBotsWithBotInfoPanel()

    private val botEntries: List<BotEntry> by lazy { BootstrapProcess.list() }

    private val selectedBotFiles: List<String>
        get() {
            val files = ArrayList<String>()
            selectPanel.selectedBotList.toList().forEach { botInfo ->
                files += botInfo.host // host serves as filename here
            }
            return Collections.unmodifiableList(files)
        }

    private var botsOnServerCount = 0;
    private val botsOnServerLabel = JLabel();

    init {
        // Set label to bold
        val font = botsOnServerLabel.font
        botsOnServerLabel.font = font.deriveFont(font.style or Font.BOLD)

        val upperPanel = JPanel(MigLayout("insets 10"))
        upperPanel.add(botsOnServerLabel)

        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        val lowerPanel = JPanel(MigLayout("insets 10, fill")).apply {
            add(selectPanel, "north")
            add(buttonPanel, "center")
        }
        add(upperPanel)
        add(lowerPanel, "south")

        val okButton: JButton

        buttonPanel.apply {
            okButton = addButton("boot_up", onOK, "tag ok")
            addButton("cancel", onCancel, "tag cancel")
        }
        okButton.isEnabled = false

        selectPanel.selectedBotList.onChanged {
            okButton.isEnabled = selectPanel.selectedBotListModel.size >= 0
        }

        onOK.subscribe {
            bootUpBots()
            SelectBotsForBootUpDialog.dispose()
        }

        onCancel.subscribe { SelectBotsForBootUpDialog.dispose() }
        updateAvailableBots()

        Client.onBotListUpdate.subscribe { updateBotsOnServer(it) }
        updateBotsOnServer()
    }

    fun clearSelectedBots() {
        (selectPanel.selectedBotList.model as DefaultListModel).clear()
    }

    private fun updateBotsOnServer(botListUpdate: BotListUpdate? = null) {
        botsOnServerCount = botListUpdate?.bots?.size ?: 0
        botsOnServerLabel.text = String.format(ResourceBundles.MESSAGES.get("x_bots_are_ready"), botsOnServerCount)
    }

    fun updateAvailableBots() {
        SwingUtilities.invokeLater {
            val availableBotListModel = selectPanel.joinedBotListModel

            availableBotListModel.clear()

            botEntries.forEach { botEntry ->
                val info = botEntry.info
                selectPanel.joinedBotListModel.addElement(
                    BotInfo(
                        info.name,
                        info.version,
                        info.author,
                        info.description,
                        info.url,
                        info.countryCode,
                        info.gameTypes,
                        info.platform,
                        info.programmingLang,
                        host = botEntry.filename, // host serves as filename here
                        port = -1
                    )
                )
            }
        }
    }

    private fun bootUpBots() {
        SelectBotsForBattleDialog.isVisible = true
        BootstrapProcess.run(selectedBotFiles)
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectBotsForBootUpDialog.isVisible = true
    }
}