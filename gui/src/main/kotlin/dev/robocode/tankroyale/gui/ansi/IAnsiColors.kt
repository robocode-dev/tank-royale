package dev.robocode.tankroyale.gui.ansi

import java.awt.Color

/**
 * Defines ANSI colors.
 */
interface IAnsiColors {
    /** Black */
    val black: Color
    /** Red */
    val red: Color
    /** Green */
    val green: Color
    /** Yellow */
    val yellow: Color
    /** Blue */
    val blue: Color
    /** Magenta */
    val magenta: Color
    /** Cyan */
    val cyan: Color
    /** White */
    val white: Color

    /** Bright black */
    val brightBlack: Color
    /** Bright red */
    val brightRed: Color
    /** Bright green */
    val brightGreen: Color
    /** Bright yellow */
    val brightYellow: Color
    /** Bright blue */
    val brightBlue: Color
    /** Bright magenta */
    val brightMagenta: Color
    /** Bright cyan */
    val brightCyan: Color
    /** Bright white */
    val brightWhite: Color

    /** Default color */
    val default: Color
}