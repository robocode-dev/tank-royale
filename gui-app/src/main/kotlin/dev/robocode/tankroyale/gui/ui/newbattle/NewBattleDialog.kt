package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.GuiTask.enqueue
import net.miginfocom.swing.MigLayout
import java.awt.event.ItemEvent
import javax.swing.*

object NewBattleDialog : RcDialog(MainWindow, "select_bots_dialog") {

    private val selectBotsAndStartPanel = NewBattlePanel()

    init {
        contentPane.add(selectBotsAndStartPanel)
        pack()
        setLocationRelativeTo(MainWindow) // center on main window
    }
}

class NewBattlePanel : JPanel(MigLayout("fill", "[]", "[][grow][][]")) {

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

        add(topPanel, "wrap")
        add(BotSelectionPanel, "grow, wrap")
        add(BotInfoPanel, "grow, wrap")
        add(buttonPanel, "center")

        val startBattleButton: JButton

        buttonPanel.apply {
            startBattleButton = addButton("start_battle", onStartBattle)
            addButton("cancel", onCancel)
        }
        startBattleButton.isEnabled = false

        BotSelectionEvents.onSelectedBotListUpdated.subscribe(this) {
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
                    enqueue {
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
}