package dev.robocode.tankroyale.gui.ansi

enum class AnsiColorOld(val offset: Int, val cssColor: String?) {
    BLACK(0, "#000000"),
    RED(1, "#AA0000"),
    GREEN(2, "#00AA00"),
    YELLOW(3, "#AAAA00"),
    BLUE(4, "#0000AA"),
    MAGENTA(5, "#AA00AA"),
    CYAN(6, "#00AAAA"),
    WHITE(7, "#AAAAAA"),
    SET_COLOR(8, null),
    DEFAULT(9, null);

    companion object {
        infix fun webColorFrom(offset: Int): String? =
            entries.firstOrNull { it.offset == offset }?.cssColor
    }
}

enum class BrightAnsiColorOld(val offset: Int, val cssColor: String?) {
    BRIGHT_BLACK(0, "#555555"),
    BRIGHT_RED(1, "#FF5555"),
    BRIGHT_GREEN(2, "#55FF55"),
    BRIGHT_YELLOW(3, "#FFFF55"),
    BRIGHT_BLUE(4, "#5555FF"),
    BRIGHT_MAGENTA(5, "#FF55FF"),
    BRIGHT_CYAN(6, "#55FFFF"),
    BRIGHT_WHITE(7, "#FFFFFF");

    companion object {
        infix fun webColorFrom(offset: Int): String? =
            entries.firstOrNull { it.offset == offset }?.cssColor
    }
}