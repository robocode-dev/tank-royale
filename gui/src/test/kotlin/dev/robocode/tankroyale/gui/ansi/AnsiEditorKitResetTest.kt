package dev.robocode.tankroyale.gui.ansi

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.awt.Color
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyleConstants

class AnsiEditorKitResetTest : StringSpec({

    "should set default foreground color after RESET code" {
        val ansiEditorKit = AnsiEditorKit()
        val doc = DefaultStyledDocument()

        // Simulate the server banner output:
        // Bold green text followed by RESET, then plain text
        val ansiText = "\u001B[1m\u001B[32mRobocode Tank Royale\u001B[0m\nRobocode Tank Royale Server 0.36.0"

        ansiEditorKit.insertAnsi(doc, ansiText)

        // Get the attributes of the text after the reset
        val attrs = doc.getCharacterElement(doc.getText(0, doc.length).indexOf("Server")).attributes

        // The foreground color should be the default color (brightWhite), not black
        val foreground = StyleConstants.getForeground(attrs)
        foreground shouldBe DefaultAnsiColors.default
        foreground shouldBe Color(0xff, 0xff, 0xff) // brightWhite
    }

    "should preserve font settings after RESET code" {
        val fontSize = 14
        val ansiEditorKit = AnsiEditorKit(fontSize = fontSize)
        val doc = DefaultStyledDocument()

        val ansiText = "\u001B[1m\u001B[32mBold Green\u001B[0m\nPlain text after reset"

        ansiEditorKit.insertAnsi(doc, ansiText)

        // Get the attributes of the text after the reset
        val plainTextStart = doc.getText(0, doc.length).indexOf("Plain")
        val attrs = doc.getCharacterElement(plainTextStart).attributes

        // Font family and size should be preserved
        StyleConstants.getFontFamily(attrs) shouldBe "Monospaced"
        StyleConstants.getFontSize(attrs) shouldBe fontSize
    }

    "should reset bold and other attributes but keep default color" {
        val ansiEditorKit = AnsiEditorKit()
        val doc = DefaultStyledDocument()

        val ansiText = "\u001B[1m\u001B[32mBold Green\u001B[0mPlain"

        ansiEditorKit.insertAnsi(doc, ansiText)

        // Get the attributes of "Plain"
        val plainStart = doc.getText(0, doc.length).indexOf("Plain")
        val attrs = doc.getCharacterElement(plainStart).attributes

        // Should not be bold
        StyleConstants.isBold(attrs) shouldBe false

        // Should have default foreground color
        StyleConstants.getForeground(attrs) shouldBe DefaultAnsiColors.default
    }
})

