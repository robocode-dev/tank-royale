package dev.robocode.tankroyale.gui.ansi

import java.awt.Color

/**
 * ANSI colors tuned for a light-background console.
 *
 * Every color that would be invisible or barely readable on a white/off-white background
 * (bright yellow, bright cyan, bright white, and the default foreground) is replaced with
 * a darker, high-contrast equivalent.  All other hues keep the same character as the dark
 * palette but are shifted to work on light surfaces.
 */
object LightAnsiColors : IAnsiColors {
    override val black = Color(0x3d, 0x3d, 0x3d)
    override val red = Color(0xc0, 0x39, 0x2b)
    override val green = Color(0x27, 0xae, 0x60)
    override val yellow = Color(0xb7, 0x95, 0x0b)       // dark goldenrod – visible on white
    override val blue = Color(0x24, 0x71, 0xa3)
    override val magenta = Color(0x7d, 0x3c, 0x98)
    override val cyan = Color(0x1a, 0x8a, 0x8a)         // dark teal – visible on white
    override val white = Color(0x9e, 0x9e, 0x9e)

    override val brightBlack = Color(0x7f, 0x7f, 0x7f)
    override val brightRed = Color(0xe7, 0x4c, 0x3c)
    override val brightGreen = Color(0x2e, 0xcc, 0x71)
    override val brightYellow = Color(0xd4, 0xa0, 0x17) // golden amber – replaces invisible #ffff55
    override val brightBlue = Color(0x34, 0x98, 0xdb)
    override val brightMagenta = Color(0x9b, 0x59, 0xb6)
    override val brightCyan = Color(0x1a, 0xbc, 0x9c)   // turquoise – replaces invisible #55ffff
    override val brightWhite = Color(0x9e, 0x9e, 0x9e)  // medium gray – replaces invisible #ffffff

    override val default = Color(0x1a, 0x1a, 0x2e)      // near-black – matches RobocodeLightColors.FOREGROUND
}
