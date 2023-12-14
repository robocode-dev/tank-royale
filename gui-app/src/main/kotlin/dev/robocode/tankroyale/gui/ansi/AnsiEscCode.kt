package dev.robocode.tankroyale.gui.ansi

import java.lang.IllegalStateException

/**
 * Encapsulates an escape code for ANSI colors and styles.
 * Read this [Wikipedia page](https://en.wikipedia.org/wiki/ANSI_escape_code) to learn mode about ANSI escape codes.
 *
 * Note that only a subset of the escape codes are currently supported. The most common console colors and styles are
 * currently supported.
 *
 * @param escCode is the Escape Code to encapsulate.
 */
enum class AnsiEscCode(val escCode: String) {
    /** All attributes become turned off */
    RESET("\u001b[0m"),

    /** Bold (increased intensity) */
    BOLD("\u001b[1m"),

    /** Faint (decreased intensity) */
    FAINT("\u001b[2m"),

    /** Italic */
    ITALIC("\u001b[3m"),

    /** Underline */
    UNDERLINE("\u001b[4m"),

    /** Not bold */
    NOT_BOLD("\u001b[21m"), // Double underlined on some systems

    /** Normal intensity (neither bold nor fains) */
    NORMAL("\u001b[22m"),

    /** Not italic */
    NOT_ITALIC("\u001b[23m"), // nor black letter

    /** Not underlined */
    NOT_UNDERLINED("\u001b[24m"),

    /** Black foreground color */
    BLACK("\u001b[30m"),

    /** Red foreground color */
    RED("\u001b[31m"),

    /** Green foreground color */
    GREEN("\u001b[32m"),

    /** Yellow foreground color */
    YELLOW("\u001b[33m"),

    /** Blue foreground color */
    BLUE("\u001b[34m"),

    /** Magenta foreground color */
    MAGENTA("\u001b[35m"),

    /** Cyan foreground color */
    CYAN("\u001b[36m"),

    /** White foreground color */
    WHITE("\u001b[37m"),

    /** Default foreground color */
    DEFAULT("\u001b[39m"),

    /** Bright black foreground color */
    BRIGHT_BLACK("\u001b[90m"),

    /** Bright red foreground color */
    BRIGHT_RED("\u001b[91m"),

    /** Bright green foreground color */
    BRIGHT_GREEN("\u001b[92m"),

    /** Bright yellow foreground color */
    BRIGHT_YELLOW("\u001b[93m"),

    /** Bright blue foreground color */
    BRIGHT_BLUE("\u001b[94m"),

    /** Bright magenta foreground color */
    BRIGHT_MAGENTA("\u001b[95m"),

    /** Bright cyan foreground color */
    BRIGHT_CYAN("\u001b[96m"),

    /** Bright white foreground color */
    BRIGHT_WHITE("\u001b[97m"),

    /** Black background color */
    BLACK_BACKGROUND("\u001b[40m"),

    /** Red background color */
    RED_BACKGROUND("\u001b[41m"),

    /** Green background color */
    GREEN_BACKGROUND("\u001b[42m"),

    /** Yellow background color */
    YELLOW_BACKGROUND("\u001b[43m"),

    /** Blue background color */
    BLUE_BACKGROUND("\u001b[44m"),

    /** Magenta background color */
    MAGENTA_BACKGROUND("\u001b[45m"),

    /** Cyan background color */
    CYAN_BACKGROUND("\u001b[46m"),

    /** White background color */
    WHITE_BACKGROUND("\u001b[47m"),

    /** Default background color */
    DEFAULT_BACKGROUND("\u001b[49m"),

    /** Bright black background color */
    BRIGHT_BLACK_BACKGROUND("\u001b[100m"),

    /** Bright red background color */
    BRIGHT_RED_BACKGROUND("\u001b[101m"),

    /** Bright green background color */
    BRIGHT_GREEN_BACKGROUND("\u001b[102m"),

    /** Bright yellow background color */
    BRIGHT_YELLOW_BACKGROUND("\u001b[103m"),

    /** Bright blue background color */
    BRIGHT_BLUE_BACKGROUND("\u001b[104m"),

    /** Bright magenta background color */
    BRIGHT_MAGENTA_BACKGROUND("\u001b[105m"),

    /** Bright cyan background color */
    BRIGHT_CYAN_BACKGROUND("\u001b[106m"),

    /** Bright white background color */
    BRIGHT_WHITE_BACKGROUND("\u001b[107m"),
    ;

    /** {@inheritCode} */
    override fun toString() = escCode

    companion object {
        /**
         * Returns a [AnsiEscCode] instance based on an escape code.
         *
         * @return a [AnsiEscCode] instance based on an escape code.
         * @throws IllegalStateException if no [AnsiEscCode] exists for the escape code.
         */
        fun fromEscCode(escCode: String): AnsiEscCode = entries.firstOrNull { it.escCode == escCode }
            ?: throw IllegalStateException(
                "No enum entry is defined for escape code '${escCode.replace("\u001b", "")}'"
            )
    }
}