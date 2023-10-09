package dev.robocode.tankroyale.gui.util

import java.util.*

object TimerTaskUtil {

    fun createTimerTask(callable: () -> Unit): TimerTask =
        object : TimerTask() {
            override fun run() {
                callable.invoke()
            }
        }
}