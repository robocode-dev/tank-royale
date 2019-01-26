package net.robocode2.gui

import net.miginfocom.swing.MigLayout
import net.robocode2.gui.extensions.JComponentExt.addNewButton
import net.robocode2.gui.utils.Observable
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SelectBots(frame: JFrame? = null) : JDialog(frame, ResourceBundles.WINDOW_TITLES.get("select_bots")) {

    // Private events
    private val onAdd = Observable()
    private val onAddAll = Observable()
    private val onRemove = Observable()
    private val onRemoveAll = Observable()

    private val availableBotListModel = DefaultListModel<String>()
    private val selectedBotListModel = DefaultListModel<String>()

    private val availableBotList = JList<String>(availableBotListModel)
    private val selectedBotList = JList<String>(selectedBotListModel)

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        setSize(400, 250)
        minimumSize = size
        setLocationRelativeTo(null) // center on screen

        contentPane = JPanel(MigLayout(
                "insets 10, fill",
                "[grow][][grow]"))

        val leftPanel = JPanel(MigLayout("fill"))
        leftPanel.add(JScrollPane(availableBotList), "grow")

        val rightPanel = JPanel(MigLayout("fill"))
        rightPanel.add(JScrollPane(selectedBotList), "grow")

        val centerPanel = JPanel(MigLayout("insets 0"))

        // Sets the preferred size to avoid right panel with to grow much larger than the right panel
        leftPanel.preferredSize = Dimension(10, 10)
        rightPanel.preferredSize = Dimension(10, 10)

        contentPane.add(leftPanel, "grow")
        contentPane.add(centerPanel, "")
        contentPane.add(rightPanel, "grow")

        leftPanel.border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("available_bots"))
        rightPanel.border = BorderFactory.createTitledBorder(ResourceBundles.STRINGS.get("selected_bots"))

        val addPanel = JPanel(MigLayout("insets 0, fill", "[fill]"))
        val removePanel = JPanel(MigLayout("insets 0, fill", "[fill]"))

        val middlePanel = JPanel(MigLayout("fill"))

        centerPanel.add(addPanel, "north")
        centerPanel.add(middlePanel, "h 300")
        centerPanel.add(removePanel, "south")

        addPanel.addNewButton("add_arrow", onAdd, "cell 0 1")
        addPanel.addNewButton("add_all_arrow", onAddAll, "cell 0 2")
        removePanel.addNewButton("arrow_remove", onRemove, "cell 0 3")
        removePanel.addNewButton("arrow_remove_all", onRemoveAll, "cell 0 4")

        for (i in 1..20) {
            availableBotListModel.addElement("avail: $i")
//            selectedBotListModel.addElement("selected: $i")
        }

        onAdd.subscribe {
            availableBotList.selectedValuesList.forEach {
                selectedBotListModel.addElement(it)
            }
        }
        onAddAll.subscribe {
            for (i in 0 until availableBotListModel.size) {
                selectedBotListModel.addElement(availableBotListModel[i])
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
        availableBotList.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val index = availableBotList.locationToIndex(e.point)
                    selectedBotListModel.addElement(availableBotListModel[index])
                }
            }
        })
        selectedBotList.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val index = selectedBotList.locationToIndex(e.point)
                    selectedBotListModel.removeElement(selectedBotListModel[index])
                }
            }
        })
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectBots().isVisible = true
    }
}