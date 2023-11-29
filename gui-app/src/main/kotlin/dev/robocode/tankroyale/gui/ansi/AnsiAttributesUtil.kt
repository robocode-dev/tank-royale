package dev.robocode.tankroyale.gui.ansi

import javax.swing.text.MutableAttributeSet
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

object AnsiAttributesUtil {

    fun updateAttributeSet(ansiCode: String, attributeSet: MutableAttributeSet): MutableAttributeSet {

        var attrSet: MutableAttributeSet = SimpleAttributeSet(attributeSet)

        when (AnsiEscCode.fromCode(ansiCode)) {
            AnsiEscCode.RESET -> {
                attrSet = SimpleAttributeSet()
            }

            AnsiEscCode.BOLD -> {
                StyleConstants.setBold(attrSet, true)
            }

            AnsiEscCode.FAINT -> {
                StyleConstants.setBold(attrSet, false)
            }

            AnsiEscCode.ITALIC -> {
                StyleConstants.setItalic(attrSet, true)
            }

            AnsiEscCode.UNDERLINE -> {
                StyleConstants.setUnderline(attrSet, true)
            }

            AnsiEscCode.NORMAL -> {
                StyleConstants.setBold(attrSet, false)
            }

            AnsiEscCode.NOT_BOLD -> {
                StyleConstants.setBold(attrSet, false)
            }

            AnsiEscCode.NOT_ITALIC -> {
                StyleConstants.setItalic(attrSet, false)
            }

            AnsiEscCode.NOT_UNDERLINED -> {
                StyleConstants.setUnderline(attrSet, false)
            }

            AnsiEscCode.BLACK -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BLACK.color)
            }

            AnsiEscCode.RED -> {
                StyleConstants.setForeground(attrSet, AnsiColor.RED.color)
            }

            AnsiEscCode.GREEN -> {
                StyleConstants.setForeground(attrSet, AnsiColor.GREEN.color)
            }

            AnsiEscCode.YELLOW -> {
                StyleConstants.setForeground(attrSet, AnsiColor.YELLOW.color)
            }

            AnsiEscCode.BLUE -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BLUE.color)
            }

            AnsiEscCode.MAGENTA -> {
                StyleConstants.setForeground(attrSet, AnsiColor.MAGENTA.color)
            }

            AnsiEscCode.CYAN -> {
                StyleConstants.setForeground(attrSet, AnsiColor.CYAN.color)
            }

            AnsiEscCode.WHITE -> {
                StyleConstants.setForeground(attrSet, AnsiColor.WHITE.color)
            }

            AnsiEscCode.BRIGHT_BLACK -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_BLACK.color)
            }

            AnsiEscCode.BRIGHT_RED -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_RED.color)
            }

            AnsiEscCode.BRIGHT_GREEN -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_GREEN.color)
            }

            AnsiEscCode.BRIGHT_YELLOW -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_YELLOW.color)
            }

            AnsiEscCode.BRIGHT_BLUE -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_BLUE.color)
            }

            AnsiEscCode.BRIGHT_MAGENTA -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_MAGENTA.color)
            }

            AnsiEscCode.BRIGHT_CYAN -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_CYAN.color)
            }

            AnsiEscCode.BRIGHT_WHITE -> {
                StyleConstants.setForeground(attrSet, AnsiColor.BRIGHT_WHITE.color)
            }

            AnsiEscCode.DEFAULT -> {
                StyleConstants.setForeground(attrSet, AnsiColor.DEFAULT.color)
            }
        }
        return attrSet
    }
}