package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.ui.desktop.model.BotInfo
import net.miginfocom.swing.MigLayout
import java.util.*
import javax.swing.*

class BotInfoPanel : JPanel(MigLayout("fillx", "[][grow]")) {

    private val nameTextField = JNonEditableTextField()
    private val versionTextField = JNonEditableTextField()
    private val authorTextField = JNonEditableTextField()
    private val descriptionTextField = JTextArea()
    private val urlTextPane = JNonEditableHtmlPane()
    private val countryCodeTextPane = JNonEditableHtmlPane()
    private val gameTypesTextField = JNonEditableTextField()
    private val platformTextField = JNonEditableTextField()
    private val programmingLangTextField = JNonEditableTextField()

    init {
        border = BorderFactory.createTitledBorder("Bot Info")

        addLabel("bot_info.name")
        add(nameTextField, "growx, wrap")

        addLabel("bot_info.version")
        add(versionTextField, "growx, wrap")

        addLabel("bot_info.author")
        add(authorTextField, "growx, wrap")

        addLabel("bot_info.country_code")
        add(countryCodeTextPane, "growx, wrap")

        addLabel("bot_info.description")
        add(descriptionTextField, "growx, wrap")
        descriptionTextField.border = nameTextField.border
        descriptionTextField.background = background
        descriptionTextField.font = font

        addLabel("bot_info.url")
        add(urlTextPane, "growx, wrap")

        addLabel("bot_info.game_types")
        add(gameTypesTextField, "growx, wrap")

        addLabel("bot_info.platform")
        add(platformTextField, "growx, wrap")

        addLabel("bot_info.programming_lang")
        add(programmingLangTextField, "growx, wrap")

        updateBotInfo(null)
    }

    fun updateBotInfo(botInfo: BotInfo?) {
        nameTextField.text = botInfo?.name
        versionTextField.text = botInfo?.version
        authorTextField.text = botInfo?.author
        descriptionTextField.text =
            if (botInfo != null) botInfo.description?.let { truncateDescriptionLines(it) } else ""
        urlTextPane.text = if (botInfo != null) botInfo.url?.let { generateUrlHtml(botInfo.url) } else ""
        gameTypesTextField.text = if (botInfo != null) gameTypesToString(botInfo.gameTypes) else ""
        countryCodeTextPane.text =
            if (botInfo != null) botInfo.countryCode?.let { generateCountryHtml(botInfo.countryCode) } else ""
        platformTextField.text = botInfo?.platform
        programmingLangTextField.text = botInfo?.programmingLang
    }

    private fun truncateDescriptionLines(text: String): String {
        var lines = text.lines()
        var truncated = false
        if (lines.size > 3) { // Reduce the number of lines beyond 3 lines
            lines = lines.subList(0, 3)
            truncated = true
        }
        var desc = lines.joinToString(separator = "\n")
        if (truncated) { // Add 3 dots if text got truncated
            desc += "\n..."
        }
        return desc
    }

    private fun gameTypesToString(gameTypes: Set<String>): String = gameTypes.joinToString(separator = ", ")

    private fun generateUrlHtml(url: String): String =
        "<html><body style=\"font-family: sans-serif;font-size: ${font.size}\"><a href=\"${url}\">${url}</a></body></html>"

    private fun generateCountryHtml(countryCode: String): String {
        val countryName = Locale("", countryCode).displayCountry

        return """<html><body><table cellspacing='0' cellpadding='0' border='0'><tr><td style='font-family: sans-serif;
        font-size: 10'>${countryName} (${countryCode})</td>&nbsp;<td><img width='20' height='15'
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