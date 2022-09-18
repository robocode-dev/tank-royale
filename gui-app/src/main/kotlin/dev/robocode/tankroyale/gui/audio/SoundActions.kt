package dev.robocode.tankroyale.gui.audio

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.*
import dev.robocode.tankroyale.gui.settings.ConfigSettings

object SoundActions {
    private val gunshot = Sound.fromFile("sounds/gunshot.wav")
    private val bulletHit = Sound.fromFile("sounds/bullet_hit.wav")
    private val wallCollision = Sound.fromFile("sounds/wall_collision.wav")
    private val botsCollision = Sound.fromFile("sounds/bots_collision.wav")
    private val bulletsCollision = Sound.fromFile("sounds/bullets_collision.wav")
    private val deathExplosion = Sound.fromFile("sounds/death.wav")

    init {
        ClientEvents.onTickEvent.subscribe(this) { playEventSounds(it) }
    }

    private fun playEventSounds(tickEvent: TickEvent) {
        if (ConfigSettings.enableSounds) {
            tickEvent.events.forEach { event ->
                when (event) {
                    is BulletFiredEvent -> playGunshot()
                    is BulletHitBotEvent -> playBulletHit()
                    is BotHitWallEvent -> playWallCollision()
                    is BotHitBotEvent -> playBotsCollision()
                    is BulletHitBulletEvent -> playBulletsCollision()
                    is BotDeathEvent -> playDeathExplosion()
                    else -> {}
                }
            }
        }
    }

    private fun playGunshot() {
        if (ConfigSettings.enableGunshotSound) {
            gunshot.play()
        }
    }

    private fun playBulletHit() {
        if (ConfigSettings.enableBulletHitSound) {
            bulletHit.play()
        }
    }

    private fun playWallCollision() {
        if (ConfigSettings.enableWallCollisionSound) {
            wallCollision.play()
        }
    }

    private fun playBotsCollision() {
        if (ConfigSettings.enableBotCollisionSound) {
            botsCollision.play()
        }
    }

    private fun playBulletsCollision() {
        if (ConfigSettings.enableBulletCollisionSound) {
            bulletsCollision.play()
        }
    }

    private fun playDeathExplosion() {
        if (ConfigSettings.enableDeathExplosionSound) {
            deathExplosion.play()
        }
    }
}
