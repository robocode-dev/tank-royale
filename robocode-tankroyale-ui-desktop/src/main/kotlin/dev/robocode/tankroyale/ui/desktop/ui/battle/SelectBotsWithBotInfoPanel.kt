package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.extensions.JListExt.onSelection
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*

@UnstableDefault
@ImplicitReflectionSerializer
private object SelectBotsWithBotInfoDialog : JDialog(MainWindow, ResourceBundles.UI_TITLES.get("select_bots_dialog")) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(600, 700)

        setLocationRelativeTo(null) // center on screen

        val selectBotsAndStartPanel = SelectBotsAndStartPanel()

        contentPane.add(selectBotsAndStartPanel)

        onActivated {
            selectBotsAndStartPanel.apply {
                updateAvailableBots()
                clear()
            }
        }
    }
}

class SelectBotsWithBotInfoPanel : JPanel(MigLayout("fill")) {

    private val selectBotsPanel = SelectBotsPanel()
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

@UnstableDefault
@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        SelectBotsWithBotInfoDialog.isVisible = true
    }
}