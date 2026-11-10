package dev.robocode.tankroyale.gui.settings

import io.kotest.core.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.charset.StandardCharsets
import java.util.Properties

class PropertiesStoreEncodingTest : StringSpec({
    tags(Tag("Unit"))

    "a path with letters outside ASCII is read back as it was written" {
        val written = Properties().apply {
            setProperty("bot-directories", "C:/Users/Jørgen/rumble-bots,true,C:/Users/Ærø/bots,false")
            setProperty("bot-directory-labels", "c:/users/jørgen/rumble-bots;rumble-downloads")
        }

        val bytes = PropertiesStore.write(written, "title")

        PropertiesStore.read(bytes) shouldBe written
        bytes.all { it >= 0 } shouldBe true
        Properties().apply { load(bytes.inputStream()) } shouldBe written
    }

    "an older version, which read the file as ISO-8859-1, reads the same letters" {
        val written = Properties().apply { setProperty("bot-directories", "/home/J\u00f8rgen/\u0420\u0443/bots,true") }

        Properties().apply { load(PropertiesStore.write(written, "title").inputStream()) }.getProperty("bot-directories") shouldBe "/home/J\u00f8rgen/\u0420\u0443/bots,true"
    }

    "a file an earlier version wrote in UTF-8 is read as written" {
        val bytes = "bot-directories=C:/Users/J\u00f8rgen/bots,true\n".toByteArray(StandardCharsets.UTF_8)

        PropertiesStore.read(bytes).getProperty("bot-directories") shouldBe "C:/Users/J\u00f8rgen/bots,true"
    }

    "a file written by an older version in a single-byte charset is read as before" {
        val bytes = "bot-directories=C:/Users/Jørgen/bots,true\n".toByteArray(StandardCharsets.ISO_8859_1)

        PropertiesStore.read(bytes).getProperty("bot-directories") shouldBe "C:/Users/Jørgen/bots,true"
    }

    "the file is written sorted, one setting per line" {
        val written = Properties().apply {
            setProperty("tps", "30")
            setProperty("game-type", "classic")
        }

        String(PropertiesStore.write(written, "title"), StandardCharsets.UTF_8) shouldBe "game-type=classic\ntps=30\n"
    }
})
