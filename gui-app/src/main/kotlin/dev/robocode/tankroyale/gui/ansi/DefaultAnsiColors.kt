package dev.robocode.tankroyale.gui.ansi

import java.awt.Color

/**
 * Default ANSI colors.
 */
object DefaultAnsiColors : IAnsiColors {
    override val black = Color(0x00, 0x00, 0x00)
    override val red = Color(0xaa, 0x00, 0x00)
    override val green = Color(0x00, 0xaa, 0x00)
    override val yellow = Color(0xaa, 0xaa, 0x00)
    override val blue = Color(0x00, 0x00, 0xaa)
    override val magenta = Color(0xaa, 0x00, 0xaa)
    override val cyan = Color(0x00, 0xaa, 0xaa)
    override val white = Color(0xaa, 0xaa, 0xaa)

    override val brightBlack = Color(0x55, 0x55, 0x55)
    override val brightRed = Color(0xff, 0x55, 0x55)
    override val brightGreen = Color(0x55, 0xff, 0x55)
    override val brightYellow = Color(0xff, 0xff, 0x55)
    override val brightBlue = Color(0x55, 0x55, 0xff)
    override val brightMagenta = Color(0xff, 0x55, 0xff)
    override val brightCyan = Color(0x55, 0xff, 0xff)
    override val brightWhite = Color(0xff, 0xff, 0xff)

    override val default = brightWhite
}
