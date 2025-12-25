package dev.robocode.tankroyale.gui.ui.console

import dev.robocode.tankroyale.gui.ansi.AnsiTextBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import javax.swing.SwingUtilities

class ConsolePanelTest : StringSpec({
    "should preserve double backslashes in text" {
        val consolePanel = ConsolePanel()

        // Test text with double backslashes (like in ASCII art)
        val textWithBackslashes = "\\\\_\\\\_\\\\/ _ \\\\| __)/ _ \\\\/ __// _ \\\\| _ \\\\__|"

        // Append the text using SwingUtilities to ensure proper thread handling
        SwingUtilities.invokeAndWait {
            consolePanel.append(textWithBackslashes)
        }

        // Wait a bit for EDT to process
        Thread.sleep(100)

        // Get the document text
        var documentText = ""
        SwingUtilities.invokeAndWait {
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        // The text should contain double backslashes, not single ones
        documentText shouldContain "\\\\_\\\\_\\\\"
        documentText shouldContain "\\\\/ _ \\\\"
    }

    "should not unescape escape sequences in runtime text" {
        val consolePanel = ConsolePanel()

        // Test text with literal backslash-n (should NOT become a newline)
        val textWithLiteralEscapes = "Path: C:\\\\temp\\\\file.txt"

        SwingUtilities.invokeAndWait {
            consolePanel.append(textWithLiteralEscapes)
        }

        Thread.sleep(100)

        var documentText = ""
        SwingUtilities.invokeAndWait {
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        // Should preserve the double backslashes
        documentText shouldContain "C:\\\\temp\\\\file.txt"
    }

    "should handle ANSI colored text with backslashes" {
        val consolePanel = ConsolePanel()

        // Create ANSI text with backslashes using AnsiTextBuilder
        val ansiText = AnsiTextBuilder()
            .brightGreen()
            .text("Banner with \\\\ backslashes \\\\")
            .defaultColor()
            .build()

        SwingUtilities.invokeAndWait {
            consolePanel.append(ansiText)
        }

        Thread.sleep(100)

        var documentText = ""
        SwingUtilities.invokeAndWait {
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        // The backslashes should be preserved in the rendered output
        documentText shouldContain "\\\\"
        documentText shouldContain "backslashes"
    }

    "should preserve server banner ASCII art format" {
        val consolePanel = ConsolePanel()

        // Simulate actual server banner line with backslashes
        val bannerLine = " ____ ___  ___  ___  ___  ___  ___  _____"
        val bannerLine2 = "\\\\_\\\\_\\\\/ _ \\\\| __)/ _ \\\\/ __// _ \\\\| _ \\\\__|"

        SwingUtilities.invokeAndWait {
            consolePanel.append(bannerLine + "\n")
            consolePanel.append(bannerLine2)
        }

        Thread.sleep(100)

        var documentText = ""
        SwingUtilities.invokeAndWait {
            documentText = consolePanel.ansiEditorPane.document.getText(0, consolePanel.ansiEditorPane.document.length)
        }

        // Should contain the double backslashes from the banner
        documentText shouldContain "\\\\_\\\\_\\\\"
        // Should NOT have single backslashes where doubles were expected
        documentText shouldNotContain "\\_\\_\\/ _ \\"
    }
})

