package dev.robocode.tankroyale.gui

import io.kotest.core.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.ByteBuffer

/**
 * The desktop program's texts are UTF-8, and no letter in them was lost to a conversion between encodings: such a letter
 * becomes the replacement character, which a tooltip or a dialog then shows as a question mark in a box.
 */
class ResourceTextEncodingTest : StringSpec({
    tags(Tag("Unit"))

    "every text file of the desktop program is UTF-8 and holds no replacement character" {
        val resources = listOf(Path.of("src", "main", "resources"), Path.of("gui", "src", "main", "resources")).first { Files.isDirectory(it) }
        val problems = Files.list(resources).use { files ->
            files.iterator().asSequence().filter { it.fileName.toString().endsWith(".properties") }.sorted().toList()
        }.mapNotNull { file ->
            val bytes = Files.readAllBytes(file)
            val decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            val text = try {
                decoder.decode(ByteBuffer.wrap(bytes)).toString()
            } catch (e: java.nio.charset.CharacterCodingException) {
                return@mapNotNull "${file.fileName} is not UTF-8"
            }
            text.lines().withIndex().firstOrNull { '�' in it.value }?.let { "${file.fileName}:${it.index + 1} holds a replacement character" }
        }

        problems.shouldBeEmpty()
    }
})
