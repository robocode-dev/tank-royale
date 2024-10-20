package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.model.BotInfo
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import net.miginfocom.swing.MigLayout
import java.awt.Desktop
import java.awt.Insets
import java.util.*
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLEditorKit

object BotInfoPanel : JPanel(MigLayout("", "[][sg,grow][10][][sg,grow]")) {

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

        addLabel("bot_info.name", "cell 0 0")
        add(nameTextField, "cell 1 0, growx")

        addLabel("bot_info.version", "cell 0 1")
        add(versionTextField, "cell 1 1, growx")

        addLabel("bot_info.authors", "cell 0 2")
        add(authorsTextField, "cell 1 2, growx")

        addLabel("bot_info.homepage", "cell 0 3")
        add(homepageTextPane, "cell 1 3, growx")

        addLabel("bot_info.description", "cell 0 4")
        add(descriptionTextField, "growx, span 4")
        descriptionTextField.border = nameTextField.border
        descriptionTextField.background = background
        descriptionTextField.font = font

        addLabel("bot_info.platform", "cell 3 0")
        add(platformTextField, "cell 4 0, growx")

        addLabel("bot_info.programming_lang", "cell 3 1")
        add(programmingLangTextField, "cell 4 1, growx")

        addLabel("bot_info.game_types", "cell 3 2")
        add(gameTypesTextField, "cell 4 2, growx")

        addLabel("bot_info.country_codes", "cell 3 3")
        add(countryCodesTextPane, "cell 4 3, growx")

        updateBotInfo(null)

        BotSelectionEvents.apply {
            onBotDirectorySelected.subscribe(BotInfoPanel) { updateBotInfo(it) }
            onJoinedBotSelected.subscribe(BotInfoPanel) { updateBotInfo(it) }
            onBotSelected.subscribe(BotInfoPanel) { updateBotInfo(it) }
        }
    }

    private fun updateBotInfo(botInfo: BotInfo?) {
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
            desc += " …"
        }
        return desc
    }

    private fun generateUrlHtml(url: String): String =
        "<a href=\"${url}\">${url}</a>"

    private fun generateCountryHtml(countryCodes: List<String>): String {
        var html = """
              <table cellspacing="0" cellpadding="0">
                <tr>"""
        countryCodes.forEach { html += generateCountryHtml(it) + " " }
        html += """
                </tr>
              </table>
              """.trimIndent()
        return html
    }

    private fun generateCountryHtml(countryCode: String): String {
        val name = Locale.Builder().setRegion(countryCode).build().displayCountry
        val cc = countryCode.trim().lowercase()
        return """
            <td>${name} (${cc})&nbsp;</td>
            <td><img width="16" height="12" src="https://flagcdn.com/w20/${cc}.png">&nbsp;&nbsp;</td>
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

            border = UIManager.getBorder("TextField.border")
            margin = Insets(0, 0, 0, 0)

            minimumSize = JTextField().minimumSize

            editorKit = HTMLEditorKit().apply {
                styleSheet = BotInfoStyleSheet()
            }

            addHyperlinkListener {
                if (it.eventType == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(it.url.toURI())
                }
            }
        }
    }
}
