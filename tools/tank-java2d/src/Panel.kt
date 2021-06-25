import java.awt.Color
import java.awt.Color.*
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.geom.Arc2D
import javax.swing.JPanel

class Panel : JPanel() {

    override fun paintComponent(g: Graphics) {
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        try {
            draw(g)
        } finally {
            g.dispose()
        }
    }

    private fun draw(g: Graphics2D) {
        val gfxState = Graphics2DState(g)

        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        Tank(500.0, 500.0, 0.0, 0.0, 0.0).paint(g)

        gfxState.restore(g)
    }
}
