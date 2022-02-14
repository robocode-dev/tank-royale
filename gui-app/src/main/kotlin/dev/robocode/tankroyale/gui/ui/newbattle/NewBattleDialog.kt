package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.MainWindow
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.util.Event
import net.miginfocom.swing.MigLayout
import java.awt.EventQueue
import java.awt.event.ItemEvent
import javax.swing.*

object NewBattleDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    private val selectBotsAndStartPanel = NewBattlePanel()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

//        size = Dimension(1000, 650)

        contentPane.add(selectBotsAndStartPanel)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window

    }
}

class NewBattlePanel : JPanel(MigLayout("fill")) {

    private val onStartBattle = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val gameTypeComboBox = GameTypeComboBox()

    private var selectedBots = emptyList<BotInfo>()

    init {
        val topPanel = JPanel(MigLayout("left, insets 10")).apply {
            addLabel("game_type")
            add(gameTypeComboBox)
        }

        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        val lowerPanel = JPanel(MigLayout("insets 10, fill")).apply {
            add(SelectBotsAndBotInfoPanel, "north")
            add(buttonPanel, "center")
        }
        add(topPanel, "north")
        add(lowerPanel, "south")

        val startBattleButton: JButton

        buttonPanel.apply {
            startBattleButton = addButton("start_battle", onStartBattle)
            addButton("cancel", onCancel)
        }
        startBattleButton.isEnabled = false

        BotSelectionChannel.onSelectedBotListUpdated.subscribe(this) {
            selectedBots = it
            startBattleButton.isEnabled = selectedBots.size >= 2
        }

        onStartBattle.subscribe(NewBattleDialog) { startGame() }

        onCancel.subscribe(NewBattleDialog) { NewBattleDialog.dispose() }

        with(gameTypeComboBox) {
            addActionListener {
                ServerSettings.apply {
                    gameType = gameTypeComboBox.getSelectedGameType()
                    save()
                }
            }

            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    SwingUtilities.invokeLater {
                        BotSelectionPanel.update()
                    }
                }
            }
        }

        BotSelectionPanel.update()
    }

    private fun startGame() {
        isVisible = true

        val botAddresses = selectedBots.map { it.botAddress }
        Client.startGame(botAddresses.toSet())

        NewBattleDialog.dispose()
    }

    private companion object SelectBotsAndBotInfoPanel : JPanel(MigLayout("fill")) {
        init {
            add(BotSelectionPanel, "center")

            val groupPanel = JPanel(MigLayout("fill"))
            groupPanel.add(BotInfoPanel, "grow")
            add(groupPanel, "south")
        }
    }
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        NewBattleDialog.isVisible = true
    }
}
