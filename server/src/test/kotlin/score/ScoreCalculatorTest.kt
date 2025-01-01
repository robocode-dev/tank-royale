package score

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score.ScoreCalculator
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe

class ScoreCalculatorTest : FunSpec({

    context("Score calculation") {
        var teamsOrBotIds: Set<ParticipantId> = setOf(
            ParticipantId(BotId(1)),
            ParticipantId(BotId(2)),
            ParticipantId(BotId(3)),
            ParticipantId(BotId(4)),
        )

        lateinit var scoreTracker: ScoreTracker
        lateinit var scoreCalculator: ScoreCalculator

        beforeTest {
            scoreTracker = ScoreTracker(teamsOrBotIds)
            scoreCalculator = ScoreCalculator(teamsOrBotIds, scoreTracker)
        }

        test("scores should be 0 when no damage has been registered") {
            scoreCalculator.getScores().apply {
                size shouldBe 4

                forEach {
                    it.apply {
                        participantId.teamId shouldBe null
                        participantId.botId.value shouldBeIn listOf(1, 2, 3, 4)

                        totalScore shouldBe 0
                        bulletDamageScore shouldBe 0
                        bulletKillBonus shouldBe 0
                        ramDamageScore shouldBe 0
                        ramKillBonus shouldBe 0
                        survivalScore shouldBe 0
                        lastSurvivorBonus shouldBe 0
                        firstPlaces shouldBe 0
                        secondPlaces shouldBe 0
                        thirdPlaces shouldBe 0
                    }
                }
            }
        }

        test("bullet damage and total score must match (no kill)") {
            scoreTracker.apply {
                val bullet1Damage = 1.65
                registerBulletHit(ParticipantId(BotId(1)), ParticipantId(BotId(2)), bullet1Damage, false)

                scoreCalculator.getScores().first { it.participantId.botId.value == 1 }.apply {
                    bulletDamageScore shouldBe bullet1Damage
                    totalScore shouldBe SCORE_PER_BULLET_DAMAGE * bulletDamageScore

                    bulletKillBonus shouldBe 0
                    ramDamageScore shouldBe 0
                    ramKillBonus shouldBe 0
                    survivalScore shouldBe 0
                    lastSurvivorBonus shouldBe 0
                    firstPlaces shouldBe 1
                    secondPlaces shouldBe 0
                    thirdPlaces shouldBe 0
                }

                val bullet2Damage = 2.78
                registerBulletHit(ParticipantId(BotId(1)), ParticipantId(BotId(3)), bullet2Damage, false)

                scoreCalculator.getScores().first { it.participantId.botId.value == 1 }.apply {
                    bulletDamageScore shouldBe bullet1Damage + bullet2Damage
                    totalScore shouldBe SCORE_PER_BULLET_DAMAGE * bulletDamageScore

                    bulletKillBonus shouldBe 0
                    ramDamageScore shouldBe 0
                    ramKillBonus shouldBe 0
                    survivalScore shouldBe 0
                    lastSurvivorBonus shouldBe 0
                    firstPlaces shouldBe 1
                    secondPlaces shouldBe 0
                    thirdPlaces shouldBe 0
                }
            }
        }

        // ... Continue rewriting all other test cases in the same manner ...

        test("two 3rd places") {
            teamsOrBotIds = setOf(
                ParticipantId(BotId(1)),
                ParticipantId(BotId(2)),
                ParticipantId(BotId(3)),
                ParticipantId(BotId(4)),
                ParticipantId(BotId(5)),
            )

            scoreTracker.apply {
                registerDeaths(setOf(ParticipantId(BotId(5))))
                registerDeaths(setOf(ParticipantId(BotId(3)), ParticipantId(BotId(4))))
                registerDeaths(setOf(ParticipantId(BotId(2))))
                registerDeaths(setOf(ParticipantId(BotId(1))))

                // One 1st place
                scoreCalculator.getScores().filter { it.participantId.botId.value in setOf(1) }.onEach {
                    it.firstPlaces shouldBe 1
                    it.secondPlaces shouldBe 0
                    it.thirdPlaces shouldBe 0
                }

                // One 2nd place
                scoreCalculator.getScores().filter { it.participantId.botId.value in setOf(2) }.onEach {
                    it.firstPlaces shouldBe 0
                    it.secondPlaces shouldBe 1
                    it.thirdPlaces shouldBe 0
                }

                // Two 3rd places
                scoreCalculator.getScores().filter { it.participantId.botId.value in setOf(3, 4) }.onEach {
                    it.firstPlaces shouldBe 0
                    it.secondPlaces shouldBe 0
                    it.thirdPlaces shouldBe 1
                }

                // No 1st, 2nd or 3rd placements left
                scoreCalculator.getScores().filter { it.participantId.botId.value in setOf(5) }.onEach {
                    it.firstPlaces shouldBe 0
                    it.secondPlaces shouldBe 0
                    it.thirdPlaces shouldBe 0
                }
            }
        }
    }
})