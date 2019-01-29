package net.robocode2.gui.utils

interface Disposable {
    val isDisposed: Boolean
    fun dispose()
}