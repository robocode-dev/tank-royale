package net.robocode2.gui.extensions

import com.sun.java.accessibility.util.AWTEventMonitor.removeWindowListener
import net.robocode2.gui.utils.Disposable
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener

object WindowExt {

    fun Window.onClosing(listener: ((WindowEvent) -> Unit)): Disposable {
        val windowListener = object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                listener.invoke(e)
            }
        }
        addWindowListener(windowListener)
        return disposable(windowListener)
    }

    private fun disposable(l: WindowListener): Disposable {
        return object: Disposable {
            private var disposed = false

            override val isDisposed: Boolean
                get() = disposed

            override fun dispose() {
                removeWindowListener(l)
                disposed = true
            }
        }
    }
}