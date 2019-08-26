package dev.robocode.tankroyale.ui.desktop.fx

import java.awt.Graphics2D

interface Animation {
    fun isFinished(): Boolean

    fun paint(g: Graphics2D, time: Int)
}