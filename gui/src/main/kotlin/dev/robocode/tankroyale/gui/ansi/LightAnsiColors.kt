package dev.robocode.tankroyale.gui.ansi

import java.awt.Color

/**
 * ANSI colors tuned for a light-background console.
 *
 * All colors — including "bright" variants — are darkened enough to provide
 * sufficient contrast against a white/off-white background (WCAG AA ≥ 4.5:1).
 */
object LightAnsiColors : IAnsiColors {
    override val black       = Color(0x3d, 0x3d, 0x3d)
    override val red         = Color(0xb0, 0x30, 0x30)
    override val green       = Color(0x1a, 0x6b, 0x30)
    override val yellow      = Color(0x8a, 0x60, 0x00)
    override val blue        = Color(0x1a, 0x52, 0x76)
    override val magenta     = Color(0x6c, 0x31, 0x82)
    override val cyan        = Color(0x0e, 0x66, 0x55)
    override val white       = Color(0x7f, 0x7f, 0x7f)

    override val brightBlack   = Color(0x55, 0x55, 0x55)
    override val brightRed     = Color(0xc0, 0x39, 0x2b)
    override val brightGreen   = Color(0x1a, 0x6b, 0x30)
    override val brightYellow  = Color(0x8a, 0x60, 0x00)
    override val brightBlue    = Color(0x1a, 0x52, 0x76)
    override val brightMagenta = Color(0x7d, 0x3c, 0x98)
    override val brightCyan    = Color(0x0e, 0x66, 0x55)
    override val brightWhite   = Color(0x7f, 0x7f, 0x7f)

    override val default = Color(0x1a, 0x1a, 0x1a)
}
