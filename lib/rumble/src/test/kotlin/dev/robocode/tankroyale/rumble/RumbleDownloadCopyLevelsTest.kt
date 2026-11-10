package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.rumble.CopyLevel.COPY_WITH_CONDITIONS
import dev.robocode.tankroyale.rumble.CopyLevel.DO_NOT_COPY
import dev.robocode.tankroyale.rumble.CopyLevel.FREE_TO_COPY
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** The copy level a declared license is read at. No Swing, no disk. */
class RumbleDownloadCopyLevelsTest {

    private fun assertLevel(expected: CopyLevel?, vararg licenses: String?) =
        licenses.forEach { assertEquals(expected, CopyLevels.of(it), it.toString()) }

    @Test
    @Tag("RCL-094")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL094_UnitPositive_licensesOnTheBlueOakListsGetTheirLevelInAnyLetterCase() {
        assertLevel(FREE_TO_COPY, "MIT", "mit", "Apache-2.0", "BSD-3-Clause", "BSD-2-Clause", "ISC", "Zlib", "BSL-1.0", "Unlicense", "0BSD", "CC0-1.0", "BlueOak-1.0.0", " MIT ")
        assertLevel(COPY_WITH_CONDITIONS, "GPL-3.0-or-later", "GPL-2.0-only", "LGPL-2.1-or-later", "AGPL-3.0-only", "MPL-2.0", "EPL-2.0", "EUPL-1.2", "gpl-3.0-only")
        assertLevel(COPY_WITH_CONDITIONS, "WTFPL", "Beerware", "Artistic-1.0", "Vim")
        assertLevel(COPY_WITH_CONDITIONS, "CC-BY-4.0", "CC-BY-SA-4.0", "CC-BY-3.0", "cc-by-sa-2.0")
        assertLevel(COPY_WITH_CONDITIONS, "GPL-3.0", "GPL-2.0+", "LGPL-2.1", "AGPL-3.0", "LGPL-3.0+", "MPL-2.0+", "EPL-2.0+")
        assertLevel(COPY_WITH_CONDITIONS, "GPL-2.0-with-classpath-exception", "GPL-3.0-with-GCC-exception", "eCos-2.0", "wxWindows")
        assertLevel(FREE_TO_COPY, "Apache-2.0+", "StandardML-NJ", "Nunit", "bzip2-1.0.5", "BSD-2-Clause-FreeBSD")
        RumbleLicenses.ALL.forEach { assertEquals(if (it.id == "GPL-3.0-or-later") COPY_WITH_CONDITIONS else FREE_TO_COPY, CopyLevels.of(it.id), it.id) }
    }

    @Test
    @Tag("RCL-094")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL094_UnitPositive_expressionsTakeTheMostPermissiveChoiceAndTheMostCarefulPart() {
        assertLevel(FREE_TO_COPY, "MIT OR Apache-2.0", "GPL-3.0-only OR MIT")
        assertLevel(COPY_WITH_CONDITIONS, "MIT AND GPL-3.0-only", "GPL-2.0-only WITH Classpath-exception-2.0", "Proprietary OR GPL-3.0-or-later")
        assertLevel(DO_NOT_COPY, "MIT AND CC-BY-NC-4.0", "LicenseRef-mine OR CC-BY-ND-4.0")
    }

    @Test
    @Tag("RCL-094")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL094_UnitNegative_noLicenseAnUnknownOneOrOneForbiddingChangesOrSaleIsDoNotCopy() {
        assertLevel(DO_NOT_COPY, "", "   ", "Proprietary", "Custom license", "LicenseRef-mine", "All rights reserved", "MIT2")
        assertLevel(DO_NOT_COPY, "CC-BY-NC-4.0", "CC-BY-ND-4.0", "CC-BY-NC-SA-4.0", "CC-BY-NC-ND-4.0")
        assertLevel(DO_NOT_COPY, "(MIT OR Apache-2.0)", "MIT AND (GPL-3.0-only OR MIT)", "MIT OR", "OR MIT", "MIT WITH")
        assertLevel(DO_NOT_COPY, "Proprietary or MIT", "MIT or GPL-3.0-only", "MIT and more")
        assertLevel(null, null)
    }

    @Test
    @Tag("RCL-095")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL095_UnitNegative_aJoinedBotTakesTheTeamMembersOnlyOfTheBotWithItsNameVersionAndAuthors() {
        val ann = BotIdentity("Walls", "1.0", listOf("Ann"))
        val team = BotIdentity("Twins", "1.0", listOf("Ann"))
        val known = KnownLicenses(
            listOf(ann to "MIT", team to "MIT"),
            listOf(ann to emptyList(), team to listOf("Walls", "Walls"), BotIdentity("Pair", "1.0", listOf("Bo")) to listOf("A"), BotIdentity("Pair", "1.0", listOf("Bo")) to listOf("B")),
        )

        assertEquals(emptyList<String>(), known.teamMembersOf(BotIdentity("Walls", "1.0", listOf(" Ann "))), "a single bot is not a team")
        assertEquals(listOf("Walls", "Walls"), known.teamMembersOf(team), "a team's members in order, a repeated member each time")
        assertEquals(null, known.teamMembersOf(BotIdentity("Walls", "2.0", listOf("Ann"))), "another version is another bot")
        assertEquals(null, known.teamMembersOf(BotIdentity("Pair", "1.0", listOf("Bo"))), "two such bots that disagree leave the members not known")
    }
}
