package dev.robocode.tankroyale.ui.desktop.utils

interface Disposable {
    val isDisposed: Boolean
    fun dispose()
}