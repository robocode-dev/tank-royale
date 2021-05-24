package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class NewBattlePanel : JPanel(MigLayout("fill")) {

    private val selectBotsPanel = SelectBotsPanel()
    private val botInfoPanel = BotInfoPanel()

    private val botsDirectoryList = selectBotsPanel.botsDirectoryList
    private val joinedBotList = selectBotsPanel.joinedBotList
    val selectedBotList = selectBotsPanel.selectedBotList

    val botsDirectoryListModel = selectBotsPanel.botsDirectoryListModel
    val joinedBotListModel = selectBotsPanel.joinedBotListModel
    val selectedBotListModel = selectBotsPanel.selectedBotListModel

    init {
        add(selectBotsPanel, "center")

        val groupPanel = JPanel(MigLayout("fill"))
        groupPanel.add(botInfoPanel, "grow")
        add(groupPanel, "south")

        botsDirectoryList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo as BotInfo) }
        selectedBotList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo as BotInfo) }
        joinedBotList.onSelection { botInfo -> botInfoPanel.updateBotInfo(botInfo as BotInfo) }
    }
}