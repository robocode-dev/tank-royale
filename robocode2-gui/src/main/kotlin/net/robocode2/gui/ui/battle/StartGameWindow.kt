package net.robocode2.gui.ui.battle

import kotlinx.serialization.ImplicitReflectionSerializer
import net.robocode2.gui.bootstrap.BootstrapProcess
import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.WindowExt.onClosing
import net.robocode2.gui.model.BotAddress
import net.robocode2.gui.model.BotInfo
import net.robocode2.gui.server.ServerProcess
import net.robocode2.gui.ui.ResourceBundles
import net.robocode2.gui.utils.Disposable
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*


@ImplicitReflectionSerializer
object StartGameWindow : JFrame(getWindowTitle()) {

    private val textArea = JTextArea()
    private val startButton = JButton(ResourceBundles.STRINGS.get("start_battle"))

    private val disposables = ArrayList<Disposable>()
    private var botEntries: Set<BotInfo> = emptySet()

    init {
        setSize(500, 300)
        setLocationRelativeTo(null) // center on screen

        textArea.isEditable = false
        textArea.foreground = Color.WHITE
        textArea.background = Color(0x28, 0x28, 0x28)

        val scrollPane = JScrollPane(textArea)

        contentPane.add(scrollPane, BorderLayout.CENTER)
        contentPane.add(startButton, BorderLayout.SOUTH)

        textArea.append(ResourceBundles.STRINGS.get("start_game_waiting_for_bots"))

        disposables += Client.onBotListUpdate.subscribe {
            botEntries = it.bots

            textArea.text = ResourceBundles.STRINGS.get("start_game_joined_bots") + '\n'
            botEntries.forEach { bot -> textArea.append(bot.displayText + '\n') }
        }

        startButton.addActionListener {
            Client.onGameStarted.subscribe { dispose() }
            Client.onGameAborted.subscribe { BootstrapProcess.stopRunning() }
            Client.onGameEnded.subscribe { BootstrapProcess.stopRunning() }

            val botAddresses = HashSet<BotAddress>()
            botEntries.forEach { botAddresses += it.botAddress }

            Client.startGame(SelectBotsPanel.gameSetup, botAddresses)
        }

        onClosing {
            disposables.forEach { it.dispose() }
        }
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("start_game_window")
}

@ImplicitReflectionSerializer
fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    StartGameWindow.isVisible = true
}