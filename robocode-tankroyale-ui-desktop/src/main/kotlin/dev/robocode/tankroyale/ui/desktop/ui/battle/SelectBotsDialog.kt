package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
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

        contentPane.add(SelectBotsPanel2) // FIXME: Name of panel -> SelectBotsPanel

        onActivated {
            SelectBotsPanel2.updateAvailableBots()
            SelectBotsPanel2.selectedBotListModel.clear()
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
private object SelectBotsPanel2 : JPanel(MigLayout("fill")) {
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
            addButton("start_battle", onStartBattle, "tag ok")
            addButton("cancel", onCancel, "tag cancel")
        }

        availableBotList.cellRenderer = BotInfoCellRenderer()
        selectedBotList.cellRenderer = BotInfoCellRenderer()

        onCancel.subscribe { SelectBotsDialog.dispose() }

        onAdd.subscribe {
            availableBotList.selectedValuesList.forEach {
                selectedBotListModel.addElement(it)
            }
        }
        onAddAll.subscribe {
            for (i in 0 until availableBotListModel.size) {
                val botInfo = availableBotListModel[i]
                if (!selectedBotListModel.contains(botInfo)) {
                    selectedBotListModel.addElement(botInfo)
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
                    val botInfo = availableBotListModel[index]
                    if (!selectedBotListModel.contains(botInfo)) {
                        selectedBotListModel.addElement(botInfo)
                    }
                }
            }
        })
        selectedBotList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) {
                    val index = selectedBotList.locationToIndex(e.point)
                    selectedBotListModel.removeElement(selectedBotListModel[index])
                }
            }
        })

        onStartBattle.subscribe { startGame() }

        Client.onBotListUpdate.subscribe { updateAvailableBots() }

        updateAvailableBots()
    }

    fun updateAvailableBots() {
        availableBotListModel.clear()
        Client.availableBots.forEach { availableBotListModel.addElement(it) }
    }

    @UnstableDefault
    private fun startGame() {
        isVisible = true

        val botAddresses = selectedBotListModel.toArray().map { b -> (b as BotInfo).botAddress }
        Client.startGame(GamesSettings.games[ServerProcess.gameType]!!, botAddresses.toSet())

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

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectBotsDialog.isVisible = true
    }
}