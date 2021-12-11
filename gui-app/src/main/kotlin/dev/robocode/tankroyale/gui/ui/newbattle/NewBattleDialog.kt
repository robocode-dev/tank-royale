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
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*

object NewBattleDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    private val selectBotsAndStartPanel = NewBattlePanel()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(900, 600)

        setLocationRelativeTo(MainWindow) // center on main window

        contentPane.add(selectBotsAndStartPanel)
    }
}

class NewBattlePanel : JPanel(MigLayout("fill")) {

    private val onStartBattle = Event<JButton>()
    private val onCancel = Event<JButton>()

    private var selectedBots = emptyList<BotInfo>()

    init {
        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        val lowerPanel = JPanel(MigLayout("insets 10, fill")).apply {
            add(SelectBotsAndBotInfoPanel, "north")
            add(buttonPanel, "center")
        }
        add(lowerPanel, "south")

        val startBattleButton: JButton

        buttonPanel.apply {
            addLabel("game_type")
            add(GameTypeComboBox)
            add(JPanel())
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

        GameTypeComboBox.addActionListener {
            ServerSettings.apply {
                gameType = GameTypeComboBox.getSelectedGameType()
                save()
            }
        }
    }

    private fun startGame() {
        isVisible = true

        val botAddresses = selectedBots.map { it.botAddress }
        Client.startGame(botAddresses.toSet())

        NewBattleDialog.dispose()
    }

    private companion object SelectBotsAndBotInfoPanel : JPanel(MigLayout("fill")) {
        init {
            add(SelectBotsPanel, "center")

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
