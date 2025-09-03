package dev.robocode.tankroyale.gui.ui.components

import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.Timer

// Improved version of the java-swing-switch-button from DJ-Raven:
// https://github.com/DJ-Raven/java-swing-switch-button/blob/main/src/swing/SwitchButton.java
//
// This version takes an initial selection state, and has been ported for Kotlin as well
class SwitchButton(initialSelected: Boolean) : JComponent() {

    private var knobLocation: Float = 2f
    private val animationTimer = createAnimationTimer()

    var isSelected = initialSelected
        set(value) {
            if (field == value) {
                return // no change -> leave
            }
            field = value
            animationTimer.start()
            fireSwitchEvent()
        }

    private val eventHandlers: MutableList<SwitchEvent> = mutableListOf()

    init {
        background = Color(0x3f, 0x3f, 0xff)
        preferredSize = Dimension(32, 16)
        foreground = Color.WHITE
        cursor = Cursor(Cursor.HAND_CURSOR)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                knobLocation = if (isSelected) width - height + 2f else 2f

                repaint() // paint for the first time!
            }
        })

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(me: MouseEvent) {
                isSelected = !isSelected
            }
        })
    }

    private fun createAnimationTimer(): Timer = Timer(1000 / 60) { _ ->
        if (isSelected) {
            val endLocation = width - height + 2f
            if (knobLocation < endLocation) {
                knobLocation += 4f
            } else {
                animationTimer.stop()
                knobLocation = endLocation
            }
        } else {
            val endLocation = 2f
            if (knobLocation > endLocation) {
                knobLocation -= 4f
            } else {
                animationTimer.stop()
                knobLocation = endLocation
            }
        }
        repaint()
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
        g2.fillOval(knobLocation.toInt(), 2, height - 4, height - 4)
    }

    private val alpha: Float
        get() {
            val width = width - height
            var alpha = (knobLocation - 2) / width
            if (alpha < 0) {
                alpha = 0f
            }
            if (alpha > 1) {
                alpha = 1f
            }
            return alpha
        }

    private fun fireSwitchEvent() {
        eventHandlers.forEach { it(isSelected) }
    }

    fun addSwitchHandler(event: SwitchEvent) {
        eventHandlers.add(event)
    }
}

typealias SwitchEvent = (Boolean) -> Unit
