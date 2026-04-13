package dev.robocode.tankroyale.gui.ui.arena

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.TankColorMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.awt.Color

class TankColorResolutionTest : StringSpec({

    val botId = 1
    val defaultColor = "#000000"
    val botColor1 = "#FF0000"
    val botColor2 = "#00FF00"

    fun createGameSetup() = GameSetup(
        gameType = "classic",
        arenaWidth = 800,
        isArenaWidthLocked = true,
        arenaHeight = 600,
        isArenaHeightLocked = true,
        minNumberOfParticipants = 2,
        isMinNumberOfParticipantsLocked = true,
        isMaxNumberOfParticipantsLocked = true,
        numberOfRounds = 10,
        isNumberOfRoundsLocked = true,
        gunCoolingRate = 0.1,
        isGunCoolingRateLocked = true,
        maxInactivityTurns = 450,
        isMaxInactivityTurnsLocked = true,
        turnTimeout = 30000,
        isTurnTimeoutLocked = true,
        readyTimeout = 1000000,
        isReadyTimeoutLocked = true,
        defaultTurnsPerSecond = 30
    )

    fun createBotState(id: Int, bodyColor: String? = null, isDebuggingEnabled: Boolean = false) = BotState(
        isDroid = false,
        id = id,
        sessionId = "session",
        energy = 100.0,
        x = 0.0,
        y = 0.0,
        direction = 0.0,
        gunDirection = 0.0,
        radarDirection = 0.0,
        radarSweep = 0.0,
        speed = 0.0,
        turnRate = 0.0,
        gunTurnRate = 0.0,
        radarTurnRate = 0.0,
        gunHeat = 0.0,
        enemyCount = 0,
        bodyColor = bodyColor,
        isDebuggingEnabled = isDebuggingEnabled
    )

    fun resetLockedColors() {
        val fireMethod = Event::class.java.getDeclaredMethod("fire", Any::class.java)
        fireMethod.isAccessible = true
        fireMethod.invoke(ClientEvents.onGameStarted, GameStartedEvent(createGameSetup(), emptyList()))
    }

    "BOT_COLORS: returns bot-defined color when present" {
        ConfigSettings.tankColorMode = TankColorMode.BOT_COLORS
        val bot = createBotState(botId, bodyColor = botColor1)
        val tank = Tank(bot)
        
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true
        
        val resolvedColor = method.invoke(tank, bot.bodyColor, defaultColor) as Color
        resolvedColor shouldBe Color.RED
    }

    "BOT_COLORS: falls back to default when absent" {
        ConfigSettings.tankColorMode = TankColorMode.BOT_COLORS
        val bot = createBotState(botId, bodyColor = null)
        val tank = Tank(bot)
        
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true
        
        val resolvedColor = method.invoke(tank, bot.bodyColor, defaultColor) as Color
        resolvedColor shouldBe Color.BLACK
    }

    "BOT_COLORS_ONCE: first non-null color is cached and returned on subsequent calls" {
        ConfigSettings.tankColorMode = TankColorMode.BOT_COLORS_ONCE
        resetLockedColors()
        
        val bot1 = createBotState(botId, bodyColor = botColor1)
        val tank1 = Tank(bot1)
        
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true
        
        method.invoke(tank1, bot1.bodyColor, defaultColor) shouldBe Color.RED
        
        // Subsequent call with different color should return first color
        method.invoke(tank1, botColor2, defaultColor) shouldBe Color.RED
    }

    "BOT_COLORS_ONCE: cached color persists across a round boundary (no game reset between rounds)" {
        ConfigSettings.tankColorMode = TankColorMode.BOT_COLORS_ONCE
        resetLockedColors()

        val bot = createBotState(botId, bodyColor = botColor1)
        val tank = Tank(bot)
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true

        // Round 1 — lock the color
        method.invoke(tank, botColor1, defaultColor) shouldBe Color.RED

        // Simulate round boundary: no onGameStarted fired, bot sends a new color
        val tank2 = Tank(createBotState(botId, bodyColor = botColor2))
        method.invoke(tank2, botColor2, defaultColor) shouldBe Color.RED // still locked from round 1
    }

    "BOT_COLORS_ONCE: cache is cleared on onGameStarted" {
        ConfigSettings.tankColorMode = TankColorMode.BOT_COLORS_ONCE
        resetLockedColors()
        
        val bot = createBotState(botId, bodyColor = botColor1)
        val tank = Tank(bot)
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true
        
        method.invoke(tank, botColor1, defaultColor) shouldBe Color.RED
        
        // New game
        resetLockedColors()
        
        // Now it should accept a new color as "first"
        method.invoke(tank, botColor2, defaultColor) shouldBe Color.GREEN
    }

    "DEFAULT_COLORS: always returns default regardless of bot color" {
        ConfigSettings.tankColorMode = TankColorMode.DEFAULT_COLORS
        val bot = createBotState(botId, bodyColor = botColor1)
        val tank = Tank(bot)
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true
        
        method.invoke(tank, bot.bodyColor, defaultColor) shouldBe Color.BLACK
    }

    "BOT_COLORS_WHEN_DEBUGGING: returns bot color when isDebuggingEnabled == true" {
        ConfigSettings.tankColorMode = TankColorMode.BOT_COLORS_WHEN_DEBUGGING
        val bot = createBotState(botId, bodyColor = botColor1, isDebuggingEnabled = true)
        val tank = Tank(bot)
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true
        
        method.invoke(tank, bot.bodyColor, defaultColor) shouldBe Color.RED
    }

    "BOT_COLORS_WHEN_DEBUGGING: returns default when isDebuggingEnabled == false" {
        ConfigSettings.tankColorMode = TankColorMode.BOT_COLORS_WHEN_DEBUGGING
        val bot = createBotState(botId, bodyColor = botColor1, isDebuggingEnabled = false)
        val tank = Tank(bot)
        val method = Tank::class.java.getDeclaredMethod("resolveColor", String::class.java, String::class.java)
        method.isAccessible = true
        
        method.invoke(tank, bot.bodyColor, defaultColor) shouldBe Color.BLACK
    }
})
