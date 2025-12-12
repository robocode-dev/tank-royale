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

    /**
     * Updates styling by processing a full SGR parameter list, e.g., [1, 38, 5, 21],
     * applying each command in sequence (bold, then 256-color foreground blue in this example).
     */
    fun MutableAttributeSet.updateAnsiParams(params: List<Int>, ansiColors: IAnsiColors): MutableAttributeSet {
        var attributes: MutableAttributeSet = SimpleAttributeSet(this)
        var i = 0
        while (i < params.size) {
            when (val code = params[i]) {
                0 -> {
                    attributes = SimpleAttributeSet()
                }

                1 -> StyleConstants.setBold(attributes, true)
                2 -> StyleConstants.setBold(attributes, false) // faint -> approximate as not bold
                3 -> StyleConstants.setItalic(attributes, true)
                4 -> StyleConstants.setUnderline(attributes, true)
                21, 22 -> StyleConstants.setBold(attributes, false)
                23 -> StyleConstants.setItalic(attributes, false)
                24 -> StyleConstants.setUnderline(attributes, false)
                30 -> StyleConstants.setForeground(attributes, ansiColors.black)
                31 -> StyleConstants.setForeground(attributes, ansiColors.red)
                32 -> StyleConstants.setForeground(attributes, ansiColors.green)
                33 -> StyleConstants.setForeground(attributes, ansiColors.yellow)
                34 -> StyleConstants.setForeground(attributes, ansiColors.blue)
                35 -> StyleConstants.setForeground(attributes, ansiColors.magenta)
                36 -> StyleConstants.setForeground(attributes, ansiColors.cyan)
                37 -> StyleConstants.setForeground(attributes, ansiColors.white)
                39 -> StyleConstants.setForeground(attributes, ansiColors.default)
                40 -> StyleConstants.setBackground(attributes, ansiColors.black)
                41 -> StyleConstants.setBackground(attributes, ansiColors.red)
                42 -> StyleConstants.setBackground(attributes, ansiColors.green)
                43 -> StyleConstants.setBackground(attributes, ansiColors.yellow)
                44 -> StyleConstants.setBackground(attributes, ansiColors.blue)
                45 -> StyleConstants.setBackground(attributes, ansiColors.magenta)
                46 -> StyleConstants.setBackground(attributes, ansiColors.cyan)
                47 -> StyleConstants.setBackground(attributes, ansiColors.white)
                49 -> StyleConstants.setBackground(attributes, ansiColors.default)
                in 90..97 -> { // bright foreground
                    val color = when (code) {
                        90 -> ansiColors.brightBlack
                        91 -> ansiColors.brightRed
                        92 -> ansiColors.brightGreen
                        93 -> ansiColors.brightYellow
                        94 -> ansiColors.brightBlue
                        95 -> ansiColors.brightMagenta
                        96 -> ansiColors.brightCyan
                        else -> ansiColors.brightWhite
                    }
                    StyleConstants.setForeground(attributes, color)
                }

                in 100..107 -> { // bright background
                    val color = when (code) {
                        100 -> ansiColors.brightBlack
                        101 -> ansiColors.brightRed
                        102 -> ansiColors.brightGreen
                        103 -> ansiColors.brightYellow
                        104 -> ansiColors.brightBlue
                        105 -> ansiColors.brightMagenta
                        106 -> ansiColors.brightCyan
                        else -> ansiColors.brightWhite
                    }
                    StyleConstants.setBackground(attributes, color)
                }

                38 -> { // extended foreground color
                    if (i + 1 < params.size) {
                        val mode = params[i + 1]
                        if (mode == 5 && i + 2 < params.size) {
                            StyleConstants.setForeground(attributes, get8BitColor(params[i + 2], ansiColors))
                            i += 2
                        } else if (mode == 2 && i + 4 < params.size) {
                            StyleConstants.setForeground(attributes, Color(params[i + 2], params[i + 3], params[i + 4]))
                            i += 4
                        }
                    }
                }

                48 -> { // extended background color
                    if (i + 1 < params.size) {
                        val mode = params[i + 1]
                        if (mode == 5 && i + 2 < params.size) {
                            StyleConstants.setBackground(attributes, get8BitColor(params[i + 2], ansiColors))
                            i += 2
                        } else if (mode == 2 && i + 4 < params.size) {
                            StyleConstants.setBackground(attributes, Color(params[i + 2], params[i + 3], params[i + 4]))
                            i += 4
                        }
                    }
                }
            }
            i++
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