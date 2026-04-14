package core

import dev.robocode.tankroyale.server.core.GunEngine
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.MIN_FIREPOWER
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class GunEngineTest : FunSpec({

    val setup = GameSetup(
        gunCoolingRate = 0.1, maxNumberOfParticipants = 10,
        isArenaWidthLocked = true, isArenaHeightLocked = true, isMinNumberOfParticipantsLocked = true,
        isMaxNumberOfParticipantsLocked = true, isNumberOfRoundsLocked = true, isGunCoolingRateLocked = true,
        isMaxInactivityTurnsLocked = true, isTurnTimeoutLocked = true, isReadyTimeoutLocked = true
    )
    val botId1 = BotId(1)
    
    val engine = GunEngine(setup)

    context("TR-SRV-ENG-001: Gun firing").config(tags = setOf(Tag("TR-SRV-ENG-001"))) {

        test("Positive: Cold gun fires with sufficient energy") {
            val bot = MutableBot(botId1, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot.gunHeat = 0.0
            bot.energy = 100.0
            
            val intent = BotIntent(firepower = 1.0)
            val botsMap = mapOf(botId1 to bot)
            val botIntentsMap = mapOf(botId1 to intent)
            val botsCopies = mapOf(botId1 to bot.copy())
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)

            val outcomes = engine.coolDownAndFireGuns(botsMap, botIntentsMap, botsCopies, null, bullets, turn)
            
            outcomes shouldHaveSize 1
            outcomes[0].firepower shouldBe 1.0
            bullets shouldHaveSize 1
            bot.gunHeat shouldBe 1.2 // 1 + 1.0 / 5 = 1.2
            bot.energy shouldBe 99.0
        }

        test("Negative: Hot gun does not fire") {
            val bot = MutableBot(botId1, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot.gunHeat = 0.5
            bot.energy = 100.0
            
            val intent = BotIntent(firepower = 1.0)
            val botsMap = mapOf(botId1 to bot)
            val botIntentsMap = mapOf(botId1 to intent)
            val botsCopies = mapOf(botId1 to bot.copy())
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)

            val outcomes = engine.coolDownAndFireGuns(botsMap, botIntentsMap, botsCopies, null, bullets, turn)
            
            outcomes.shouldBeEmpty()
            bullets.shouldBeEmpty()
            bot.gunHeat shouldBe 0.4 // Cooled down by 0.1
            bot.energy shouldBe 100.0
        }

        test("Negative: Insufficient energy to fire") {
            val bot = MutableBot(botId1, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot.gunHeat = 0.0
            bot.energy = 0.5 // Energy is less than intent.firepower (1.0)
            
            val intent = BotIntent(firepower = 1.0)
            val botsMap = mapOf(botId1 to bot)
            val botIntentsMap = mapOf(botId1 to intent)
            val botsCopies = mapOf(botId1 to bot.copy())
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)

            val outcomes = engine.coolDownAndFireGuns(botsMap, botIntentsMap, botsCopies, null, bullets, turn)
            
            outcomes.shouldBeEmpty()
            bullets.shouldBeEmpty()
            bot.energy shouldBe 0.5
        }

        test("Negative: Intent firepower below minimum") {
            val bot = MutableBot(botId1, position = Point(500.0, 500.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)
            bot.gunHeat = 0.0
            bot.energy = 100.0
            
            val intent = BotIntent(firepower = MIN_FIREPOWER - 0.01)
            val botsMap = mapOf(botId1 to bot)
            val botIntentsMap = mapOf(botId1 to intent)
            val botsCopies = mapOf(botId1 to bot.copy())
            val bullets = mutableSetOf<Bullet>()
            val turn = MutableTurn(1)

            val outcomes = engine.coolDownAndFireGuns(botsMap, botIntentsMap, botsCopies, null, bullets, turn)
            
            outcomes.shouldBeEmpty()
            bullets.shouldBeEmpty()
        }
    }
})
