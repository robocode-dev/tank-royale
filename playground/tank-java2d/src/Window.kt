import java.awt.EventQueue
import javax.swing.JFrame

object Window : JFrame() {

    var panel = Panel()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(1000, 1000)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(panel)
    }
}

private fun main() {
    Window.isVisible = true
}