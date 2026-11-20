package dev.robocode.tankroyale.common.util

import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Files
import java.nio.file.Path

/** BR-051: the one rule by which every part of the engine finds a team's members, in both directions. */
@Suppress("unused") // Test class is discovered by Kotest via reflection
class TeamMembersTest : FunSpec({
    tags(Tag("BR-051"))

    /** A folder beside the others whose bot file has [name] and [version], or no bot file when [name] is null. */
    fun bot(root: Path, folder: String, name: String?, version: String = "1.0.0"): Path {
        val dir = Files.createDirectories(root.resolve(folder))
        if (name != null) Files.writeString(dir.resolve("$folder.json"), "$name|$version")
        return dir
    }

    /** Reads the fake bot file the way a real reader reads `name` and `version`. */
    fun identity(dir: Path): Pair<String, String>? {
        val file = dir.resolve("${dir.fileName}.json")
        if (!Files.exists(file)) return null
        val (name, version) = Files.readString(file).split('|')
        return name to version
    }

    fun withFolder(block: (Path) -> Unit) {
        val root = Files.createTempDirectory("team-members")
        try {
            block(root)
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    test("testBR051_UnitPositive_aMemberIsFoundByItsFolderFirstThenByItsWholeNameAndVersion") {
        withFolder { root ->
            val team = bot(root, "CornersCrazy", "CornersCrazy")
            val corners = bot(root, "Corners", "Corners")
            val spaced = bot(root, "MyBot", "My Bot", "2.1")
            // A folder named exactly like the entry is the member, also when another folder holds a bot of that identity.
            val exact = bot(root, "Crazy 1.0.0", null)
            bot(root, "Crazy", "Crazy")

            TeamMembers.find(team, "Corners", ::identity) shouldBe TeamMemberMatch.Found(corners.toAbsolutePath().normalize())
            TeamMembers.find(team, "Corners 1.0.0", ::identity) shouldBe TeamMemberMatch.Found(corners)
            TeamMembers.find(team, "My Bot 2.1", ::identity) shouldBe TeamMemberMatch.Found(spaced)
            TeamMembers.find(team, "Crazy 1.0.0", ::identity) shouldBe TeamMemberMatch.Found(exact.toAbsolutePath().normalize())
        }
    }

    test("testBR051_UnitNegative_noOrSeveralMatchingFoldersNameNoMemberAndNothingIsGuessed") {
        withFolder { root ->
            val team = bot(root, "Pair", "Pair")
            bot(root, "Corners", "Corners")
            bot(root, "CornersCopy", "Corners")
            bot(root, "NoFile", null)
            bot(root, "Spaced", "My Bot", "2.1")

            val ambiguous = TeamMembers.find(team, "Corners 1.0.0", ::identity)
            ambiguous.shouldBeInstanceOf<TeamMemberMatch.Ambiguous>()
            ambiguous.folders.map { it.fileName.toString() } shouldBe listOf("Corners", "CornersCopy")
            val problem = TeamMembers.problem(team, ambiguous).orEmpty()
            problem shouldContain "Corners 1.0.0"
            problem shouldContain "CornersCopy"

            // The entry is never split or trimmed, and a bot without a bot file is found only by its folder.
            TeamMembers.find(team, "My Bot  2.1", ::identity) shouldBe TeamMemberMatch.NotFound("My Bot  2.1")
            TeamMembers.find(team, " Corners", ::identity) shouldBe TeamMemberMatch.NotFound(" Corners")
            TeamMembers.find(team, "NoFile 1.0.0", ::identity) shouldBe TeamMemberMatch.NotFound("NoFile 1.0.0")
            TeamMembers.find(team, "Nobody 1.0.0", ::identity) shouldBe TeamMemberMatch.NotFound("Nobody 1.0.0")
            TeamMembers.problem(team, TeamMemberMatch.NotFound("Nobody 1.0.0")).orEmpty() shouldContain "Nobody 1.0.0"
            // The team itself and folders outside the team's own folder are never members.
            TeamMembers.find(team, "Pair 1.0.0", ::identity) shouldBe TeamMemberMatch.NotFound("Pair 1.0.0")
            Files.createDirectories(root.parent.resolve("outside-${root.fileName}"))
            TeamMembers.find(team, "../outside-${root.fileName}", ::identity).shouldBeInstanceOf<TeamMemberMatch.NotFound>()
            root.parent.resolve("outside-${root.fileName}").toFile().deleteRecursively()
        }
    }
})
