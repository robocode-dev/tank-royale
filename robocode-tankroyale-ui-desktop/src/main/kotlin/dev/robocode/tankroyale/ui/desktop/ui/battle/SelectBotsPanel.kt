package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.ui.desktop.extensions.JListExt.toList
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

@UnstableDefault
@ImplicitReflectionSerializer
object SelectBotsDialog2 : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    private val selectBotsPanel = SelectBotsPanel()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(600, 450)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(selectBotsPanel)
    }
}

class SelectBotsPanel : JPanel(MigLayout("fill")) {
    // Private events

    private val onAdd = Event<JButton>()
    private val onAddAll = Event<JButton>()
    private val onRemove = Event<JButton>()
    private val onRemoveAll = Event<JButton>()

    val availableBotListModel = DefaultListModel<BotInfo>()
    val selectedBotListModel = DefaultListModel<BotInfo>()
    val availableBotList = JList<BotInfo>(availableBotListModel)
    val selectedBotList = JList<BotInfo>(selectedBotListModel)

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
        add(selectionPanel, "north")

        addPanel.apply {
            addButton("add_arrow", onAdd, "cell 0 1")
            addButton("add_all_arrow", onAddAll, "cell 0 2")
        }
        removePanel.apply {
            addButton("arrow_remove", onRemove, "cell 0 3")
            addButton("arrow_remove_all", onRemoveAll, "cell 0 4")
        }

        availableBotList.cellRenderer = BotInfoListCellRenderer()
        selectedBotList.cellRenderer = BotInfoListCellRenderer()

        onAdd.subscribe {
            availableBotList.selectedValuesList.forEach { bot ->
                if (selectedBotList.toList().count { sel -> bot.host == sel.host && bot.port == sel.port } == 0) {
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