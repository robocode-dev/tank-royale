package score

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.score.ResultsView
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ResultsViewTest : FunSpec({
    tags(Tag("Unit"))

    fun bot(id: Int, name: String) = Participant().apply {
        this.id = id
        this.name = name
    }

    fun teamMember(id: Int, name: String, teamId: Int, teamName: String) = bot(id, name).apply {
        this.teamId = teamId
        this.teamName = teamName
    }

    fun score(botId: Int, bulletDamageScore: Double) =
        Score(ParticipantId(BotId(botId)), bulletDamageScore = bulletDamageScore)

    test("Unit: Team and unteamed bot with the same id and the same score both appear in the results") {
        // Team id 1 (members: bots 2 and 3) and the unteamed bot with id 1 share both id and total score
        val participants = listOf(
            bot(1, "Solo"),
            teamMember(2, "Alpha", teamId = 1, teamName = "Team"),
            teamMember(3, "Beta", teamId = 1, teamName = "Team"),
        )
        val botScores = listOf(score(1, 100.0), score(2, 60.0), score(3, 40.0))

        val results = ResultsView.getResults(botScores, participants)

        results.size shouldBe 2
        results.map { it.totalScore } shouldBe listOf(100.0, 100.0)
    }

    test("Unit: Results are sorted by total score descending and ranked") {
        val participants = listOf(bot(1, "A"), bot(2, "B"), bot(3, "C"))
        val botScores = listOf(score(1, 10.0), score(2, 30.0), score(3, 20.0))

        val results = ResultsView.getResults(botScores, participants)

        results.map { it.participantId.botId.value } shouldBe listOf(2, 3, 1)
        results.map { it.rank } shouldBe listOf(1, 2, 3)
    }

    test("Unit: Team members' scores are accumulated into one row") {
        val participants = listOf(
            teamMember(1, "Alpha", teamId = 5, teamName = "Team"),
            teamMember(2, "Beta", teamId = 5, teamName = "Team"),
        )
        val botScores = listOf(score(1, 10.0), score(2, 15.0))

        val results = ResultsView.getResults(botScores, participants)

        results.size shouldBe 1
        results.single().totalScore shouldBe 25.0
    }
})
