package dev.robocode.tankroyale.ui.desktop.ui.selection

import dev.robocode.tankroyale.ui.desktop.ui.extensions.JListExt.onSelection
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class SelectBotsWithBotInfoPanel : JPanel(MigLayout("fill")) {

    private val selectBotsPanel = SelectBotsPanel()
    private val botInfoPanel = BotInfoPanel()

    private val botsDirectoryList = selectBotsPanel.botsDirectoryList
    private val joinedBotList = selectBotsPanel.joinedBotList
    val selectedBotList = selectBotsPanel.selectedBotList

    val botsDirectoryListModel = selectBotsPanel.botsDirectoryListModel
    val joinedBotListModel = selectBotsPanel.joinedBotListModel
    val selectedBotListModel = selectBotsPanel.selectedBotListModel

    init {
        val groupPanel = JPanel(MigLayout("fill"))
        groupPanel.add(botInfoPanel, "grow")

        add(selectBotsPanel, "north")

        add(groupPanel, "south")

        botsDirectoryList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo) }
        selectedBotList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo) }
        joinedBotList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo) }
    }
}