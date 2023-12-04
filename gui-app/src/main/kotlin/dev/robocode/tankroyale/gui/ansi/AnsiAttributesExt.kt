package dev.robocode.tankroyale.gui.ansi

import javax.swing.text.MutableAttributeSet
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

object AnsiAttributesExt {

    fun MutableAttributeSet.updateAnsi(ansiEscCode: AnsiEscCode, ansiColors: IAnsiColors): MutableAttributeSet {

        var attrSet: MutableAttributeSet = SimpleAttributeSet(this)

        when (ansiEscCode) {
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
                StyleConstants.setForeground(attrSet, ansiColors.black)
            }

            AnsiEscCode.RED -> {
                StyleConstants.setForeground(attrSet, ansiColors.red)
            }

            AnsiEscCode.GREEN -> {
                StyleConstants.setForeground(attrSet, ansiColors.green)
            }

            AnsiEscCode.YELLOW -> {
                StyleConstants.setForeground(attrSet, ansiColors.yellow)
            }

            AnsiEscCode.BLUE -> {
                StyleConstants.setForeground(attrSet, ansiColors.blue)
            }

            AnsiEscCode.MAGENTA -> {
                StyleConstants.setForeground(attrSet, ansiColors.magenta)
            }

            AnsiEscCode.CYAN -> {
                StyleConstants.setForeground(attrSet, ansiColors.cyan)
            }

            AnsiEscCode.WHITE -> {
                StyleConstants.setForeground(attrSet, ansiColors.white)
            }

            AnsiEscCode.BRIGHT_BLACK -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightBlack)
            }

            AnsiEscCode.BRIGHT_RED -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightRed)
            }

            AnsiEscCode.BRIGHT_GREEN -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightGreen)
            }

            AnsiEscCode.BRIGHT_YELLOW -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightYellow)
            }

            AnsiEscCode.BRIGHT_BLUE -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightBlue)
            }

            AnsiEscCode.BRIGHT_MAGENTA -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightMagenta)
            }

            AnsiEscCode.BRIGHT_CYAN -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightCyan)
            }

            AnsiEscCode.BRIGHT_WHITE -> {
                StyleConstants.setForeground(attrSet, ansiColors.brightWhite)
            }

            AnsiEscCode.DEFAULT -> {
                StyleConstants.setForeground(attrSet, ansiColors.default)
            }
        }
        return attrSet
    }
}