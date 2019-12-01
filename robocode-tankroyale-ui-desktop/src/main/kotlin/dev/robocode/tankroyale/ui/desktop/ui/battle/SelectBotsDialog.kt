package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.extensions.JListExt.onContentsChanged
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.GameType
import dev.robocode.tankroyale.ui.desktop.settings.GamesSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder


@UnstableDefault
@ImplicitReflectionSerializer
object SelectBotsDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(600, 450)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(SelectBotsPanel)

        onActivated {
            SelectBotsPanel.updateAvailableBots()
            SelectBotsPanel.selectedBotListModel.clear()
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
private object SelectBotsPanel : JPanel(MigLayout("fill")) {
    // Private events
    private val onStartBattle = Event<JButton>()

    private val onCancel = Event<JButton>()
    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    private val availableBotListModel = DefaultListModel<BotInfo>()
    val selectedBotListModel = DefaultListModel<BotInfo>()
    private val availableBotList = JList<BotInfo>(availableBotListModel)
    private val selectedBotList = JList<BotInfo>(selectedBotListModel)

    private val startBattleButton: JButton


    init {
        val leftSelectionPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(availableBotList), "grow")
            // Sets the preferred size to avoid right panel width to grow much larger than the right panel
            preferredSize = Dimension(10, 10)
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("available_bots"))
        }

        val rightSelectionPanel = JPanel(MigLayout("fill")).apply {
            add(JScrollPane(selectedBotList), "grow")
            preferredSize = Dimension(10, 10)
            border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("selected_bots"))
        }

        val addPanel = JPanel(MigLayout("insets 0, fill", "[fill]"))
        val removePanel = JPanel(MigLayout("insets 0, fill", "[fill]"))

        val middlePanel = JPanel(MigLayout("fill"))

        val centerSelectionPanel = JPanel(MigLayout("insets 0")).apply {
            add(addPanel, "north")
            add(middlePanel, "h 300")
            add(removePanel, "south")
        }
        val selectionPanel = JPanel(MigLayout("", "[grow][][grow]")).apply {
            add(leftSelectionPanel, "grow")
            add(centerSelectionPanel, "")
            add(rightSelectionPanel, "grow")
        }
        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        val lowerPanel = JPanel(MigLayout("insets 10, fill")).apply {
            add(selectionPanel, "north")
            add(buttonPanel, "center")
        }

        add(lowerPanel, "south, h 1000000")

        addPanel.apply {
            addButton("add_arrow", onAdd, "cell 0 1")
            addButton("add_all_arrow", onAddAll, "cell 0 2")
        }
        removePanel.apply {
            addButton("arrow_remove", onRemove, "cell 0 3")
            addButton("arrow_remove_all", onRemoveAll, "cell 0 4")
        }
        buttonPanel.apply {
            startBattleButton = addButton("start_battle", onStartBattle, "tag ok")
            addButton("cancel", onCancel, "tag cancel")
        }

        availableBotList.cellRenderer = BotInfoCellRenderer()
        selectedBotList.cellRenderer = BotInfoCellRenderer()

        onCancel.subscribe { SelectBotsDialog.dispose() }

        onAdd.subscribe {
            availableBotList.selectedValuesList.forEach { bot ->
                if (selectedBotList.selectedValuesList.count { sel -> bot.host == sel.host && bot.port == sel.port } == 0) {
                    selectedBotListModel.addElement(bot)
                }
            }
        }
        onAddAll.subscribe {
            for (i in 0 until availableBotListModel.size) {
                val bot = availableBotListModel[i]
                if (!selectedBotListModel.contains(bot)) {
                    selectedBotListModel.addElement(bot)
                }
            }
        }
        onRemove.subscribe {
            selectedBotList.selectedValuesList.forEach {
                selectedBotListModel.removeElement(it)
            }
        }
        onRemoveAll.subscribe {
            selectedBotListModel.clear()
        }
        availableBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = availableBotList.locationToIndex(e.point)
                    if (index >= 0 && index < availableBotListModel.size()) {
                        val botInfo = availableBotListModel[index]
                        if (!selectedBotListModel.contains(botInfo)) {
                            selectedBotListModel.addElement(botInfo)
                        }
                    }
                }
            }
        })
        selectedBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = selectedBotList.locationToIndex(e.point)
                    if (index >= 0 && index < selectedBotListModel.size()) {
                        selectedBotListModel.removeElement(selectedBotListModel[index])
                    }
                }
            }
        })

        selectedBotList.onContentsChanged { startBattleButton.isEnabled = selectedBotList.model.size > 0 }

        onStartBattle.subscribe { startGame() }

        Client.onBotListUpdate.subscribe { updateAvailableBots() }

        updateAvailableBots()
    }

    fun updateAvailableBots() {
        SwingUtilities.invokeLater {
            availableBotListModel.clear()
            Client.availableBots.forEach { availableBotListModel.addElement(it) }
            startBattleButton.isEnabled = Client.availableBots.isNotEmpty()
        }
    }

    @UnstableDefault
    private fun startGame() {
        isVisible = true

        val gameType = ServerProcess.gameType
            ?: GameType.CLASSIC.type // FIXME: Dialog must be shown to select game with remote server

        val botAddresses = selectedBotListModel.toArray().map { b -> (b as BotInfo).botAddress }
        Client.startGame(GamesSettings.games[gameType]!!, botAddresses.toSet())

        SelectBotsDialog.dispose()
    }

    class BotInfoCellRenderer : JLabel(), ListCellRenderer<BotInfo> {

        init {
            isOpaque = true
        }

        override fun getListCellRendererComponent(
            list: JList<out BotInfo>, value: BotInfo, index: Int, isSelected: Boolean, cellHasFocus: Boolean
        ): Component {

            text = value.displayText
            border = EmptyBorder(1, 1, 1, 1)

            if (isSelected) {
                background = list.selectionBackground
                foreground = list.selectionForeground
            } else {
                background = list.background
                foreground = list.foreground
            }
            font = list.font

            return this
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectBotsDialog.isVisible = true
    }
}