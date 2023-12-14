package dev.robocode.tankroyale.gui.ansi

import javax.swing.text.MutableAttributeSet
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * Extension functions to update a [MutableAttributeSet] instance using ANSI styles.
 */
object AnsiAttributesExt {

    /**
     * Updates the styling on a [MutableAttributeSet] based on an ANSI Escape Code.
     *
     * @param escCode is the [AnsiEscCode] defining the style to set, e.g. [AnsiEscCode.BOLD].
     * @param ansiColors is the [IAnsiColors] that defines the foreground and background colors to use for the styling.
     */
    fun MutableAttributeSet.updateAnsi(escCode: AnsiEscCode, ansiColors: IAnsiColors): MutableAttributeSet {

        var attributes: MutableAttributeSet = SimpleAttributeSet(this)

        when (escCode) {
            AnsiEscCode.RESET ->
                attributes = SimpleAttributeSet()

            AnsiEscCode.BOLD ->
                StyleConstants.setBold(attributes, true)

            AnsiEscCode.FAINT ->
                StyleConstants.setBold(attributes, false)

            AnsiEscCode.ITALIC ->
                StyleConstants.setItalic(attributes, true)

            AnsiEscCode.UNDERLINE ->
                StyleConstants.setUnderline(attributes, true)

            AnsiEscCode.NORMAL ->
                StyleConstants.setBold(attributes, false)

            AnsiEscCode.NOT_BOLD ->
                StyleConstants.setBold(attributes, false)

            AnsiEscCode.NOT_ITALIC ->
                StyleConstants.setItalic(attributes, false)

            AnsiEscCode.NOT_UNDERLINED ->
                StyleConstants.setUnderline(attributes, false)

            AnsiEscCode.BLACK ->
                StyleConstants.setForeground(attributes, ansiColors.black)

            AnsiEscCode.RED ->
                StyleConstants.setForeground(attributes, ansiColors.red)

            AnsiEscCode.GREEN ->
                StyleConstants.setForeground(attributes, ansiColors.green)

            AnsiEscCode.YELLOW ->
                StyleConstants.setForeground(attributes, ansiColors.yellow)

            AnsiEscCode.BLUE ->
                StyleConstants.setForeground(attributes, ansiColors.blue)

            AnsiEscCode.MAGENTA ->
                StyleConstants.setForeground(attributes, ansiColors.magenta)

            AnsiEscCode.CYAN ->
                StyleConstants.setForeground(attributes, ansiColors.cyan)

            AnsiEscCode.WHITE ->
                StyleConstants.setForeground(attributes, ansiColors.white)

            AnsiEscCode.BRIGHT_BLACK ->
                StyleConstants.setForeground(attributes, ansiColors.brightBlack)

            AnsiEscCode.BRIGHT_RED ->
                StyleConstants.setForeground(attributes, ansiColors.brightRed)

            AnsiEscCode.BRIGHT_GREEN ->
                StyleConstants.setForeground(attributes, ansiColors.brightGreen)

            AnsiEscCode.BRIGHT_YELLOW ->
                StyleConstants.setForeground(attributes, ansiColors.brightYellow)

            AnsiEscCode.BRIGHT_BLUE ->
                StyleConstants.setForeground(attributes, ansiColors.brightBlue)

            AnsiEscCode.BRIGHT_MAGENTA ->
                StyleConstants.setForeground(attributes, ansiColors.brightMagenta)

            AnsiEscCode.BRIGHT_CYAN ->
                StyleConstants.setForeground(attributes, ansiColors.brightCyan)

            AnsiEscCode.BRIGHT_WHITE ->
                StyleConstants.setForeground(attributes, ansiColors.brightWhite)

            AnsiEscCode.DEFAULT ->
                StyleConstants.setForeground(attributes, ansiColors.default)

            AnsiEscCode.BLACK_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.black)

            AnsiEscCode.RED_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.red)

            AnsiEscCode.GREEN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.green)

            AnsiEscCode.YELLOW_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.yellow)

            AnsiEscCode.BLUE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.blue)

            AnsiEscCode.MAGENTA_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.magenta)

            AnsiEscCode.CYAN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.cyan)

            AnsiEscCode.WHITE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.white)

            AnsiEscCode.BRIGHT_BLACK_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightBlack)

            AnsiEscCode.BRIGHT_RED_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightRed)

            AnsiEscCode.BRIGHT_GREEN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightGreen)

            AnsiEscCode.BRIGHT_YELLOW_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightYellow)

            AnsiEscCode.BRIGHT_BLUE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightBlue)

            AnsiEscCode.BRIGHT_MAGENTA_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightMagenta)

            AnsiEscCode.BRIGHT_CYAN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightCyan)

            AnsiEscCode.BRIGHT_WHITE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightWhite)

            AnsiEscCode.DEFAULT_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.default)
        }
        return attributes
    }
}