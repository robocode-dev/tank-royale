package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

object SelectBotsAndBotInfoPanel : JPanel(MigLayout("fill")) {

    init {
        add(SelectBotsPanel, "center")

        val groupPanel = JPanel(MigLayout("fill"))
        groupPanel.add(BotInfoPanel, "grow")
        add(groupPanel, "south")

        SelectBotsPanel.botsDirectoryList.onSelection { updateBotInfo(it) }
        SelectBotsPanel.joinedBotList.onSelection { updateBotInfo(it) }

        with (SelectBotsPanel.selectedBotList) {
            onSelection { updateBotInfo(it) }

            onChanged {
                BotSelectionChannel.onBotsSelected.fire(SelectBotsPanel.selectedBotListModel.list())
            }
        }
    }

    private fun updateBotInfo(botInfo: BotInfo) {
        BotInfoPanel.updateBotInfo(botInfo)
    }
}