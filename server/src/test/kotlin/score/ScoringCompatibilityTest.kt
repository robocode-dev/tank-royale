package score

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.TeamId
import dev.robocode.tankroyale.server.rules.BONUS_PER_LAST_SURVIVOR
import dev.robocode.tankroyale.server.rules.SCORE_PER_SURVIVAL
import dev.robocode.tankroyale.server.score.ResultsView
import dev.robocode.tankroyale.server.score.ScoreTracker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ScoringCompatibilityTest {
    private val teamA = TeamId(10)
    private val teamB = TeamId(20)
    private val teamA1 = ParticipantId(BotId(1), teamA)
    private val teamA2 = ParticipantId(BotId(2), teamA)
    private val teamB1 = ParticipantId(BotId(3), teamB)
    private val teamB2 = ParticipantId(BotId(4), teamB)
    private val participants = setOf(teamA1, teamA2, teamB1, teamB2)

    @Test
    @Tag("SCR-001")
    @Tag("Unit")
    @Tag("Positive")
    fun testSCR001_UnitPositive_awardsSurvivalForEachNewOpponentDeath() {
        val tracker = ScoreTracker(participants)

        tracker.registerDeaths(setOf(teamA1))

        assertEquals(0.0, tracker.calculateScore(teamA2).survivalScore, 0.0)
        assertEquals(SCORE_PER_SURVIVAL, tracker.calculateScore(teamB1).survivalScore, 0.0)
        assertEquals(SCORE_PER_SURVIVAL, tracker.calculateScore(teamB2).survivalScore, 0.0)
    }

    @Test
    @Tag("SCR-001")
    @Tag("Unit")
    @Tag("Negative")
    fun testSCR001_UnitNegative_ignoresTeammateAndRepeatedDeaths() {
        val tracker = ScoreTracker(participants)

        tracker.registerDeaths(setOf(teamA1))
        tracker.registerDeaths(setOf(teamA1))

        assertEquals(0.0, tracker.calculateScore(teamA2).survivalScore, 0.0)
        assertEquals(SCORE_PER_SURVIVAL, tracker.calculateScore(teamB1).survivalScore, 0.0)
    }

    @Test
    @Tag("SCR-001")
    @Tag("Unit")
    @Tag("Positive")
    fun testSCR001_UnitPositive_countsSimultaneousDeathsSeparately() {
        val teamC = ParticipantId(BotId(5), TeamId(30))
        val tracker = ScoreTracker(participants + teamC)

        tracker.registerDeaths(setOf(teamA1, teamA2))

        assertEquals(2 * SCORE_PER_SURVIVAL, tracker.calculateScore(teamB1).survivalScore, 0.0)
        assertEquals(2 * SCORE_PER_SURVIVAL, tracker.calculateScore(teamC).survivalScore, 0.0)
    }

    @Test
    @Tag("SCR-001")
    @Tag("Unit")
    @Tag("Positive")
    fun testSCR001_UnitPositive_distinguishesNegativeTeamIdFromUnteamedBot() {
        val unteamedBot = ParticipantId(BotId(1))
        val negativeTeamMember = ParticipantId(BotId(2), TeamId(-1))
        val otherTeamMember = ParticipantId(BotId(3), TeamId(30))
        val tracker = ScoreTracker(setOf(unteamedBot, negativeTeamMember, otherTeamMember))

        tracker.registerDeaths(setOf(unteamedBot))

        assertEquals(SCORE_PER_SURVIVAL, tracker.calculateScore(negativeTeamMember).survivalScore, 0.0)
        assertEquals(SCORE_PER_SURVIVAL, tracker.calculateScore(otherTeamMember).survivalScore, 0.0)
    }

    @Test
    @Tag("SCR-002")
    @Tag("Unit")
    @Tag("Positive")
    fun testSCR002_UnitPositive_awardsEveryLastTeamMemberOnceAndAggregatesTheirScores() {
        val tracker = ScoreTracker(participants)
        tracker.registerDeaths(setOf(teamB1, teamB2))
        tracker.registerDeaths(setOf(teamB1, teamB2))

        assertEquals(2 * BONUS_PER_LAST_SURVIVOR, tracker.calculateScore(teamA1).lastSurvivorBonus, 0.0)
        assertEquals(2 * BONUS_PER_LAST_SURVIVOR, tracker.calculateScore(teamA2).lastSurvivorBonus, 0.0)

        val results = ResultsView.getResults(
            participants.map(tracker::calculateScore),
            listOf(
                participant(1, teamA.id, "Team A"),
                participant(2, teamA.id, "Team A"),
                participant(3, teamB.id, "Team B"),
                participant(4, teamB.id, "Team B"),
            ),
        )
        val teamResult = results.single { it.participantId.teamId == teamA }
        assertEquals(4 * SCORE_PER_SURVIVAL, teamResult.survivalScore, 0.0)
        assertEquals(4 * BONUS_PER_LAST_SURVIVOR, teamResult.lastSurvivorBonus, 0.0)
    }

    @Test
    @Tag("SCR-002")
    @Tag("Unit")
    @Tag("Positive")
    fun testSCR002_UnitPositive_countsUnteamedOpponentWithNegativeTeamIdCollision() {
        val unteamedBot = ParticipantId(BotId(1))
        val negativeTeamMember = ParticipantId(BotId(2), TeamId(-1))
        val otherOpponent = ParticipantId(BotId(3), TeamId(20))
        val tracker = ScoreTracker(setOf(unteamedBot, negativeTeamMember, otherOpponent))

        tracker.registerDeaths(setOf(unteamedBot, otherOpponent))

        assertEquals(2 * BONUS_PER_LAST_SURVIVOR, tracker.calculateScore(negativeTeamMember).lastSurvivorBonus, 0.0)
    }

    @Test
    @Tag("SCR-002")
    @Tag("Unit")
    @Tag("Negative")
    fun testSCR002_UnitNegative_doesNotAwardLastSurvivorPointsForADraw() {
        val tracker = ScoreTracker(participants)

        tracker.registerDeaths(participants)

        participants.forEach { participant ->
            assertEquals(0.0, tracker.calculateScore(participant).lastSurvivorBonus, 0.0)
        }
    }

    private fun participant(id: Int, teamId: Int, teamName: String) = Participant().apply {
        this.id = id
        this.teamId = teamId
        this.teamName = teamName
    }
}
