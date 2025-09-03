package dev.robocode.tankroyale.gui.ansi.esc_code

/**
 * Encapsulates an escape code command for ANSI text colors and styles.
 * Read this [Wikipedia page](https://en.wikipedia.org/wiki/ANSI_escape_code) to learn mode about ANSI escape codes.
 *
 * Note that only a subset of the escape codes are currently supported. But The most common console colors and styles
 * are currently supported.
 *
 * @param commandCode is the encapsulated escape command code.
 */
enum class CommandCode(val commandCode: Int) {
    /** All attributes become turned off */
    RESET(0),

    /** Bold (increased intensity) */
    BOLD(1),

    /** Faint (decreased intensity) */
    FAINT(2),

    /** Italic */
    ITALIC(3),

    /** Underline */
    UNDERLINE(4),

    /** Not bold */
    NOT_BOLD(21),  // Double underlined on some systems

    /** Normal intensity (neither bold nor faint) */
    NORMAL(22),

    /** Not italic */
    NOT_ITALIC(23),  // nor black letter

    /** Not underlined */
    NOT_UNDERLINED(24),

    /** Black foreground color */
    BLACK(30),

    /** Red foreground color */
    RED(31),

    /** Green foreground color */
    GREEN(32),

    /** Yellow foreground color */
    YELLOW(33),

    /** Blue foreground color */
    BLUE(34),

    /** Magenta foreground color */
    MAGENTA(35),

    /** Cyan foreground color */
    CYAN(36),

    /** White foreground color */
    WHITE(37),

    /** Set foreground color */
    SET_FOREGROUND_COLOR(38),

    /** Default foreground color */
    DEFAULT(39),

    /** Black background color */
    BLACK_BACKGROUND(40),

    /** Red background color */
    RED_BACKGROUND(41),

    /** Green background color */
    GREEN_BACKGROUND(42),

    /** Yellow background color */
    YELLOW_BACKGROUND(43),

    /** Blue background color */
    BLUE_BACKGROUND(44),

    /** Magenta background color */
    MAGENTA_BACKGROUND(45),

    /** Cyan background color */
    CYAN_BACKGROUND(46),

    /** White background color */
    WHITE_BACKGROUND(47),

    /** Set background color */
    SET_BACKGROUND_COLOR(48),

    /** Default background color */
    DEFAULT_BACKGROUND(49),

    /** Bright black foreground color */
    BRIGHT_BLACK(90),

    /** Bright red foreground color */
    BRIGHT_RED(91),

    /** Bright green foreground color */
    BRIGHT_GREEN(92),

    /** Bright yellow foreground color */
    BRIGHT_YELLOW(93),

    /** Bright blue foreground color */
    BRIGHT_BLUE(94),

    /** Bright magenta foreground color */
    BRIGHT_MAGENTA(95),

    /** Bright cyan foreground color */
    BRIGHT_CYAN(96),

    /** Bright white foreground color */
    BRIGHT_WHITE(97),

    /** Bright black background color */
    BRIGHT_BLACK_BACKGROUND(100),

    /** Bright red background color */
    BRIGHT_RED_BACKGROUND(101),

    /** Bright green background color */
    BRIGHT_GREEN_BACKGROUND(102),

    /** Bright yellow background color */
    BRIGHT_YELLOW_BACKGROUND(103),

    /** Bright blue background color */
    BRIGHT_BLUE_BACKGROUND(104),

    /** Bright magenta background color */
    BRIGHT_MAGENTA_BACKGROUND(105),

    /** Bright cyan background color */
    BRIGHT_CYAN_BACKGROUND(106),

    /** Bright white background color */
    BRIGHT_WHITE_BACKGROUND(107);

    companion object {
        /**
         * Creates a [CommandCode] from a command code integer.
         * @param commandCode is the command code.
         */
        fun fromCode(commandCode: Int): CommandCode =
            entries.firstOrNull { it.commandCode == commandCode }
                ?: throw UnsupportedOperationException("commandCode not supported: $commandCode")
    }
}