package dev.robocode.tankroyale.ui.desktop.util

interface Disposable {
    val isDisposed: Boolean
    fun dispose()
}