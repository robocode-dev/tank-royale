package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.model.BotAddress
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*


@UnstableDefault
@ImplicitReflectionSerializer
object StartGameWindow : JFrame(ResourceBundles.UI_TITLES.get("start_game_window")) {

    private val textArea = JTextArea()
    private val startButton = JButton(ResourceBundles.STRINGS.get("start_battle"))

    private var botEntries: Set<BotInfo> = emptySet()

    init {
        setSize(500, 300)
        setLocationRelativeTo(null) // center on screen

        textArea.apply {
            isEditable = false
            foreground = Color.WHITE
            background = Color(0x28, 0x28, 0x28)
        }
        val scrollPane = JScrollPane(textArea)

        contentPane.add(scrollPane, BorderLayout.CENTER)
        contentPane.add(startButton, BorderLayout.SOUTH)

        textArea.append(ResourceBundles.STRINGS.get("start_game_waiting_for_bots"))

        Client.onBotListUpdate.subscribe {
            botEntries = it.bots

            textArea.text = ResourceBundles.STRINGS.get("start_game_joined_bots") + '\n'
            botEntries.forEach { bot -> textArea.append(bot.displayText + '\n') }
        }

        startButton.addActionListener {
            Client.apply {
                onGameStarted.subscribe { dispose() }
                onGameAborted.subscribe { BootstrapProcess.stopRunning() }
                onGameEnded.subscribe { BootstrapProcess.stopRunning() }

                val botAddresses = HashSet<BotAddress>()
                botEntries.forEach { botAddresses += it.botAddress }

                startGame(SelectBotsPanel.gameSetup, botAddresses)
            }
        }
    }
}

@UnstableDefault
@ImplicitReflectionSerializer
fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    StartGameWindow.isVisible = true
}