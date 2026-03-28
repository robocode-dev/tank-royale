package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.BulletFiredEvent
import dev.robocode.tankroyale.server.event.ScannedBotEvent
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*

/** Engine for handling gun cooling and firing. */
class GunEngine(private val setup: GameSetup) {

    // Only accessed while holding GameServer.tickLock — enforced by ModelUpdater's threading contract.
    // GunEngine performs no synchronisation of its own; callers are responsible.
    private var nextBulletId = 0

    fun coolDownAndFireGuns(
        botsMap: Map<BotId, MutableBot>,
        botIntentsMap: Map<BotId, BotIntent>,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
        bullets: MutableSet<Bullet>,
        turn: MutableTurn
    ) {
        botsMap.values.forEach { bot ->
            if (bot.gunHeat == 0.0 && bot.isEnabled) {
                checkIfGunMustFire(bot, botIntentsMap, botsCopies, lastRound, bullets, turn)
            } else {
                coolDownGun(bot)
            }
        }
    }

    private fun checkIfGunMustFire(
        bot: MutableBot,
        botIntentsMap: Map<BotId, BotIntent>,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
        bullets: MutableSet<Bullet>,
        turn: MutableTurn
    ) {
        botIntentsMap[bot.id]?.let {
            val firepower = it.firepower ?: 0.0
            if (firepower >= MIN_FIREPOWER && bot.energy > firepower) {
                fireBullet(bot, firepower, botIntentsMap, botsCopies, lastRound, bullets, turn)
            }
        }
    }

    private fun coolDownGun(bot: MutableBot) {
        bot.gunHeat = (bot.gunHeat - setup.gunCoolingRate).coerceAtLeast(0.0)
    }

    private fun fireBullet(
        bot: MutableBot,
        firepower: Double,
        botIntentsMap: Map<BotId, BotIntent>,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
        bullets: MutableSet<Bullet>,
        turn: MutableTurn
    ) {
        val power = firepower.coerceAtMost(MAX_FIREPOWER)
        val previousBotState = botsCopies[bot.id]!!
        var fireDirection = bot.gunDirection

        if (botIntentsMap[bot.id]?.fireAssist == true &&
            bot.gunDirection == bot.radarDirection &&
            previousBotState.gunDirection == previousBotState.radarDirection
        ) {
            lastRound?.lastTurn?.let { previousTurn ->
                previousTurn.botEvents[bot.id]?.find { it is ScannedBotEvent }?.let {
                    val scan = (it as ScannedBotEvent)
                    fireDirection = angle(bot.x, bot.y, scan.x, scan.y)
                }
            }
        }

        bot.gunHeat = calcGunHeat(power)

        val bullet = Bullet(
            id = BulletId(++nextBulletId),
            botId = bot.id,
            startPosition = bot.position,
            direction = fireDirection,
            power = power,
            color = bot.bulletColor,
        )
        bullets += bullet

        val bulletFiredEvent = BulletFiredEvent(turn.turnNumber, bullet.copy())
        turn.addPrivateBotEvent(bot.id, bulletFiredEvent)
        turn.addObserverEvent(bulletFiredEvent)

        bot.changeEnergy(-firepower)
    }

    fun reset() {
        nextBulletId = 0
    }
}
