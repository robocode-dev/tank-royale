package dev.robocode.tankroyale.gui.ui.selection

import dev.robocode.tankroyale.gui.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.gui.bootstrap.BotEntry
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.GamesSettings
import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*

object NewBattleDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(750, 600)

        setLocationRelativeTo(null) // center on screen

        val selectBotsAndStartPanel = SelectBotsAndStartPanel()

        contentPane.add(selectBotsAndStartPanel)

        onActivated {
            selectBotsAndStartPanel.apply {
                updateBotsDirectoryBots()
                updateJoinedBots()
                clearSelectedBots()
            }
        }
    }
}

class SelectBotsAndStartPanel : JPanel(MigLayout("fill")) {
    // Private events
    private val onStartBattle = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val selectPanel = SelectBotsWithBotInfoPanel()

    private val botsDirectoryEntries: List<BotEntry> by lazy { BootstrapProcess.list() }

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

    fun updateBotsDirectoryBots() {
        selectPanel.botsDirectoryListModel.clear()

        botsDirectoryEntries.forEach { botEntry ->
            val info = botEntry.info
            selectPanel.botsDirectoryListModel.addElement(
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

    private fun startGame() {
        isVisible = true

        val botAddresses = selectPanel.selectedBotListModel.toArray().map { b -> (b as BotInfo).botAddress }
        Client.startGame(GamesSettings.games[ServerProcess.gameType.displayName]!!, botAddresses.toSet())

        NewBattleDialog.dispose()
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        NewBattleDialog.isVisible = true
    }
}