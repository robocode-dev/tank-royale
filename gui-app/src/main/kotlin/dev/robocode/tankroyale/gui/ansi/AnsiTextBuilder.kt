package dev.robocode.tankroyale.gui.ansi

import java.lang.IllegalArgumentException

class AnsiTextBuilder {

    private val builder = StringBuilder()

    fun build(): String = builder.toString()

    fun text(plain: Any?): AnsiTextBuilder {
        builder.append(plain)
        return this
    }

    fun newline(): AnsiTextBuilder {
        builder.append("\n")
        return this
    }

    fun tab(count: Int = 1): AnsiTextBuilder {
        repeat("\t", count)
        return this
    }

    fun space(count: Int = 1): AnsiTextBuilder {
        repeat(" ", count)
        return this
    }

    private fun repeat(fragment: Any, count: Int) {
        require(count >= 0) { "count must be >= 0" }
        for (i in 1..count) {
            builder.append(fragment)
        }
    }

    fun esc(code: AnsiEscCode): AnsiTextBuilder {
        builder.append(code)
        return this
    }

    fun reset(): AnsiTextBuilder {
        builder.append(AnsiEscCode.RESET)
        return this
    }

    fun bold(enabled: Boolean = true): AnsiTextBuilder {
        builder.append(if (enabled) AnsiEscCode.BOLD else AnsiEscCode.NORMAL)
        return this
    }

    fun italic(): AnsiTextBuilder {
        builder.append(AnsiEscCode.ITALIC)
        return this
    }

    fun underline(): AnsiTextBuilder {
        builder.append(AnsiEscCode.UNDERLINE)
        return this
    }

    fun black(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BLACK)
        return this
    }

    fun red(): AnsiTextBuilder {
        builder.append(AnsiEscCode.RED)
        return this
    }

    fun green(): AnsiTextBuilder {
        builder.append(AnsiEscCode.GREEN)
        return this
    }

    fun yellow(): AnsiTextBuilder {
        builder.append(AnsiEscCode.YELLOW)
        return this
    }

    fun blue(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BLUE)
        return this
    }

    fun magenta(): AnsiTextBuilder {
        builder.append(AnsiEscCode.MAGENTA)
        return this
    }

    fun cyan(): AnsiTextBuilder {
        builder.append(AnsiEscCode.CYAN)
        return this
    }

    fun white(): AnsiTextBuilder {
        builder.append(AnsiEscCode.WHITE)
        return this
    }

    fun default(): AnsiTextBuilder {
        builder.append(AnsiEscCode.DEFAULT)
        return this
    }

    fun brightBlack(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_BLACK)
        return this
    }

    fun brightRed(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_RED)
        return this
    }

    fun brightGreen(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_GREEN)
        return this
    }

    fun brightYellow(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_YELLOW)
        return this
    }

    fun brightBlue(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_BLUE)
        return this
    }

    fun brightMagenta(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_MAGENTA)
        return this
    }

    fun brightCyan(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_CYAN)
        return this
    }

    fun brightWhite(): AnsiTextBuilder {
        builder.append(AnsiEscCode.BRIGHT_WHITE)
        return this
    }
}