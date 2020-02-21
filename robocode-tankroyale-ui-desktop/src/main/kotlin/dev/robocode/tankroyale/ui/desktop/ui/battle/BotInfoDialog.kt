package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import kotlinx.serialization.ImplicitReflectionSerializer
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.util.*
import javax.swing.*


@ImplicitReflectionSerializer
object BotInfoDialog : JDialog(MainWindow) {

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(500, 350)

        setLocationRelativeTo(null) // center on screen

//        contentPane.add(JScrollPane(BotInfoPanel))
    }
}

private class BotInfoPanel(botInfo: BotInfo) : JPanel(MigLayout("fillx", "[][grow]")) {

    val nameTextField = JNonEditableTextField()
    val versionTextField = JNonEditableTextField()
    val authorTextField = JNonEditableTextField()
    val descriptionTextField = JTextArea()
    val urlTextPane = JNonEditableHtmlPane()
    val countryCodeTextPane = JNonEditableHtmlPane()
    val gameTypesTextField = JNonEditableTextField()
    val platformTextField = JNonEditableTextField()
    val programmingLangTextField = JNonEditableTextField()

    init {
        addLabel("bot_info.name")
        add(nameTextField, "growx, wrap")
        nameTextField.text = botInfo.name

        addLabel("bot_info.version")
        add(versionTextField, "growx, wrap")
        versionTextField.text = botInfo.version

        addLabel("bot_info.author")
        add(authorTextField, "growx, wrap")
        authorTextField.text = botInfo.author

        if (botInfo.description != null) {
            addLabel("bot_info.description")
            add(descriptionTextField, "growx, wrap")

            descriptionTextField.border = nameTextField.border
            descriptionTextField.background = background
            descriptionTextField.font = font
            descriptionTextField.text = truncateDescriptionLines(botInfo.description)
        }
        if (botInfo.url != null) {
            addLabel("bot_info.url")
            add(urlTextPane, "growx, wrap")
            urlTextPane.text = generateUrlHtml(botInfo.url)
        }
        addLabel("bot_info.game_types")
        add(gameTypesTextField, "growx, wrap")
        gameTypesTextField.text = gameTypesToString(botInfo.gameTypes)

        if (botInfo.countryCode != null) {
            addLabel("bot_info.country_code")
            add(countryCodeTextPane, "growx, wrap")
            countryCodeTextPane.text = generateCountryHtml(botInfo.countryCode)
        }
        if (botInfo.platform != null) {
            addLabel("bot_info.platform")
            add(platformTextField, "growx, wrap")
            platformTextField.text = "Platform for the bot"
        }
        if (botInfo.programmingLang != null) {
            addLabel("bot_info.programming_lang")
            add(programmingLangTextField, "growx, wrap")
            programmingLangTextField.text = "Programming language for the bot"
        }
    }

    private fun truncateDescriptionLines(text: String): String {
        var lines = text?.lines()
        var truncated = false
        if (lines.size > 3) { // Reduce the number of lines beyond 3 lines
            lines = lines.subList(0, 3)
            truncated = true
        }
        var text = lines.joinToString(separator = "\n")
        if (truncated) { // Add 3 dots if text got truncated
            text += "\n..."
        }
        return text
    }

    private fun gameTypesToString(gameTypes: Set<String>): String = gameTypes.joinToString(separator = ", ")

    private fun generateUrlHtml(url: String): String =
        "<html><body style=\"font-family: sans-serif;font-size: ${font.size}\"><a href=\"${url}\">${url}</a></body></html>"

    private fun generateCountryHtml(countryCode: String): String {
        val countryName = Locale("", countryCode).displayCountry

        return """<html><body><table cellspacing='0' cellpadding='0' border='0'><tr><td style='font-family: sans-serif;
        font-size: 10'>${countryName} (${countryCode})</td>&nbsp;<td><img
        src='https://www.countryflags.io/${countryCode}/flat/16.png'></td></tr></table></body></html>"""
    }

    private class JNonEditableTextField : JTextField() {
        init {
            isEditable = false
        }
    }

    private class JNonEditableHtmlPane : JTextPane() {
        init {
            isEditable = false
            contentType = "text/html"
            border = JTextField().border
            background = parent?.background
        }
    }
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        BotInfoDialog.isVisible = true
    }
}
