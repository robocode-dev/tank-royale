package dev.robocode.tankroyale.gui.ansi

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
     *
     * @return a text string containing ANSI Escape Codes.
     */
    fun build(): String = builder.toString()

    /**
     * Appends plain text to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     *
     * @param plain a plain text string.
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
     * @return an instance of this builder used for chaining methods.
     *
     * @param count is the number of tab characters to append. Default is 1.
     */
    fun tab(count: Int = 1): AnsiTextBuilder {
        repeat("\t", count)
        return this
    }

    /**
     * Appends space character(s) to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     *
     * @param count is the number of space characters to append. Default is 1.
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
     * Appends a [AnsiEscCode] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     *
     * @param escCode is the [AnsiEscCode] to append.
     */
    fun esc(escCode: AnsiEscCode): AnsiTextBuilder {
        builder.append(escCode)
        return this
    }

    /**
     * Appends a [AnsiEscCode.RESET] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun reset(): AnsiTextBuilder {
        builder.append(AnsiEscCode.RESET)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BOLD] or [AnsiEscCode.NOT_BOLD] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     *
     * @param enabled `true` to append [AnsiEscCode.BOLD]; `false` to append [AnsiEscCode.NOT_BOLD]. Default is `true`.
     */
    fun bold(enabled: Boolean = true): AnsiTextBuilder {
        builder.append(if (enabled) AnsiEscCode.BOLD else AnsiEscCode.NOT_BOLD)
        return this
    }

    /**
     * Appends a [AnsiEscCode.FAINT] or [AnsiEscCode.NORMAL] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     *
     * @param enabled `true` to append [AnsiEscCode.FAINT]; `false` to append [AnsiEscCode.NORMAL]. Default is `true`.
     */
    fun faint(enabled: Boolean = true): AnsiTextBuilder {
        builder.append(if (enabled) AnsiEscCode.FAINT else AnsiEscCode.NORMAL)
        return this
    }

    /**
     * Appends a [AnsiEscCode.ITALIC] or [AnsiEscCode.NOT_ITALIC] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     *
     * @param enabled `true` to append [AnsiEscCode.ITALIC]; `false` to append [AnsiEscCode.NOT_ITALIC]. Default is `true`.
     */
    fun italic(enabled: Boolean = true): AnsiTextBuilder {
        builder.append(if (enabled) AnsiEscCode.ITALIC else AnsiEscCode.NOT_ITALIC)
        return this
    }

    /**
     * Appends a [AnsiEscCode.UNDERLINE] or [AnsiEscCode.NOT_UNDERLINED] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     *
     * @param enabled `true` to append [AnsiEscCode.UNDERLINE]; `false` to append [AnsiEscCode.NOT_UNDERLINED]. Default is `true`.
     */
    fun underline(enabled: Boolean = true): AnsiTextBuilder {
        builder.append(if (enabled) AnsiEscCode.UNDERLINE else AnsiEscCode.NOT_UNDERLINED)
        return this
    }

    /**
     * Appends a [AnsiEscCode.NORMAL] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun normal(): AnsiTextBuilder {
        builder.append(AnsiEscCode.NORMAL)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BLACK] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun black(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BLACK)
        return this
    }

    /**
     * Appends a [AnsiEscCode.RED] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun red(): AnsiTextBuilder {
        builder.append(AnsiEscCode.RED)
        return this
    }

    /**
     * Appends a [AnsiEscCode.GREEN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun green(): AnsiTextBuilder {
        builder.append(AnsiEscCode.GREEN)
        return this
    }

    /**
     * Appends a [AnsiEscCode.YELLOW] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun yellow(): AnsiTextBuilder {
        builder.append(AnsiEscCode.YELLOW)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BLUE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun blue(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BLUE)
        return this
    }

    /**
     * Appends a [AnsiEscCode.MAGENTA] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun magenta(): AnsiTextBuilder {
        builder.append(AnsiEscCode.MAGENTA)
        return this
    }

    /**
     * Appends a [AnsiEscCode.CYAN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun cyan(): AnsiTextBuilder {
        builder.append(AnsiEscCode.CYAN)
        return this
    }

    /**
     * Appends a [AnsiEscCode.WHITE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun white(): AnsiTextBuilder {
        builder.append(AnsiEscCode.WHITE)
        return this
    }

    /**
     * Appends a [AnsiEscCode.DEFAULT] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun defaultColor(): AnsiTextBuilder {
        builder.append(AnsiEscCode.DEFAULT)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_BLACK] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlack(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_BLACK)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_RED] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightRed(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_RED)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_GREEN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightGreen(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_GREEN)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_YELLOW] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightYellow(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_YELLOW)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_BLUE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlue(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_BLUE)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_MAGENTA] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightMagenta(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_MAGENTA)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_CYAN] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightCyan(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_CYAN)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_WHITE] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightWhite(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_WHITE)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BLACK_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun blackBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BLACK_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.RED_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun redBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.RED_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.GREEN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun greenBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.GREEN_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.YELLOW_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun yellowBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.YELLOW_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BLUE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun blueBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BLUE_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.MAGENTA_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun magentaBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.MAGENTA_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.CYAN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun cyanBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.CYAN_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.WHITE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun whiteBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.WHITE_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.DEFAULT_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun defaultBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.DEFAULT_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_BLACK_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlackBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_BLACK_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_RED_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightRedBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_RED_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_GREEN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightGreenBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_GREEN_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_YELLOW_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightYellowBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_YELLOW_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_BLUE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightBlueBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_BLUE_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_MAGENTA_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightMagentaBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_MAGENTA_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_CYAN_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightCyanBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_CYAN_BACKGROUND)
        return this
    }

    /**
     * Appends a [AnsiEscCode.BRIGHT_WHITE_BACKGROUND] to the ANSI string.
     * @return an instance of this builder used for chaining methods.
     */
    fun brightWhiteBg(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_WHITE_BACKGROUND)
        return this
    }
}