package dev.robocode.tankroyale.gui.ansi

import java.lang.IllegalStateException

/** [ANSI Escape Code](https://en.wikipedia.org/wiki/ANSI_escape_code) */
enum class AnsiEscCode(/* public */ val code: String) {
    RESET("\u001b[0m"),

    BOLD("\u001b[1m"),
    FAINT("\u001b[2m"),
    ITALIC("\u001b[3m"),
    UNDERLINE("\u001b[4m"),

    NOT_BOLD("\u001b[21m"),    // Double underlined on some systems
    NORMAL("\u001b[22m"),     // Neither bold nor fains
    NOT_ITALIC("\u001b[23m"), // Neither italic, nor black letter
    NOT_UNDERLINED("\u001b[24m"),

    BLACK("\u001b[30m"),
    RED("\u001b[31m"),
    GREEN("\u001b[32m"),
    YELLOW("\u001b[33m"),
    BLUE("\u001b[34m"),
    MAGENTA("\u001b[35m"),
    CYAN("\u001b[36m"),
    WHITE("\u001b[37m"),

    //    SET_COLOR("\u001b[38m"),
    DEFAULT("\u001b[39m"),

    BRIGHT_BLACK("\u001b[90m"),
    BRIGHT_RED("\u001b[91m"),
    BRIGHT_GREEN("\u001b[92m"),
    BRIGHT_YELLOW("\u001b[93m"),
    BRIGHT_BLUE("\u001b[94m"),
    BRIGHT_MAGENTA("\u001b[95m"),
    BRIGHT_CYAN("\u001b[96m"),
    BRIGHT_WHITE("\u001b[97m"),
    ;

    override fun toString() = code

    companion object {
        fun fromCode(code: String): AnsiEscCode = entries.firstOrNull { it.code == code }
            ?: throw IllegalStateException("No enum entry is defined for escape code '${code.replace("\u001b", "")}'")
    }
}