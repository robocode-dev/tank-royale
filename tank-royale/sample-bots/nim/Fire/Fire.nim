## Fire
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## Sits still, continuously rotates its gun, and only moves when hit.

import asyncdispatch
import robocode_tank_royale

type
  Fire = ref object of Bot
    dist: float

method run(self: Fire) {.async.} =
  ## Called when a new round is started -> initialize and do some movement.
  ## Set colors
  await self.setBodyColor(Color.fromRgb(0xFF, 0xAA, 0x00))   ## orange
  await self.setGunColor(Color.fromRgb(0xFF, 0x77, 0x00))    ## dark orange
  await self.setTurretColor(Color.fromRgb(0xFF, 0x77, 0x00)) ## dark orange
  await self.setRadarColor(Color.fromRgb(0xFF, 0x00, 0x00))  ## red
  await self.setScanColor(Color.fromRgb(0xFF, 0x00, 0x00))   ## red
  await self.setBulletColor(Color.fromRgb(0x00, 0x88, 0xFF)) ## light blue

  ## Spin the gun around slowly... forever
  while self.isRunning():
    ## Turn the gun a bit if the bot if the target speed is 0
    await self.turnGunRight(5.0)

method onScannedBot(self: Fire, event: ScannedBotEvent) {.async.} =
  ## We scanned another bot -> fire!
  ## If the other bot is close by, and we have plenty of life, fire hard!
  var distance = self.distanceTo(event.x, event.y)
  if distance < 50.0 and self.getEnergy() > 50.0:
    await self.fire(3.0)
  else:
    ## Otherwise, only fire 1
    await self.fire(1.0)
  ## Rescan
  await self.rescan()

method onHitByBullet(self: Fire, event: HitByBulletEvent) {.async.} =
  ## We were hit by a bullet -> turn perpendicular to the bullet, and move a bit
  ## Turn perpendicular to the bullet direction
  await self.turnLeft(self.normalizeRelativeAngle(90.0 - (self.getDirection() - event.bullet.direction)))

  ## Move forward or backward depending if the distance is positive or negative
  await self.forward(self.dist)
  self.dist *= -1.0 ## Change distance, meaning forward or backward direction

  ## Rescan
  await self.rescan()

method onHitBot(self: Fire, event: HitBotEvent) {.async.} =
  ## We have hit another bot -> aim at it and fire hard!
  ## Turn gun to the bullet direction
  var direction = self.angleTo(event.x, event.y)
  var gunBearing = self.normalizeRelativeAngle(direction - self.getGunDirection())
  await self.turnGunRight(gunBearing)

  ## Fire hard
  await self.fire(3.0)

when isMainModule:
  let bot = Fire(dist: 50.0)
  waitFor bot.start()