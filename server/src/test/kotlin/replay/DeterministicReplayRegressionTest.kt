package replay

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.server.core.ModelUpdater
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.BotIntent
import dev.robocode.tankroyale.server.model.GameSetup
import dev.robocode.tankroyale.server.model.InitialPosition
import dev.robocode.tankroyale.server.model.ParticipantId
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Fixed-input regression hook for the battle model.
 *
 * This deliberately serializes only stable model outputs. It is a small deterministic seam for
 * replay-regression tests, not a persisted replay format.
 */
class DeterministicReplayRegressionTest : FunSpec({

    tags(Tag("BR-048"))

    test("BR-048: Positive and negative: fixed input matches baseline and changed input differs") {
        val firstRun = replaySnapshot(targetSpeed = 8.0)
        val secondRun = replaySnapshot(targetSpeed = 8.0)
        val changedRun = replaySnapshot(targetSpeed = 0.0)

        CURRENT_BEHAVIOR_VERSION shouldBe SNAPSHOT_BEHAVIOR_VERSION
        firstRun shouldBe expectedSnapshot
        secondRun shouldBe expectedSnapshot
        changedRun shouldNotBe expectedSnapshot
    }
}) {

    companion object {
        private val bot1 = BotId(1)
        private val bot2 = BotId(2)
        private val participantIds = setOf(ParticipantId(bot1), ParticipantId(bot2))
        private val initialPositions = mapOf(
            bot1 to InitialPosition(100.0, 100.0, 0.0),
            bot2 to InitialPosition(700.0, 500.0, 180.0),
        )
        private val droidFlags = mapOf(bot1 to false, bot2 to false)
        private val setup = GameSetup(
            maxNumberOfParticipants = 2,
            isArenaWidthLocked = true,
            isArenaHeightLocked = true,
            isMinNumberOfParticipantsLocked = true,
            isMaxNumberOfParticipantsLocked = true,
            isNumberOfRoundsLocked = true,
            isGunCoolingRateLocked = true,
            isMaxInactivityTurnsLocked = true,
            isTurnTimeoutLocked = true,
            isReadyTimeoutLocked = true,
        )

        private fun replaySnapshot(targetSpeed: Double): String {
            val updater = ModelUpdater(setup, participantIds, initialPositions, droidFlags, true)
            val snapshots = buildList {
                updater.update(emptyMap())
                add(snapshot(updater))

                repeat(3) {
                    updater.update(
                        mapOf(
                            bot1 to BotIntent(targetSpeed = targetSpeed),
                            bot2 to BotIntent(targetSpeed = 0.0),
                        )
                    )
                    add(snapshot(updater))
                }
            }
            return snapshots.joinToString(separator = "\n")
        }

        // Update this baseline only with the corresponding behavior-version bump.
        private const val SNAPSHOT_BEHAVIOR_VERSION = 1

        private val expectedSnapshot = """
            rounds=1;bots=1:x=100.0,y=100.0,speed=0.0,gunHeat=2.9|2:x=700.0,y=500.0,speed=0.0,gunHeat=2.9;scores=
            rounds=1;bots=1:x=101.0,y=100.0,speed=1.0,gunHeat=2.8|2:x=700.0,y=500.0,speed=0.0,gunHeat=2.8;scores=
            rounds=1;bots=1:x=103.0,y=100.0,speed=2.0,gunHeat=2.6999999999999997|2:x=700.0,y=500.0,speed=0.0,gunHeat=2.6999999999999997;scores=
            rounds=1;bots=1:x=106.0,y=100.0,speed=3.0,gunHeat=2.5999999999999996|2:x=700.0,y=500.0,speed=0.0,gunHeat=2.5999999999999996;scores=
        """.trimIndent()

        private fun snapshot(updater: ModelUpdater): String {
            val bots = listOf(bot1, bot2).joinToString(separator = "|") { botId ->
                requireNotNull(updater.getBot(botId)).let { bot ->
                    "${botId.value}:x=${bot.x},y=${bot.y},speed=${bot.speed},gunHeat=${bot.gunHeat}"
                }
            }
            val scores = updater.getResults()
                .sortedBy { it.participantId.id }
                .joinToString(separator = "|")
            return "rounds=${updater.numberOfRounds};bots=$bots;scores=$scores"
        }
    }
}
