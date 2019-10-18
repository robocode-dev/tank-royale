import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel

class Panel : JPanel() {

    private var time = 1

    private val explosion = Explosion(400.0, 400.0, 500, 10, 400)

    override fun paintComponent(g: Graphics) {
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        try {
            draw(g)
        } finally {
            g.dispose()
        }
        if (explosion.done) {
            time = 1
            explosion.done = false
        } else {
            time++
        }
    }

    fun draw(g: Graphics2D) {
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        explosion.update(g, time)
    }
}
