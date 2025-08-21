package dev.robocode.tankroyale.botapi.graphics

data class Point(val x: Double, val y: Double) {
    fun getX(): Double = x
    fun getY(): Double = y
    override fun toString(): String = "(X=$x, Y=$y)"
}
