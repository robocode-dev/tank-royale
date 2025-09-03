package dev.robocode.tankroyale.gui.ansi

import dev.robocode.tankroyale.gui.ansi.esc_code.CommandCode
import dev.robocode.tankroyale.gui.ansi.esc_code.EscapeSequence

/**
 * The AnsiTextBuilder is builder for building ANSI texts using a
 * [Fluent Interface](https://java-design-patterns.com/patterns/fluentinterface/).
 *
 * Example of how to use the AnsiTextBuilder:
 * <pre>
 *     val ansiTextBuilder = AnsiTextBuilder()
 *         .bold().red().text("bold").newline()
 *     editorPane.text = ansiTextBuilder.build()
 * </pre>
 */
class AnsiTextBuilder {

    private val builder = StringBuilder()

    /**
     * Builds a text string containing ANSI Escape Codes based on the methods being called on the builder.
     * This method is the last method to call, when building the ANSI text is done.
     * @return a text string containing ANSI Escape Codes.
     */
    fun build(): String = builder.toString()

    /**
     * Appends plain text to the ANSI string.
     * @param plain a plain text string.
     * @return an instance of this builder used for chaining methods.
     */
    fun text(plain: Any?): AnsiTextBuilder {
        builder.append(plain)
        return this
    }

    /**
     * Appends a new-line character to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun newline(): AnsiTextBuilder {
        builder.append("\n")
        return this
    }

    /**
     * Appends tab character(s) to the ANSI string.
     * @param count is the number of tab characters to append. Default is 1.
     * @return an instance of this builder used for chaining methods.
     */
    fun tab(count: Int = 1): AnsiTextBuilder {
        repeat("\t", count)
        return this
    }

    /**
     * Appends space character(s) to the ANSI string.
     * @param count is the number of space characters to append. Default is 1.
     * @return an instance of this builder used for chaining methods.
     */
    fun space(count: Int = 1): AnsiTextBuilder {
        repeat(" ", count)
        return this
    }

    private fun repeat(fragment: Any, count: Int) {
        require(count >= 0) { "count must be >= 0" }
        builder.append(fragment.toString().repeat(count))
    }

    /**
     * Appends a [EscapeSequence] to the ANSI string.
     * @param escSequence is the [EscapeSequence] to append.
     * @return an instance of this builder used for chaining methods.
     */
    fun esc(escSequence: EscapeSequence): AnsiTextBuilder {
        builder.append(escSequence)
        return this
    }

    /**
     * Appends a [CommandCode] to the ANSI string.
     * @param escCode is the [CommandCode] to append.
     * @return an instance of this builder used for chaining methods.
     */
    private fun esc(escCode: CommandCode): AnsiTextBuilder = esc(EscapeSequence(escCode))

    /**
     * Appends a [CommandCode.RESET] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun reset(): AnsiTextBuilder = esc(CommandCode.RESET)

    /**
     * Appends a [CommandCode.BOLD] or [CommandCode.NOT_BOLD] to the ANSI string.
     * @param enabled `true` to append [CommandCode.BOLD]; `false` to append [CommandCode.NOT_BOLD]. Default is `true`.
     * @return an instance of this builder used for chaining methods.
     */
    fun bold(enabled: Boolean = true): AnsiTextBuilder = esc(if (enabled) CommandCode.BOLD else CommandCode.NOT_BOLD)

    /**
     * Appends a [CommandCode.FAINT] or [CommandCode.NORMAL] to the ANSI string.
     * @param enabled `true` to append [CommandCode.FAINT]; `false` to append [CommandCode.NORMAL]. Default is `true`.
     * @return an instance of this builder used for chaining methods.
     */
    fun faint(enabled: Boolean = true): AnsiTextBuilder = esc(if (enabled) CommandCode.FAINT else CommandCode.NORMAL)

    /**
     * Appends a [CommandCode.ITALIC] or [CommandCode.NOT_ITALIC] to the ANSI string.
     * @param enabled `true` to append [CommandCode.ITALIC]; `false` to append [CommandCode.NOT_ITALIC]. Default is `true`.
     * @return an instance of this builder used for chaining methods.
     */
    fun italic(enabled: Boolean = true): AnsiTextBuilder =
        esc(if (enabled) CommandCode.ITALIC else CommandCode.NOT_ITALIC)

    /**
     * Appends a [CommandCode.UNDERLINE] or [CommandCode.NOT_UNDERLINED] to the ANSI string.
     * @param enabled `true` to append [CommandCode.UNDERLINE]; `false` to append [CommandCode.NOT_UNDERLINED]. Default is `true`.
     * @return an instance of this builder used for chaining methods.
     */
    fun underline(enabled: Boolean = true): AnsiTextBuilder =
        esc(if (enabled) CommandCode.UNDERLINE else CommandCode.NOT_UNDERLINED)

    /**
     * Appends a [CommandCode.NORMAL] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun normal(): AnsiTextBuilder = esc(CommandCode.NORMAL)

    /**
     * Appends a [CommandCode.BLACK] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun black(): AnsiTextBuilder = esc(CommandCode.BLACK)

    /**
     * Appends a [CommandCode.RED] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun red(): AnsiTextBuilder = esc(CommandCode.RED)

    /**
     * Appends a [CommandCode.GREEN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun green(): AnsiTextBuilder = esc(CommandCode.GREEN)

    /**
     * Appends a [CommandCode.YELLOW] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun yellow(): AnsiTextBuilder = esc(CommandCode.YELLOW)

    /**
     * Appends a [CommandCode.BLUE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun blue(): AnsiTextBuilder = esc(CommandCode.BLUE)

    /**
     * Appends a [CommandCode.MAGENTA] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun magenta(): AnsiTextBuilder = esc(CommandCode.MAGENTA)

    /**
     * Appends a [CommandCode.CYAN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun cyan(): AnsiTextBuilder = esc(CommandCode.CYAN)

    /**
     * Appends a [CommandCode.WHITE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun white(): AnsiTextBuilder = esc(CommandCode.WHITE)

    /**
     * Appends a [CommandCode.DEFAULT] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun defaultColor(): AnsiTextBuilder = esc(CommandCode.DEFAULT)

    /**
     * Appends a [CommandCode.BRIGHT_BLACK] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlack(): AnsiTextBuilder = esc(CommandCode.BRIGHT_BLACK)

    /**
     * Appends a [CommandCode.BRIGHT_RED] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightRed(): AnsiTextBuilder = esc(CommandCode.BRIGHT_RED)

    /**
     * Appends a [CommandCode.BRIGHT_GREEN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightGreen(): AnsiTextBuilder = esc(CommandCode.BRIGHT_GREEN)

    /**
     * Appends a [CommandCode.BRIGHT_YELLOW] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightYellow(): AnsiTextBuilder = esc(CommandCode.BRIGHT_YELLOW)

    /**
     * Appends a [CommandCode.BRIGHT_BLUE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlue(): AnsiTextBuilder = esc(CommandCode.BRIGHT_BLUE)

    /**
     * Appends a [CommandCode.BRIGHT_MAGENTA] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightMagenta(): AnsiTextBuilder = esc(CommandCode.BRIGHT_MAGENTA)

    /**
     * Appends a [CommandCode.BRIGHT_CYAN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightCyan(): AnsiTextBuilder = esc(CommandCode.BRIGHT_CYAN)

    /**
     * Appends a [CommandCode.BRIGHT_WHITE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightWhite(): AnsiTextBuilder = esc(CommandCode.BRIGHT_WHITE)

    /**
     * Appends a [CommandCode.BLACK_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun blackBg(): AnsiTextBuilder = esc(CommandCode.BLACK_BACKGROUND)

    /**
     * Appends a [CommandCode.RED_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun redBg(): AnsiTextBuilder = esc(CommandCode.RED_BACKGROUND)

    /**
     * Appends a [CommandCode.GREEN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun greenBg(): AnsiTextBuilder = esc(CommandCode.GREEN_BACKGROUND)

    /**
     * Appends a [CommandCode.YELLOW_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun yellowBg(): AnsiTextBuilder = esc(CommandCode.YELLOW_BACKGROUND)

    /**
     * Appends a [CommandCode.BLUE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun blueBg(): AnsiTextBuilder = esc(CommandCode.BLUE_BACKGROUND)

    /**
     * Appends a [CommandCode.MAGENTA_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun magentaBg(): AnsiTextBuilder = esc(CommandCode.MAGENTA_BACKGROUND)

    /**
     * Appends a [CommandCode.CYAN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun cyanBg(): AnsiTextBuilder = esc(CommandCode.CYAN_BACKGROUND)

    /**
     * Appends a [CommandCode.WHITE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun whiteBg(): AnsiTextBuilder = esc(CommandCode.WHITE_BACKGROUND)

    /**
     * Appends a [CommandCode.DEFAULT_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun defaultBg(): AnsiTextBuilder = esc(CommandCode.DEFAULT_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_BLACK_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlackBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_BLACK_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_RED_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightRedBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_RED_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_GREEN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightGreenBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_GREEN_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_YELLOW_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightYellowBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_YELLOW_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_BLUE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlueBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_BLUE_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_MAGENTA_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightMagentaBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_MAGENTA_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_CYAN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightCyanBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_CYAN_BACKGROUND)

    /**
     * Appends a [CommandCode.BRIGHT_WHITE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightWhiteBg(): AnsiTextBuilder = esc(CommandCode.BRIGHT_WHITE_BACKGROUND)

    /**
     * Appends an 8-bit foreground color in 6x6x6 RGB format to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     * @param red is the red color component, where 0 <= red <= 5
     * @param green is the green color component, where 0 <= green <= 5
     * @param blue is the blue color component, where 0 <= blue <= 5
     */
    fun setColor8bit(red: Int, green: Int, blue: Int): AnsiTextBuilder {
        require(red in 0..5) { "red must be a value from 0 to 5" }
        require(green in 0..5) { "green must be a value from 0 to 5" }
        require(blue in 0..5) { "blue must be a value from 0 to 5" }
        return esc(EscapeSequence(CommandCode.SET_FOREGROUND_COLOR, 5, 16 + red * 36 + green * 6 + blue))
    }

    /**
     * Appends an 8-bit background color in 6x6x6 RGB format to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     * @param red is the red color component, where 0 <= red <= 5
     * @param green is the green color component, where 0 <= green <= 5
     * @param blue is the blue color component, where 0 <= blue <= 5
     */
    fun setBgColor8bit(red: Int, green: Int, blue: Int): AnsiTextBuilder {
        require(red in 0..5) { "red must be a value from 0 to 5" }
        require(green in 0..5) { "green must be a value from 0 to 5" }
        require(blue in 0..5) { "blue must be a value from 0 to 5" }
        return esc(EscapeSequence(CommandCode.SET_BACKGROUND_COLOR, 5, 16 + red * 36 + green * 6 + blue))
    }

    /**
     * Appends a grayscale foreground color to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     * @param grayScale is the grayscale scale, where 0 <= grayScale <= 23, and where 0 is darkest and 23 is lightest.
     */
    fun setGrayscaleColor(grayScale: Int): AnsiTextBuilder {
        require(grayScale in 0..23) { "grayScale must be a value from 0 to 23" }
        return esc(EscapeSequence(CommandCode.SET_FOREGROUND_COLOR, 5, 232 + grayScale))
    }

    /**
     * Appends a grayscale background color to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     * @param grayScale is the grayscale scale, where 0 <= grayScale <= 23, and where 0 is darkest and 23 is lightest.
     */
    fun setGrayscaleBgColor(grayScale: Int): AnsiTextBuilder {
        require(grayScale in 0..23) { "grayScale must be a value from 0 to 23" }
        return esc(EscapeSequence(CommandCode.SET_BACKGROUND_COLOR, 5, 232 + grayScale))
    }

    /**
     * Appends an 24-bit foreground color to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     * @param red red color component, where 0 <= red <= 255
     * @param green green color component, where 0 <= green <= 255
     * @param blue blue color component, where 0 <= blue <= 255
     */
    fun setColor(red: Int, green: Int, blue: Int): AnsiTextBuilder =
        esc(EscapeSequence(CommandCode.SET_FOREGROUND_COLOR, 2, red, green, blue))

    /**
     * Appends an 24-bit foreground color to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     * @param red red color component, where 0 <= red <= 255
     * @param green green color component, where 0 <= green <= 255
     * @param blue blue color component, where 0 <= blue <= 255
     */
    fun setBgColor(red: Int, green: Int, blue: Int): AnsiTextBuilder =
        esc(EscapeSequence(CommandCode.SET_BACKGROUND_COLOR, 2, red, green, blue))
}