package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.settings.GamesSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.Strings
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
    private val onSetupRules = Event<JButton>()

    private val startBattleButton: JButton

    private var selectedBots = emptyList<BotInfo>()
    private var gameTypeComboBox = GameTypeComboBox()

    init {
        val topPanel = JPanel(MigLayout("left, insets 5")).apply {
            border = BorderFactory.createTitledBorder(Strings.get("select_game_type"))

            val hint = Hints.get("new_battle.game_type")
            addLabel("game_type").apply {
                toolTipText = hint
            }
            gameTypeComboBox.toolTipText = hint
            add(gameTypeComboBox)

            addButton("setup_rules", onSetupRules).apply {
                toolTipText = Hints.get("new_battle.setup_rules")
            }
        }

        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        add(topPanel, "wrap")
        add(BotSelectionPanel, "grow, wrap")
        add(BotInfoPanel, "grow, wrap")
        add(buttonPanel, "center")

        buttonPanel.apply {
            startBattleButton = addButton("start_battle", onStartBattle)
            addButton("cancel", onCancel)
        }
        startBattleButton.isEnabled = false
        updateStartButtonHint()

        BotSelectionEvents.onSelectedBotListUpdated.subscribe(this) {
            selectedBots = it

            val maxParticipants = maxNumberOfParticipants()

            startBattleButton.isEnabled = selectedBots.size >= minNumberOfParticipants() &&
                    (maxParticipants == null || selectedBots.size <= maxParticipants)
        }

        onStartBattle.subscribe(this) { startGame() }
        onCancel.subscribe(this) { NewBattleDialog.dispose() }
        onSetupRules.subscribe(this) { SetupRulesDialog.isVisible = true }

        gameTypeComboBox.apply {
            addActionListener {
                ServerSettings.apply {
                    gameType = gameTypeComboBox.getSelectedGameType()
                    save()
                }
                updateStartButtonHint()
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

    private fun minNumberOfParticipants(): Int =
        GamesSettings.games[ServerSettings.gameType.displayName]?.minNumberOfParticipants ?: 2

    private fun maxNumberOfParticipants(): Int? =
        GamesSettings.games[ServerSettings.gameType.displayName]?.maxNumberOfParticipants

    private fun updateStartButtonHint() {
        startBattleButton.toolTipText = Hints.get("new_battle.start_button")
            .format(minNumberOfParticipants(), maxNumberOfParticipants()?.toString() ?: Strings.get("unlimited"))
    }

    private fun startGame() {
        isVisible = true

        val botAddresses = selectedBots.map { it.botAddress }
        Client.startGame(botAddresses.toSet())

        NewBattleDialog.dispose()
    }
}