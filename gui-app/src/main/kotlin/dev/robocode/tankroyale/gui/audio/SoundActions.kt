package dev.robocode.tankroyale.gui.audio

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.model.*

object SoundActions {
    var enabled = true

    init {
        val gunshot = Sound.fromFile("sounds/gunshot.wav")
        val bulletHit = Sound.fromFile("sounds/bullet_hit.wav")
        val wallCollision = Sound.fromFile("sounds/wall_collision.wav")
        val botsCollision = Sound.fromFile("sounds/bots_collision.wav")
        val bulletsCollision = Sound.fromFile("sounds/bullets_collision.wav")
        val death = Sound.fromFile("sounds/death.wav")

        ClientEvents.onTickEvent.subscribe(this) {
            if (enabled) {
                it.events.forEach { event ->
                    when (event) {
                        is BulletFiredEvent -> gunshot.play()
                        is BulletHitBotEvent -> bulletHit.play()
                        is BotHitWallEvent -> wallCollision.play()
                        is BotHitBotEvent -> botsCollision.play()
                        is BulletHitBulletEvent -> bulletsCollision.play()
                        is BotDeathEvent -> death.play()
                        else -> {}
                    }
                }
            }
        }
    }
}
