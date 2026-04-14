package core

import dev.robocode.tankroyale.server.core.CollisionDetector
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.BOT_BOUNDING_CIRCLE_RADIUS
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class CollisionDetectorTest : FunSpec({

    val setup = GameSetup(
        arenaWidth = 1000, arenaHeight = 1000, maxNumberOfParticipants = 10,
        isArenaWidthLocked = true, isArenaHeightLocked = true, isMinNumberOfParticipantsLocked = true,
        isMaxNumberOfParticipantsLocked = true, isNumberOfRoundsLocked = true, isGunCoolingRateLocked = true,
        isMaxInactivityTurnsLocked = true, isTurnTimeoutLocked = true, isReadyTimeoutLocked = true
    )
    val botId1 = BotId(1)
    val botId2 = BotId(2)
    val participantId1 = ParticipantId(botId1)
    val participantId2 = ParticipantId(botId2)
    val participantIds = setOf(participantId1, participantId2)
    
    val detector = CollisionDetector(setup, participantIds)

    context("TR-SRV-PHY-001: Bullet-bot collisions").config(tags = setOf(Tag("TR-SRV-PHY-001"))) {

        test("Positive: Direct hit on bot center") {
            val bot = MutableBot(botId2, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            val bullet = Bullet(
                id = BulletId(1), botId = botId1, power = 1.0, direction = 0.0, color = null,
                startPosition = Point(480.0, 500.0), tick = 0
            )
            
            val bullets = mutableSetOf(bullet)
            val botsMap = mapOf(botId2 to bot)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBulletHits(bullets, botsMap, turn)
            
            result.hitResults.bulletHitBots shouldHaveSize 1
            result.hitResults.bulletHitBots[0].victimId shouldBe botId2
            bullets.shouldBeEmpty()
        }

        test("Positive: Edge hit (tangent)") {
            val bot = MutableBot(botId2, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            // Bot radius is 18.0. Path at y = 518.0 should just touch the bot.
            // Bullet starts at 485.0, ends at 502.0 (with speed 17.0). Bot x is 500.0.
            val bullet = Bullet(
                id = BulletId(1), botId = botId1, power = 1.0, direction = 0.0, color = null,
                startPosition = Point(485.0, 518.0), tick = 0
            )
            
            val bullets = mutableSetOf(bullet)
            val botsMap = mapOf(botId2 to bot)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBulletHits(bullets, botsMap, turn)
            
            result.hitResults.bulletHitBots shouldHaveSize 1
        }

        test("Positive: Diagonal hit") {
            val bot = MutableBot(botId2, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            val bullet = Bullet(
                id = BulletId(1), botId = botId1, power = 1.0, direction = 45.0, color = null,
                startPosition = Point(480.0, 480.0), tick = 0
            )
            
            val bullets = mutableSetOf(bullet)
            val botsMap = mapOf(botId2 to bot)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBulletHits(bullets, botsMap, turn)
            
            result.hitResults.bulletHitBots shouldHaveSize 1
        }

        test("Negative: Complete miss") {
            val bot = MutableBot(botId2, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            val bullet = Bullet(
                id = BulletId(1), botId = botId1, power = 1.0, direction = 0.0, color = null,
                startPosition = Point(480.0, 550.0), tick = 0
            )
            
            val bullets = mutableSetOf(bullet)
            val botsMap = mapOf(botId2 to bot)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBulletHits(bullets, botsMap, turn)
            
            result.hitResults.bulletHitBots.shouldBeEmpty()
            bullets shouldHaveSize 1
        }

        test("Edge: Overkill capping") {
            val bot = MutableBot(botId2, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot.energy = 5.0
            
            val bullet = Bullet(
                id = BulletId(1), botId = botId1, power = 3.0, direction = 0.0, color = null,
                startPosition = Point(485.0, 500.0), tick = 0
            )
            // Bullet power 3.0 deals 16 damage (4 * 3 + 2 * (3 - 1) = 12 + 4 = 16)
            
            val bullets = mutableSetOf(bullet)
            val botsMap = mapOf(botId2 to bot)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBulletHits(bullets, botsMap, turn)
            
            result.scoringRecords shouldHaveSize 1
            result.scoringRecords[0].damage shouldBe 5.0 // Capped at bot energy
            result.scoringRecords[0].isKilled shouldBe true
        }
    }

    context("TR-SRV-PHY-002: Bullet-bullet collisions").config(tags = setOf(Tag("TR-SRV-PHY-002"))) {

        test("Positive: Head-on collision") {
            val b1 = Bullet(
                id = BulletId(1), botId = botId1, power = 1.0, direction = 0.0, color = null,
                startPosition = Point(495.0, 500.0), tick = 0
            )
            val b2 = Bullet(
                id = BulletId(2), botId = botId2, power = 1.0, direction = 180.0, color = null,
                startPosition = Point(505.0, 500.0), tick = 0
            )
            
            val bullets = mutableSetOf(b1, b2)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBulletHits(bullets, emptyMap(), turn)
            
            result.hitResults.bulletHitBullets shouldHaveSize 1
            bullets.shouldBeEmpty()
        }

        test("Negative: Near miss (passing by)") {
            val bullet1 = Bullet(
                id = BulletId(1), botId = botId1, power = 1.0, direction = 0.0, color = null,
                startPosition = Point(450.0, 500.0), tick = 0
            )
            val bullet2 = Bullet(
                id = BulletId(2), botId = botId2, power = 1.0, direction = 180.0, color = null,
                startPosition = Point(550.0, 505.0), tick = 0
            )
            
            val bullets = mutableSetOf(bullet1, bullet2)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBulletHits(bullets, emptyMap(), turn)
            
            result.hitResults.bulletHitBullets.shouldBeEmpty()
            bullets shouldHaveSize 2
        }
    }

    context("TR-SRV-PHY-003: Bot-wall collisions").config(tags = setOf(Tag("TR-SRV-PHY-003"))) {

        test("Positive: Bot hits right wall") {
            val bot = MutableBot(botId1, position = Point(995.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot.speed = 8.0
            
            val botsMap = mapOf(botId1 to bot)
            val botsCopies = mapOf(botId1 to bot.copy())
            
            val lastTurn = MutableTurn(0)
            val lastRound = MutableRound(1)
            lastRound.turns.add(lastTurn)
            
            val turn = MutableTurn(1)

            val outcomes = detector.checkAndHandleBotWallCollisions(botsMap, botsCopies, lastRound, turn)
            
            outcomes shouldHaveSize 1
            outcomes[0].botId shouldBe botId1
            bot.position.x shouldBe (1000.0 - BOT_BOUNDING_CIRCLE_RADIUS)
        }

        test("Negative: Bot is at safe distance from wall") {
            val bot = MutableBot(botId1, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            
            val botsMap = mapOf(botId1 to bot)
            val botsCopies = mapOf(botId1 to bot.copy())
            
            val lastTurn = MutableTurn(0)
            val lastRound = MutableRound(1)
            lastRound.turns.add(lastTurn)
            
            val turn = MutableTurn(1)

            val outcomes = detector.checkAndHandleBotWallCollisions(botsMap, botsCopies, lastRound, turn)
            
            outcomes.shouldBeEmpty()
            bot.position.x shouldBe 500.0
        }
    }

    context("TR-SRV-PHY-004: Bot-bot collisions").config(tags = setOf(Tag("TR-SRV-PHY-004"))) {

        test("Positive: Bots overlap") {
            val bot1 = MutableBot(botId1, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            val bot2 = MutableBot(botId2, position = Point(530.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            
            val botsMap = mapOf(botId1 to bot1, botId2 to bot2)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBotCollisions(botsMap, null, turn)
            
            result.outcomes shouldHaveSize 1
            result.outcomes[0].bot1Id shouldBe botId1
            result.outcomes[0].bot2Id shouldBe botId2
        }

        test("Negative: Bots at safe distance") {
            val bot1 = MutableBot(botId1, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            val bot2 = MutableBot(botId2, position = Point(540.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            
            val botsMap = mapOf(botId1 to bot1, botId2 to bot2)
            val turn = MutableTurn(1)

            val result = detector.checkAndHandleBotCollisions(botsMap, null, turn)
            
            result.outcomes.shouldBeEmpty()
        }
    }
})
