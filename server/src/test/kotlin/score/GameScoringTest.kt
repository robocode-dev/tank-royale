package score

import dev.robocode.tankroyale.server.core.*
import dev.robocode.tankroyale.server.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds

class GameScoringTest : FunSpec({

    tags(Tag("TR-SRV-SCR-002"))

    val setup = GameSetup(
        arenaWidth = 800,
        arenaHeight = 600,
        minNumberOfParticipants = 2,
        maxNumberOfParticipants = 10,
        numberOfRounds = 3,
        gunCoolingRate = 0.1,
        maxInactivityTurns = 10,
        turnTimeout = 30.milliseconds,
        readyTimeout = 1000.milliseconds,
        defaultTurnsPerSecond = 30,
        isArenaWidthLocked = false,
        isArenaHeightLocked = false,
        isMinNumberOfParticipantsLocked = false,
        isMaxNumberOfParticipantsLocked = false,
        isNumberOfRoundsLocked = false,
        isGunCoolingRateLocked = false,
        isMaxInactivityTurnsLocked = false,
        isTurnTimeoutLocked = false,
        isReadyTimeoutLocked = false
    )

    val botId1 = BotId(1)
    val botId2 = BotId(2)
    val participantId1 = ParticipantId(botId1)
    val participantId2 = ParticipantId(botId2)
    val participantIds = setOf(participantId1, participantId2)

    val initialPositions = mapOf(
        botId1 to InitialPosition(100.0, 100.0, 0.0),
        botId2 to InitialPosition(150.0, 100.0, 180.0)
    )
    val droidFlags = mapOf(botId1 to false, botId2 to false)

    test("TR-SRV-SCR-002: Positive: End-to-end scoring multiple rounds") {
        val modelUpdater = ModelUpdater(setup, participantIds, initialPositions, droidFlags, true)

        // ROUND 1: Bot 1 damages Bot 2 and then Bot 2 dies
        modelUpdater.update(emptyMap()) // Initialize round 1 (Round 1, Turn 1)

        // Set gunHeat to 0 so they can fire immediately
        modelUpdater.getBot(botId1)?.gunHeat = 0.0
        modelUpdater.getBot(botId2)?.gunHeat = 0.0

        // Bot 1 at (100, 100) facing 0 (East), Bot 2 at (150, 100) facing 180 (West).
        // Turn 2 intent: Bot 1 fires
        modelUpdater.update(mapOf(botId1 to BotIntent().apply { firepower = 3.0 }))
        
        // Bullet speed = 11. Distance is 50. Takes ~5 turns.
        repeat(20) {
            if (!modelUpdater.isAlive(botId2)) return@repeat
            modelUpdater.update(emptyMap())
        }
        
        modelUpdater.getBot(botId2)?.energy!! shouldBeLessThan 100.0
        
        // Manually make Bot 2 low health so next hit kills it
        modelUpdater.getBot(botId2)?.energy = 0.01
        modelUpdater.getBot(botId1)?.gunHeat = 0.0
        
        // Fire one more bullet to kill
        modelUpdater.update(mapOf(botId1 to BotIntent().apply { firepower = 1.0 }))
        repeat(30) {
            if (!modelUpdater.isAlive(botId2)) return@repeat
            modelUpdater.update(emptyMap())
        }
        
        modelUpdater.isAlive(botId2) shouldBe false
        
        // Round 1 ended.
        val resultsR1 = modelUpdater.getResults()
        val score1_R1 = resultsR1.find { it.participantId == participantId1 }!!
        
        score1_R1.totalScore shouldBeGreaterThan 0.0
        score1_R1.bulletDamageScore shouldBeGreaterThan 0.0
        score1_R1.survivalScore shouldBeGreaterThan 0.0
        score1_R1.lastSurvivorBonus shouldBeGreaterThan 0.0
        
        // ROUND 2: Bot 2 damages Bot 1 and then Bot 1 dies
        modelUpdater.update(emptyMap()) // Starts round 2
        
        modelUpdater.getBot(botId1)?.gunHeat = 0.0
        modelUpdater.getBot(botId2)?.gunHeat = 0.0
        modelUpdater.getBot(botId1)?.energy = 0.01

        modelUpdater.update(mapOf(botId2 to BotIntent().apply { firepower = 1.0 }))
        repeat(30) {
            if (!modelUpdater.isAlive(botId1)) return@repeat
            modelUpdater.update(emptyMap())
        }
        
        modelUpdater.isAlive(botId1) shouldBe false
        
        // Round 2 ended.
        val resultsR2 = modelUpdater.getResults()
        val score1_R2 = resultsR2.find { it.participantId == participantId1 }!!
        val score2_R2 = resultsR2.find { it.participantId == participantId2 }!!
        
        // Scores should be accumulated
        score1_R2.totalScore shouldBeGreaterThanOrEqual score1_R1.totalScore
        score2_R2.totalScore shouldBeGreaterThan 0.0
    }
})
