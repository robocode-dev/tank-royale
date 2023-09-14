package dev.robocode.tankroyale.gui.ansi

enum class AnsiEscapeCode(val code: String) {
    RESET("\u001B[0m"),

    BOLD("\u001B[1m"),
    NOT_BOLD_OR_FAINT("\u001B[22m"),

    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),
    SET_COLOR("\u001B[38m"),
    DEFAULT("\u001B[39m"),
    ;

    override fun toString() = code
}