package dev.robocode.tankroyale.gui.ui.newbattle

import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

object SelectBotsAndBotInfoPanel : JPanel(MigLayout("fill")) {

    init {
        add(SelectBotsPanel, "center")

        val groupPanel = JPanel(MigLayout("fill"))
        groupPanel.add(BotInfoPanel, "grow")
        add(groupPanel, "south")
    }
}