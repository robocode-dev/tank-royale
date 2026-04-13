package dev.robocode.tankroyale.gui.booter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files

class BotIdentityReaderTest : FunSpec({

    context("BotIdentityReader") {

        test("regular bot directory returns single identity") {
            val tempRoot = Files.createTempDirectory("bot-test-root")
            try {
                val botDir = tempRoot.resolve("MyBot")
                Files.createDirectory(botDir)
                botDir.resolve("MyBot.json").toFile().writeText(
                    """{"name": "MyBot", "version": "1.0", "authors": ["Alice"]}"""
                )
                val identities = BotIdentityReader.readIdentities(botDir)
                identities shouldHaveSize 1
                identities[0] shouldBe BotIdentity("MyBot", "1.0")
            } finally {
                tempRoot.toFile().deleteRecursively()
            }
        }

        test("team directory expands to member identities") {
            val tempRoot = Files.createTempDirectory("team-test-root")
            try {
                val teamDir = tempRoot.resolve("MyTeam")
                Files.createDirectory(teamDir)
                teamDir.resolve("MyTeam.json").toFile().writeText(
                    """{"name": "MyTeam", "version": "2.0", "authors": ["Bob"], "teamMembers": ["Droid", "Droid"]}"""
                )
                val droidDir = tempRoot.resolve("Droid")
                Files.createDirectory(droidDir)
                droidDir.resolve("Droid.json").toFile().writeText(
                    """{"name": "MyFirstDroid", "version": "1.0", "authors": ["Bob"]}"""
                )

                val identities = BotIdentityReader.readIdentities(teamDir)
                identities shouldHaveSize 2
                identities.shouldContainExactlyInAnyOrder(
                    BotIdentity("MyFirstDroid", "1.0"),
                    BotIdentity("MyFirstDroid", "1.0")
                )
            } finally {
                tempRoot.toFile().deleteRecursively()
            }
        }

        test("team directory with distinct members returns correct multiset") {
            val tempRoot = Files.createTempDirectory("team-test-distinct")
            try {
                val teamDir = tempRoot.resolve("MyTeam")
                Files.createDirectory(teamDir)
                teamDir.resolve("MyTeam.json").toFile().writeText(
                    """{"name": "MyTeam", "version": "1.0", "authors": [], "teamMembers": ["BotA", "BotB"]}"""
                )
                val botADir = tempRoot.resolve("BotA")
                Files.createDirectory(botADir)
                botADir.resolve("BotA.json").toFile().writeText(
                    """{"name": "BotA", "version": "1.0", "authors": []}"""
                )
                val botBDir = tempRoot.resolve("BotB")
                Files.createDirectory(botBDir)
                botBDir.resolve("BotB.json").toFile().writeText(
                    """{"name": "BotB", "version": "2.0", "authors": []}"""
                )

                val identities = BotIdentityReader.readIdentities(teamDir)
                identities shouldHaveSize 2
                identities.shouldContainExactlyInAnyOrder(
                    BotIdentity("BotA", "1.0"),
                    BotIdentity("BotB", "2.0")
                )
            } finally {
                tempRoot.toFile().deleteRecursively()
            }
        }

        test("missing JSON file throws with descriptive message") {
            val tempRoot = Files.createTempDirectory("missing-json-test")
            try {
                val botDir = tempRoot.resolve("MyBot")
                Files.createDirectory(botDir)
                val ex = shouldThrow<IllegalArgumentException> {
                    BotIdentityReader.readIdentities(botDir)
                }
                ex.message shouldContain "Missing MyBot.json"
            } finally {
                tempRoot.toFile().deleteRecursively()
            }
        }

        test("malformed JSON throws with descriptive message") {
            val tempRoot = Files.createTempDirectory("malformed-json-test")
            try {
                val botDir = tempRoot.resolve("MyBot")
                Files.createDirectory(botDir)
                botDir.resolve("MyBot.json").toFile().writeText("{ not valid json }")
                val ex = shouldThrow<IllegalArgumentException> {
                    BotIdentityReader.readIdentities(botDir)
                }
                ex.message shouldContain "Malformed MyBot.json"
            } finally {
                tempRoot.toFile().deleteRecursively()
            }
        }

        test("missing name field throws with descriptive message") {
            val tempRoot = Files.createTempDirectory("missing-name-test")
            try {
                val botDir = tempRoot.resolve("MyBot")
                Files.createDirectory(botDir)
                botDir.resolve("MyBot.json").toFile().writeText("""{"version": "1.0"}""")
                val ex = shouldThrow<IllegalArgumentException> {
                    BotIdentityReader.readIdentities(botDir)
                }
                ex.message shouldContain "Missing 'name'"
            } finally {
                tempRoot.toFile().deleteRecursively()
            }
        }

        test("missing version field throws with descriptive message") {
            val tempRoot = Files.createTempDirectory("missing-version-test")
            try {
                val botDir = tempRoot.resolve("MyBot")
                Files.createDirectory(botDir)
                botDir.resolve("MyBot.json").toFile().writeText("""{"name": "MyBot"}""")
                val ex = shouldThrow<IllegalArgumentException> {
                    BotIdentityReader.readIdentities(botDir)
                }
                ex.message shouldContain "Missing 'version'"
            } finally {
                tempRoot.toFile().deleteRecursively()
            }
        }
    }
})
