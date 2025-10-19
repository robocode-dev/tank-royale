## Crazy
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## This robot moves in a zigzag pattern while firing at enemies.

import asyncdispatch
import robocode_tank_royale

type
  Crazy = ref object of Bot
    movingForward: bool

method run(self: Crazy) {.async.} =
  ## Called when a new round is started -> initialize and do some movement.
  ## Set colors
  await self.setBodyColor(Color.fromRgb(0x00, 0xC8, 0x00))   ## lime
  await self.setTurretColor(Color.fromRgb(0x00, 0x96, 0x32)) ## green
  await self.setRadarColor(Color.fromRgb(0x00, 0x64, 0x64))  ## dark cyan
  await self.setBulletColor(Color.fromRgb(0xFF, 0xFF, 0x64)) ## yellow
  await self.setScanColor(Color.fromRgb(0xFF, 0xC8, 0xC8))   ## light red

  ## Loop while as long as the bot is running
  while self.isRunning():
    ## Tell the game we will want to move ahead 40000 -- some large number
    await self.setForward(40000.0)
    self.movingForward = true
    ## Tell the game we will want to turn left 90
    await self.setTurnLeft(90.0)
    ## At this point, we have indicated to the game that *when we do something*,
    ## we will want to move ahead and turn left. That's what "set" means.
    ## It is important to realize we have not done anything yet!
    ## In order to actually move, we'll want to call a method that takes real time, such as
    ## waitFor.
    ## waitFor actually starts the action -- we start moving and turning.
    ## It will not return until we have finished turning.
    await self.waitFor(proc(): bool = self.getTurnRemaining() == 0.0)
    ## Note: We are still moving ahead now, but the turn is complete.
    ## Now we'll turn the other way...
    await self.setTurnRight(180.0)
    ## ... and wait for the turn to finish ...
    await self.waitFor(proc(): bool = self.getTurnRemaining() == 0.0)
    ## ... then the other way ...
    await self.setTurnLeft(180.0)
    ## ... and wait for that turn to finish.
    await self.waitFor(proc(): bool = self.getTurnRemaining() == 0.0)
    ## then back to the top to do it all again.

method onHitWall(self: Crazy, event: HitWallEvent) {.async.} =
  ## We collided with a wall -> reverse the direction
  ## Bounce off!
  await self.reverseDirection()

method reverseDirection(self: Crazy) {.async.} =
  ## ReverseDirection: Switch from ahead to back & vice versa
  if self.movingForward:
    await self.setBack(40000.0)
    self.movingForward = false
  else:
    await self.setForward(40000.0)
    self.movingForward = true

method onScannedBot(self: Crazy, event: ScannedBotEvent) {.async.} =
  ## We scanned another bot -> fire!
  await self.fire(1.0)

method onHitBot(self: Crazy, event: HitBotEvent) {.async.} =
  ## We hit another bot -> back up!
  ## If we're moving into the other bot, reverse!
  if event.isRammed:
    await self.reverseDirection()

when isMainModule:
  let bot = Crazy(movingForward: false)
  waitFor bot.start()