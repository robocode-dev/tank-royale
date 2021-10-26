package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.util.*
import javax.swing.*

class BotInfoPanel : JPanel(MigLayout("fillx", "[][grow]")) {

    private val nameTextField = JNonEditableTextField()
    private val versionTextField = JNonEditableTextField()
    private val authorsTextField = JNonEditableTextField()
    private val descriptionTextField = JTextArea()
    private val homepageTextPane = JNonEditableHtmlPane()
    private val countryCodesTextPane = JNonEditableHtmlPane()
    private val gameTypesTextField = JNonEditableTextField()
    private val platformTextField = JNonEditableTextField()
    private val programmingLangTextField = JNonEditableTextField()

    init {
        border = BorderFactory.createTitledBorder("Bot Info")

        addLabel("bot_info.name")
        add(nameTextField, "growx, wrap")

        addLabel("bot_info.version")
        add(versionTextField, "growx, wrap")

        addLabel("bot_info.authors")
        add(authorsTextField, "growx, wrap")

        addLabel("bot_info.country_codes")
        add(countryCodesTextPane, "growx, wrap")
        countryCodesTextPane.minimumSize = Dimension(100, 24)

        addLabel("bot_info.description")
        add(descriptionTextField, "growx, wrap")
        descriptionTextField.border = nameTextField.border
        descriptionTextField.background = background
        descriptionTextField.font = font

        addLabel("bot_info.homepage")
        add(homepageTextPane, "growx, wrap")
        homepageTextPane.minimumSize = descriptionTextField.minimumSize

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
        authorsTextField.text = botInfo?.authors?.joinToString(separator = ", ") ?: ""
        descriptionTextField.text = botInfo?.description?.let { truncateDescriptionLines(it) } ?: ""
        homepageTextPane.text = botInfo?.homepage?.let { generateUrlHtml(botInfo.homepage) } ?: ""
        gameTypesTextField.text = botInfo?.gameTypes?.joinToString(separator = ", ") ?: ""
        countryCodesTextPane.text = botInfo?.countryCodes?.let { generateCountryHtml(botInfo.countryCodes) } ?: ""
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

    private fun generateUrlHtml(url: String): String =
        "<html><body style=\"font-family: sans-serif;font-size: ${font.size}\"><a href=\"${url}\">${url}</a></body></html>"

    private fun generateCountryHtml(countryCodes: List<String>): String {
        var html = """
              <table cellspacing="0" cellpadding="0" border="0">
                <tr>"""
        countryCodes.forEach { html += generateCountryHtml(it) + " " }
        html += """
                </tr>
              </table>
            </html>""".trimIndent()
        return html
    }

    private fun generateCountryHtml(countryCode: String): String {
        val countryName = Locale("", countryCode).displayCountry

        return """
            <td style="font-family: sans-serif; font-size: 10">${countryName} (${countryCode})&nbsp;</td>
            <td><img width="20" height="15" src="https://www.countryflags.io/${countryCode}/flat/16.png">&nbsp;&nbsp;</td>
        """.trimIndent()
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