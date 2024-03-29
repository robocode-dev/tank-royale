package dev.robocode.tankroyale.gui.ansi

import java.awt.Color

class AnsiColorIndex(colorScheme: IAnsiColors) {

    val colors: Array<Color> = arrayOf(
        // standard colors
        colorScheme.black,
        colorScheme.red,
        colorScheme.green,
        colorScheme.yellow,
        colorScheme.blue,
        colorScheme.magenta,
        colorScheme.cyan,
        colorScheme.white,

        // high-intensity colors
        colorScheme.brightBlack,
        colorScheme.brightRed,
        colorScheme.brightGreen,
        colorScheme.brightYellow,
        colorScheme.brightBlue,
        colorScheme.brightMagenta,
        colorScheme.brightCyan,
        colorScheme.brightWhite,
    )
}