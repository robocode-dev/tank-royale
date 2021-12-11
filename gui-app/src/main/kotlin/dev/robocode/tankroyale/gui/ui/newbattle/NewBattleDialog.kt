package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.MainWindow
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.settings.MiscSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.config.BotDirectoryConfigDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.showError
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosed
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onOpened
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*

object NewBattleDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    private val selectBotsAndStartPanel = NewBattlePanel()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(750, 600)

        setLocationRelativeTo(MainWindow) // center on main window

        contentPane.add(selectBotsAndStartPanel)

        onActivated {
            selectBotsAndStartPanel.apply {
                updateJoinedBots()
                clearSelectedBots()
            }
        }

        onOpened {
            makeSureBotDirIsConfigured()
        }
    }

    private fun makeSureBotDirIsConfigured() {
        if (MiscSettings.getBotDirectories().isEmpty()) {
            selectBotsAndStartPanel.showError(ResourceBundles.MESSAGES.get("no_bot_dir"))

            BotDirectoryConfigDialog.onClosed {
                makeSureBotDirIsConfigured()
            }
            BotDirectoryConfigDialog.isVisible = true
        }
    }
}

class NewBattlePanel : JPanel(MigLayout("fill")) {
    // Private events
    private val onStartBattle = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val selectPanel = SelectBotsAndBotInfoPanel()
    private val gameTypeComboBox = GameTypeComboBox()

    init {
        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        val lowerPanel = JPanel(MigLayout("insets 10, fill")).apply {
            add(selectPanel, "north")
            add(buttonPanel, "center")
        }
        add(lowerPanel, "south")

        val startBattleButton: JButton

        buttonPanel.apply {
            addLabel("game_type")
            add(gameTypeComboBox)
            add(JPanel())
            startBattleButton = addButton("start_battle", onStartBattle)
            addButton("cancel", onCancel)
        }
        startBattleButton.isEnabled = false

        BotSelectionChannel.onBotsSelected.subscribe(this) {
            startBattleButton.isEnabled = it.size >= 2
        }

        onStartBattle.subscribe(NewBattleDialog) { startGame() }

        onCancel.subscribe(NewBattleDialog) { NewBattleDialog.dispose() }

        Client.onBotListUpdate.subscribe(NewBattleDialog) { updateJoinedBots() }
        updateJoinedBots()

        gameTypeComboBox.addActionListener {
            ServerSettings.apply {
                gameType = gameTypeComboBox.selectedGameType
                save()
            }
        }
    }

    fun clearSelectedBots() {
        selectPanel.selectedBotListModel.clear()
    }

    fun updateJoinedBots() {
        SwingUtilities.invokeLater {
            selectPanel.joinedBotListModel.apply {
                clear()
                Client.joinedBots.forEach { addElement(it) }
            }
        }
    }

    private fun startGame() {
        isVisible = true

        val botAddresses = selectPanel.selectedBotListModel.list().map { b -> b.botAddress }
        Client.startGame(botAddresses.toSet())

        NewBattleDialog.dispose()
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        NewBattleDialog.isVisible = true
    }
}