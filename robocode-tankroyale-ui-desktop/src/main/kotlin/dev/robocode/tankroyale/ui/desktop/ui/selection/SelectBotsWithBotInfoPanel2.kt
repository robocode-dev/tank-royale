package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.extensions.JTableExt.onSelection
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class SelectBotsWithBotInfoPanel2 : JPanel(MigLayout("fill")) {

    private val selectBotsPanel = SelectBotsPanel2()
    private val botInfoPanel = BotInfoPanel()

    val availableBotTable = selectBotsPanel.availableBotTable
    val selectedBotTable = selectBotsPanel.selectedBotTable

    init {
        val groupPanel = JPanel(MigLayout("fill"))
        groupPanel.add(botInfoPanel, "grow")

        add(selectBotsPanel, "north")

        add(groupPanel, "south")

        availableBotTable.onSelection {
                row -> botInfoPanel.updateBotInfo(availableBotTable[row].botInfo)
        }
        selectedBotTable.onSelection {
                row -> botInfoPanel.updateBotInfo(selectedBotTable[row].botInfo)
        }
    }
}