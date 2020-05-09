package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.bootstrap.BotEntry
import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.ui.extensions.JTableExt.onChanged
import dev.robocode.tankroyale.ui.desktop.ui.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.util.*
import javax.swing.*
import kotlin.collections.ArrayList


@UnstableDefault
@ImplicitReflectionSerializer
object NewBattleDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("new_battle_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(600, 600)

        setLocationRelativeTo(null) // center on screen

        val newBattleDialogPanel = NewBattleDialogPanel()

        contentPane.add(newBattleDialogPanel)

        onActivated {
            newBattleDialogPanel.apply {
                updateAvailableBots()
                clearSelectedBots()
            }
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
class NewBattleDialogPanel : JPanel(MigLayout("fill")) {
    // Private events
    private val onStartBattle = Event<JButton>()
    private val onCancel = Event<JButton>()

    private val selectPanel = SelectBotsWithBotInfoPanel2()

    private val botEntries: List<BotEntry> by lazy { BootstrapProcess.list() }

    private val selectedOfflineBotFiles: List<String>
        get() {
            val files = ArrayList<String>()
            selectPanel.selectedBotTable.rows()
                .filter { it.availability === BotAvailability.OFFLINE }
                .forEach {
                    files.add(it.botInfo.host)
                }
            return Collections.unmodifiableList(files)
        }


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
        selectPanel.selectedBotTable.onChanged {
            startBattleButton.isEnabled = selectPanel.selectedBotTable.rowCount >= 1
        }

        onStartBattle.subscribe { startGame() }

        onCancel.subscribe { SelectBotsForBattleDialog.dispose() }

        Client.onBotListUpdate.subscribe { updateAvailableBots() }
        updateAvailableBots()
    }

    fun clearSelectedBots() {
        selectPanel.selectedBotTable.clear()
    }

    fun updateAvailableBots() {
        SwingUtilities.invokeLater {
            val table = selectPanel.availableBotTable
            table.clear()

            Client.availableBots.forEach { table.add(it, BotAvailability.READY) }

            botEntries.forEach { botEntry ->
                val info = botEntry.info
                selectPanel.availableBotTable.add(
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
                    ),
                    BotAvailability.OFFLINE
                )
            }
        }
    }

    @ImplicitReflectionSerializer
    @UnstableDefault
    private fun startGame() {
//        isVisible = true // TODO: Necessary?

        if (selectedOfflineBotFiles.isNotEmpty()) {

        }
/*
        val gameType = ServerProcess.gameType
            ?: GameType.CLASSIC.type // FIXME: Dialog must be shown to select game type with remote server

        val botAddresses = selectPanel.selectedBotListModel.toArray()
            .map { b -> (b as BotInfo).botAddress }
        Client.startGame(GamesSettings.games[gameType]!!, botAddresses.toSet())
*/
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