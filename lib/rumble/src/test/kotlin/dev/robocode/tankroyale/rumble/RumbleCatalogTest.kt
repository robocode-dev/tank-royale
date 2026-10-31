package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RumbleCatalogTest {

    private fun entry(name: String, status: String = "active", extra: String = "", teamMembers: String = "") = """
        {
          "name": "$name", "version": "1.0", "platform": "java", "path": "bots/java/$name",
          "sourceHash": "sha256:abc", "owner": "someone", "authors": ["A. Author"], "addedAt": "2026-10-01",
          "status": "$status"$teamMembers$extra
        }
    """.trimIndent()

    private fun catalog(schemaVersion: Int = 1, vararg entries: String) = """
        { "schemaVersion": $schemaVersion, "generatedAt": "2026-10-31T10:00:00Z",
          "commit": "05c4ecfd6c67ec33e10afa85aa0bc9b81df20416", "futureField": true,
          "bots": [${entries.joinToString(",")}] }
    """.trimIndent()

    @Test
    @Tag("RCL-021")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL021_UnitPositive_entriesAreReadAndOnlyActiveOnesAreCurrent() {
        val parsed = Catalog.parse(
            catalog(
                1,
                entry("Walls", extra = ", \"unknownEntryField\": 7"),
                entry("OldWalls", status = "superseded"),
                entry("Duo", teamMembers = ", \"teamMembers\": [\"Walls 1.0\", \"Walls 1.0\"]"),
            )
        )

        assertEquals(1, parsed.schemaVersion)
        assertEquals("05c4ecfd6c67ec33e10afa85aa0bc9b81df20416", parsed.commit)
        assertEquals(3, parsed.bots.size)
        assertEquals(listOf("Walls", "Duo"), parsed.activeBots.map { it.name })
        val walls = parsed.bots.first { it.name == "Walls" }
        assertEquals("bots/java/Walls", walls.path)
        assertEquals("sha256:abc", walls.sourceHash)
        assertEquals(listOf("A. Author"), walls.authors)
        assertEquals(emptyList<String>(), walls.teamMembers)
        assertEquals(listOf("Walls 1.0", "Walls 1.0"), parsed.bots.first { it.name == "Duo" }.teamMembers)
    }

    @Test
    @Tag("RCL-021")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL021_UnitNegative_unsupportedVersionsAndMalformedDocumentsAreRefused() {
        val newer = assertThrows(CatalogUnreadable::class.java) { Catalog.parse(catalog(2, entry("Walls"))) }
        assertTrue(newer.message!!.contains("version 2"), newer.message)
        assertTrue(newer.message!!.contains("Update"), newer.message)

        val notJson = assertThrows(CatalogUnreadable::class.java) { Catalog.parse("this is not json") }
        assertTrue(notJson.message!!.startsWith("The Rumble catalog is not valid"), notJson.message)

        val noHash = entry("Walls").replace("\"sourceHash\": \"sha256:abc\",", "")
        assertThrows(CatalogUnreadable::class.java) { Catalog.parse(catalog(1, noHash)) }
    }
}
