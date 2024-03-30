package dev.robocode.tankroyale.gui.ansi

import dev.robocode.tankroyale.gui.ansi.esc_code.CommandCode
import dev.robocode.tankroyale.gui.ansi.esc_code.EscapeSequence
import java.awt.Color
import javax.swing.text.MutableAttributeSet
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * Extension functions to update a [MutableAttributeSet] instance using ANSI styles.
 */
object AnsiAttributesExt {

    /**
     * Updates the styling on a [MutableAttributeSet] based on an ANSI Escape Sequence.
     *
     * @param escapeSequence is the [EscapeSequence] defining the updated text style.
     * @param ansiColors is the [IAnsiColors] that defines the color scheme for the foreground and background colors
     * used for the styling.
     */
    fun MutableAttributeSet.updateAnsi(escapeSequence: EscapeSequence, ansiColors: IAnsiColors): MutableAttributeSet {

        var attributes: MutableAttributeSet = SimpleAttributeSet(this)

        when (escapeSequence.commandCode()) {
            CommandCode.RESET ->
                attributes = SimpleAttributeSet()

            CommandCode.BOLD ->
                StyleConstants.setBold(attributes, true)

            CommandCode.FAINT ->
                StyleConstants.setBold(attributes, false)

            CommandCode.ITALIC ->
                StyleConstants.setItalic(attributes, true)

            CommandCode.UNDERLINE ->
                StyleConstants.setUnderline(attributes, true)

            CommandCode.NORMAL ->
                StyleConstants.setBold(attributes, false)

            CommandCode.NOT_BOLD ->
                StyleConstants.setBold(attributes, false)

            CommandCode.NOT_ITALIC ->
                StyleConstants.setItalic(attributes, false)

            CommandCode.NOT_UNDERLINED ->
                StyleConstants.setUnderline(attributes, false)

            CommandCode.BLACK ->
                StyleConstants.setForeground(attributes, ansiColors.black)

            CommandCode.RED ->
                StyleConstants.setForeground(attributes, ansiColors.red)

            CommandCode.GREEN ->
                StyleConstants.setForeground(attributes, ansiColors.green)

            CommandCode.YELLOW ->
                StyleConstants.setForeground(attributes, ansiColors.yellow)

            CommandCode.BLUE ->
                StyleConstants.setForeground(attributes, ansiColors.blue)

            CommandCode.MAGENTA ->
                StyleConstants.setForeground(attributes, ansiColors.magenta)

            CommandCode.CYAN ->
                StyleConstants.setForeground(attributes, ansiColors.cyan)

            CommandCode.WHITE ->
                StyleConstants.setForeground(attributes, ansiColors.white)

            CommandCode.BRIGHT_BLACK ->
                StyleConstants.setForeground(attributes, ansiColors.brightBlack)

            CommandCode.BRIGHT_RED ->
                StyleConstants.setForeground(attributes, ansiColors.brightRed)

            CommandCode.BRIGHT_GREEN ->
                StyleConstants.setForeground(attributes, ansiColors.brightGreen)

            CommandCode.BRIGHT_YELLOW ->
                StyleConstants.setForeground(attributes, ansiColors.brightYellow)

            CommandCode.BRIGHT_BLUE ->
                StyleConstants.setForeground(attributes, ansiColors.brightBlue)

            CommandCode.BRIGHT_MAGENTA ->
                StyleConstants.setForeground(attributes, ansiColors.brightMagenta)

            CommandCode.BRIGHT_CYAN ->
                StyleConstants.setForeground(attributes, ansiColors.brightCyan)

            CommandCode.BRIGHT_WHITE ->
                StyleConstants.setForeground(attributes, ansiColors.brightWhite)

            CommandCode.DEFAULT ->
                StyleConstants.setForeground(attributes, ansiColors.default)

            CommandCode.BLACK_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.black)

            CommandCode.RED_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.red)

            CommandCode.GREEN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.green)

            CommandCode.YELLOW_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.yellow)

            CommandCode.BLUE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.blue)

            CommandCode.MAGENTA_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.magenta)

            CommandCode.CYAN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.cyan)

            CommandCode.WHITE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.white)

            CommandCode.BRIGHT_BLACK_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightBlack)

            CommandCode.BRIGHT_RED_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightRed)

            CommandCode.BRIGHT_GREEN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightGreen)

            CommandCode.BRIGHT_YELLOW_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightYellow)

            CommandCode.BRIGHT_BLUE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightBlue)

            CommandCode.BRIGHT_MAGENTA_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightMagenta)

            CommandCode.BRIGHT_CYAN_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightCyan)

            CommandCode.BRIGHT_WHITE_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.brightWhite)

            CommandCode.DEFAULT_BACKGROUND ->
                StyleConstants.setBackground(attributes, ansiColors.default)

            CommandCode.SET_FOREGROUND_COLOR -> {
                setForegroundColor(attributes, escapeSequence.parameters(), ansiColors)
            }

            CommandCode.SET_BACKGROUND_COLOR -> {
                setBackgroundColor(attributes, escapeSequence.parameters(), ansiColors)
            }
        }
        return attributes
    }

    private fun setForegroundColor(attributes: MutableAttributeSet, parameters: List<Int>, ansiColors: IAnsiColors) {
        if (parameters.isNotEmpty()) {
            val mode = parameters[0]
            if (mode == 5 && parameters.size == 2) { // set 8-bit color
                StyleConstants.setForeground(attributes, get8BitColor(parameters[1], ansiColors))
            } else if (mode == 2) {
                StyleConstants.setForeground(attributes, get24BitColor(parameters))
            }
        }
    }

    private fun setBackgroundColor(attributes: MutableAttributeSet, parameters: List<Int>, ansiColors: IAnsiColors) {
        if (parameters.isNotEmpty()) {
            val mode = parameters[0]
            if (mode == 5 && parameters.size == 2) { // set 8-bit color
               StyleConstants.setBackground(attributes, get8BitColor(parameters[1], ansiColors))
            } else if (mode == 2) {
                StyleConstants.setBackground(attributes, get24BitColor(parameters))
            }
        }
    }

    private fun get8BitColor(colorCode: Int, ansiColors: IAnsiColors): Color {
        if (colorCode >= 232) {
            return getGrayScaleColor(colorCode)
        } else if (colorCode >= 16) {
            return get8BitColorFromColorCode(colorCode)
        }
        return AnsiColorIndex(ansiColors).colors[colorCode]
    }

    private fun get8BitColorFromColorCode(colorCode: Int): Color {
        var color = colorCode - 16

        val r = color / 36
        color -= r * 36
        val g = color / 6
        color -= g * 6
        val b = color

        return Color(r * 0x33, g * 0x33, b * 0x33)
    }

    private fun getGrayScaleColor(colorCode: Int): Color {
        val c = (colorCode - 232) * 10 + 13
        return Color(c, c, c)
    }

    private fun get24BitColor(parameters: List<Int>): Color {
        var r = 0
        var g = 0
        var b = 0
        if (parameters.size >= 2) {
            r = parameters[1]
        }
        if (parameters.size >= 3) {
            g = parameters[2]
        }
        if (parameters.size >= 4) {
            b = parameters[3]
        }
        return Color(r, g, b)
    }
}