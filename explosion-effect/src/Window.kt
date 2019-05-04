import java.awt.EventQueue
import javax.swing.JFrame

object Window : JFrame() {

    var panel = Panel()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(800, 800)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(panel)
    }

    fun refresh() {
        panel.repaint()
    }
}

private fun main() {
    EventQueue.invokeLater {
        Window.isVisible = true
    }

    Thread.sleep(500)

    Thread().run {
        for (i in 1..1000) {

            Window.refresh()

            Thread.sleep(1000 / 50)
        }
    }
}