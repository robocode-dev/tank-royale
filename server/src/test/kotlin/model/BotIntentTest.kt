package model

import dev.robocode.tankroyale.server.model.BotIntent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import model.factory.BotUpdate


class BotIntentTest : StringSpec({

    "targetSpeed must be updated" {
        val botIntent = BotIntent(targetSpeed = 1.2)

        botIntent.update(BotUpdate(targetSpeed = null))
        botIntent shouldBe BotIntent(targetSpeed = 1.2)

        botIntent.update(BotUpdate(targetSpeed = 7.8))
        botIntent shouldBe BotIntent(targetSpeed = 7.8)

        botIntent.update(BotUpdate(targetSpeed = null))
        botIntent shouldBe BotIntent(targetSpeed = 7.8)
    }

    "turnRate must be updated" {
        val botIntent = BotIntent(turnRate = 1.2)

        botIntent.update(BotUpdate(turnRate = null))
        botIntent shouldBe BotIntent(turnRate = 1.2)

        botIntent.update(BotUpdate(turnRate = 7.8))
        botIntent shouldBe BotIntent(turnRate = 7.8)

        botIntent.update(BotUpdate(turnRate = null))
        botIntent shouldBe BotIntent(turnRate = 7.8)
    }

    "gunTurnRate must be updated" {
        val botIntent = BotIntent(gunTurnRate = 1.2)

        botIntent.update(BotUpdate(gunTurnRate = null))
        botIntent shouldBe BotIntent(gunTurnRate = 1.2)

        botIntent.update(BotUpdate(gunTurnRate = 7.8))
        botIntent shouldBe BotIntent(gunTurnRate = 7.8)

        botIntent.update(BotUpdate(gunTurnRate = null))
        botIntent shouldBe BotIntent(gunTurnRate = 7.8)
    }

    "radarTurnRate must be updated" {
        val botIntent = BotIntent(radarTurnRate = 1.2)

        botIntent.update(BotUpdate(radarTurnRate = null))
        botIntent shouldBe BotIntent(radarTurnRate = 1.2)

        botIntent.update(BotUpdate(radarTurnRate = 7.8))
        botIntent shouldBe BotIntent(radarTurnRate = 7.8)

        botIntent.update(BotUpdate(radarTurnRate = null))
        botIntent shouldBe BotIntent(radarTurnRate = 7.8)
    }

    "bulletPower must be updated" {
        val botIntent = BotIntent(firepower = 1.2)

        botIntent.update(BotUpdate(firepower = null))
        botIntent shouldBe BotIntent(firepower = 1.2)

        botIntent.update(BotUpdate(firepower = 7.8))
        botIntent shouldBe BotIntent(firepower = 7.8)

        botIntent.update(BotUpdate(firepower = null))
        botIntent shouldBe BotIntent(firepower = 7.8)
    }

    "adjustGunForBodyTurn must be updated" {
        val botIntent = BotIntent(adjustGunForBodyTurn = true)

        botIntent.update(BotUpdate(adjustGunForBodyTurn = null))
        botIntent shouldBe BotIntent(adjustGunForBodyTurn = true)

        botIntent.update(BotUpdate(adjustGunForBodyTurn = false))
        botIntent shouldBe BotIntent(adjustGunForBodyTurn = false)

        botIntent.update(BotUpdate(adjustGunForBodyTurn = true))
        botIntent.update(BotUpdate(adjustGunForBodyTurn = null))
        botIntent shouldBe BotIntent(adjustGunForBodyTurn = true)
    }

    "adjustRadarForGunTurn must be updated" {
        val botIntent = BotIntent(adjustRadarForGunTurn = true)

        botIntent.update(BotUpdate(adjustRadarForGunTurn = null))
        botIntent shouldBe BotIntent(adjustRadarForGunTurn = true)

        botIntent.update(BotUpdate(adjustRadarForGunTurn = false))
        botIntent shouldBe BotIntent(adjustRadarForGunTurn = false)

        botIntent.update(BotUpdate(adjustRadarForGunTurn = true))
        botIntent.update(BotUpdate(adjustRadarForGunTurn = null))
        botIntent shouldBe BotIntent(adjustRadarForGunTurn = true)
    }

    "scan must be updated" {
        val botIntent = BotIntent(rescan = true)

        botIntent.update(BotUpdate(rescan = null))
        botIntent shouldBe BotIntent(rescan = true)

        botIntent.update(BotUpdate(rescan = false))
        botIntent shouldBe BotIntent(rescan = false)

        botIntent.update(BotUpdate(rescan = true))
        botIntent.update(BotUpdate(rescan = null))
        botIntent shouldBe BotIntent(rescan = true)
    }

    "bodyColor must be updated" {
        val botIntent = BotIntent(bodyColor = "#000")

        botIntent.update(BotUpdate(bodyColor = null))
        botIntent shouldBe BotIntent(bodyColor = "#000")

        botIntent.update(BotUpdate(bodyColor = "#123"))
        botIntent shouldBe BotIntent(bodyColor = "#123")

        botIntent.update(BotUpdate(bodyColor = null))
        botIntent shouldBe BotIntent(bodyColor = "#123")

        botIntent.update(BotUpdate(bodyColor = ""))
        botIntent shouldBe BotIntent(bodyColor = null)
    }

    "turretColor must be updated" {
        val botIntent = BotIntent(turretColor = "#000")

        botIntent.update(BotUpdate(turretColor = null))
        botIntent shouldBe BotIntent(turretColor = "#000")

        botIntent.update(BotUpdate(turretColor = "#123"))
        botIntent shouldBe BotIntent(turretColor = "#123")

        botIntent.update(BotUpdate(turretColor = null))
        botIntent shouldBe BotIntent(turretColor = "#123")

        botIntent.update(BotUpdate(turretColor = ""))
        botIntent shouldBe BotIntent(turretColor = null)
    }

    "radarColor must be updated" {
        val botIntent = BotIntent(radarColor = "#000")

        botIntent.update(BotUpdate(radarColor = null))
        botIntent shouldBe BotIntent(radarColor = "#000")

        botIntent.update(BotUpdate(radarColor = "#123"))
        botIntent shouldBe BotIntent(radarColor = "#123")

        botIntent.update(BotUpdate(radarColor = null))
        botIntent shouldBe BotIntent(radarColor = "#123")

        botIntent.update(BotUpdate(radarColor = ""))
        botIntent shouldBe BotIntent(radarColor = null)
    }

    "bulletColor must be updated" {
        val botIntent = BotIntent(bulletColor = "#000")

        botIntent.update(BotUpdate(bulletColor = null))
        botIntent shouldBe BotIntent(bulletColor = "#000")

        botIntent.update(BotUpdate(bulletColor = "#123"))
        botIntent shouldBe BotIntent(bulletColor = "#123")

        botIntent.update(BotUpdate(bulletColor = null))
        botIntent shouldBe BotIntent(bulletColor = "#123")

        botIntent.update(BotUpdate(bulletColor = ""))
        botIntent shouldBe BotIntent(bulletColor = null)
    }

    "scanColor must be updated" {
        val botIntent = BotIntent(scanColor = "#000")

        botIntent.update(BotUpdate(scanColor = null))
        botIntent shouldBe BotIntent(scanColor = "#000")

        botIntent.update(BotUpdate(scanColor = "#123"))
        botIntent shouldBe BotIntent(scanColor = "#123")

        botIntent.update(BotUpdate(scanColor = null))
        botIntent shouldBe BotIntent(scanColor = "#123")

        botIntent.update(BotUpdate(scanColor = ""))
        botIntent shouldBe BotIntent(scanColor = null)
    }

    "tracksColor must be updated" {
        val botIntent = BotIntent(tracksColor = "#000")

        botIntent.update(BotUpdate(tracksColor = null))
        botIntent shouldBe BotIntent(tracksColor = "#000")

        botIntent.update(BotUpdate(tracksColor = "#123"))
        botIntent shouldBe BotIntent(tracksColor = "#123")

        botIntent.update(BotUpdate(tracksColor = null))
        botIntent shouldBe BotIntent(tracksColor = "#123")

        botIntent.update(BotUpdate(tracksColor = ""))
        botIntent shouldBe BotIntent(tracksColor = null)
    }

    "gunColor must be updated" {
        val botIntent = BotIntent(gunColor = "#000")

        botIntent.update(BotUpdate(gunColor = null))
        botIntent shouldBe BotIntent(gunColor = "#000")

        botIntent.update(BotUpdate(gunColor = "#123"))
        botIntent shouldBe BotIntent(gunColor = "#123")

        botIntent.update(BotUpdate(gunColor = null))
        botIntent shouldBe BotIntent(gunColor = "#123")

        botIntent.update(BotUpdate(gunColor = ""))
        botIntent shouldBe BotIntent(gunColor = null)
    }

    "disableMovement() disables movement" {
        val botIntent = BotIntent(
            targetSpeed = 1.2,
            turnRate = 2.3,
            gunTurnRate = 3.4,
            radarTurnRate = 4.5,
            firepower = 5.6
        )
        botIntent.disableMovement()

        botIntent shouldBe BotIntent()
    }
})
