package replay

import dev.robocode.tankroyale.server.core.ModelUpdater
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.BotIntent
import dev.robocode.tankroyale.server.model.GameSetup
import dev.robocode.tankroyale.server.model.InitialPosition
import dev.robocode.tankroyale.server.model.ParticipantId
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.charset.StandardCharsets

/**
 * Fixed-input regression hook for the battle model.
 *
 * This deliberately serializes only stable model outputs. It is a small deterministic seam for
 * replay-regression tests, not a persisted replay format.
 */
class DeterministicReplayRegressionTest : FunSpec({

    tags(Tag("BR-048"))

    test("BR-048: Positive and negative: identical input is stable and changed input differs") {
        val firstRun = replaySnapshot(targetSpeed = 8.0)
        val secondRun = replaySnapshot(targetSpeed = 8.0)
        val changedRun = replaySnapshot(targetSpeed = 0.0)

        firstRun.contentEquals(secondRun) shouldBe true
        firstRun.contentEquals(changedRun) shouldBe false
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

        private fun replaySnapshot(targetSpeed: Double): ByteArray {
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
            return snapshots.joinToString(separator = "\n").toByteArray(StandardCharsets.UTF_8)
        }

        private fun snapshot(updater: ModelUpdater): String {
            val bots = listOf(bot1, bot2).joinToString(separator = "|") { botId ->
                "${botId.value}:${updater.getBot(botId)}"
            }
            val scores = updater.getResults()
                .sortedBy { it.participantId.id }
                .joinToString(separator = "|")
            return "rounds=${updater.numberOfRounds};bots=$bots;scores=$scores"
        }
    }
}
