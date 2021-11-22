package dev.robocode.tankroyale.gui.ui.extensions

import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

object WindowExt {

    fun Window.onOpened(handler: ((WindowEvent?) -> Unit)) {
        addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                handler.invoke(e)
            }
        })
    }

    fun Window.onClosing(handler: ((WindowEvent) -> Unit)) {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                handler.invoke(e)
            }
        })
    }

    fun Window.onClosed(handler: ((WindowEvent) -> Unit)) {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                handler.invoke(e)
            }
        })
    }

    fun Window.onActivated(handler: ((WindowEvent) -> Unit)) {
        addWindowListener(object : WindowAdapter() {
            override fun windowActivated(e: WindowEvent) {
                handler.invoke(e)
            }
        })
    }
}