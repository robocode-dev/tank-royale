import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.util.*
import javax.swing.JPanel

class Panel : JPanel() {

    private val explosions = Collections.synchronizedList(ArrayList<Explosion>())

    private var time = 1

    init {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                explosions.add(Explosion(e?.x!!.toDouble(), e?.y!!.toDouble(), 80, 50, 15, time))
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        try {
            draw(g)
        } finally {
            g.dispose()
        }
    }

    private fun draw(g: Graphics2D) {
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)

        with(explosions.iterator()) {
            forEach {
                if (!it.isFinished()) {
                    it.paint(g, time)
                } else {
                    remove()
                }
            }
        }
        time++;
    }
}
