package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.GameType
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
                updateAvailableBots()
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

    private val selectPanel = SelectBotsWithBotInfoPanel(onlySelectUnique = true)

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

        onCancel.subscribe { SelectBotsForBattleDialog.dispose() }

        Client.onBotListUpdate.subscribe { updateAvailableBots() }
        updateAvailableBots()
    }

    fun clearSelectedBots() {
        selectPanel.selectedBotListModel.clear()
    }

    fun updateAvailableBots() {
        SwingUtilities.invokeLater {
            val availableBotListModel = selectPanel.joinedBotListModel
            availableBotListModel.clear()
            Client.availableBots.forEach { availableBotListModel.addElement(it) }
        }
    }

    @ImplicitReflectionSerializer
    @UnstableDefault
    private fun startGame() {
        isVisible = true

        val gameType = ServerProcess.gameType
            ?: GameType.CLASSIC.type // FIXME: Dialog must be shown to select game type with remote server

        val botAddresses = selectPanel.selectedBotListModel.toArray()
            .map { b -> (b as BotInfo).botAddress }
        Client.startGame(GamesSettings.games[gameType]!!, botAddresses.toSet())

        SelectBotsForBattleDialog.dispose()
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