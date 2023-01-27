package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.BotState
import dev.robocode.tankroyale.gui.model.Participant
import dev.robocode.tankroyale.gui.ui.components.ConsoleFrame
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addOkButton
import dev.robocode.tankroyale.gui.util.Event
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit

class BotConsoleFrame(var bot: Participant, frameCounter: Int = 0) :
    ConsoleFrame(bot.displayName, isTitlePropertyName = false) {

    companion object {
        private val editorKit = HTMLEditorKit().apply {
            styleSheet.addRule("body { color: white; font-family: monospace; }")
            styleSheet.addRule(".error { color: \"#FF5733\"; }") // dark pink
            styleSheet.addRule(".linenumber { color: gray; }")
            styleSheet.addRule(".info { color: \"#377B37\"; }") // olive green
        }
    }

    private val document = editorKit.createDefaultDocument() as HTMLDocument

    private var numberOfRounds: Int = 0

    private val onOk = Event<JButton>().apply { subscribe(this) { dispose() } }
    private val onClear = Event<JButton>().apply { subscribe(this) { clear() } }
    private val onCopyToClipboard = Event<JButton>().apply { subscribe(this) { copyToClipboard() } }

    init {
        setLocation(10, 10 + frameCounter * 50) // increment y for each bot console frame
        setSize(600, 400)

        editorPane.contentType = "text/html"
        editorPane.editorKit = editorKit
        editorPane.document = document

        val buttonPanel = JPanel().apply {
            addOkButton(onOk)
            addButton("clear", onClear)
            addButton("copy_to_clipboard", onCopyToClipboard)
        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH)

        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        ClientEvents.onGameStarted.subscribe(this) { gameStartedEvent ->
            numberOfRounds = gameStartedEvent.gameSetup.numberOfRounds

            if (gameStartedEvent.participants.any { it.displayName == bot.displayName }) {
                subscribeToEvents()
            }
        }
        ClientEvents.onRoundStarted.subscribe(this) {
            updateRoundInfo(it.roundNumber)
        }
        ClientEvents.onTickEvent.subscribe(this) { tickEvent ->
            run {
                val botState = tickEvent.botStates.filter { it.id == bot.id }[0]
                updateBotState(botState, tickEvent.turnNumber)
            }
        }
        ClientEvents.onGameAborted.subscribe(this) {
            appendText("> Game was aborted!", "info")
            unsubscribeEvents()
        }
        ClientEvents.onGameEnded.subscribe(this) {
            appendText("> Game has ended", "info")
            unsubscribeEvents()
        }
    }

    private fun unsubscribeEvents() {
        ClientEvents.onRoundStarted.unsubscribe(this)
        ClientEvents.onTickEvent.unsubscribe(this)
        ClientEvents.onGameAborted.unsubscribe(this)
        ClientEvents.onGameEnded.unsubscribe(this)
    }

    private fun updateRoundInfo(roundNumber: Int) {
        var roundInfo = "Round: $roundNumber"
        if (numberOfRounds > 0) {
            roundInfo += "/$numberOfRounds"
        }

        appendText("""
            --------------------<br>
            $roundInfo<br>
            --------------------<br>
        """.trimIndent(), "info"
        )
    }

    private fun updateBotState(botState: BotState, turnNumber: Int) {
        appendText(botState.stdOut, turnNumber = turnNumber)
        appendText(botState.stdErr, "error", turnNumber)
    }

    private fun appendText(text: String?, cssClass: String? = null, turnNumber: Int? = null) {
        var html = text
        if (html != null) {
            html = html.replace("\\n", "<br>")
            if (cssClass != null) {
                html = "<span class=\"$cssClass\">$html</span>"
            }
            if (turnNumber != null) {
                html = "<span class=\"linenumber\">$turnNumber:</span> $html"
            }
            editorKit.insertHTML(document, document.length, html, 0, 0, null)
        }
    }
}
