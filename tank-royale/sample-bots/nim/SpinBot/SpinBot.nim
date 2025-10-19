## SpinBot
##
## A sample bot originally made for Robocode by Mathew Nelson.
## Ported to Nim for Robocode Tank Royale.
##
## Continuously moves in a circle while firing at maximum power when
## detecting enemies.

import asyncdispatch
import robocode_tank_royale

type
  SpinBot = ref object of Bot

method run(self: SpinBot) {.async.} =
  ## Called when a new round is started -> initialize and do some movement
  await self.setBodyColor(Color.BLUE)
  await self.setTurretColor(Color.BLUE)
  await self.setRadarColor(Color.BLACK)
  await self.setScanColor(Color.YELLOW)

  ## Repeat while the bot is running
  while self.isRunning():
    ## Tell the game that when we take move, we'll also want to turn right... a lot
    await self.setTurnRight(10000.0)
    ## Limit our speed to 5
    await self.setMaxSpeed(5.0)
    ## Start moving (and turning)
    await self.forward(10000.0)

method onScannedBot(self: SpinBot, event: ScannedBotEvent) {.async.} =
  ## We scanned another bot -> fire hard!
  await self.fire(3.0)

method onHitBot(self: SpinBot, event: HitBotEvent) {.async.} =
  ## We hit another bot -> if it's our fault, we'll stop turning and moving,
  ## so we need to turn again to keep spinning.
  var direction = self.angleTo(event.x, event.y)
  var bearing = self.calcBearing(direction)
  if bearing > -10.0 and bearing < 10.0:
    await self.fire(3.0)
  if event.isRammed:
    await self.turnRight(10.0)

when isMainModule:
  let bot = SpinBot()
  waitFor bot.start()