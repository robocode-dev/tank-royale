package score

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score.ScoreCalculator
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.rules.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe


class ScoreCalculatorTest : StringSpec({

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

    "scores should be 0 when no damage has been registered" {

        scoreCalculator.getScores().apply {

            size shouldBe 4

            forEach {
                it.apply {
                    participantId.teamId shouldBe null
                    participantId.botId.id shouldBeIn listOf(1, 2, 3, 4)

                    totalScore shouldBe 0
                    bulletDamageScore shouldBe 0
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
    }

    "bullet damage and total score must match (no kill)" {
        scoreTracker.apply {

            val bullet1Damage = 1.65

            registerBulletHit(ParticipantId(BotId(1)), ParticipantId(BotId(2)), bullet1Damage, false)

            scoreCalculator.getScores().first { it.participantId.botId.id == 1 }.apply {
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

            scoreCalculator.getScores().first { it.participantId.botId.id == 1 }.apply {
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

    "bullet damage, bullet kill bonus, and total score" {
        scoreTracker.apply {

            val bullet1Damage = 0.25
            val bullet2Damage = 0.75
            val bullet3Damage = 2.33
            val bullet4Damage = 1.2
            val bullet5Damage = 3.4

            registerBulletHit(ParticipantId(BotId(2)), ParticipantId(BotId(3)), bullet1Damage, false)
            registerBulletHit(ParticipantId(BotId(2)), ParticipantId(BotId(3)), bullet2Damage, false)
            registerBulletHit(ParticipantId(BotId(2)), ParticipantId(BotId(3)), bullet3Damage, true)

            // damage to other enemies than the one being killed above
            registerBulletHit(ParticipantId(BotId(2)), ParticipantId(BotId(1)), bullet4Damage, false)
            registerBulletHit(ParticipantId(BotId(2)), ParticipantId(BotId(4)), bullet5Damage, false)

            scoreCalculator.getScores().first { it.participantId.botId.id == 2 }.apply {
                bulletDamageScore shouldBe (bullet1Damage + bullet2Damage + bullet3Damage) + (bullet4Damage + bullet5Damage)
                bulletKillBonus shouldBe (bullet1Damage + bullet2Damage + bullet3Damage) * BONUS_PER_BULLET_KILL
                totalScore shouldBe SCORE_PER_BULLET_DAMAGE * bulletDamageScore + bulletKillBonus

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

    "ram damage, ram kill bonus, and total score" {
        scoreTracker.apply {

            for (i in 1..2) {
                registerRamHit(ParticipantId(BotId(2)), ParticipantId(BotId(3)), false)
                registerRamHit(ParticipantId(BotId(3)), ParticipantId(BotId(2)), false)
            }
            registerRamHit(ParticipantId(BotId(2)), ParticipantId(BotId(3)), true)
            registerRamHit(ParticipantId(BotId(3)), ParticipantId(BotId(2)), false)

            // damage to other enemies than the one being killed above
            registerRamHit(ParticipantId(BotId(2)), ParticipantId(BotId(1)), false)
            registerRamHit(ParticipantId(BotId(2)), ParticipantId(BotId(4)), false)

            scoreCalculator.getScores().first { it.participantId.botId.id == 2 }.apply {
                ramDamageScore shouldBe (3 + 2) * RAM_DAMAGE * SCORE_PER_RAM_DAMAGE
                ramKillBonus shouldBe 3 * RAM_DAMAGE * BONUS_PER_RAM_KILL
                totalScore shouldBe ramDamageScore + ramKillBonus

                bulletDamageScore shouldBe 0
                bulletKillBonus shouldBe 0
                survivalScore shouldBe 0
                lastSurvivorBonus shouldBe 0
                firstPlaces shouldBe 1
                secondPlaces shouldBe 0
                thirdPlaces shouldBe 0
            }

            scoreCalculator.getScores().first { it.participantId.botId.id == 3 }.apply {
                ramDamageScore shouldBe 3 * RAM_DAMAGE * SCORE_PER_RAM_DAMAGE
                ramKillBonus shouldBe 0
                totalScore shouldBe ramDamageScore + ramKillBonus
            }
        }
    }

    "survival and last survivor bonus" {
        scoreTracker.apply {

            registerDeaths(setOf(ParticipantId(BotId(4))))
            registerDeaths(setOf(ParticipantId(BotId(2))))
            registerDeaths(setOf(ParticipantId(BotId(1))))

            scoreCalculator.getScores().first { it.participantId.botId.id == 4 }.apply {
                survivalScore shouldBe 0
                lastSurvivorBonus shouldBe 0
            }

            scoreCalculator.getScores().first { it.participantId.botId.id == 2 }.apply {
                survivalScore shouldBe 1 * SCORE_PER_SURVIVAL
                lastSurvivorBonus shouldBe 0
            }

            scoreCalculator.getScores().first { it.participantId.botId.id == 1 }.apply {
                survivalScore shouldBe 2 * SCORE_PER_SURVIVAL
                lastSurvivorBonus shouldBe 0
            }

            scoreCalculator.getScores().first { it.participantId.botId.id == 3 }.apply {
                survivalScore shouldBe 3 * SCORE_PER_SURVIVAL
                lastSurvivorBonus shouldBe (teamsOrBotIds.size - 1) * BONUS_PER_LAST_SURVIVOR
            }

            // Additional check when a last survivor dies. It was still a last survivor

            registerDeaths(setOf(ParticipantId(BotId(3))))

            scoreCalculator.getScores().first { it.participantId.botId.id == 3 }.apply {
                survivalScore shouldBe 3 * SCORE_PER_SURVIVAL
                lastSurvivorBonus shouldBe (teamsOrBotIds.size - 1) * BONUS_PER_LAST_SURVIVOR
            }
        }
    }

    "ordinary placement" {
        scoreTracker.apply {

            registerDeaths(setOf(ParticipantId(BotId(4))))
            registerDeaths(setOf(ParticipantId(BotId(2))))
            registerDeaths(setOf(ParticipantId(BotId(1))))

            scoreCalculator.getScores().first { it.participantId.botId.id == 3 }.apply {
                firstPlaces shouldBe 1
                secondPlaces shouldBe 0
                thirdPlaces shouldBe 0
            }

            scoreCalculator.getScores().first { it.participantId.botId.id == 1 }.apply {
                firstPlaces shouldBe 0
                secondPlaces shouldBe 1
                thirdPlaces shouldBe 0
            }

            scoreCalculator.getScores().first { it.participantId.botId.id == 2 }.apply {
                firstPlaces shouldBe 0
                secondPlaces shouldBe 0
                thirdPlaces shouldBe 1
            }

            scoreCalculator.getScores().first { it.participantId.botId.id == 4 }.apply {
                firstPlaces shouldBe 0
                secondPlaces shouldBe 0
                thirdPlaces shouldBe 0
            }
        }
    }

    "two 1st places" {
        teamsOrBotIds = setOf(
            ParticipantId(BotId(1)),
            ParticipantId(BotId(2)),
            ParticipantId(BotId(3)),
            ParticipantId(BotId(4)),
            ParticipantId(BotId(5)),
        )

        scoreTracker.apply {
            registerDeaths(setOf(ParticipantId(BotId(4)), ParticipantId(BotId(5))))
            registerDeaths(setOf(ParticipantId(BotId(3))))
            registerDeaths(setOf(ParticipantId(BotId(1)), ParticipantId(BotId(2))))

            // Two 1st places
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(1, 2) }.onEach {
                it.firstPlaces shouldBe 1
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 0
            }

            // 3rd place, as the 1st and 2nd places are preserved
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(3) }.onEach {
                it.firstPlaces shouldBe 0
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 1
            }

            // No 1st, 2nd or 3rd placements left
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(4, 5) }.onEach {
                it.firstPlaces shouldBe 0
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 0
            }
        }
    }

    "two 2nd places" {
        teamsOrBotIds = setOf(
            ParticipantId(BotId(1)),
            ParticipantId(BotId(2)),
            ParticipantId(BotId(3)),
            ParticipantId(BotId(4)),
            ParticipantId(BotId(5)),
        )

        scoreTracker.apply {
            registerDeaths(setOf(ParticipantId(BotId(4)), ParticipantId(BotId(5))))
            registerDeaths(setOf(ParticipantId(BotId(2)), ParticipantId(BotId(3))))
            registerDeaths(setOf(ParticipantId(BotId(1))))

            // One 1st place
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(1) }.onEach {
                it.firstPlaces shouldBe 1
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 0
            }

            // Two 2nd places
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(2, 3) }.onEach {
                it.firstPlaces shouldBe 0
                it.secondPlaces shouldBe 1
                it.thirdPlaces shouldBe 0
            }

            // No 1st, 2nd or 3rd placements left (3rd places are preserved by the two 2nd places)
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(4, 5) }.onEach {
                it.firstPlaces shouldBe 0
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 0
            }
        }
    }

    "two 3rd places" {
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
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(1) }.onEach {
                it.firstPlaces shouldBe 1
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 0
            }

            // One 2nd places
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(2) }.onEach {
                it.firstPlaces shouldBe 0
                it.secondPlaces shouldBe 1
                it.thirdPlaces shouldBe 0
            }

            // Two 3rd places
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(3, 4) }.onEach {
                it.firstPlaces shouldBe 0
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 1
            }

            // No 1st, 2nd or 3rd placements left
            scoreCalculator.getScores().filter { it.participantId.botId.id in setOf(6) }.onEach {
                it.firstPlaces shouldBe 0
                it.secondPlaces shouldBe 0
                it.thirdPlaces shouldBe 0
            }
        }
    }
})