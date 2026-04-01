package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.event.BulletFiredEvent
import dev.robocode.tankroyale.server.event.ScannedBotEvent
import dev.robocode.tankroyale.server.model.*
import dev.robocode.tankroyale.server.rules.*

// ── Outcome data class ──────────────────────────────────────────────────────────

data class FireOutcome(
    val botId: BotId,
    val bullet: Bullet,
    val firepower: Double,
    val gunHeat: Double,
)

// ── Gun Engine ──────────────────────────────────────────────────────────────────

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
    ): List<FireOutcome> {
        val outcomes = mutableListOf<FireOutcome>()
        botsMap.values.forEach { bot ->
            if (bot.gunHeat == 0.0 && bot.isEnabled) {
                computeFireOutcome(bot, botIntentsMap, botsCopies, lastRound)?.let { outcome ->
                    applyFireOutcome(outcome, bot, bullets, turn)
                    outcomes.add(outcome)
                }
            } else {
                coolDownGun(bot)
            }
        }
        return outcomes
    }

    private fun computeFireOutcome(
        bot: MutableBot,
        botIntentsMap: Map<BotId, BotIntent>,
        botsCopies: Map<BotId, MutableBot>,
        lastRound: MutableRound?,
    ): FireOutcome? {
        val intent = botIntentsMap[bot.id] ?: return null
        val firepower = intent.firepower ?: 0.0
        if (firepower < MIN_FIREPOWER || bot.energy <= firepower) return null

        val power = firepower.coerceAtMost(MAX_FIREPOWER)
        val previousBotState = botsCopies[bot.id]!!
        var fireDirection = bot.gunDirection

        if (intent.fireAssist == true &&
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

        val bullet = Bullet(
            id = BulletId(++nextBulletId),
            botId = bot.id,
            startPosition = bot.position,
            direction = fireDirection,
            power = power,
            color = bot.bulletColor,
        )

        return FireOutcome(
            botId = bot.id,
            bullet = bullet,
            firepower = firepower,
            gunHeat = calcGunHeat(power),
        )
    }

    private fun applyFireOutcome(
        outcome: FireOutcome,
        bot: MutableBot,
        bullets: MutableSet<Bullet>,
        turn: MutableTurn,
    ) {
        bot.gunHeat = outcome.gunHeat
        bullets += outcome.bullet

        val bulletFiredEvent = BulletFiredEvent(turn.turnNumber, outcome.bullet.copy())
        turn.addPrivateBotEvent(bot.id, bulletFiredEvent)
        turn.addObserverEvent(bulletFiredEvent)

        bot.changeEnergy(-outcome.firepower)
    }

    private fun coolDownGun(bot: MutableBot) {
        bot.gunHeat = (bot.gunHeat - setup.gunCoolingRate).coerceAtLeast(0.0)
    }

    fun reset() {
        nextBulletId = 0
    }
}
