## Corners
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## This robot moves to a corner, then rotates its gun back and forth
## scanning for enemies. If it performs poorly in a round, it will
## try a different corner in the next round.

import asyncdispatch
import random
import robocode_tank_royale

type
  Corners = ref object of Bot
    enemies: int
    corner: int
    stopWhenSeeEnemy: bool

method run(self: Corners) {.async.} =
  ## Called when a new round is started -> initialize and do some movement.
  ## Set colors
  await self.setBodyColor(Color.RED)
  await self.setTurretColor(Color.BLACK)
  await self.setRadarColor(Color.YELLOW)
  await self.setBulletColor(Color.GREEN)
  await self.setScanColor(Color.GREEN)

  ## Save # of other bots
  self.enemies = self.getEnemyCount()

  ## Move to a corner
  await self.goCorner()

  ## Initialize gun turn speed to 3
  var gunIncrement = 3.0

  ## Spin gun back and forth
  while self.isRunning():
    for i in 0..<30:
      await self.turnGunLeft(gunIncrement)
    gunIncrement *= -1.0

method goCorner(self: Corners) {.async.} =
  ## A very inefficient way to get to a corner.
  ## Can you do better as an home exercise? :)
  ## We don't want to stop when we're just turning...
  self.stopWhenSeeEnemy = false
  ## Turn to face the wall towards our desired corner
  await self.turnLeft(self.calcBearing(float(self.corner)))
  ## Ok, now we don't want to crash into any bot in our way...
  self.stopWhenSeeEnemy = true
  ## Move to that wall
  await self.forward(5000.0)
  ## Turn to face the corner
  await self.turnLeft(90.0)
  ## Move to the corner
  await self.forward(5000.0)
  ## Turn gun to starting point
  await self.turnGunLeft(90.0)

method onScannedBot(self: Corners, event: ScannedBotEvent) {.async.} =
  ## We saw another bot -> stop and fire!
  var distance = self.distanceTo(event.x, event.y)

  ## Should we stop, or just fire?
  if self.stopWhenSeeEnemy:
    ## Stop movement
    await self.stop()
    ## Call our custom firing method
    await self.smartFire(distance)
    ## Rescan for another bot
    await self.rescan()
    ## This line will not be reached when scanning another bot.
    ## So we did not scan another bot -> resume movement
    await self.resume()
  else:
    await self.smartFire(distance)

method smartFire(self: Corners, distance: float) {.async.} =
  ## Custom fire method that determines firepower based on distance.
  ## distance: The distance to the bot to fire at.
  if distance > 200.0 or self.getEnergy() < 15.0:
    await self.fire(1.0)
  elif distance > 50.0:
    await self.fire(2.0)
  else:
    await self.fire(3.0)

method onDeath(self: Corners, event: DeathEvent) {.async.} =
  ## We died -> figure out if we need to switch to another corner
  ## Well, others should never be 0, but better safe than sorry.
  if self.enemies == 0:
    return

  ## If 75% of the bots are still alive when we die, we'll switch corners.
  if self.getEnemyCount() / self.enemies.float >= 0.75:
    self.corner += 90 ## Next corner
    self.corner = self.corner mod 360 ## Make sure the corner is within 0 - 359

    echo "I died and did poorly... switching corner to ", self.corner
  else:
    echo "I died but did well. I will still use corner ", self.corner

proc randomCorner(): int =
  ## Returns a random corner (0, 90, 180, 270)
  ## Random number is between 0-3
  90 * rand(3)

when isMainModule:
  var corner = randomCorner()
  let bot = Corners(corner: corner)
  waitFor bot.start()