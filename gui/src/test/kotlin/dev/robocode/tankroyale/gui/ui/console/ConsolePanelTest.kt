package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import javax.swing.SwingUtilities

class ConsolePanelTest : StringSpec({
    "should preserve double backslashes in text" {
        val consolePanel = ConsolePanel()
        val textWithBackslashes = "\\\\_\\\\_\\\\/ _ \\\\| __)/ _ \\\\/ __// _ \\\\| _ \\\\__|"

        var documentText = ""
        SwingUtilities.invokeAndWait {
            consolePanel.append(textWithBackslashes)
            consolePanel.flushNow()
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        documentText shouldContain "\\\\_\\\\_\\\\"
        documentText shouldContain "\\\\/ _ \\\\"
    }

    "should not unescape escape sequences in runtime text" {
        val consolePanel = ConsolePanel()
        val textWithLiteralEscapes = "Path: C:\\\\temp\\\\file.txt"

        var documentText = ""
        SwingUtilities.invokeAndWait {
            consolePanel.append(textWithLiteralEscapes)
            consolePanel.flushNow()
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        documentText shouldContain "C:\\\\temp\\\\file.txt"
    }

    "should handle ANSI colored text with backslashes" {
        val consolePanel = ConsolePanel()
        val ansiText = AnsiTextBuilder()
            .brightGreen()
            .text("Banner with \\\\ backslashes \\\\")
            .defaultColor()
            .build()

        var documentText = ""
        SwingUtilities.invokeAndWait {
            consolePanel.append(ansiText)
            consolePanel.flushNow()
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        documentText shouldContain "\\\\"
        documentText shouldContain "backslashes"
    }

    "should preserve server banner ASCII art format" {
        val consolePanel = ConsolePanel()
        val bannerLine = " ____ ___  ___  ___  ___  ___  ___  _____"
        val bannerLine2 = "\\\\_\\\\_\\\\/ _ \\\\| __)/ _ \\\\/ __// _ \\\\| _ \\\\__|"

        var documentText = ""
        SwingUtilities.invokeAndWait {
            consolePanel.append(bannerLine + "\n")
            consolePanel.append(bannerLine2)
            consolePanel.flushNow()
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        documentText shouldContain "\\\\_\\\\_\\\\"
        documentText shouldNotContain "\\_\\_\\/ _ \\"
    }

    "should enforce max character limit" {
        val consolePanel = ConsolePanel()
        val originalLimit = ConfigSettings.consoleMaxCharacters
        ConfigSettings.consoleMaxCharacters = 100

        var documentLength = 0
        try {
            SwingUtilities.invokeAndWait {
                consolePanel.append("a".repeat(150))
                consolePanel.flushNow()
                documentLength = consolePanel.ansiEditorPane.document.length
            }
        } finally {
            ConfigSettings.consoleMaxCharacters = originalLimit
        }

        documentLength shouldBe 100
    }

    "should batch multiple appends" {
        val consolePanel = ConsolePanel()

        // All appends and the pre-flush read happen atomically on the EDT,
        // so the timer cannot fire in between and spoil the "still empty" assertion.
        var documentTextBeforeFlush = ""
        var documentTextAfterFlush = ""
        SwingUtilities.invokeAndWait {
            consolePanel.append("1")
            consolePanel.append("2")
            consolePanel.append("3")
            documentTextBeforeFlush = consolePanel.ansiEditorPane.text
            consolePanel.flushNow()
            documentTextAfterFlush = consolePanel.ansiEditorPane.text
        }

        documentTextBeforeFlush shouldBe ""
        documentTextAfterFlush shouldContain "123"
    }
})

