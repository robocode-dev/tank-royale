package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.extensions.JListExt.onSelection
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class SelectBotsWithBotInfoPanel(val onlySelectUnique: Boolean = false) : JPanel(MigLayout("fill")) {

    private val selectBotsPanel = SelectBotsPanel(onlySelectUnique)
    private val botInfoPanel = BotInfoPanel()

    val availableBotListModel = selectBotsPanel.availableBotListModel
    val selectedBotListModel = selectBotsPanel.selectedBotListModel
    private val availableBotList = selectBotsPanel.availableBotList
    val selectedBotList = selectBotsPanel.selectedBotList

    init {
        val groupPanel = JPanel(MigLayout("fill"))
        groupPanel.add(botInfoPanel, "grow")

        add(selectBotsPanel, "north")

        add(groupPanel, "south")

        selectedBotList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo) }
        availableBotList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo) }
    }
}