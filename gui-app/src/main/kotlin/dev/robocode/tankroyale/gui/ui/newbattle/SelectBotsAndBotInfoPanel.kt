package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onChanged
import dev.robocode.tankroyale.gui.ui.extensions.JListExt.onSelection
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class SelectBotsAndBotInfoPanel : JPanel(MigLayout("fill")) {

    private val selectBotsPanel = SelectBotsPanel()
    private val botInfoPanel = BotInfoPanel()

    init {
        add(selectBotsPanel, "center")

        val groupPanel = JPanel(MigLayout("fill"))
        groupPanel.add(botInfoPanel, "grow")
        add(groupPanel, "south")

        selectBotsPanel.botsDirectoryList.onSelection { updateBotInfo(it) }
        selectBotsPanel.joinedBotList.onSelection { updateBotInfo(it) }

        with (selectBotsPanel.selectedBotList) {
            onSelection { updateBotInfo(it) }

            onChanged {
                BotSelectionChannel.onBotsSelected.fire(selectBotsPanel.selectedBotListModel.list())
            }
        }
    }

    private fun updateBotInfo(botInfo: BotInfo) {
        botInfoPanel.updateBotInfo(botInfo)
    }
}