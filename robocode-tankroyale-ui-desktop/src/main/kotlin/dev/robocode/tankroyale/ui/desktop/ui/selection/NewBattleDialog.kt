package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.bootstrap.BotEntry
import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.GamesSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.ui.desktop.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*


@UnstableDefault
@ImplicitReflectionSerializer
object NewBattleDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(750, 600)

        setLocationRelativeTo(null) // center on screen

        val selectBotsAndStartPanel = SelectBotsAndStartPanel()

        contentPane.add(selectBotsAndStartPanel)

        onActivated {
            selectBotsAndStartPanel.apply {
                updateOfflineBots()
                updateJoinedBots()
                clearSelectedBots()
            }
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
class SelectBotsAndStartPanel : JPanel(MigLayout("fill")) {
    // Private events
    private val onStartBattle = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val selectPanel = SelectBotsWithBotInfoPanel()

    private val offlineBotEntries: List<BotEntry> by lazy { BootstrapProcess.list() }

    init {
        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        val lowerPanel = JPanel(MigLayout("insets 10, fill")).apply {
            add(selectPanel, "north")
            add(buttonPanel, "center")
        }
        add(lowerPanel, "south")

        val startBattleButton: JButton

        buttonPanel.apply {
            startBattleButton = addButton("start_battle", onStartBattle, "tag ok")
            addButton("cancel", onCancel, "tag cancel")
        }
        startBattleButton.isEnabled = false

        selectPanel.selectedBotList.onChanged {
            startBattleButton.isEnabled = selectPanel.selectedBotListModel.size >= 2
        }

        onStartBattle.subscribe { startGame() }

        onCancel.subscribe { NewBattleDialog.dispose() }

        Client.onBotListUpdate.subscribe { updateJoinedBots() }
        updateJoinedBots()
    }

    fun clearSelectedBots() {
        selectPanel.selectedBotListModel.clear()
    }

    fun updateOfflineBots() {
        selectPanel.offlineBotListModel.clear()

        offlineBotEntries.forEach { botEntry ->
            val info = botEntry.info
            selectPanel.offlineBotListModel.addElement(
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

    fun updateJoinedBots() {
        SwingUtilities.invokeLater {
            val joinedBotListModel = selectPanel.joinedBotListModel
            joinedBotListModel.clear()
            Client.joinedBots.forEach { joinedBotListModel.addElement(it) }
        }
    }

    @ImplicitReflectionSerializer
    @UnstableDefault
    private fun startGame() {
        isVisible = true

        val botAddresses = selectPanel.selectedBotListModel.toArray()
            .map { b -> (b as BotInfo).botAddress }
        Client.startGame(GamesSettings.games[ServerProcess.gameType.type]!!, botAddresses.toSet())

        NewBattleDialog.dispose()
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        NewBattleDialog.isVisible = true
    }
}