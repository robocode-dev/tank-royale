package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.gui.ui.components.RcHtmlPane
import dev.robocode.tankroyale.gui.ui.components.RcNonEditableTextArea
import dev.robocode.tankroyale.gui.ui.components.RcNonEditableTextField
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import net.miginfocom.swing.MigLayout
import java.awt.Desktop
import java.util.*
import javax.swing.*
import javax.swing.event.HyperlinkEvent

object BotInfoPanel : JPanel(MigLayout("", "[][sg,grow][10][][sg,grow]")) {

    private val nameTextField = RcNonEditableTextField()
    private val versionTextField = RcNonEditableTextField()
    private val authorsTextField = RcNonEditableTextField()
    private val descriptionTextArea = RcNonEditableTextArea()
    private val homepageTextPane = RcHtmlPane { BotInfoStyleSheet() }
    private val countryCodesTextPane = RcHtmlPane { BotInfoStyleSheet() }
    private val gameTypesTextField = RcNonEditableTextField()
    private val platformTextField = RcNonEditableTextField()
    private val programmingLangTextField = RcNonEditableTextField()

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
        add(descriptionTextArea, "growx, span 4")
        descriptionTextArea.font = font

        addLabel("bot_info.platform", "cell 3 0")
        add(platformTextField, "cell 4 0, growx")

        addLabel("bot_info.programming_lang", "cell 3 1")
        add(programmingLangTextField, "cell 4 1, growx")

        addLabel("bot_info.game_types", "cell 3 2")
        add(gameTypesTextField, "cell 4 2, growx")

        addLabel("bot_info.country_codes", "cell 3 3")
        add(countryCodesTextPane, "cell 4 3, growx")

        homepageTextPane.addHyperlinkListener {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(it.url.toURI())
            }
        }

        updateBotInfo(null)

        BotSelectionEvents.apply {
            onBotDirectorySelected.on(BotInfoPanel) { updateBotInfo(it) }
            onJoinedBotSelected.on(BotInfoPanel) { updateBotInfo(it) }
            onBotSelected.on(BotInfoPanel) { updateBotInfo(it) }
        }
    }

    private fun updateBotInfo(botInfo: BotInfo?) {
        nameTextField.text = botInfo?.name
        versionTextField.text = botInfo?.version
        authorsTextField.text = botInfo?.authors?.joinToString(separator = ", ") ?: ""
        descriptionTextArea.text = botInfo?.description?.let { truncateDescriptionLines(it) } ?: ""
        homepageTextPane.text = botInfo?.homepage?.let { generateUrlHtml(it) } ?: ""
        gameTypesTextField.text = botInfo?.gameTypes?.joinToString(separator = ", ") ?: ""
        countryCodesTextPane.text = botInfo?.countryCodes?.let { generateCountryHtml(botInfo.countryCodes) } ?: ""
        platformTextField.text = botInfo?.platform
        programmingLangTextField.text = botInfo?.programmingLang
    }

    private fun truncateDescriptionLines(text: String): String {
        var lines = text.lines()
        var truncated = false
        if (lines.size > 3) {
            lines = lines.subList(0, 3)
            truncated = true
        }
        var desc = lines.joinToString(separator = "\n")
        if (truncated) {
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
}

