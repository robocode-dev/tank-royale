package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.rumble.DownloadTexts
import dev.robocode.tankroyale.rumble.RumbleLicenses
import io.kotest.core.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.awt.Container
import javax.swing.JTextArea
import javax.swing.SwingUtilities

/** The texts in [container] and everything inside it. */
private fun texts(container: Container): List<String> = container.components.flatMap {
    when (it) {
        is JTextArea -> listOf(it.text)
        is Container -> texts(it)
        else -> emptyList()
    }
}

/** The list box of the licenses in real Swing components: choosing replaces the explanation and moves nothing. */
class RumbleLicensePanelTest : StringSpec({
    tags(Tag("Unit"))

    "choosing licenses one after the other shows only the last and repeats nothing" {
        SwingUtilities.invokeAndWait {
            val panel = RumbleLicensePanel {}
            val linesOfOneLicense = panel.explanation.componentCount

            repeat(2) {
                RumbleLicenses.ALL.forEach { license ->
                    panel.box.selectedItem = license.id

                    panel.explanation.componentCount shouldBe linesOfOneLicense
                    val shown = texts(panel.explanation)
                    shown.count { it == Messages.get(DownloadTexts.forLicense(license, "author")) } shouldBe 1
                    shown.count { it == Messages.get("rumble.download.credit") } shouldBe 1
                    RumbleLicenses.ALL.filter { it != license }.forEach { other ->
                        shown shouldNotContain Messages.get(DownloadTexts.forLicense(other, "author"))
                    }
                }
            }
        }
    }

    // A caret that moves to new text makes the scroll pane around it jump there, which only shows in a window on the screen.
    "a text starts at its first character and its caret does not follow the text" {
        SwingUtilities.invokeAndWait {
            wrappedText("A text with several words").caretPosition shouldBe 0
        }
    }
})
