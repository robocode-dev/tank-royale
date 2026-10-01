package score

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.TeamId
import dev.robocode.tankroyale.server.rules.BONUS_PER_LAST_SURVIVOR
import dev.robocode.tankroyale.server.rules.SCORE_PER_SURVIVAL
import dev.robocode.tankroyale.server.score.ResultsView
import dev.robocode.tankroyale.server.score.ScoreTracker
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ScoringCompatibilityTest : FunSpec({
    tags(Tag("Unit"))

    val teamA = TeamId(10)
    val teamB = TeamId(20)
    val teamA1 = ParticipantId(BotId(1), teamA)
    val teamA2 = ParticipantId(BotId(2), teamA)
    val teamB1 = ParticipantId(BotId(3), teamB)
    val teamB2 = ParticipantId(BotId(4), teamB)
    val participants = setOf(teamA1, teamA2, teamB1, teamB2)

    test("testSCR_001_UnitPositive_awardsSurvivalForEachNewOpponentDeath")
        .config(tags = setOf(Tag("SCR-001"))) {
        val tracker = ScoreTracker(participants)

        tracker.registerDeaths(setOf(teamA1))

        tracker.calculateScore(teamA2).survivalScore shouldBe 0.0
        tracker.calculateScore(teamB1).survivalScore shouldBe SCORE_PER_SURVIVAL
        tracker.calculateScore(teamB2).survivalScore shouldBe SCORE_PER_SURVIVAL
    }

    test("testSCR_001_UnitNegative_ignoresTeammateAndRepeatedDeaths")
        .config(tags = setOf(Tag("SCR-001"))) {
        val tracker = ScoreTracker(participants)

        tracker.registerDeaths(setOf(teamA1))
        tracker.registerDeaths(setOf(teamA1))

        tracker.calculateScore(teamA2).survivalScore shouldBe 0.0
        tracker.calculateScore(teamB1).survivalScore shouldBe SCORE_PER_SURVIVAL
    }

    test("testSCR_001_UnitPositive_countsSimultaneousDeathsSeparately")
        .config(tags = setOf(Tag("SCR-001"))) {
        val teamC = ParticipantId(BotId(5), TeamId(30))
        val tracker = ScoreTracker(participants + teamC)

        tracker.registerDeaths(setOf(teamA1, teamA2))

        tracker.calculateScore(teamB1).survivalScore shouldBe 2 * SCORE_PER_SURVIVAL
        tracker.calculateScore(teamC).survivalScore shouldBe 2 * SCORE_PER_SURVIVAL
    }

    test("testSCR_002_UnitPositive_awardsEveryLastTeamMemberOnceAndAggregatesTheirScores")
        .config(tags = setOf(Tag("SCR-002"))) {
        val tracker = ScoreTracker(participants)
        tracker.registerDeaths(setOf(teamB1, teamB2))
        tracker.registerDeaths(setOf(teamB1, teamB2))

        tracker.calculateScore(teamA1).lastSurvivorBonus shouldBe 2 * BONUS_PER_LAST_SURVIVOR
        tracker.calculateScore(teamA2).lastSurvivorBonus shouldBe 2 * BONUS_PER_LAST_SURVIVOR

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
        teamResult.survivalScore shouldBe 4 * SCORE_PER_SURVIVAL
        teamResult.lastSurvivorBonus shouldBe 4 * BONUS_PER_LAST_SURVIVOR
    }

    test("testSCR_002_UnitNegative_doesNotAwardLastSurvivorPointsForADraw")
        .config(tags = setOf(Tag("SCR-002"))) {
        val tracker = ScoreTracker(participants)

        tracker.registerDeaths(participants)

        participants.forEach { tracker.calculateScore(it).lastSurvivorBonus shouldBe 0.0 }
    }
}) {
    companion object {
        private fun participant(id: Int, teamId: Int, teamName: String) = Participant().apply {
            this.id = id
            this.teamId = teamId
            this.teamName = teamName
        }
    }
}
