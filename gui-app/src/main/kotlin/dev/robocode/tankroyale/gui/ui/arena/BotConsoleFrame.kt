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

class BotConsoleFrame(bot: Participant, frameCounter: Int = 0) :
    ConsoleFrame(bot.displayName, isTitlePropertyName = false) {

    private val onOk = Event<JButton>().apply { subscribe(this) { dispose() } }
    private val onClear = Event<JButton>().apply { subscribe(this) { clear() } }

    private val editorKit = HTMLEditorKit().apply {
        styleSheet.addRule("body { color: white; font-family: monospace; }")
        styleSheet.addRule(".error { color: red; }")
    }

    private val document = editorKit.createDefaultDocument() as HTMLDocument

    init {
        setLocation(10, 10 + frameCounter * 50)
        setSize(400, 300)

        editorPane.contentType = "text/html"
        editorPane.editorKit = editorKit
        editorPane.document = document

        val buttonPanel = JPanel().apply {
            addOkButton(onOk)
            addButton("clear", onClear)
        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH)

        ClientEvents.onTickEvent.subscribe(this) { tickEvent ->
            run {
                val botState = tickEvent.botStates.filter { it.id == bot.id }[0]
                updateBotState(botState)
            }
        }
    }

    private fun updateBotState(botState: BotState) {
        appendText(botState.stdOut)
        appendText(botState.stdErr, "error")
    }

    private fun appendText(str: String?, cssClass: String? = null) {
        var html = str
        if (html != null) {
            html = html.replace("\\n", "<br>")
            if (cssClass != null) {
                html = "<span class=\"$cssClass\">$html</span>"
            }
            editorKit.insertHTML(document, document.length, html, 0, 0, null)
        }
    }
}
