package score

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score.RankDecorator
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.Score
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.spyk

class RankDecoratorTest : FunSpec({
    context("ScoreCalculator.updateRanks") {

        test("should assign correct ranks with different scores") {
            val score1 = spyk<Score>(Score(ParticipantId(BotId(1))))
            val score2 = spyk<Score>(Score(ParticipantId(BotId(2))))
            val score3 = spyk<Score>(Score(ParticipantId(BotId(3))))
            val score4 = spyk<Score>(Score(ParticipantId(BotId(4))))

            every { score1.totalScore } returns 100.0
            every { score2.totalScore } returns 90.0
            every { score3.totalScore } returns 80.0
            every { score4.totalScore } returns 70.0

            val scores = listOf(score1, score2, score3, score4) // "random" order

            RankDecorator.updateRanks(scores)

            scores.map { it.rank } shouldContainExactly listOf(1, 2, 3, 4)
        }

        test("should assign correct ranks with equal scores") {
            val score1 = spyk<Score>(Score(ParticipantId(BotId(1))))
            val score2 = spyk<Score>(Score(ParticipantId(BotId(2))))
            val score3 = spyk<Score>(Score(ParticipantId(BotId(3))))
            val score4 = spyk<Score>(Score(ParticipantId(BotId(4))))
            val score5 = spyk<Score>(Score(ParticipantId(BotId(5))))
            val score6 = spyk<Score>(Score(ParticipantId(BotId(6))))
            val score7 = spyk<Score>(Score(ParticipantId(BotId(7))))

            every { score1.totalScore } returns 100.0
            every { score2.totalScore } returns 90.0
            every { score3.totalScore } returns 90.0
            every { score4.totalScore } returns 80.0
            every { score5.totalScore } returns 70.0
            every { score6.totalScore } returns 70.0
            every { score7.totalScore } returns 60.0

            val scores = listOf(score1, score2, score3, score4, score5, score6, score7)
            RankDecorator.updateRanks(scores)

            scores.map { it.rank } shouldContainExactly listOf(1, 2, 2, 4, 5, 5, 7)
        }

        test("should assign correct ranks with all equal scores") {
            val score1 = spyk<Score>(Score(ParticipantId(BotId(1))))
            val score2 = spyk<Score>(Score(ParticipantId(BotId(2))))
            val score3 = spyk<Score>(Score(ParticipantId(BotId(3))))
            val score4 = spyk<Score>(Score(ParticipantId(BotId(4))))

            every { score1.totalScore } returns 100.0
            every { score2.totalScore } returns 100.0
            every { score3.totalScore } returns 100.0
            every { score4.totalScore } returns 100.0

            val scores = listOf(score3, score4, score1, score2)

            RankDecorator.updateRanks(scores)

            scores.map { it.rank } shouldContainExactly listOf(1, 1, 1, 1)
        }
    }
})