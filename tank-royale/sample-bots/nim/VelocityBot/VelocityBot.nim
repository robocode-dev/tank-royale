## VelocityBot
##
## A sample bot originally made for Robocode by Joshua Galecki.
## Ported to Nim for Robocode Tank Royale.
##
## Example bot of how to use turn rates.

import asyncdispatch
import robocode_tank_royale

type
  VelocityBot = ref object of Bot
    turnCounter: int

method run(self: VelocityBot) {.async.} =
  ## Called when a new round is started -> initialize and do some movement
  self.turnCounter = 0

  await self.setGunTurnRate(15.0)

  while self.isRunning():
    if self.turnCounter mod 64 == 0:
      ## Straighten out, if we were hit by a bullet (ends turning)
      await self.setTurnRate(0.0)

      ## Go forward with a target speed of 4
      await self.setTargetSpeed(4.0)
    if self.turnCounter mod 64 == 32:
      ## Go backwards, faster
      await self.setTargetSpeed(-6.0)
    self.turnCounter += 1
    await self.go() ## execute turn

method onScannedBot(self: VelocityBot, event: ScannedBotEvent) {.async.} =
  ## We scanned another bot -> fire!
  await self.fire(1.0)

method onHitByBullet(self: VelocityBot, event: HitByBulletEvent) {.async.} =
  ## We were hit by a bullet -> set turn rate
  ## Turn to confuse the other bots
  await self.setTurnRate(5.0)

method onHitWall(self: VelocityBot, event: HitWallEvent) {.async.} =
  ## We hit a wall -> move in the opposite direction
  ## Move away from the wall by reversing the target speed.
  ## Note that current speed is 0 as the bot just hit the wall.
  await self.setTargetSpeed(-1.0 * self.getTargetSpeed())

when isMainModule:
  let bot = VelocityBot(turnCounter: 0)
  waitFor bot.start()