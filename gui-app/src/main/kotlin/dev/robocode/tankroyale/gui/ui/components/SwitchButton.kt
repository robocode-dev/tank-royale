package dev.robocode.tankroyale.gui.ui.components

import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.Timer

// Improved version of the java-swing-switch-button from DJ-Raven:
// https://github.com/DJ-Raven/java-swing-switch-button/blob/main/src/swing/SwitchButton.java
//
// This version takes an initial selection state, and has been ported for Kotlin as well
class SwitchButton(initialSelected: Boolean) : JComponent() {

    private val timer: Timer
    private var location: Float = 2f
    private var mouseOver: Boolean = false
    private var speed: Float = 0.125f

    var isSelected = initialSelected
        set(value) {
            field = value

            timer.start()
            fireSwitchEvent()
        }

    private val events: MutableList<SwitchEvent> = mutableListOf()

    init {
        background = Color(0x3f, 0x3f, 0xff)
        preferredSize = Dimension(32, 16)
        foreground = Color.WHITE
        cursor = Cursor(Cursor.HAND_CURSOR)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                location = if (isSelected) width - height + 2f else 2f
            }
        })

        timer = Timer(0) { _ ->
            if (isSelected) {
                val endLocation = width - height + 2f
                if (location < endLocation) {
                    location += speed
                } else {
                    timer.stop()
                    location = endLocation
                }
            } else {
                val endLocation = 2f
                if (location > endLocation) {
                    location -= speed
                } else {
                    timer.stop()
                    location = endLocation
                }
            }
            repaint()
        }

        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(me: MouseEvent) {
                mouseOver = true
            }

            override fun mouseExited(me: MouseEvent) {
                mouseOver = false
            }

            override fun mouseReleased(me: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    if (mouseOver) {
                        isSelected = !isSelected
                    }
                }
            }
        })
    }

    override fun paint(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (alpha < 1) {
            g2.color = Color.GRAY
            g2.fillRoundRect(0, 0, width, height, preferredSize.height, preferredSize.height)
        }

        g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        g2.color = background
        g2.fillRoundRect(0, 0, width, height, preferredSize.height, preferredSize.height)

        g2.color = foreground
        g2.composite = AlphaComposite.SrcOver
        g2.fillOval(location.toInt(), 2, height - 4, height - 4)
    }

    private val alpha: Float
        get() {
            val width = width - height
            var alpha = (location - 2) / width
            if (alpha < 0) {
                alpha = 0f
            }
            if (alpha > 1) {
                alpha = 1f
            }
            return alpha
        }

    private fun fireSwitchEvent() {
        events.forEach { it(isSelected) }
    }

    fun addSwitchHandler(event: SwitchEvent) {
        events.add(event)
    }
}

typealias SwitchEvent = (Boolean) -> Unit
