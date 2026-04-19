@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package core

import dev.robocode.tankroyale.server.core.*
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.score.ScoreTracker
import dev.robocode.tankroyale.server.score.ScoreCalculator
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TurnProcessorTest : FunSpec({

    val setup = GameSetup(
        arenaWidth = 800, arenaHeight = 600, maxNumberOfParticipants = 10,
        numberOfRounds = 10, maxInactivityTurns = 100, gunCoolingRate = 0.1,
        isArenaWidthLocked = true, isArenaHeightLocked = true, isMinNumberOfParticipantsLocked = true,
        isMaxNumberOfParticipantsLocked = true, isNumberOfRoundsLocked = true, isGunCoolingRateLocked = true,
        isMaxInactivityTurnsLocked = true, isTurnTimeoutLocked = true, isReadyTimeoutLocked = true
    )
    val botId1 = BotId(1)
    val botId2 = BotId(2)
    val participantId1 = ParticipantId(botId1)
    val participantId2 = ParticipantId(botId2)
    val participantIds = setOf(participantId1, participantId2)

    val gunEngine = GunEngine(setup)
    val collisionDetector = CollisionDetector(setup, participantIds)
    val scoreTracker = ScoreTracker(participantIds)
    val scoreCalculator = ScoreCalculator(participantIds, scoreTracker)

    val turnProcessor = TurnProcessor(
        setup, gunEngine, collisionDetector, scoreTracker, scoreCalculator, participantIds
    )

    context("TR-SRV-PLN-001: Turn-step pipeline").config(tags = setOf(Tag("TR-SRV-PLN-001"))) {

        test("Positive: Normal turn — bots moving and scanning") {
            val bot1 = MutableBot(botId1, position = Point(100.0, 100.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            val bot2 = MutableBot(botId2, position = Point(200.0, 100.0), direction = 180.0, gunDirection = 180.0, radarDirection = 180.0)
            
            val botsMap = mutableMapOf(botId1 to bot1, botId2 to bot2)
            val botIntentsMap = mutableMapOf(
                botId1 to BotIntent().apply { targetSpeed = 8.0 },
                botId2 to BotIntent().apply { targetSpeed = 8.0; rescan = true }
            )
            val botsCopies = mutableMapOf<BotId, MutableBot>()
            val round = MutableRound(1)
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)
            
            val result = turnProcessor.processTurn(
                turn, botsMap, botIntentsMap, botsCopies, round, bullets, 0
            )
            
            // Bot 1 moves from 100 to 101
            bot1.x shouldBe 101.0
            bot1.speed shouldBe 1.0
            
            // Bot 2 moves from 200 to 199
            bot2.x shouldBe 199.0
            bot2.speed shouldBe 1.0
            
            // Bot 2 should have scanned Bot 1 (Bot 2 at (199, 100) facing West, Bot 1 at (101, 100))
            // Distance is 98. RADAR_RADIUS is 1200.
            turn.getEvents(botId2) shouldHaveSize 1
            // TODO: check for ScannedBotEvent specifically if needed
            
            result.inactivityCounter shouldBe 1
        }

        test("Positive: Fire gun — bullet created") {
            val bot1 = MutableBot(botId1, position = Point(100.0, 100.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot1.gunHeat = 0.0
            val botsMap = mutableMapOf(botId1 to bot1)
            val botIntentsMap = mutableMapOf(
                botId1 to BotIntent().apply { firepower = 1.0 }
            )
            val botsCopies = mutableMapOf<BotId, MutableBot>()
            val round = MutableRound(1)
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)
            
            turnProcessor.processTurn(
                turn, botsMap, botIntentsMap, botsCopies, round, bullets, 0
            )
            
            bullets shouldHaveSize 1
            bot1.energy shouldBe 99.0 // fired with power 1.0
            bot1.gunHeat shouldBe 1.2 // 1 + firepower/5 = 1 + 0.2
        }

        test("Negative: Disabled bot does not move") {
            val bot1 = MutableBot(botId1, position = Point(100.0, 100.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot1.energy = 0.0
            
            val botsMap = mutableMapOf(botId1 to bot1)
            val botIntentsMap = mutableMapOf(
                botId1 to BotIntent().apply { targetSpeed = 8.0 }
            )
            val botsCopies = mutableMapOf<BotId, MutableBot>()
            val round = MutableRound(1)
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)
            
            turnProcessor.processTurn(
                turn, botsMap, botIntentsMap, botsCopies, round, bullets, 0
            )
            
            bot1.x shouldBe 100.0 // no movement
            bot1.speed shouldBe 0.0
        }
        
        test("Positive: Inactivity damage applied") {
             val bot1 = MutableBot(botId1, position = Point(100.0, 100.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
             val botsMap = mutableMapOf(botId1 to bot1)
             val botIntentsMap = mutableMapOf<BotId, BotIntent>()
             val botsCopies = mutableMapOf<BotId, MutableBot>()
             val round = MutableRound(1)
             val bullets = mutableSetOf<Bullet>()
             val turn = MutableTurn(1)
             
             // setup says maxInactivityTurns = 100. 
             // We pass inactivityCounter = 101.
             val result = turnProcessor.processTurn(
                 turn, botsMap, botIntentsMap, botsCopies, round, bullets, 101
             )
             
             bot1.energy shouldBe 99.9 // INACTIVITY_DAMAGE is 0.1
             result.inactivityCounter shouldBe 102
        }
        
        test("Positive: Round ends when only one bot left") {
            val bot1 = MutableBot(botId1, position = Point(100.0, 100.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            val bot2 = MutableBot(botId2, position = Point(200.0, 100.0), direction = 180.0, gunDirection = 180.0, radarDirection = 180.0)
            bot2.energy = -1.0 // Bot 2 is dead
            
            val botsMap = mutableMapOf(botId1 to bot1, botId2 to bot2)
            val botIntentsMap = mutableMapOf<BotId, BotIntent>()
            val botsCopies = mutableMapOf<BotId, MutableBot>()
            val round = MutableRound(1)
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)

            // Give bot 1 some score so it definitely wins
            scoreTracker.registerBulletHit(participantId1, participantId2, 10.0, false)
            
            val result = turnProcessor.processTurn(
                turn, botsMap, botIntentsMap, botsCopies, round, bullets, 0
            )
            
            result.roundOutcome shouldNotBe null
            result.roundOutcome?.winnerBotIds shouldBe listOf(botId1)
        }
    }
})
