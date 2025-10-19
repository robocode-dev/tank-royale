## Walls
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## This robot navigates around the perimeter of the battlefield with
## the gun pointed inward.

import asyncdispatch
import math
import robocode_tank_royale

type
  Walls = ref object of Bot
    peek: bool
    moveAmount: float

method run(self: Walls) {.async.} =
  ## Called when a new round is started -> initialize and do some movement
  ## Set colors
  await self.setBodyColor(Color.BLACK)
  await self.setTurretColor(Color.BLACK)
  await self.setRadarColor(Color.ORANGE)
  await self.setBulletColor(Color.CYAN)
  await self.setScanColor(Color.CYAN)

  ## Initialize moveAmount to the maximum possible for the arena
  self.moveAmount = max(self.getArenaWidth(), self.getArenaHeight())
  ## Initialize peek to false
  self.peek = false

  ## turn to face a wall.
  ## `getDirection() % 90` means the remainder of getDirection() divided by 90.
  await self.turnRight(self.getDirection() mod 90.0)
  await self.forward(self.moveAmount)

  ## Turn the gun to turn right 90 degrees.
  self.peek = true
  await self.turnGunLeft(90.0)
  await self.turnLeft(90.0)

  ## Main loop
  while self.isRunning():
    ## Peek before we turn when forward() completes.
    self.peek = true
    ## Move up the wall
    await self.forward(self.moveAmount)
    ## Don't peek now
    self.peek = false
    ## Turn to the next wall
    await self.turnLeft(90.0)

method onHitBot(self: Walls, event: HitBotEvent) {.async.} =
  ## We hit another bot -> move away a bit
  ## If he's in front of us, set back up a bit.
  var bearing = self.bearingTo(event.x, event.y)
  if bearing > -90.0 and bearing < 90.0:
    await self.back(100.0)
  else: ## else he's in back of us, so set ahead a bit.
    await self.forward(100.0)

method onScannedBot(self: Walls, event: ScannedBotEvent) {.async.} =
  ## We scanned another bot -> fire!
  await self.fire(2.0)
  ## Note that scan is called automatically when the bot is turning.
  ## By calling it manually here, we make sure we generate another scan event if there's a bot
  ## on the next wall, so that we do not start moving up it until it's gone.
  if self.peek:
    await self.rescan()

when isMainModule:
  let bot = Walls(peek: false, moveAmount: 0.0)
  waitFor bot.start()