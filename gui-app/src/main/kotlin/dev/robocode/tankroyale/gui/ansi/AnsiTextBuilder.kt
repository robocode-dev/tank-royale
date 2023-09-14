package dev.robocode.tankroyale.gui.ansi

import java.lang.IllegalArgumentException

class AnsiTextBuilder {

    private val builder = StringBuilder()

    override fun toString(): String = builder.toString()

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
        if (count < 1) {
            throw IllegalArgumentException("count must be > 0");
        }
        for (i in 1..count) {
            builder.append(fragment)
        }
    }

    fun esc(code: AnsiEscapeCode): AnsiTextBuilder {
        builder.append(code)
        return this
    }

    fun reset(): AnsiTextBuilder {
        builder.append(AnsiEscapeCode.RESET)
        return this
    }

    fun default(): AnsiTextBuilder {
        builder.append(AnsiEscapeCode.DEFAULT)
        return this
    }

    fun bold(enabled: Boolean = true): AnsiTextBuilder {
        builder.append(if (enabled) AnsiEscapeCode.BOLD else AnsiEscapeCode.NOT_BOLD_OR_FAINT)
        return this
    }

    fun green(): AnsiTextBuilder {
        builder.append(AnsiEscapeCode.GREEN)
        return this
    }
}